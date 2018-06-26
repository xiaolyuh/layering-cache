package com.xiaolyuh.cache;

import java.util.concurrent.Callable;

/**
 * 基于Redis实现的二级缓存
 *
 * @author yuhao.wang
 */
public class RedisCache implements Cache {

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
