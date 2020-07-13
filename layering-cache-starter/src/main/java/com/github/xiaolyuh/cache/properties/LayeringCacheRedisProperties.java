package com.github.xiaolyuh.cache.properties;


import com.github.xiaolyuh.util.StringUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "layering-cache.redis")
public class LayeringCacheRedisProperties {
    Integer database = 0;
    String cluster = "";
    String host = "localhost";
    Integer port = 6379;
    String password = null;

    /**
     * 最大连接数
     */
    Integer maxTotal = 20;
    /**
     * 最大空闲连接数
     */
    Integer maxIdle = 20;

    /**
     * 最小连接数
     */
    Integer minIdle = 15;

    /**
     * 获取连接时的最大等待毫秒数,小于零:阻塞不确定的时间
     */
    Long maxWaitMillis = 1000L;
    /**
     * 在获取连接的时候检查有效性
     */
    Boolean testOnBorrow = false;

    public String getPassword() {
        return StringUtils.isBlank(password) ? null : password;
    }
}