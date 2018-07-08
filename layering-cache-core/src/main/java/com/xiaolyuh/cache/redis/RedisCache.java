package com.xiaolyuh.cache.redis;

import com.alibaba.fastjson.JSON;
import com.xiaolyuh.cache.utils.Lock;
import com.xiaolyuh.cache.utils.AwaitThreadContainer;
import com.xiaolyuh.cache.utils.ThreadTaskUtils;
import com.xiaolyuh.support.AbstractValueAdaptingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis实现的二级缓存
 *
 * @author yuhao.wang
 */
public class RedisCache extends AbstractValueAdaptingCache {
    protected static final Logger logger = LoggerFactory.getLogger(RedisCache.class);

    /**
     * 刷新缓存重试次数
     */
    private static final int RETRY_COUNT = 5;

    /**
     * 刷新缓存等待时间，单位毫秒
     */
    private static final long WAIT_TIME = 20;

    /**
     * 等待线程容器
     */
    private AwaitThreadContainer container = new AwaitThreadContainer();

    /**
     * redis 客户端
     */
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存有效时间,毫秒
     */
    private long expiration;

    /**
     * 缓存主动在失效前强制刷新缓存的时间
     * 单位：毫秒
     */
    private long preloadTime = 0;

    /**
     * 是否强制刷新（走数据库），默认是false
     */
    private boolean forceRefresh = false;

    /**
     * 是否使用缓存名称作为 redis key 前缀
     */
    private boolean usePrefix;


    /**
     * @param name          缓存名称
     * @param redisTemplate  redis客户端 redis 客户端
     * @param expiration    key的有效时间
     * @param preloadTime   缓存主动在失效前强制刷新缓存的时间
     * @param forceRefresh  是否强制刷新（走数据库），默认是false
     */
    public RedisCache(String name, RedisTemplate<String, Object> redisTemplate, long expiration, long preloadTime, boolean forceRefresh) {
        this(name, redisTemplate, expiration, preloadTime, forceRefresh, true);
    }

    /**
     * @param name            缓存名称
     * @param redisTemplate  redis客户端   redis 客户端
     * @param expiration      key的有效时间
     * @param preloadTime     缓存主动在失效前强制刷新缓存的时间
     * @param forceRefresh    是否强制刷新（走数据库），默认是false
     * @param allowNullValues 是否允许存NULL值，模式允许
     */
    public RedisCache(String name, RedisTemplate<String, Object> redisTemplate, long expiration, long preloadTime, boolean forceRefresh, boolean allowNullValues) {
        super(allowNullValues, name);

        Assert.notNull(redisTemplate, "RedisTemplate 不能为NULL");
        this.redisTemplate = redisTemplate;
        this.expiration = expiration;
        this.preloadTime = preloadTime;
        this.forceRefresh = forceRefresh;
        this.usePrefix = true;
    }

    @Override
    public RedisTemplate<String, Object> getNativeCache() {
        return this.redisTemplate;
    }

    @Override
    public Object get(Object key) {
        RedisCacheKey redisCacheKey = getRedisCacheKey(key);
        logger.info("redis缓存 key: {} 查询redis缓存", redisCacheKey.getKey());
        return redisTemplate.opsForValue().get(redisCacheKey.getKeyBytes());
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        RedisCacheKey redisCacheKey = getRedisCacheKey(key);
        logger.info("redis缓存 key: {} 查询redis缓存如果没有命中，从数据库获取数据", redisCacheKey.getKey());
        // 先获取缓存，如果有直接返回
        Object result = redisTemplate.opsForValue().get(redisCacheKey.getKey());
        if (result != null) {
            // 刷新缓存
            refreshCache(redisCacheKey, valueLoader);
            return (T) result;
        }
        // 查库
        return getForDb(redisCacheKey, valueLoader);
    }

    @Override
    public void put(Object key, Object value) {
        RedisCacheKey redisCacheKey = getRedisCacheKey(key);
        logger.info("redis缓存 key: {} put缓存，缓存值：{}", redisCacheKey.getKey(), JSON.toJSONString(value));
        redisTemplate.opsForValue().set(redisCacheKey.getKey(), value, expiration, TimeUnit.MILLISECONDS);
    }

    @Override
    public Object putIfAbsent(Object key, Object value) {
        logger.info("redis缓存 key: {}putIfAbsent缓存，缓存值：{}", getRedisCacheKey(key).getKey(), JSON.toJSONString(value));
        Object reult = get(key);
        if (reult != null) {
            return reult;
        }
        put(key, value);
        return null;
    }

    @Override
    public void evict(Object key) {
        RedisCacheKey redisCacheKey = getRedisCacheKey(key);
        logger.info("redis缓存 key: {} 清除缓存", redisCacheKey.getKey());
        redisTemplate.delete(redisCacheKey.getKey());
    }

    @Override
    public void clear() {
        // 必须开启了使用缓存名称作为前缀，clear才有效
        if (usePrefix) {
            logger.info("redis缓存 ，除前缀为{}的缓存", getName());
            Set<String> keys = redisTemplate.keys(getName());
            if (!CollectionUtils.isEmpty(keys)) {
                redisTemplate.delete(keys);
            }
        }
    }


    /**
     * 获取 RedisCacheKey
     *
     * @param key 缓存key
     * @return
     */
    private RedisCacheKey getRedisCacheKey(Object key) {
        return new RedisCacheKey(key, redisTemplate.getKeySerializer())
                .cacheName(getName()).usePrefix(usePrefix);
    }

