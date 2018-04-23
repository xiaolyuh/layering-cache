package com.xiaolyuh.cache.layering;

import com.xiaolyuh.cache.Cache;
import com.xiaolyuh.cache.first.FirstCache;
import com.xiaolyuh.cache.second.SecondCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * @author yuhao.wang
 */
public class LayeringCache implements Cache {
    Logger logger = LoggerFactory.getLogger(LayeringCache.class);

    private String name;

    private FirstCache firstCache;

    private SecondCache secondCache;

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
