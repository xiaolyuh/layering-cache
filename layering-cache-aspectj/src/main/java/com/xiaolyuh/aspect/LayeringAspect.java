package com.xiaolyuh.aspect;

import com.xiaolyuh.annotation.Cacheable;
import com.xiaolyuh.annotation.FirstCache;
import com.xiaolyuh.annotation.SecondaryCache;
import com.xiaolyuh.cache.Cache;
import com.xiaolyuh.expression.CacheOperationExpressionEvaluator;
import com.xiaolyuh.manager.CacheManager;
import com.xiaolyuh.setting.FirstCacheSetting;
import com.xiaolyuh.setting.LayeringCacheSetting;
import com.xiaolyuh.setting.SecondaryCacheSetting;
import com.xiaolyuh.support.CacheOperationInvoker;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.lang.UsesJava8;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * 缓存拦截，用于注册方法信息
 *
 * @author yuhao.wang
 */
@Aspect
public class LayeringAspect {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * SpEL表达式计算器
     */
    private final CacheOperationExpressionEvaluator evaluator = new CacheOperationExpressionEvaluator();

    @Autowired
    private CacheManager cacheManager;

    @Autowired(required = false)
    private KeyGenerator keyGenerator = new SimpleKeyGenerator();

    @Pointcut("@annotation(com.xiaolyuh.annotation.Cacheable)")
    public void cacheablePointcut() {
    }

    @Pointcut("@annotation(com.xiaolyuh.annotation.CacheEvict)")
    public void cacheEvictPointcut() {
    }

    @Pointcut("@annotation(com.xiaolyuh.annotation.CachePut)")
    public void cachePutPointcut() {
    }

    @Around("cacheablePointcut()")
    public Object cacheablePointcut(ProceedingJoinPoint joinPoint) throws Throwable {
        CacheOperationInvoker aopAllianceInvoker = () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable ex) {
                throw new CacheOperationInvoker.ThrowableWrapperException(ex);
            }
        };

        try {
            // 获取method
            Method method = this.getSpecificmethod(joinPoint);
            // 获取注解
            Cacheable cacheable = AnnotationUtils.findAnnotation(method, Cacheable.class);
            // 执行查询缓存方法
            return executeCacheable(aopAllianceInvoker, cacheable, method, joinPoint.getArgs(), joinPoint.getTarget());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 执行Cacheable切面
     *
     * @param invoker   获取缓存的回调方法
     * @param cacheable {@link Cacheable}
     * @param method    {@link Method}
     * @param args      注解方法参数
     * @param target    target
     * @return {@link Object}
     */
    private Object executeCacheable(CacheOperationInvoker invoker, Cacheable cacheable,
                                    Method method, Object[] args, Object target) {

        // 解析SpEL表达式获取cacheName和key
        String[] cacheNames = cacheable.cacheNames();
        Assert.notEmpty(cacheable.cacheNames(), "缓存名称不能为NULL");
        String cacheName = generateValue(cacheNames[0], method, args, target).toString();
        Object key = generateKey(cacheable.key(), method, args, target);

        // 从解决中获取缓存配置
        FirstCache firstCache = cacheable.firstCache();
        SecondaryCache secondaryCache = cacheable.secondaryCache();
        FirstCacheSetting firstCacheSetting = new FirstCacheSetting(firstCache.initialCapacity(), firstCache.maximumSize(),
                firstCache.expireTime(), firstCache.timeUnit(), firstCache.expireMode());

        SecondaryCacheSetting secondaryCacheSetting = new SecondaryCacheSetting(secondaryCache.expiration(),
                secondaryCache.preloadTime(), secondaryCache.timeUnit(), secondaryCache.forceRefresh());
        LayeringCacheSetting layeringCacheSetting = new LayeringCacheSetting(firstCacheSetting, secondaryCacheSetting);

        // 通过cacheName和缓存配置获取Cache
        Cache cache = cacheManager.getCache(cacheName, layeringCacheSetting);

        // 通Cache获取值
        return OptionalUnwrapper.wrap(cache.get(key, () -> OptionalUnwrapper.unwrap(invoker.invoke())));
    }

    /**
     * 解析SpEL表达式，获取注解上的key属性值
     *
     * @return Object
     */
    protected Object generateKey(String key, Method method, Object[] args, Object target) {

        // 获取注解上的key属性值
        Class<?> targetClass = getTargetClass(target);
        if (StringUtils.hasText(key)) {
            EvaluationContext evaluationContext = evaluator.createEvaluationContext(method, args, target,
                    targetClass, CacheOperationExpressionEvaluator.NO_RESULT);

            AnnotatedElementKey methodCacheKey = new AnnotatedElementKey(method, targetClass);
            return evaluator.getExpressionValue(key, methodCacheKey, evaluationContext);
        }
        return this.keyGenerator.generate(target, method, args);
    }

    /**
     * 解析SpEL表达式，获取注解上的value属性值（cacheNames）
     *
     * @return
     */
    private Object generateValue(String value, Method method, Object[] args, Object target) {
        Assert.isTrue(!StringUtils.isEmpty(value), "缓存名称不能为NULL");
        // 获取注解上的value属性值
        Class<?> targetClass = getTargetClass(target);
        EvaluationContext evaluationContext = evaluator.createEvaluationContext(method, args, target,
                targetClass, CacheOperationExpressionEvaluator.NO_RESULT);

        AnnotatedElementKey methodCacheKey = new AnnotatedElementKey(method, targetClass);
        return evaluator.getExpressionValue(value, methodCacheKey, evaluationContext);
    }

    /**
     * 获取类信息
     *
     * @param target Object
     * @return targetClass
     */
    private Class<?> getTargetClass(Object target) {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
        if (targetClass == null && target != null) {
            targetClass = target.getClass();
        }
        return targetClass;
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

    /**
     * 内部类依赖 jdk 1.8
     */
    @UsesJava8
    private static class OptionalUnwrapper {

        public static Object unwrap(Object optionalObject) {
            Optional<?> optional = (Optional<?>) optionalObject;
            if (!optional.isPresent()) {
                return null;
            }
            Object result = optional.get();
            Assert.isTrue(!(result instanceof Optional), "Multi-level Optional usage not supported");
            return result;
        }

        public static Object wrap(Object value) {
            return Optional.ofNullable(value);
        }
    }
}