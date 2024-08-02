package com.github.xiaolyuh.cache.caffeine;

import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.xiaolyuh.cache.AbstractValueAdaptingCache;
import com.github.xiaolyuh.setting.FirstCacheSetting;
import com.github.xiaolyuh.support.CacheMode;
import com.github.xiaolyuh.support.ExpireMode;
import com.github.xiaolyuh.support.NullValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于Caffeine实现的一级缓存
 *
 * @author yuhao.wang
 */
public class CaffeineCache extends AbstractValueAdaptingCache {
    protected static final Logger logger = LoggerFactory.getLogger(CaffeineCache.class);

    /**
     * 缓存对象
     */
    private final Cache<Object, Object> cache;

    /**
     * 使用name和{@link FirstCacheSetting}创建一个 {@link CaffeineCache} 实例
     *
     * @param name              缓存名称
     * @param firstCacheSetting 一级缓存配置 {@link FirstCacheSetting}
     * @param stats             是否开启统计模式
     * @param cacheMode         缓存模式
     */
    public CaffeineCache(String name, FirstCacheSetting firstCacheSetting, boolean stats, CacheMode cacheMode) {

        super(stats, name, cacheMode);
        this.cache = getCache(firstCacheSetting);
    }

    @Override
    public Cache<Object, Object> getNativeCache() {
        return this.cache;
    }

