package com.xiaolyuh.cache.redis;

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
     * 等待线程容器
     */
    ThreadAwaitContainer container = new ThreadAwaitContainer();

    /**
     * redis 客户端
     */
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存有效时间,毫秒
     */
    private long expiration;

    /**
     * 是否使用缓存名称作为 redis key 前缀
     */
    private boolean usePrefix;

    /**
     * @param name          缓存名称
     * @param redisTemplate redis 客户端
     * @param expiration    key的有效时间
     */
    public RedisCache(String name, RedisTemplate<String, Object> redisTemplate, long expiration) {
        this(name, redisTemplate, expiration, true);
    }

    /**
     * @param name            缓存名称
     * @param redisTemplate   redis 客户端
     * @param expiration      key的有效时间
     * @param allowNullValues 是否允许存NULL值，模式允许
     */
    public RedisCache(String name, RedisTemplate<String, Object> redisTemplate, long expiration, boolean allowNullValues) {
        super(allowNullValues, name);

        Assert.notNull(redisTemplate, "RedisTemplate 不能为NULL");
        this.redisTemplate = redisTemplate;
        this.expiration = expiration;
        this.usePrefix = true;
    }

    @Override
    public RedisTemplate<String, Object> getNativeCache() {
        return this.redisTemplate;
    }

    @Override
    public Object get(Object key) {
        RedisCacheKey redisCacheKey = getRedisCacheKey(key);
        return redisTemplate.opsForValue().get(redisCacheKey.getKeyBytes());
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        RedisCacheKey redisCacheKey = getRedisCacheKey(key);
        // 先获取缓存，如果有直接返回
        Object result = redisTemplate.opsForValue().get(redisCacheKey.getKey());
        if (result != null) {
            return (T) result;
        }
        // 查库
        return getForDb(redisCacheKey, valueLoader);
    }

    @Override
    public void put(Object key, Object value) {
        redisTemplate.opsForValue().set(getRedisCacheKey(key).getKey(), value, expiration, TimeUnit.MILLISECONDS);
    }

    @Override
    public Object putIfAbsent(Object key, Object value) {
        Object reult = get(key);
        if (reult != null) {
            return reult;
        }
        put(key, value);
        return null;
    }

    @Override
    public void evict(Object key) {
        redisTemplate.delete(getRedisCacheKey(key).getKey());
    }

    @Override
    public void clear() {
        // 必须开启了使用缓存名称作为前缀，clear才有效
        if (usePrefix) {
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
                    return (T) result;
                }

                // 获取分布式锁去后台查询数据
                if (redisLock.lock()) {
                    T t = (T) loaderAndPutValue(redisCacheKey, valueLoader);
                    // 唤醒线程
                    container.signalAll(redisCacheKey.getKey());
                    return t;
                }
                // 线程等待
                container.await(redisCacheKey.getKey(), 20);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                redisLock.unlock();
            }
        }

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
            return result;
        } catch (Exception e) {
            logger.error("加载缓存数据异常,{}", e.getMessage(), e);
            throw new LoaderCacheValueException(key, valueLoader, e);
        }
    }

}
