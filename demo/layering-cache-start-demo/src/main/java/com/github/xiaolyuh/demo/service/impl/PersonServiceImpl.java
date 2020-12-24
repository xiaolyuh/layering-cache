package com.github.xiaolyuh.demo.service.impl;

import com.github.xiaolyuh.annotation.*;
import com.github.xiaolyuh.demo.entity.Person;
import com.github.xiaolyuh.demo.entity.User;
import com.github.xiaolyuh.demo.service.PersonService;
import com.github.xiaolyuh.redis.clinet.RedisClient;
import com.github.xiaolyuh.redis.serializer.*;
import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

@Service
public class PersonServiceImpl implements PersonService {
    Logger logger = LoggerFactory.getLogger(PersonServiceImpl.class);

    @Autowired
    private RedisClient redisClient;

    @Override
    @CachePut(value = "cache-prefix:people", key = "#person.id", depict = "用户信息缓存")
    public Person save(Person person) {
        logger.info("为id、key为:" + person.getId() + "数据做了缓存");
        return null;
    }

    @Override
    @CacheEvict(value = "cache-prefix:people", key = "#id")//2
    public void remove(Long id) {
        logger.info("删除了id、key为" + id + "的数据缓存");
        //这里不做实际删除操作
    }

    @Override
    @CacheEvict(value = "cache-prefix:people", allEntries = true)//2
    public void removeAll() {
        logger.info("删除了所有缓存的数据缓存");
        //这里不做实际删除操作
    }

    @Override
    @Cacheable(value = "cache-prefix:people", key = "#person.id", depict = "用户信息缓存",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 15, preloadTime = 8, forceRefresh = true, timeUnit = TimeUnit.MINUTES))
    public Person findOne(Person person) {
        Person p = new Person(2L, "name2", 12, "address2");
        logger.info("为id、key为:" + p.getId() + "数据做了缓存");
        try {
            Thread.sleep(2050);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return p;
    }


    public void testSerializer() {
        User user = new User();
        KryoRedisSerializer kryoRedisSerializer = new KryoRedisSerializer();
        FastJsonRedisSerializer fastJsonRedisSerializer = new FastJsonRedisSerializer();
        JacksonRedisSerializer jacksonRedisSerializer = new JacksonRedisSerializer();
        JdkRedisSerializer jdkRedisSerializer = new JdkRedisSerializer();
        ProtostuffRedisSerializer protostuffRedisSerializer = new ProtostuffRedisSerializer();


        int count = 100_000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.set("Serializer:KryoRedisSerializer", user, 10, TimeUnit.MINUTES, kryoRedisSerializer);
        }
        long kryoSet = System.currentTimeMillis() - start;
        String kryoSetSInfo = systemInfo();

        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.set("Serializer:fastJsonRedisSerializer", user, 10, TimeUnit.MINUTES, fastJsonRedisSerializer);
        }
        long fastJsonSet = System.currentTimeMillis() - start;
        String fastJsonSetSInfo = systemInfo();

        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.set("Serializer:jacksonRedisSerializer", user, 10, TimeUnit.MINUTES, jacksonRedisSerializer);
        }
        long jacksonSet = System.currentTimeMillis() - start;
        String jacksonSetSInfo = systemInfo();

        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.set("Serializer:jdkRedisSerializer", user, 10, TimeUnit.MINUTES, jdkRedisSerializer);
        }
        long jdkSet = System.currentTimeMillis() - start;
        String jdkSetInfo = systemInfo();

        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.set("Serializer:protostuffRedisSerializer", user, 10, TimeUnit.MINUTES, protostuffRedisSerializer);
        }
        long protostufSet = System.currentTimeMillis() - start;
        String protostufSetInfo = systemInfo();

        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.get("Serializer:KryoRedisSerializer", User.class, kryoRedisSerializer);
        }
        long kryoGet = System.currentTimeMillis() - start;
        String kryoGetInfo = systemInfo();

        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.get("Serializer:fastJsonRedisSerializer", User.class, fastJsonRedisSerializer);
        }
        long fastJsonGet = System.currentTimeMillis() - start;
        String fastJsonGetInfo = systemInfo();

        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.get("Serializer:jacksonRedisSerializer", User.class, jacksonRedisSerializer);
        }
        long jacksonGet = System.currentTimeMillis() - start;
        String jacksonGetInfo = systemInfo();

        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.get("Serializer:jdkRedisSerializer", User.class, jdkRedisSerializer);
        }
        long jdkGet = System.currentTimeMillis() - start;
        String jdkGetInfo = systemInfo();

        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.get("Serializer:protostuffRedisSerializer", User.class, protostuffRedisSerializer);
        }
        long protostufGet = System.currentTimeMillis() - start;
        String protostufGetInfo = systemInfo();


        System.out.println("KryoRedisSerializer:" + kryoRedisSerializer.serialize(user).length + " b");
        System.out.println("fastJsonRedisSerializer:" + fastJsonRedisSerializer.serialize(user).length + " b");
        System.out.println("jacksonRedisSerializer:" + jacksonRedisSerializer.serialize(user).length + " b");
        System.out.println("jdkRedisSerializer:" + jdkRedisSerializer.serialize(user).length + " b");
        System.out.println("protostuffRedisSerializer:" + protostuffRedisSerializer.serialize(user).length + " b");
        System.out.println();

        System.out.println("KryoRedisSerializer serialize:" + kryoSet + " ms  " + kryoSetSInfo);
        System.out.println("fastJsonRedisSerializer serialize:" + fastJsonSet + " ms  " + fastJsonSetSInfo);
        System.out.println("jacksonRedisSerializer serialize:" + jacksonSet + " ms  " + jacksonSetSInfo);
        System.out.println("jdkRedisSerializer serialize:" + jdkSet + " ms  " + jdkSetInfo);
        System.out.println("protostuffRedisSerializer serialize:" + protostufSet + " ms  " + protostufSetInfo);
        System.out.println();

        System.out.println("KryoRedisSerializer deserialize:" + kryoGet + " ms  " + kryoGetInfo);
        System.out.println("fastJsonRedisSerializer deserialize:" + fastJsonGet + " ms  " + fastJsonGetInfo);
        System.out.println("jacksonRedisSerializer deserialize:" + jacksonGet + " ms  " + jacksonGetInfo);
        System.out.println("jdkRedisSerializer deserialize:" + jdkGet + " ms  " + jdkGetInfo);
        System.out.println("protostuffRedisSerializer deserialize:" + protostufGet + " ms  " + protostufGetInfo);


        System.out.println(systemInfo());
    }

    private String systemInfo() {
        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        //获取CPU
        double cpuLoad = osmxb.getSystemCpuLoad();
        int percentCpuLoad = (int) (cpuLoad * 100);

        //获取内存
        double totalvirtualMemory = osmxb.getTotalPhysicalMemorySize();
        double freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize();

        double value = freePhysicalMemorySize / totalvirtualMemory;
        int percentMemoryLoad = (int) ((1 - value) * 100);

        return String.format("CPU = %s,Mem = %s", percentCpuLoad, percentMemoryLoad);
    }

}
