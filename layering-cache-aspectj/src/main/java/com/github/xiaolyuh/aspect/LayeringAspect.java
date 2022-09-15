package com.github.xiaolyuh.aspect;

import com.github.xiaolyuh.annotation.CacheEvict;
import com.github.xiaolyuh.annotation.CachePut;
import com.github.xiaolyuh.annotation.Cacheable;
import com.github.xiaolyuh.annotation.Caching;
import com.github.xiaolyuh.annotation.FirstCache;
import com.github.xiaolyuh.annotation.SecondaryCache;
import com.github.xiaolyuh.cache.Cache;
import com.github.xiaolyuh.expression.CacheOperationExpressionEvaluator;
import com.github.xiaolyuh.manager.CacheManager;
import com.github.xiaolyuh.redis.serializer.SerializationException;
import com.github.xiaolyuh.setting.FirstCacheSetting;
import com.github.xiaolyuh.setting.LayeringCacheSetting;
import com.github.xiaolyuh.setting.SecondaryCacheSetting;
import com.github.xiaolyuh.support.CacheMode;
import com.github.xiaolyuh.support.KeyGenerator;
import com.github.xiaolyuh.support.SimpleKeyGenerator;
import com.github.xiaolyuh.util.ToStringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * 缓存拦截，用于注册方法信息
 *
 * @author yuhao.wang
 */
@Aspect
public class LayeringAspect {
    private static final String CACHE_KEY_ERROR_MESSAGE = "缓存Key %s 不能为NULL";
    private static final String CACHE_NAME_ERROR_MESSAGE = "缓存名称不能为NULL";

    /**
     * SpEL表达式计算器
     */
    private final CacheOperationExpressionEvaluator evaluator = new CacheOperationExpressionEvaluator();

    @Autowired
    private CacheManager cacheManager;

    @Autowired(required = false)
    private KeyGenerator keyGenerator = new SimpleKeyGenerator();

    @Pointcut("@annotation(com.github.xiaolyuh.annotation.Cacheable)")
    public void cacheablePointcut() {
    }

    @Pointcut("@annotation(com.github.xiaolyuh.annotation.CacheEvict)")
    public void cacheEvictPointcut() {
    }

    @Pointcut("@annotation(com.github.xiaolyuh.annotation.CachePut)")
    public void cachePutPointcut() {
    }

    @Pointcut("@annotation(com.github.xiaolyuh.annotation.Caching)")
    public void cachingPointcut() {
    }

    @Around("cacheablePointcut()")
    public Object cacheablePointcut(ProceedingJoinPoint joinPoint) throws Throwable {

        // 获取method
        Method method = this.getSpecificmethod(joinPoint);
        // 获取注解
        Cacheable cacheable = AnnotationUtils.findAnnotation(method, Cacheable.class);
        assert cacheable != null;

        try {
            // 执行查询缓存方法
            return executeCacheable(getCacheOperationInvoker(joinPoint), cacheable, method, joinPoint.getArgs(), joinPoint.getTarget());
        } catch (SerializationException e) {
            // 如果是序列化异常需要先删除原有缓存,在执行缓存方法
            String[] cacheNames = cacheable.cacheNames();
            delete(cacheNames, cacheable.key(), method, joinPoint.getArgs(), joinPoint.getTarget());
            return executeCacheable(getCacheOperationInvoker(joinPoint), cacheable, method, joinPoint.getArgs(), joinPoint.getTarget());
        }
    }

    @Around("cacheEvictPointcut()")
    public Object cacheEvictPointcut(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取method
        Method method = this.getSpecificmethod(joinPoint);
        // 获取注解
        CacheEvict cacheEvict = AnnotationUtils.findAnnotation(method, CacheEvict.class);
        assert cacheEvict != null;

        // 执行删除方法，先执行方法的目的是防止回写脏数据到缓存
        Object result = joinPoint.proceed();
        executeEvict(cacheEvict, method, joinPoint.getArgs(), joinPoint.getTarget());
        return result;
    }

