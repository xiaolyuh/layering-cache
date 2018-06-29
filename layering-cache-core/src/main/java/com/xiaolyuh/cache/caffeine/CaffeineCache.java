package com.xiaolyuh.cache.caffeine;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.xiaolyuh.support.AbstractValueAdaptingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.UsesJava8;
import org.springframework.util.Assert;

import java.util.concurrent.Callable;

/**
 * 基于Caffeine实现的一级缓存
 *
 * @author yuhao.wang
 */
@UsesJava8
public class CaffeineCache extends AbstractValueAdaptingCache {
    protected static final Logger logger = LoggerFactory.getLogger(CaffeineCache.class);

    /**
     * 缓存对象
     */
    private final com.github.benmanes.caffeine.cache.Cache<Object, Object> cache;


    /**
     * 使用name和{@link com.github.benmanes.caffeine.cache.Cache}创建一个 {@link CaffeineCache} 实例
     *
     * @param name  缓存名称
     * @param cache 一个 Caffeine Cache 的实例对象
     */
    public CaffeineCache(String name, com.github.benmanes.caffeine.cache.Cache<Object, Object> cache) {
        this(name, cache, true);
    }

    /**
     * 使用name和{@link com.github.benmanes.caffeine.cache.Cache}创建一个 {@link CaffeineCache} 实例
     *
     * @param name            缓存名称
     * @param cache           t一个 Caffeine Cache 的实例对象
     * @param allowNullValues 缓存是否允许存NULL（true：允许）
     */
    public CaffeineCache(String name, com.github.benmanes.caffeine.cache.Cache<Object, Object> cache,
                         boolean allowNullValues) {

        super(allowNullValues, name);
        Assert.notNull(cache, "Cache 不能为NULL");
        this.cache = cache;
    }

    @Override
    public com.github.benmanes.caffeine.cache.Cache<Object, Object> getNativeCache() {
        return this.cache;
    }

    @Override
    protected Object lookup(Object key) {
        if (this.cache instanceof LoadingCache) {
            Object value = ((LoadingCache<Object, Object>) this.cache).get(key);
            return fromStoreValue(value);
        }
        return fromStoreValue(cache.getIfPresent(key));
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object result = this.cache.get(key, (vl) -> loaderValue(key, valueLoader));
        return (T) fromStoreValue(result);
    }

    @Override
    public void put(Object key, Object value) {
        this.cache.put(key, value);
    }

    @Override
    public Object putIfAbsent(Object key, Object value) {
        Object result = this.cache.get(key, (v) -> toStoreValue(v));
        return fromStoreValue(result);
    }

    @Override
    public void evict(Object key) {
        this.cache.invalidate(key);
    }

    @Override
    public void clear() {
        this.cache.invalidateAll();
    }

    /**
     * 加载数据
     */
    private <T> Object loaderValue(Object key, Callable<T> valueLoader) {
        try {
            return toStoreValue(valueLoader.call());
        } catch (Exception e) {
            logger.error("加载缓存数据异常,{}", e.getMessage(), e);
            throw new LoaderCacheValueException(key, valueLoader, e);
        }
    }
}
