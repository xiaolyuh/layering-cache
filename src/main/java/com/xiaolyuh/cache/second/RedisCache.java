package com.xiaolyuh.cache.second;

import java.util.concurrent.Callable;

/**
 * 基于Caffeine实现的一级缓存
 *
 * @author yuhao.wang
 */
public class RedisCache implements SecondCache {
    public String getName() {
        return null;
    }

    public Object getNativeCache() {
        return null;
    }

    public ValueWrapper get(Object key) {
        return null;
    }

    public <T> T get(Object key, Class<T> type) {
        return null;
    }

    public <T> T get(Object key, Callable<T> valueLoader) {
        return null;
    }

    public void put(Object key, Object value) {

    }

    public ValueWrapper putIfAbsent(Object key, Object value) {
        return null;
    }

    public void evict(Object key) {

    }

    public void clear() {

    }
}
