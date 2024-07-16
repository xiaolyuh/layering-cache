package com.github.xiaolyuh.listener;

import com.github.xiaolyuh.manager.AbstractCacheManager;
import com.github.xiaolyuh.util.BeanFactory;
import io.lettuce.core.pubsub.RedisPubSubListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * redis消息的订阅者
 *
 * @author yuhao.wang
 */
public class RedisMessageListener implements RedisPubSubListener<String, String> {
    private static final Logger log = LoggerFactory.getLogger(RedisMessageListener.class);

    public static final String CHANNEL = "layering-cache-channel";

    /**
     * redis消息处理器
     */
    private RedisMessageService redisMessageService;

    public void init(AbstractCacheManager cacheManager) {
        this.redisMessageService = BeanFactory.getBean(RedisMessageService.class).init(cacheManager);
        // 创建监听
        cacheManager.getRedisClient().subscribe(this, RedisMessageListener.CHANNEL);
    }

    @Override
    public void message(String channel, String message) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("redis消息订阅者接收到频道【{}】发布的消息。消息内容：{}", channel, message);
            }

            // 更新最后一次处理拉消息的时间
            RedisMessageService.updateLastPushTime();

            redisMessageService.pullMessage();
        } catch (Exception e) {
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
}
