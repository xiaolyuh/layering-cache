package com.xiaolyuh.manager;

import com.xiaolyuh.cache.Cache;

import java.util.Collection;

/**
 * @author yuhao.wang
 */
public class LayeringCacheManager implements CacheManager {
    @Override
    public Cache getCache(String name) {
        return null;
    }

    @Override
    public Collection<String> getCacheNames() {
        return null;
    }
}
