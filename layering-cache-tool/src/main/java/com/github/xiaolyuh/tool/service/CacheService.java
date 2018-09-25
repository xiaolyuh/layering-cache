package com.github.xiaolyuh.tool.service;

import com.github.xiaolyuh.cache.Cache;
import com.github.xiaolyuh.manager.AbstractCacheManager;
import com.github.xiaolyuh.setting.LayeringCacheSetting;
import com.github.xiaolyuh.util.StringUtils;

import java.util.Set;

/**
 * 操作缓存的服务
 *
 * @author yuhao.wang3
 */
public class CacheService {
    /**
     * 删除缓存
     *
     * @param cacheName   缓存名称
     * @param internalKey 内部缓存名，由[一级缓存有效时间-二级缓存有效时间-二级缓存自动刷新时间]组成
     * @param key         key，可以为NULL，如果是NULL则清空缓存
     */
    public void deleteCache(String cacheName, String internalKey, String key) {
        if (StringUtils.isBlank(cacheName) || StringUtils.isBlank(internalKey)) {
            return;
        }

        Set<AbstractCacheManager> cacheManagers = AbstractCacheManager.getCacheManager();
        if (StringUtils.isBlank(key)) {
            // 清空缓存
            for (AbstractCacheManager cacheManager : cacheManagers) {
                LayeringCacheSetting layeringCacheSetting = new LayeringCacheSetting();
                layeringCacheSetting.setInternalKey(internalKey);
                Cache cache = cacheManager.getCache(cacheName, layeringCacheSetting);
                cache.clear();
            }

            return;
        }

        // 删除指定key
        for (AbstractCacheManager cacheManager : cacheManagers) {
            LayeringCacheSetting layeringCacheSetting = new LayeringCacheSetting();
            layeringCacheSetting.setInternalKey(internalKey);
            Cache cache = cacheManager.getCache(cacheName, layeringCacheSetting);
            cache.evict(key);
        }
    }
}
