package com.xiaolyuh.manager;

import com.xiaolyuh.cache.Cache;
import com.xiaolyuh.cache.LayeringCache;
import com.xiaolyuh.cache.caffeine.CaffeineCache;
import com.xiaolyuh.cache.redis.RedisCache;
import com.xiaolyuh.setting.LayeringCacheSetting;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author yuhao.wang
 */
public class LayeringCacheManager extends AbstractCacheManager {
    /**
     * redis 客户端
     */
    private RedisTemplate<String, Object> redisTemplate;

    public LayeringCacheManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected Cache getMissingCache(String name, LayeringCacheSetting layeringCacheSetting) {
        // 创建一级缓存
        CaffeineCache caffeineCache = new CaffeineCache(name, layeringCacheSetting.getFirstCacheSetting());
        // 创建二级缓存
        RedisCache redisCache = new RedisCache(name, redisTemplate, layeringCacheSetting.getSecondaryCacheSetting());
        return new LayeringCache(redisTemplate, caffeineCache, redisCache);
    }

    @Override
    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }
}