    @Around("cachePutPointcut()")
    public Object cachePutPointcut(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取method
        Method method = this.getSpecificmethod(joinPoint);
        // 获取注解
        CachePut cachePut = AnnotationUtils.findAnnotation(method, CachePut.class);
        assert cachePut != null;

        // 指定调用方法获取缓存值
        Object result = joinPoint.proceed();
        executePut(result, cachePut, method, joinPoint.getArgs(), joinPoint.getTarget());
        return result;
    }

    @Around("cachingPointcut()")
    public Object cachingPointcut(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取method
        Method method = this.getSpecificmethod(joinPoint);
        // 获取注解
        Caching caching = AnnotationUtils.findAnnotation(method, Caching.class);
        assert caching != null;
        Cacheable[] cacheables = caching.cacheable();
        CachePut[] puts = caching.put();
        CacheEvict[] evicts = caching.evict();

        Object result = evicts.length > 0 ? joinPoint.proceed() : EmptyObject.getInstance();
        for (CacheEvict cacheEvict : evicts) {
            executeEvict(cacheEvict, method, joinPoint.getArgs(), joinPoint.getTarget());
        }

        result = result instanceof EmptyObject && puts.length > 0 ? joinPoint.proceed() : result;
        for (CachePut cachePut : puts) {
            executePut(result, cachePut, method, joinPoint.getArgs(), joinPoint.getTarget());
        }

        for (Cacheable cacheable : cacheables) {
            Object finalResult = result;
            Callable callable = result instanceof EmptyObject ? getCacheOperationInvoker(joinPoint) : () -> finalResult;
            result = executeCacheable(callable, cacheable, method, joinPoint.getArgs(), joinPoint.getTarget());
        }

        // 执行查询缓存方法
        return isEmptyAnnotation(result, cacheables, puts, evicts) ? joinPoint.proceed() : result;
    }

    /**
     * 是否是空注解
     *
     * @param cacheables Cacheable注解
     * @param puts       CachePut注解
     * @param evicts     CacheEvict注解
     * @return boolean
     */
    private boolean isEmptyAnnotation(Object result, Cacheable[] cacheables, CachePut[] puts, CacheEvict[] evicts) {
        return result instanceof EmptyObject && cacheables.length == 0 && puts.length == 0 && evicts.length == 0;
    }

