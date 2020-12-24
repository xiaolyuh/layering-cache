package com.github.xiaolyuh.util;

import com.github.xiaolyuh.redis.serializer.ProtostuffRedisSerializer;
import com.github.xiaolyuh.redis.serializer.RedisSerializer;

/**
 * 全局配置
 *
 * @author yuhao.wang3
 */
public class GlobalConfig {
    public static final String MESSAGE_KEY = "layering-cache:message-key:%s";

    public static String NAMESPACE = "";

    public static void setNamespace(String namespace) {
        GlobalConfig.NAMESPACE = namespace;
    }

    public static String getMessageRedisKey() {
        return String.format(MESSAGE_KEY, GlobalConfig.NAMESPACE);
    }

    public static String getMessageRedisKey(String nameSpace) {
        return String.format(MESSAGE_KEY, nameSpace);
    }

    /**
     * 缓存统计和消息推送序列化器
     */
    public static final RedisSerializer GLOBAL_REDIS_SERIALIZER = new ProtostuffRedisSerializer();
}
