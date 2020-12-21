package com.github.xiaolyuh.cache;

import com.alibaba.fastjson.JSON;
import com.github.xiaolyuh.listener.RedisPubSubMessage;
import com.github.xiaolyuh.listener.RedisPubSubMessageType;
import com.github.xiaolyuh.listener.RedisPublisher;
import com.github.xiaolyuh.redis.clinet.RedisClient;
import com.github.xiaolyuh.setting.LayeringCacheSetting;
import com.github.xiaolyuh.stats.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * 多级缓存
 *
 * @author yuhao.wang
 */
public class LayeringCache extends AbstractValueAdaptingCache {
    Logger logger = LoggerFactory.getLogger(LayeringCache.class);

    /**
     * redis 客户端
     */
    private final RedisClient redisClient;

    /**
     * 一级缓存
     */
    private final AbstractValueAdaptingCache firstCache;

    /**
     * 二级缓存
     */
    private final AbstractValueAdaptingCache secondCache;

    /**
     * 多级缓存配置
     */
    private final LayeringCacheSetting layeringCacheSetting;

    /**
     * 是否使用一级缓存， 默认true
     */
    private final boolean enableFirstCache;

    /**
     * @param redisClient          redisClient
     * @param firstCache           一级缓存
     * @param secondCache          二级缓存
     * @param stats                是否开启统计
     * @param layeringCacheSetting 多级缓存配置
     */
    public LayeringCache(RedisClient redisClient, AbstractValueAdaptingCache firstCache,
                         AbstractValueAdaptingCache secondCache, boolean stats, LayeringCacheSetting layeringCacheSetting) {
        this(redisClient, firstCache, secondCache, layeringCacheSetting.isEnableFirstCache(), stats, secondCache.getName(), layeringCacheSetting);
    }

    /**
     * 创建一个多级缓存对象
     *
     * @param redisClient          redisClient
     * @param firstCache           一级缓存
     * @param secondCache          二级缓存
     * @param enableFirstCache     是否使用一级缓存，默认是
     * @param stats                是否开启统计，默认否
     * @param name                 缓存名称
     * @param layeringCacheSetting 多级缓存配置
     */
    public LayeringCache(RedisClient redisClient, AbstractValueAdaptingCache firstCache,
                         AbstractValueAdaptingCache secondCache, boolean enableFirstCache, boolean stats, String name, LayeringCacheSetting layeringCacheSetting) {
        super(stats, name);
        this.redisClient = redisClient;
        this.firstCache = firstCache;
        this.secondCache = secondCache;
        this.enableFirstCache = enableFirstCache;
        this.layeringCacheSetting = layeringCacheSetting;
    }

    @Override
    public LayeringCache getNativeCache() {
        return this;
    }

    @Override
    public <T> T get(String key, Class<T> resultType) {
        if (enableFirstCache) {
            Object result = firstCache.get(key, resultType);
            if (logger.isDebugEnabled()) {
                logger.debug("查询一级缓存。 key={},返回值是:{}", key, JSON.toJSONString(result));
            }
            if (result != null) {
                return (T) fromStoreValue(result);
            }
        }

        T result = secondCache.get(key, resultType);

        if (enableFirstCache) {
            firstCache.putIfAbsent(key, result, resultType);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("查询二级缓存,并将数据放到一级缓存。 key={},返回值是:{}", key, JSON.toJSONString(result));
        }
        return result;
    }

    @Override
    public <T> T get(String key, Class<T> resultType, Callable<T> valueLoader) {
        if (enableFirstCache) {
            T result = firstCache.get(key, resultType);
            if (logger.isDebugEnabled()) {
                logger.debug("查询一级缓存。 key={},返回值是:{}", key, JSON.toJSONString(result));
            }
            if (result != null) {
                return (T) fromStoreValue(result);
            }
        }
        T result = secondCache.get(key, resultType, valueLoader);

        if (enableFirstCache) {
            firstCache.putIfAbsent(key, result, resultType);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("查询二级缓存,并将数据放到一级缓存。 key={},返回值是:{}", key, JSON.toJSONString(result));
        }
        return result;
    }

    @Override
    public void put(String key, Object value) {
        secondCache.put(key, value);
        // 删除一级缓存
        if (enableFirstCache) {
            deleteFirstCache(key, redisClient);
        }
    }

    @Override
    public <T> T putIfAbsent(String key, Object value, Class<T> resultType) {
        T result = secondCache.putIfAbsent(key, value, resultType);
        // 删除一级缓存
        if (enableFirstCache) {
            deleteFirstCache(key, redisClient);
        }
        return result;
    }

    @Override
    public void evict(String key) {
        // 删除的时候要先删除二级缓存再删除一级缓存，否则有并发问题
        secondCache.evict(key);
        // 删除一级缓存
        if (enableFirstCache) {
            deleteFirstCache(key, redisClient);
        }
    }

    @Override
    public void clear() {
        // 删除的时候要先删除二级缓存再删除一级缓存，否则有并发问题
        secondCache.clear();
        if (enableFirstCache) {
            // 清除一级缓存需要用到redis的订阅/发布模式，否则集群中其他服服务器节点的一级缓存数据无法删除
            RedisPubSubMessage message = new RedisPubSubMessage();
            message.setCacheName(getName());
            message.setMessageType(RedisPubSubMessageType.CLEAR);
            // 发布消息
            RedisPublisher.publisher(redisClient, message);
        }
    }


    /**
     * 获取一级缓存
     *
     * @return FirstCache
     */
    public Cache getFirstCache() {
        return firstCache;
    }

    /**
     * 获取二级缓存
     *
     * @return SecondCache
     */
    public Cache getSecondCache() {
        return secondCache;
    }

    @Override
    public CacheStats getCacheStats() {
        CacheStats cacheStats = new CacheStats();
        cacheStats.addCacheRequestCount(firstCache.getCacheStats().getCacheRequestCount().longValue());
        cacheStats.addCachedMethodRequestCount(secondCache.getCacheStats().getCachedMethodRequestCount().longValue());
        cacheStats.addCachedMethodRequestTime(secondCache.getCacheStats().getCachedMethodRequestTime().longValue());

        firstCache.getCacheStats().addCachedMethodRequestCount(secondCache.getCacheStats().getCacheRequestCount().longValue());

        setCacheStats(cacheStats);
        return cacheStats;
    }

    public LayeringCacheSetting getLayeringCacheSetting() {
        return layeringCacheSetting;
    }

    @Override
    public boolean isAllowNullValues() {
        return secondCache.isAllowNullValues();
    }
}