    /**
     * 执行Cacheable切面
     *
     * @param valueLoader 加载缓存的回调方法
     * @param cacheable   {@link Cacheable}
     * @param method      {@link Method}
     * @param args        注解方法参数
     * @param target      target
     * @return {@link Object}
     */
    private Object executeCacheable(Callable valueLoader, Cacheable cacheable, Method method, Object[] args, Object target) {

        // 解析SpEL表达式获取cacheName和key
        String[] cacheNames = cacheable.cacheNames();
        Assert.notEmpty(cacheable.cacheNames(), CACHE_NAME_ERROR_MESSAGE);
        String cacheName = cacheNames[0];
        boolean isNoNeedCache = isNoNeedCache(cacheable.noNeedCacheKey(), method, args, target);
        if(isNoNeedCache) {
            try {
                return valueLoader.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        Object key = generateKey(cacheable.key(), method, args, target);
        Assert.notNull(key, String.format(CACHE_KEY_ERROR_MESSAGE, cacheable.key()));

        // 从注解中获取缓存配置
        FirstCache firstCache = cacheable.firstCache();
        SecondaryCache secondaryCache = cacheable.secondaryCache();
        FirstCacheSetting firstCacheSetting = new FirstCacheSetting(firstCache.initialCapacity(), firstCache.maximumSize(),
                firstCache.expireTime(), firstCache.timeUnit(), firstCache.expireMode());

        SecondaryCacheSetting secondaryCacheSetting = new SecondaryCacheSetting(secondaryCache.expireTime(),
                secondaryCache.preloadTime(), secondaryCache.timeUnit(), secondaryCache.forceRefresh(),
                true, secondaryCache.magnification());

        LayeringCacheSetting layeringCacheSetting = new LayeringCacheSetting(firstCacheSetting, secondaryCacheSetting,
                cacheable.depict(), cacheable.cacheMode());

        // 通过cacheName和缓存配置获取Cache
        Cache cache = cacheManager.getCache(cacheName, layeringCacheSetting);

        // 通Cache获取值
        return cache.get(ToStringUtils.toString(key), method.getReturnType(), valueLoader);
    }

    /**
     * 解析SpEL表达式，获取注解上的noNeedCachekey属性值
     *
     * @return Object
     */
    private boolean isNoNeedCache(String noNeedCacheKeySpEl, Method method, Object[] args, Object target) {

        // 获取注解上的key属性值
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
        if (StringUtils.hasText(noNeedCacheKeySpEl)) {
            EvaluationContext evaluationContext = evaluator.createEvaluationContext(method, args, target,
                targetClass, CacheOperationExpressionEvaluator.NO_RESULT);

            AnnotatedElementKey methodCacheKey = new AnnotatedElementKey(method, targetClass);
            // 兼容传null值得情况
            Object keyValue = evaluator.key(noNeedCacheKeySpEl, methodCacheKey, evaluationContext);
            return Objects.isNull(keyValue) ? false : (Boolean)keyValue;
        }
        return false;
    }

    /**
     * 执行 CacheEvict 切面
     *
     * @param cacheEvict {@link CacheEvict}
     * @param method     {@link Method}
     * @param args       注解方法参数
     * @param target     target
     * @return {@link Object}
     */
    private void executeEvict(CacheEvict cacheEvict, Method method, Object[] args, Object target) {
        // 删除缓存
        // 解析SpEL表达式获取cacheName和key
        String[] cacheNames = cacheEvict.cacheNames();
        Assert.notEmpty(cacheEvict.cacheNames(), CACHE_NAME_ERROR_MESSAGE);
        // 判断是否删除所有缓存数据
        if (cacheEvict.allEntries()) {
            // 删除所有缓存数据（清空）
            for (String cacheName : cacheNames) {
                Collection<Cache> caches = cacheManager.getCache(cacheName);
                if (CollectionUtils.isEmpty(caches)) {
                    // 如果没有找到Cache就新建一个默认的
                    Cache cache = cacheManager.getCache(cacheName,
                            new LayeringCacheSetting(new FirstCacheSetting(), new SecondaryCacheSetting(), "默认缓存配置（清除时生成）", cacheEvict.cacheMode()));
                    cache.clear();
                } else {
                    for (Cache cache : caches) {
                        cache.clear();
                    }
                }
            }
        } else {
            // 删除指定key
            delete(cacheNames, cacheEvict.key(), method, args, target);
        }
    }

    /**
     * 删除执行缓存名称上的指定key
     *
     * @param cacheNames 缓存名称
     * @param keySpEL    key的SpEL表达式
     * @param method     {@link Method}
     * @param args       参数列表
     * @param target     目标类
     */
    private void delete(String[] cacheNames, String keySpEL, Method method, Object[] args, Object target) {
        Object key = generateKey(keySpEL, method, args, target);
        Assert.notNull(key, String.format(CACHE_KEY_ERROR_MESSAGE, keySpEL));
        for (String cacheName : cacheNames) {
            Collection<Cache> caches = cacheManager.getCache(cacheName);
            if (CollectionUtils.isEmpty(caches)) {
                // 如果没有找到Cache就新建一个默认的
                Cache cache = cacheManager.getCache(cacheName,
                        new LayeringCacheSetting(new FirstCacheSetting(), new SecondaryCacheSetting(), "默认缓存配置（删除时生成）", CacheMode.ALL));
                cache.evict(ToStringUtils.toString(key));
            } else {
                for (Cache cache : caches) {
                    cache.evict(ToStringUtils.toString(key));
                }
            }
        }
    }

    /**
     * 执行 CachePut 切面
     *
     * @param result   执行方法的返回值
     * @param cachePut {@link CachePut}
     * @param method   {@link Method}
     * @param args     注解方法参数
     * @param target   target
     */
    private void executePut(Object result, CachePut cachePut, Method method, Object[] args, Object target) throws Throwable {

        String[] cacheNames = cachePut.cacheNames();
        Assert.notEmpty(cachePut.cacheNames(), CACHE_NAME_ERROR_MESSAGE);
        // 解析SpEL表达式获取 key
        Object key = generateKey(cachePut.key(), method, args, target);
        Assert.notNull(key, String.format(CACHE_KEY_ERROR_MESSAGE, cachePut.key()));

        // 从解决中获取缓存配置
        FirstCache firstCache = cachePut.firstCache();
        SecondaryCache secondaryCache = cachePut.secondaryCache();
        FirstCacheSetting firstCacheSetting = new FirstCacheSetting(firstCache.initialCapacity(), firstCache.maximumSize(),
                firstCache.expireTime(), firstCache.timeUnit(), firstCache.expireMode());

        SecondaryCacheSetting secondaryCacheSetting = new SecondaryCacheSetting(secondaryCache.expireTime(),
                secondaryCache.preloadTime(), secondaryCache.timeUnit(), secondaryCache.forceRefresh(),
                true, secondaryCache.magnification());

        LayeringCacheSetting layeringCacheSetting = new LayeringCacheSetting(firstCacheSetting, secondaryCacheSetting,
                cachePut.depict(), cachePut.cacheMode());


        for (String cacheName : cacheNames) {
            // 通过cacheName和缓存配置获取Cache
            Cache cache = cacheManager.getCache(cacheName, layeringCacheSetting);
            cache.put(ToStringUtils.toString(key), result);
        }
    }

    private Callable getCacheOperationInvoker(ProceedingJoinPoint joinPoint) {
        return () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable ex) {
                throw (Exception) ex;
            }
        };
    }

