package com.github.xiaolyuh.cache;

import com.alibaba.fastjson.JSON;
import com.github.xiaolyuh.redis.clinet.RedisClient;
import com.github.xiaolyuh.setting.LayeringCacheSetting;
import com.github.xiaolyuh.stats.CacheStats;
import com.github.xiaolyuh.support.CacheMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
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
     * @param redisClient          redisClient
     * @param firstCache           一级缓存
     * @param secondCache          二级缓存
     * @param stats                是否开启统计
     * @param layeringCacheSetting 多级缓存配置
     */
    public LayeringCache(RedisClient redisClient, AbstractValueAdaptingCache firstCache,
                         AbstractValueAdaptingCache secondCache, boolean stats, LayeringCacheSetting layeringCacheSetting) {
        this(redisClient, firstCache, secondCache, layeringCacheSetting.getCacheMode(), stats, secondCache.getName(), layeringCacheSetting);
    }

    /**
     * 创建一个多级缓存对象
     *
     * @param redisClient          redisClient
     * @param firstCache           一级缓存
     * @param secondCache          二级缓存
     * @param cacheMode            缓存模式
     * @param stats                是否开启统计，默认否
     * @param name                 缓存名称
     * @param layeringCacheSetting 多级缓存配置
     */
    public LayeringCache(RedisClient redisClient, AbstractValueAdaptingCache firstCache,
                         AbstractValueAdaptingCache secondCache, CacheMode cacheMode, boolean stats, String name, LayeringCacheSetting layeringCacheSetting) {
        super(stats, name,cacheMode);
        this.redisClient = redisClient;
        this.firstCache = firstCache;
        this.secondCache = secondCache;
        this.layeringCacheSetting = layeringCacheSetting;
    }

    @Override
    public LayeringCache getNativeCache() {
        return this;
    }

    @Override
    public <T> T get(String key, Class<T> resultType) {
        // 开启一级缓存
        if (!CacheMode.SECOND.equals(cacheMode)) {
            Object result = firstCache.get(key, resultType);
            if (logger.isDebugEnabled()) {
                logger.debug("查询一级缓存。 key={}:{},返回值是:{}", getName(), key, JSON.toJSONString(result));
            }
            if (result != null) {
                return (T) fromStoreValue(result);
            }
        }

        // 开启二级缓存
        T result = null;
        if (!CacheMode.FIRST.equals(cacheMode)) {
            result = secondCache.get(key, resultType);
        }

        // 开启一级缓存
        if (!CacheMode.SECOND.equals(cacheMode)) {
            firstCache.putIfAbsent(key, result, resultType);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("查询二级缓存,并将数据放到一级缓存。 key={}:{},返回值是:{}", getName(), key, JSON.toJSONString(result));
        }
        return result;
    }

    @Override
    public <T> T get(String key, Class<T> resultType, Callable<T> valueLoader) {
        // 开启一级缓存
        if (!CacheMode.SECOND.equals(cacheMode)) {
            T result = CacheMode.FIRST.equals(cacheMode) ? firstCache.get(key, resultType, valueLoader) : firstCache.get(key, resultType);
            if (logger.isDebugEnabled()) {
                logger.debug("查询一级缓存。 key={}:{},返回值是:{}", getName(), key, JSON.toJSONString(result));
            }
            if (result != null) {
                return (T) fromStoreValue(result);
            }
        }
        // 开启二级缓存
        T result = null;
        if (!CacheMode.FIRST.equals(cacheMode)) {
            result = secondCache.get(key, resultType, valueLoader);
        }

        // 开启一级缓存
        if (!CacheMode.SECOND.equals(cacheMode)) {
            firstCache.putIfAbsent(key, result, resultType);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("查询二级缓存,并将数据放到一级缓存。 key={}:{},返回值是:{}", getName(), key, JSON.toJSONString(result));
        }
        return result;
    }

    @Override
    @SuppressWarnings("all")
    public  <K,V> Map<K, V> getAllPresent(List<String> keys, Class<V> resultType) {
        Map<K, V> values = new HashMap<>(keys.size());
        // 开启一级缓存
        if (!CacheMode.SECOND.equals(cacheMode)) {
             values.putAll(firstCache.getAllPresent(keys, resultType));
            if (logger.isDebugEnabled()) {
                logger.debug("查询一级缓存。 cacheName={} keys={},返回值是:{}", getName(), JSON.toJSONString(keys), JSON.toJSONString(values));
            }
            if(values.size() == keys.size() || CacheMode.FIRST.equals(cacheMode)){
                return values;
            }
        }

        // 开启二级缓存
        // 找出一级缓存中没有的键
        List<String> missingKeys = keys;
        if(!CacheMode.SECOND.equals(cacheMode)){
            missingKeys = keys.stream()
                .filter(key -> !values.containsKey(key))
                .collect(Collectors.toList());
        }

        values.putAll(secondCache.getAllPresent(missingKeys, resultType));
        // 开启一级缓存
        if (!CacheMode.SECOND.equals(cacheMode)) {
            for (String key : keys) {
                firstCache.put(key,values.get(key));
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("查询二级缓存,并将数据放到一级缓存。 cacheName={} keys={},返回值是:{}", getName(), JSON.toJSONString(keys), JSON.toJSONString(values));
        }
        return values;
    }

    @Override
    @SuppressWarnings("all")
    public <K,V> Map<K, V> getAll(List<String> keys, Class<V> resultType, Function<String[], Object> valueLoader) {
        // 开启一级缓存
        if (CacheMode.FIRST.equals(cacheMode)) {
            Map<K, V> values = firstCache.getAll(keys, resultType, valueLoader);
            if (logger.isDebugEnabled()) {
                logger.debug("查询一级缓存。 key={}:{},返回值是:{}", getName(), keys, JSON.toJSONString(values));
            }
            return values;
        }
        // 开启二级缓存
        Map<K, V> values = new HashMap<>(keys.size());
        List<String> missingKeys = keys;

        if (!CacheMode.SECOND.equals(cacheMode)) {
            values.putAll(firstCache.getAllPresent(keys, resultType));
            missingKeys = keys.stream()
                .filter(key -> !values.containsKey(key))
                .collect(Collectors.toList());
        }

        if(!missingKeys.isEmpty()){
            Map<K, V> missKeysValues = secondCache.getAll(missingKeys, resultType, valueLoader);

            // 开启一级缓存
            if (logger.isDebugEnabled()) {
                logger.debug("查询二级缓存,并将数据放到一级缓存。 key={}:{},返回值是:{}", getName(), keys, JSON.toJSONString(missKeysValues));
            }
            if (!CacheMode.SECOND.equals(cacheMode)) {
                for (Entry<K, V> entry : missKeysValues.entrySet()) {
                    firstCache.put((String) entry.getKey(),entry.getValue());
                }
            }

            values.putAll(missKeysValues);
        }
        if(isAllowNullValues()){
            List<K> nullValuesKeys = values.entrySet().stream().filter(kvEntry -> {
                return fromStoreValue(kvEntry.getValue()) == null;
            }).map(Entry::getKey).collect(Collectors.toList());
            for (K nullValuesKey : nullValuesKeys) {
                values.remove(nullValuesKey);
            }
        }
        return values;
    }

    @Override
    public void put(String key, Object value) {
        // 只开启一级缓存
        if (CacheMode.FIRST.equals(cacheMode)) {
            firstCache.put(key, value);
            return;
        }

        // 开启二级缓存
        secondCache.put(key, value);
        // 删除一级缓存
        deleteClusterFirstCacheByKey(key, redisClient);
    }

    @Override
    public <T> T putIfAbsent(String key, Object value, Class<T> resultType) {
        // 只开启一级缓存
        if (CacheMode.FIRST.equals(cacheMode)) {
            return firstCache.putIfAbsent(key, value, resultType);
        }

        // 开启二级缓存
        T result = secondCache.putIfAbsent(key, value, resultType);
        // 删除一级缓存
        deleteClusterFirstCacheByKey(key, redisClient);
        return result;
    }

    @Override
    public void evict(String key) {
        if (!CacheMode.FIRST.equals(cacheMode)) {
            // 开启二级缓存、删除的时候要先删除二级缓存再删除一级缓存，否则有并发问题
            secondCache.evict(key);
        }
        if (!CacheMode.SECOND.equals(cacheMode)) {
            // 删除一级缓存
            deleteClusterFirstCacheByKey(key, redisClient);
        }
    }

    @Override
    public void clear() {
        if (!CacheMode.FIRST.equals(cacheMode)) {
            // 开启二级缓存、删除的时候要先删除二级缓存再删除一级缓存，否则有并发问题
            secondCache.clear();
        }
        if (!CacheMode.SECOND.equals(cacheMode)) {
            // 删除一级缓存
            clearClusterFirstCache(redisClient);
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
