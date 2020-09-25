package com.github.xiaolyuh.redis.clinet;

import lombok.Data;

@Data
public class RedisProperties {
    Integer database = 0;
    /**
     * 不为空表示集群版，示例
     * localhost:7379,localhost2:7379
     */
    String cluster = "";
    String host = "localhost";
    Integer port = 6379;
    String password = null;
}