    /**
     * 解析SpEL表达式，获取注解上的key属性值
     *
     * @return Object
     */
    private Object generateKey(String keySpEl, Method method, Object[] args, Object target) {

        // 获取注解上的key属性值
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
        if (StringUtils.hasText(keySpEl)) {
            EvaluationContext evaluationContext = evaluator.createEvaluationContext(method, args, target,
                    targetClass, CacheOperationExpressionEvaluator.NO_RESULT);

            AnnotatedElementKey methodCacheKey = new AnnotatedElementKey(method, targetClass);
            // 兼容传null值得情况
            Object keyValue = evaluator.key(keySpEl, methodCacheKey, evaluationContext);
            return Objects.isNull(keyValue) ? "null" : keyValue;
        }
        return this.keyGenerator.generate(target, method, args);
    }

    /**
     * 获取Method
     *
     * @param pjp ProceedingJoinPoint
     * @return {@link Method}
     */
    private Method getSpecificmethod(ProceedingJoinPoint pjp) {
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        Method method = methodSignature.getMethod();
        // The method may be on an interface, but we need attributes from the
        // target class. If the target class is null, the method will be
        // unchanged.
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(pjp.getTarget());
        if (targetClass == null && pjp.getTarget() != null) {
            targetClass = pjp.getTarget().getClass();
        }
        Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
        // If we are dealing with method with generic parameters, find the
        // original method.
        specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
        return specificMethod;
    }

}

class EmptyObject {
    private static final EmptyObject EMPTY_OBJECT = new EmptyObject();

    private EmptyObject() {
    }

    //获取唯一可用的对象
    public static EmptyObject getInstance() {
        return EMPTY_OBJECT;
    }

}