    /**
     * 同一个线程循环5次查询缓存，每次等待20毫秒，如果还是没有数据直接去库查询数据
     */
    private <T> T getForDb(RedisCacheKey redisCacheKey, Callable<T> valueLoader) {
        Lock redisLock = new Lock(redisTemplate, redisCacheKey.getKey() + "_sync_lock");
        // 同一个线程循环5次查询缓存，每次等待20毫秒，如果还是没有数据直接去库查询数据
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                // 先取缓存，如果有直接返回，没有再去做拿锁操作
                Object result = redisTemplate.opsForValue().get(redisCacheKey.getKey());
                if (result != null) {
                    logger.info("redis缓存 key: {} 获取到锁后查询查询缓存命中，不需要从数据库获取数据", redisCacheKey.getKey());
                    return (T) result;
                }

                // 获取分布式锁去后台查询数据
                if (redisLock.lock()) {
                    T t = (T) loaderAndPutValue(redisCacheKey, valueLoader);
                    logger.info("redis缓存 key: {} 从数据库获取数据完毕，唤醒所有等待线程", redisCacheKey.getKey());
                    // 唤醒线程
                    container.signalAll(redisCacheKey.getKey());
                    return t;
                }
                // 线程等待
                logger.info("redis缓存 key: {} 从数据库获取数据未获取到锁，进入等待状态，等待{}毫秒", redisCacheKey.getKey(), WAIT_TIME);
                container.await(redisCacheKey.getKey(), WAIT_TIME);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                redisLock.unlock();
            }
        }
        logger.info("redis缓存 key:{} 等待{}次，共{}毫秒，任未获取到缓存，直接走走库获取数据", redisCacheKey.getKey(), RETRY_COUNT, RETRY_COUNT * WAIT_TIME, WAIT_TIME);
        return (T) fromStoreValue(loaderAndPutValue(redisCacheKey, valueLoader));
    }

    /**
     * 加载并将数据放到redis缓存
     */
    private <T> Object loaderAndPutValue(RedisCacheKey key, Callable<T> valueLoader) {
        try {
            // 加载数据
            Object result = toStoreValue(valueLoader.call());
            // redis 缓存不允许直接存NULL，如果结果返回NULL需要删除缓存
            if (result == null) {
                redisTemplate.delete(key.getKey());
            } else {
                // 缓存值不为NULL，将数据放到缓存
                redisTemplate.opsForValue().set(key.getKey(), result, expiration, TimeUnit.MILLISECONDS);
            }
            logger.info("redis缓存 key:{} 从数据库获取数据，并将其放入缓存。数据:{}", key.getKey(), JSON.toJSONString(result));
            return result;
        } catch (Exception e) {
            logger.error("加载缓存数据异常,{}", e.getMessage(), e);
            throw new LoaderCacheValueException(key, valueLoader, e);
        }
    }

    /**
     * 刷新缓存数据
     */
    private <T> void refreshCache(RedisCacheKey redisCacheKey, Callable<T> valueLoader) {
        Long ttl = redisTemplate.getExpire(redisCacheKey.getKey());
        if (null != ttl && ttl <= preloadTime) {
            // 判断是否需要强制刷新在开启刷新线程
            if (!getForceRefresh()) {
                logger.info("redis缓存 key:{} 软刷新缓存", redisCacheKey.getKey());
                softRefresh(redisCacheKey);
            } else {
                logger.info("redis缓存 key:{} 强刷新缓存", redisCacheKey.getKey());
                forceRefresh(redisCacheKey, valueLoader);
            }
        }
    }

    /**
     * 软刷新，直接修改缓存时间
     *
     * @param redisCacheKey {@link RedisCacheKey}
     */
    private void softRefresh(RedisCacheKey redisCacheKey) {
        // 加一个分布式锁，只放一个请求去刷新缓存
        Lock redisLock = new Lock(redisTemplate, redisCacheKey.getKey() + "_lock");
        try {
            if (redisLock.tryLock()) {
                redisTemplate.expire(redisCacheKey.getKey(), this.expiration, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            redisLock.unlock();
        }
    }

    /**
     * 硬刷新（走数据库）
     *
     * @param redisCacheKey {@link RedisCacheKey}
     * @param valueLoader   数据加载器
     */
    private <T> void forceRefresh(RedisCacheKey redisCacheKey, Callable<T> valueLoader) {
        // 尽量少的去开启线程，因为线程池是有限的
        ThreadTaskUtils.run(() -> {
            // 加一个分布式锁，只放一个请求去刷新缓存
            Lock redisLock = new Lock(redisTemplate, redisCacheKey.getKey() + "_lock");
            try {
                if (redisLock.lock()) {
                    // 获取锁之后再判断一下过期时间，看是否需要加载数据
                    Long ttl = redisTemplate.getExpire(redisCacheKey.getKey());
                    if (null != ttl && ttl <= this.preloadTime) {
                        // 加载数据并放到缓存
                        loaderAndPutValue(redisCacheKey, valueLoader);
                    }
                }
            } catch (Exception e) {
                logger.info(e.getMessage(), e);
            } finally {
                redisLock.unlock();
            }
        });
    }

    /**
     * 是否强制刷新（走数据库），默认是false
     *
     * @return boolean
     */
    private boolean getForceRefresh() {
        return forceRefresh;
    }
}