    @Override
    public <T> T get(String key, Class<T> resultType) {
        if (logger.isDebugEnabled()) {
            logger.debug("caffeine缓存 key={}:{} 获取缓存", getName(), key);
        }

        if (isStats()) {
            getCacheStats().addCacheRequestCount(1);
        }

        if (this.cache instanceof LoadingCache) {
            return (T) ((LoadingCache<Object, Object>) this.cache).get(key);
        }
        return (T) cache.getIfPresent(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> resultType, Callable<T> valueLoader) {
        if (logger.isDebugEnabled()) {
            logger.debug("caffeine缓存 key={}:{} 获取缓存， 如果没有命中就走库加载缓存", getName(), key);
        }

        if (isStats()) {
            getCacheStats().addCacheRequestCount(1);
        }

        Object result = this.cache.get(key, k -> loaderValue(key, valueLoader));
        // 如果不允许存NULL值 直接删除NULL值缓存
        boolean isEvict = !isAllowNullValues() && (result == null || result instanceof NullValue);
        if (isEvict) {
            evict(key);
        }
        return (T) fromStoreValue(result);
    }

    @Override
    @SuppressWarnings("all")
    public <K, V> Map<K, V> getAll(List<String> keys, Class<V> resultType) {
        if (logger.isDebugEnabled()) {
            logger.debug("caffeine缓存 keys={}:{} 获取缓存", getName(), JSON.toJSONString(keys));
        }

        if (isStats()) {
            getCacheStats().addCacheRequestCount(keys.size());
        }

        if (this.cache instanceof LoadingCache) {
            Map<K, V> cacheValues = ((LoadingCache) this.cache).getAll(keys);
            return cacheValues;
        }

        Map<Object, Object> allPresent = cache.getAllPresent(keys);
        if (logger.isDebugEnabled()) {
            logger.debug("caffeine缓存 keys={}:{} 获取缓存,结果是：{}", getName(), JSON.toJSONString(keys), JSON.toJSONString(allPresent));
        }
        return (Map<K, V>) new HashMap<>(allPresent);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> getAll(List<String> keys, Class<V> resultType, Function<String[], Object> valueLoader) {
        if (logger.isDebugEnabled()) {
            logger.debug("caffeine缓存 keys={}:{} 获取缓存,如果没有命中就走库加载缓存", getName(), JSON.toJSONString(keys));
        }
        if (isStats()) {
            getCacheStats().addCacheRequestCount(keys.size());
        }

        // 从 Caffeine 缓存中获取值
        Map<Object, Object> cacheAllPresent = cache.getAllPresent(keys);
        HashMap<Object, Object> cacheValues = new HashMap<>(cacheAllPresent);

        // 找出缓存中没有的键
        List<String> missingKeys = keys.stream()
                .filter(key -> !cacheValues.containsKey(key))
                .collect(Collectors.toList());

        // 加载 获取这些缺失的值
        if (!missingKeys.isEmpty()) {

            long start = System.currentTimeMillis();
            if (isStats()) {
                getCacheStats().addCachedMethodRequestCount(1);
            }

            List<Object> loadValues = (List<Object>) valueLoader.apply(missingKeys.toArray(new String[0]));

            if (logger.isDebugEnabled()) {
                logger.debug("caffeine缓存 cacheName={}  missingKeys{} 从库加载缓存 {}", getName(), JSON.toJSONString(missingKeys), JSON.toJSONString(loadValues));
            }

            if (isStats()) {
                getCacheStats().addCachedMethodRequestTime(System.currentTimeMillis() - start);
            }
            for (int i = 0; i < missingKeys.size(); i++) {
                if (loadValues.get(i) != null) {
                    cacheValues.put(missingKeys.get(i), loadValues.get(i));
                    cache.put(missingKeys.get(i), loadValues.get(i));
                } else if (isAllowNullValues()) {
                    // 如果允许NULL值，则缓存NullValue
                    cacheValues.put(missingKeys.get(i), NullValue.INSTANCE);
                    cache.put(missingKeys.get(i), NullValue.INSTANCE);

                }
            }
        }

        return (Map<K, V>) cacheValues;
    }

    @Override
    public void put(String key, Object value) {
        // 允许存NULL值
        if (isAllowNullValues()) {
            if (logger.isDebugEnabled()) {
                logger.debug("caffeine缓存 key={}:{} put缓存，缓存值：{}", getName(), key, JSON.toJSONString(value));
            }
            this.cache.put(key, toStoreValue(value));
            return;
        }

        // 不允许存NULL值
        if (value != null && !(value instanceof NullValue)) {
            if (logger.isDebugEnabled()) {
                logger.debug("caffeine缓存 key={}:{} put缓存，缓存值：{}", getName(), key, JSON.toJSONString(value));
            }
            this.cache.put(key, toStoreValue(value));
            return;
        }
        logger.debug("缓存值为NULL并且不允许存NULL值，不缓存数据");
    }

    @Override
    public <T> T putIfAbsent(String key, Object value, Class<T> resultType) {
        if (logger.isDebugEnabled()) {
            logger.debug("caffeine缓存 key={}:{} putIfAbsent 缓存，缓存值：{}", getName(), key, JSON.toJSONString(value));
        }
        boolean flag = !isAllowNullValues() && (value == null || value instanceof NullValue);
        if (flag) {
            return null;
        }
        Object result = this.cache.get(key, k -> toStoreValue(value));
        return (T) fromStoreValue(result);
    }

    @Override
    public void evict(String key) {
        if (logger.isDebugEnabled()) {
            logger.debug("caffeine缓存 key={}:{} 清除缓存", getName(), key);
        }
        this.cache.invalidate(key);
    }

    @Override
    public void evictAll(List<String> keys) {
        if (logger.isDebugEnabled()) {
            logger.debug("caffeine缓存  cacheName={}, keys={} 批量清除缓存", getName(), JSON.toJSONString(keys));
        }
        this.cache.invalidateAll(keys);
    }

    @Override
    public void clear() {
        logger.debug("caffeine缓存清空缓存");
        this.cache.invalidateAll();
    }

    /**
     * 加载数据
     */
    private <T> Object loaderValue(Object key, Callable<T> valueLoader) {
        long start = System.currentTimeMillis();
        if (isStats()) {
            getCacheStats().addCachedMethodRequestCount(1);
        }

        try {
            T t = valueLoader.call();
            if (logger.isDebugEnabled()) {
                logger.debug("caffeine缓存 key={}:{} 从库加载缓存 {}", getName(), key, JSON.toJSONString(t));
            }

            if (isStats()) {
                getCacheStats().addCachedMethodRequestTime(System.currentTimeMillis() - start);
            }
            return toStoreValue(t);
        } catch (Exception e) {
            throw new LoaderCacheValueException(key, e);
        }

    }

    /**
     * 根据配置获取本地缓存对象
     *
     * @param firstCacheSetting 一级缓存配置
     * @return {@link Cache}
     */
    private static Cache<Object, Object> getCache(FirstCacheSetting firstCacheSetting) {
        // 根据配置创建Caffeine builder
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        builder.initialCapacity(firstCacheSetting.getInitialCapacity());
        builder.maximumSize(firstCacheSetting.getMaximumSize());
        builder.softValues();
        if (ExpireMode.WRITE.equals(firstCacheSetting.getExpireMode())) {
            builder.expireAfterWrite(firstCacheSetting.getExpireTime(), firstCacheSetting.getTimeUnit());
        } else if (ExpireMode.ACCESS.equals(firstCacheSetting.getExpireMode())) {
            builder.expireAfterAccess(firstCacheSetting.getExpireTime(), firstCacheSetting.getTimeUnit());
        }
        // 根据Caffeine builder创建 Cache 对象
        return builder.build();
    }

    @Override
    public boolean isAllowNullValues() {
        return false;
    }

    @Override
    public long estimatedSize() {
        return cache.estimatedSize();
    }
}
