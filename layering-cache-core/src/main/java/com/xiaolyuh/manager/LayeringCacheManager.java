package com.xiaolyuh.manager;

import com.xiaolyuh.cache.Cache;
import com.xiaolyuh.listener.RedisMessageListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.Collection;

/**
 * @author yuhao.wang
 */
public class LayeringCacheManager extends AbstractCacheManager {
    /**
     * redis 客户端
     */
    private RedisTemplate<? extends Object, ? extends Object> redisTemplate;

    public LayeringCacheManager(RedisTemplate<? extends Object, ? extends Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Cache getCache(String name) {

        return null;
    }

    @Override
    public Collection<String> getCacheNames() {
        return null;
    }

    @Override
    public RedisTemplate<? extends Object, ? extends Object> getRedisTemplate() {
        return redisTemplate;
    }
}
