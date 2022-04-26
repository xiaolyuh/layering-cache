package com.github.xiaolyuh.cache.properties;


import com.github.xiaolyuh.util.StringUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis 配置
 *
 * @author wangyuhao
 */
@Data
@ConfigurationProperties(prefix = "layering-cache.redis")
public class LayeringCacheRedisProperties {
    //********************单机配置项**************************/
    /**
     * 单机配置项
     */
    String host = "localhost";
    Integer port = 6379;

    //********************集群配置**************************/
    /**
     * 不为空表示集群版，示例
     * localhost:7379,localhost2:7379
     */
    String cluster = "";

    //********************哨兵配置**************************/
    /**
     * 哨兵master名称,示例：mymaster
     */
    String sentinelMaster = "mymaster";

    /**
     * 哨兵节点，示例：localhost:26397,localhost2:26397
     */
    String sentinelNodes = "";

    //********************通用配置**************************/
    /**
     * 使用数据库
     */
    Integer database = 0;

    /**
     * 密码
     */
    String password = null;

    /**
     * 超时时间 单位秒 默认一个小时
     */
    Integer timeout = 3600;
    /**
     * 序列化方式:
     * com.github.xiaolyuh.redis.serializer.KryoRedisSerializer
     * com.github.xiaolyuh.redis.serializer.FastJsonRedisSerializer
     * com.github.xiaolyuh.redis.serializer.JacksonRedisSerializer
     * com.github.xiaolyuh.redis.serializer.JdkRedisSerializer
     * com.github.xiaolyuh.redis.serializer.ProtostuffRedisSerializer
     */
    String serializer = "com.github.xiaolyuh.redis.serializer.ProtostuffRedisSerializer";

    public String getPassword() {
        return StringUtils.isBlank(password) ? null : password;
    }
}