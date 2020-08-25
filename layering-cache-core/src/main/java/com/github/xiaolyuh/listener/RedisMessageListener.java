package com.github.xiaolyuh.listener;

import com.alibaba.fastjson.JSON;
import com.github.xiaolyuh.cache.Cache;
import com.github.xiaolyuh.cache.LayeringCache;
import com.github.xiaolyuh.manager.AbstractCacheManager;
import io.lettuce.core.pubsub.RedisPubSubListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * redis消息的订阅者
 *
 * @author yuhao.wang
 */
public class RedisMessageListener implements RedisPubSubListener<String, String> {
    private static final Logger log = LoggerFactory.getLogger(RedisMessageListener.class);

    public static final String CHANNEL = "layering-cache-channel";

    /**
     * 缓存管理器
     */
    private AbstractCacheManager cacheManager;


    @Override
    public void message(String channel, String message) {
        try {
            log.debug("redis消息订阅者接收到频道【{}】发布的消息。消息内容：{}", channel, message);
            RedisPubSubMessage redisPubSubMessage = JSON.parseObject(message, RedisPubSubMessage.class);
            // 根据缓存名称获取多级缓存，可能有多个
            Collection<Cache> caches = cacheManager.getCache(redisPubSubMessage.getCacheName());
            for (Cache cache : caches) {
                // 判断缓存是否是多级缓存
                if (cache != null && cache instanceof LayeringCache) {
                    switch (redisPubSubMessage.getMessageType()) {
                        case EVICT:
                            if (RedisPubSubMessage.SOURCE.equals(redisPubSubMessage.getSource())) {
                                ((LayeringCache) cache).getSecondCache().evict(redisPubSubMessage.getKey());
                            }
                            // 获取一级缓存，并删除一级缓存数据
                            ((LayeringCache) cache).getFirstCache().evict(redisPubSubMessage.getKey());
                            log.info("删除一级缓存{}数据,key={}", redisPubSubMessage.getCacheName(), redisPubSubMessage.getKey());
                            break;

                        case CLEAR:
                            if (RedisPubSubMessage.SOURCE.equals(redisPubSubMessage.getSource())) {
                                ((LayeringCache) cache).getSecondCache().clear();
                            }
                            // 获取一级缓存，并删除一级缓存数据
                            ((LayeringCache) cache).getFirstCache().clear();
                            log.info("清除一级缓存{}数据", redisPubSubMessage.getCacheName());
                            break;

                        default:
                            log.error("接收到没有定义的订阅消息频道数据");
                            break;
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("layering-cache 清楚一级缓存异常：{}", e.getMessage(), e);
        }
    }

    @Override
    public void message(String pattern, String channel, String message) {

    }

    @Override
    public void subscribed(String channel, long count) {

    }

    @Override
    public void psubscribed(String pattern, long count) {

    }

    @Override
    public void unsubscribed(String channel, long count) {

    }

    @Override
    public void punsubscribed(String pattern, long count) {

    }

    public void setCacheManager(AbstractCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
}
