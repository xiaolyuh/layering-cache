package com.xiaolyuh.cache;

import com.xiaolyuh.cache.caffeine.CaffeineCache;
import com.xiaolyuh.cache.redis.RedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * @author yuhao.wang
 */
public class LayeringCache implements Cache {
    Logger logger = LoggerFactory.getLogger(LayeringCache.class);

    private String name;

    private CaffeineCache firstCache;

    private RedisCache secondCache;


    @Override
    public String getName() {
        return null;
    }

    @Override
    public Cache getNativeCache() {
        return null;
    }

    @Override
    public Object get(Object key) {
        return null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return null;
    }

    @Override
    public void put(Object key, Object value) {

    }

    @Override
    public Object putIfAbsent(Object key, Object value) {
        return null;
    }

    @Override
    public void evict(Object key) {

    }

    @Override
    public void clear() {

    }
}
