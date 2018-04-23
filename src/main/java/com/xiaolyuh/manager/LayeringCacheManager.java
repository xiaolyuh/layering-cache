package com.xiaolyuh.manager;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;

/**
 * @author yuhao.wang
 */
public class LayeringCacheManager implements CacheManager {
    public Cache getCache(String name) {
        return null;
    }

    public Collection<String> getCacheNames() {
        return null;
    }
}
