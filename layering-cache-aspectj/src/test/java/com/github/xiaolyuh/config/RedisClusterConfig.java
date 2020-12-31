package com.github.xiaolyuh.config;

import com.github.xiaolyuh.redis.clinet.ClusterRedisClient;
import com.github.xiaolyuh.redis.clinet.RedisClient;
import com.github.xiaolyuh.redis.clinet.RedisProperties;
import com.github.xiaolyuh.redis.serializer.FastJsonRedisSerializer;
import com.github.xiaolyuh.redis.serializer.JacksonRedisSerializer;
import com.github.xiaolyuh.redis.serializer.JdkRedisSerializer;
import com.github.xiaolyuh.redis.serializer.KryoRedisSerializer;
import com.github.xiaolyuh.redis.serializer.ProtostuffRedisSerializer;
import com.github.xiaolyuh.redis.serializer.StringRedisSerializer;
import com.github.xiaolyuh.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({"classpath:application.properties"})
public class RedisClusterConfig {

    @Value("${spring.redis.cluster:127.0.0.1:7000,127.0.0.1:7001,127.0.0.1:7002,127.0.0.1:7003,127.0.0.1:7004,127.0.0.1:7005}")
    private String cluster;

    @Value("${spring.redis.password:}")
    private String password;

    @Bean
    public RedisClient layeringCacheRedisClient() {
        RedisProperties redisProperties = new RedisProperties();
        redisProperties.setPassword(StringUtils.isBlank(password) ? null : password);
        redisProperties.setCluster(cluster);

        KryoRedisSerializer kryoRedisSerializer = new KryoRedisSerializer();
        FastJsonRedisSerializer fastJsonRedisSerializer = new FastJsonRedisSerializer();
        JacksonRedisSerializer jacksonRedisSerializer = new JacksonRedisSerializer();
        JdkRedisSerializer jdkRedisSerializer = new JdkRedisSerializer();
        ProtostuffRedisSerializer protostuffRedisSerializer = new ProtostuffRedisSerializer();

        StringRedisSerializer keyRedisSerializer = new StringRedisSerializer();
        ClusterRedisClient redisClient = new ClusterRedisClient(redisProperties);
        redisClient.setKeySerializer(keyRedisSerializer);
        redisClient.setValueSerializer(kryoRedisSerializer);
        return redisClient;
    }
}
