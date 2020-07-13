package com.github.xiaolyuh.web.controller;

import com.github.xiaolyuh.redis.clinet.ClusterRedisClient;
import com.github.xiaolyuh.redis.clinet.RedisClient;
import com.github.xiaolyuh.redis.clinet.RedisProperties;
import com.github.xiaolyuh.redis.clinet.SingleRedisClient;
import com.github.xiaolyuh.redis.serializer.KryoRedisSerializer;
import com.github.xiaolyuh.redis.serializer.StringRedisSerializer;
import com.github.xiaolyuh.util.StringUtils;
import com.github.xiaolyuh.web.utils.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class RedisController {
    public static final Map<String, RedisClient> redisClientMap = new ConcurrentHashMap<>();

    @RequestMapping("/redis/redis-config")
    @ResponseBody
    public Result login(String address, String password, Integer port, Integer database) {
        try {
            RedisProperties redisProperties = new RedisProperties();
            if (address.contains(":")) {
                redisProperties.setCluster(address);
            } else {
                redisProperties.setHost(address);
            }
            redisProperties.setPassword(StringUtils.isBlank(password) ? null : password);
            redisProperties.setPort(port);
            redisProperties.setDatabase(database);
            redisProperties.setMaxIdle(2);
            redisProperties.setMinIdle(2);
            redisProperties.setMaxTotal(2);

            String key = address + ":" + port + ":" + database;
            redisClientMap.put(key, getRedisClient(redisProperties));

            RedisClient redisClient = redisClientMap.get(key);
            redisClient.get("test");
            return Result.success();
        } catch (Exception e) {
            return Result.error("配置redis失败" + e.getMessage());
        }
    }

    @RequestMapping("/redis/redis-list")
    @ResponseBody
    public Result redisList() {
        List<Map<String, String>> list = new ArrayList<>();
        try {
            for (String key : redisClientMap.keySet()) {
                Map<String, String> map = new HashMap<>();
                map.put("address", key);
                list.add(map);
            }
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("配置redis失败" + e.getMessage());
        }
    }


    private RedisClient getRedisClient(RedisProperties redisProperties) {

        KryoRedisSerializer<Object> kryoRedisSerializer = new KryoRedisSerializer<>(Object.class);
        StringRedisSerializer keyRedisSerializer = new StringRedisSerializer();

        RedisClient redisClient = new SingleRedisClient(redisProperties);
        if (StringUtils.isNotBlank(redisProperties.getCluster())) {
            redisClient = new ClusterRedisClient(redisProperties);
        }

        redisClient.setKeySerializer(keyRedisSerializer);
        redisClient.setValueSerializer(kryoRedisSerializer);
        return redisClient;
    }
}