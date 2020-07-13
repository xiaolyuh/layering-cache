package com.github.xiaolyuh.listener;

import com.alibaba.fastjson.JSON;
import com.github.xiaolyuh.redis.clinet.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * redis消息的发布者
 *
 * @author yuhao.wang
 */
public class RedisPublisher {
    private static final Logger logger = LoggerFactory.getLogger(RedisPublisher.class);

    private RedisPublisher() {
    }

    /**
     * 发布消息到频道（Channel）
     *
     * @param redisClient redis客户端
     * @param message     消息内容
     */
    public static void publisher(RedisClient redisClient, RedisPubSubMessage message) {
        redisClient.publish(RedisMessageListener.CHANNEL, JSON.toJSONString(message));
        logger.debug("redis消息发布者向频道【{}】发布了【{}】消息", RedisMessageListener.CHANNEL, message.toString());
    }
}
