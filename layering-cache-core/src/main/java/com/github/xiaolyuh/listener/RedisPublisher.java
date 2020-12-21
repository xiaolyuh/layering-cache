package com.github.xiaolyuh.listener;

import com.alibaba.fastjson.JSON;
import com.github.xiaolyuh.redis.clinet.RedisClient;
import com.github.xiaolyuh.util.GlobalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

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
        publisher(redisClient, message, GlobalConfig.NAMESPACE);
    }

    /**
     * 发布消息到频道（Channel）
     *
     * @param redisClient redis客户端
     * @param message     消息内容
     * @param nameSpace   命名空间
     */
    public static void publisher(RedisClient redisClient, RedisPubSubMessage message, String nameSpace) {
        String messageJson = JSON.toJSONString(message);
        // pull 拉模式消息
        redisClient.lpush(GlobalConfig.getMessageRedisKey(nameSpace), GlobalConfig.GLOBAL_REDIS_SERIALIZER, messageJson);
        redisClient.expire(GlobalConfig.getMessageRedisKey(nameSpace), 25, TimeUnit.HOURS);
        // pub/sub 推模式消息
        redisClient.publish(RedisMessageListener.CHANNEL, "m");
        if (logger.isDebugEnabled()) {
            logger.debug("redis消息发布者向频道【{}】发布了【{}】消息", RedisMessageListener.CHANNEL, message.toString());
        }
    }
}
