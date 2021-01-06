package com.github.xiaolyuh.test;

import com.alibaba.fastjson.JSON;
import com.github.xiaolyuh.cache.Cache;
import com.github.xiaolyuh.cache.LayeringCache;
import com.github.xiaolyuh.config.CacheConfig;
import com.github.xiaolyuh.domain.User;
import com.github.xiaolyuh.manager.CacheManager;
import com.github.xiaolyuh.manager.LayeringCacheManager;
import com.github.xiaolyuh.redis.clinet.RedisClient;
import com.github.xiaolyuh.redis.serializer.FastJsonRedisSerializer;
import com.github.xiaolyuh.redis.serializer.JacksonRedisSerializer;
import com.github.xiaolyuh.redis.serializer.JdkRedisSerializer;
import com.github.xiaolyuh.redis.serializer.KryoRedisSerializer;
import com.github.xiaolyuh.redis.serializer.ProtostuffRedisSerializer;
import com.github.xiaolyuh.support.CacheMode;
import com.github.xiaolyuh.util.GlobalConfig;
import com.sun.management.OperatingSystemMXBean;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


// SpringJUnit4ClassRunner再Junit环境下提供Spring TestContext Framework的功能。
@RunWith(SpringJUnit4ClassRunner.class)
// @ContextConfiguration用来加载配置ApplicationContext，其中classes用来加载配置类
@ContextConfiguration(classes = {CacheConfig.class})
public class CacheAspectTest {
    private Logger logger = LoggerFactory.getLogger(CacheAspectTest.class);

    @Autowired
    private TestService testService;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private CacheManager cacheManager;

    @Test
    public void testGetUserNameLongParam() {
        long userId = 111;

        User user = testService.getUserById(userId);
        user = testService.getUserById(userId);
        sleep(5);
        user = testService.getUserById(userId);
        sleep(4);
        user = testService.getUserById(userId);
        sleep(10);

        Object result = redisClient.get("user:info:113:113", User.class);
        Assert.assertNull(result);

        user = testService.getUserById(userId);
        Assert.assertNotNull(user);
    }

    @Test
    public void testGetUserNameArrayAndLongParam() {
        String[] lastName = {"w", "y", "h"};
        long userId = 122;

        User user = testService.getUserNoKey(userId, lastName);
        user = testService.getUserNoKey(userId, lastName);
        sleep(5);
        user = testService.getUserNoKey(userId, lastName);
        sleep(4);
        user = testService.getUserNoKey(userId, lastName);
        sleep(10);
        Object result = redisClient.get("user:info:113:113", User.class);
        Assert.assertNull(result);

        user = testService.getUserNoKey(userId, lastName);
        Assert.assertNotNull(user);
    }

    @Test
    public void testGetUserNameObjectParam() {
        User user = new User();
        user.setUserId(113);
        user.setAge(31);
        user.setLastName(new String[]{"w", "y", "h"});

        user = testService.getUserObjectPram(user);
        user = testService.getUserObjectPram(user);
        sleep(5);
        user = testService.getUserObjectPram(user);
        sleep(4);
        user = testService.getUserObjectPram(user);
        sleep(11);
        Object result = redisClient.get("user:info:113:113", User.class);
        Assert.assertNull(result);

        user = testService.getUserObjectPram(user);
        Assert.assertNotNull(user);
    }

    @Test
    public void testGetUserNameObjectAndIntegerParam() {
        User user = new User();
        user.setUserId(114);
        user.setAge(31);
        user.setLastName(new String[]{"w", "y", "h"});

        testService.getUser(user, user.getAge());
        user = testService.getUser(user, user.getAge());
        Assert.assertNotNull(user);
        sleep(5);
        user = testService.getUser(user, user.getAge());
        sleep(4);
        user = testService.getUser(user, user.getAge());
        sleep(11);
        Object result = redisClient.get("user:info:114:114", User.class);
        Assert.assertNull(result);

        user = testService.getUser(user, user.getAge());
        Assert.assertNotNull(user);
    }

    @Test
    public void testGetNullUser() {
        long userId = 115;

        testService.getNullUser(userId);
        User user = testService.getNullUser(userId);
        Assert.assertNull(user);

        sleep(5);
        user = testService.getNullUser(userId);
        sleep(4);
        user = testService.getNullUser(userId);
        sleep(11);
        Object result = redisClient.get("user:info:115:115", User.class);
        Assert.assertNull(result);

        user = testService.getNullUser(userId);
        Assert.assertNull(user);
    }

    @Test
    public void testGetUserNoParam() {
        User user1 = testService.getUserNoParam();
        Assert.assertNotNull(user1);
        User user = testService.getUserNoParam();
        Assert.assertNotNull(user);
        Assert.assertEquals(JSON.toJSONString(user1), JSON.toJSONString(user));

        sleep(5);
        testService.getUserNoParam();
        sleep(4);
        testService.getUserNoParam();
        sleep(11);
        Object result = redisClient.get("user:info:{params:[]}", User.class);
        Assert.assertNull(result);

        user = testService.getUserNoParam();
        Assert.assertNotNull(user);
    }

    @Test
    public void testGetString() {
        String string = testService.getString(211);
        Assert.assertNotNull(string);
        string = testService.getString(211);
        Assert.assertNotNull(string);
        sleep(5);
        string = testService.getString(211);
        Assert.assertNotNull(string);
    }

    @Test
    public void testGetInt() {
        Integer anInt = testService.getInt(212);
        Assert.assertNotNull(anInt);
        anInt = testService.getInt(212);
        Assert.assertNotNull(anInt);
        sleep(5);
        anInt = testService.getInt(212);
        Assert.assertNotNull(anInt);
    }

    @Test
    public void testGetLong() {
        Long aLong = testService.getLong(213);
        Assert.assertNotNull(aLong);
        aLong = testService.getLong(213);
        Assert.assertNotNull(aLong);
        sleep(5);
        testService.getLong(213);
    }

    @Test
    public void testGetDouble() {
        double aDouble = testService.getDouble(223);
        Assert.assertNotNull(aDouble);
        aDouble = testService.getDouble(223);
        Assert.assertNotNull(aDouble);
        sleep(5);
        testService.getDouble(223);
    }

    @Test
    public void testGetFloat() {
        float aFloat = testService.getFloat(224);
        Assert.assertNotNull(aFloat);
        aFloat = testService.getFloat(224);
        Assert.assertNotNull(aFloat);
        sleep(5);
        testService.getFloat(224);
    }

    @Test
    public void testGetBigDecimal() {
        BigDecimal bigDecimal = testService.getBigDecimal(225);
        Assert.assertNotNull(bigDecimal);
        bigDecimal = testService.getBigDecimal(225);
        Assert.assertNotNull(bigDecimal);
        sleep(5);
        testService.getBigDecimal(225);
    }

    @Test
    public void testGetEnum() {
        CacheMode cacheMode = testService.getEnum(214);
        Assert.assertNotNull(cacheMode);
        cacheMode = testService.getEnum(214);
        Assert.assertEquals(cacheMode, CacheMode.ONLY_FIRST);
        sleep(5);
        cacheMode = testService.getEnum(214);
        Assert.assertEquals(cacheMode, CacheMode.ONLY_FIRST);
    }

    @Test
    public void testGetDate() {
        Date date = testService.getDate(244);
        Assert.assertNotNull(date);
        date = testService.getDate(244);
        Assert.assertTrue(date.getTime() <= System.currentTimeMillis());
        sleep(5);
        date = testService.getDate(244);
        Assert.assertTrue(date.getTime() <= System.currentTimeMillis());
    }


    @Test
    public void testGetArray() {
        long[] array = testService.getArray(215);
        Assert.assertNotNull(array);
        array = testService.getArray(215);
        Assert.assertEquals(array.length, 3);
        sleep(5);
        array = testService.getArray(215);
        Assert.assertEquals(array.length, 3);
    }

    @Test
    public void testGetObjectArray() {
        User[] array = testService.getObjectArray(216);
        Assert.assertNotNull(array);
        array = testService.getObjectArray(216);
        Assert.assertEquals(array.length, 3);
        sleep(5);
        array = testService.getObjectArray(216);
        Assert.assertEquals(array.length, 3);
    }

    @Test
    public void testGetList() {
        List<String> list = testService.getList(217);
        Assert.assertNotNull(list);
        list = testService.getList(217);
        Assert.assertEquals(list.size(), 3);
        sleep(5);
        list = testService.getList(217);
        Assert.assertEquals(list.size(), 3);

    }

    @Test
    public void testGetLinkList() {
        LinkedList<String> list = testService.getLinkedList(235);
        Assert.assertNotNull(list);
        list = testService.getLinkedList(235);
        Assert.assertEquals(list.size(), 3);
        sleep(5);
        list = testService.getLinkedList(235);
        Assert.assertEquals(list.size(), 3);

    }

    @Test
    public void testGetListObject() {
        List<User> list = testService.getListObject(236);
        Assert.assertNotNull(list);
        list = testService.getListObject(236);
        Assert.assertEquals(list.size(), 3);
        sleep(5);
        list = testService.getListObject(236);
        Assert.assertEquals(list.size(), 3);

    }

    @Test
    public void testGetSet() {
        Set<String> set = testService.getSet(237);
        Assert.assertNotNull(set);
        set = testService.getSet(237);
        Assert.assertEquals(set.size(), 3);
        sleep(5);
        set = testService.getSet(237);
        Assert.assertEquals(set.size(), 3);

    }

    @Test
    public void testGetSetObject() {
        Set<User> set = testService.getSetObject(238);
        Assert.assertNotNull(set);
        set = testService.getSetObject(238);
        Assert.assertEquals(set.size(), 1);
        sleep(5);
        set = testService.getSetObject(238);
        Assert.assertEquals(set.size(), 1);

    }

    @Test
    public void testGetException() {
        List<User> list = null;
        try {
            list = testService.getException(219);
        } catch (Exception e) {
            Assert.assertNotNull(e);
            return;
        }
        Assert.assertTrue(false);

    }

    @Test
    public void testGetNullPram() {
        User user = testService.getNullUser(null);
        user = testService.getNullUser(null);
        sleep(5);
        user = testService.getNullUser(null);

        Assert.assertNull(user);
    }

    @Test
    public void testGetNullUserAllowNullValueTrueMagnification() {
        User user = testService.getNullUserAllowNullValueTrueMagnification(1181L);
        user = testService.getNullUserAllowNullValueTrueMagnification(1181L);
        sleep(5);
        user = testService.getNullUserAllowNullValueTrueMagnification(1181L);

        Assert.assertNull(user);
    }

    @Test
    public void testGetNullUserAllowNullValueFalse() {
        User user = testService.getNullUserAllowNullValueFalse(1182L);
        user = testService.getNullUserAllowNullValueFalse(1182L);
        sleep(5);
        user = testService.getNullUserAllowNullValueFalse(1182L);

        Assert.assertNull(user);
    }


    @Test
    public void testGetNullObjectPram() {
        try {
            User user = testService.getNullObjectPram(null);
        } catch (Exception e) {
            Assert.assertNotNull(e);
            return;
        }
        Assert.assertTrue(false);
    }

    @Test
    public void testGetNullObjectPramIgnoreException() {
        User user = testService.getNullObjectPramIgnoreException(null);
        Assert.assertNull(user);
    }

    @Test
    public void testPutUser() {
        long userId = 116;
        testService.putUser(userId);
        User user = testService.getUserById(userId);
        logger.debug(JSON.toJSONString(user));
        Assert.assertNotNull(user);
        Assert.assertEquals(user.getUserId(), 11L);
    }

    @Test
    public void testPutUserNoParam() {
        User user = testService.putUserNoParam();
        logger.debug(JSON.toJSONString(user));
        Assert.assertNotNull(user);
        Assert.assertEquals(user.getUserId(), 11L);
    }

    @Test
    public void testPutNullUser() {
        long userId = 118_1117_10_1;
        testService.putNullUser1118(userId);
        sleep(1);
        User user = testService.getUserById118(userId);
        logger.debug(JSON.toJSONString(user));
        Assert.assertNull(user);
    }


    @Test
    public void testPutNullUserAllowNullValueTrueMagnification() {
        long userId = 118_1117_1;
        testService.putNullUserAllowNullValueTrueMagnification(userId);
        User user = testService.getUserById(userId);
        logger.debug(JSON.toJSONString(user));
        Assert.assertNull(user);
        sleep(1);
        user = testService.getUserById(userId);
        Assert.assertNull(user);
        sleep(4);
        user = testService.getUserById(userId);
        Assert.assertNotNull(user);
    }

    @Test
    public void testPutNullUserAllowNullValueFalse() {
        long userId = 118_1117_6;
        testService.putNullUserAllowNullValueFalse(userId);
        User user = testService.getUserById(userId);
        logger.debug(JSON.toJSONString(user));
        Assert.assertNotNull(user);
    }

    @Test
    public void testEvictUser() {
        long userId = 118;
        User user = testService.putUser(userId);
        sleep(3);
        testService.evictUser(userId);
        sleep(3);
        Object result = redisClient.get("user:info:118:118", User.class);
        Assert.assertNull(result);
    }

    @Test
    public void testEvictUserNoKey() {
        long userId = 300_118;
        User user = new User();
        user.setUserId(userId);
        user.setBirthday(new Date(1593530584170L));
        testService.putUserNoKey(userId, user.getLastName(), user);
        String key = "user:info:params:[300118,[w,四川,~！@#%……&*（）——+：“？》:''\\>?《~!@#$%^&*()_+\\\\],address:addredd:成都,age:122,birthday:1593530584170,height:18.2,income:22.22,lastName:[w,四川,~！@#%……&*（）——+：“？》:''\\>?《~!@#$%^&*()_+\\\\],lastNameList:[W,成都],lastNameSet:[成都,W],name:name,userId:300118]";
        User result = redisClient.get(key, User.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getUserId(), user.getUserId());
        sleep(3);
        testService.evictUserNoKey(userId, user.getLastName(), user);
        sleep(3);
        Object result2 = redisClient.get(key, User.class);
        Assert.assertNull(result2);
    }

    @Test
    public void testEvictAllUser() {
        testService.putUserById(119);
        testService.putUserById(120);
        testService.putUserById(121);
        sleep(5);
        testService.evictAllUser();
        sleep(3);
        Object result1 = redisClient.get("user:info:119", User.class);
        Object result2 = redisClient.get("user:info:121", User.class);
        Assert.assertNull(result1);
        Assert.assertNull(result2);
    }

    @Test
    public void testEvictAllUserNoCacheMannger() {
        testService.putUserById(119_119);
        testService.putUserById(119_120);
        testService.putUserById(119_121);
        sleep(2);
        ((LayeringCacheManager) cacheManager).getCacheContainer().clear();
        Assert.assertTrue(((LayeringCacheManager) cacheManager).getCacheContainer().size() == 0);
        testService.evictUser(119_119);
        sleep(2);
        Object result1 = redisClient.get("user:info:119119", User.class);
        Object result2 = redisClient.get("user:info:119121", User.class);
        Assert.assertNull(result1);
        Assert.assertNotNull(result2);

        ((LayeringCacheManager) cacheManager).getCacheContainer().clear();
        Assert.assertTrue(((LayeringCacheManager) cacheManager).getCacheContainer().size() == 0);
        testService.evictAllUser();
        sleep(2);
        result2 = redisClient.get("user:info:119121", User.class);
        Object result3 = redisClient.get("user:info:119122", User.class);
        Assert.assertNull(result2);
        Assert.assertNull(result3);
    }

    @Test
    public void getUserById118DisableFirstCache() {
        testService.getUserById118DisableFirstCache(118_118);
        sleep(2);
        Collection<Cache> caches = cacheManager.getCache("user:info:118:3-0-2");
        String key = "118118";
        for (Cache cache : caches) {
            User result = cache.get(key, User.class);
            Assert.assertNotNull(result);

            result = ((LayeringCache) cache).getFirstCache().get(key, User.class);
            Assert.assertNull(result);

            result = ((LayeringCache) cache).getSecondCache().get(key, User.class);
            Assert.assertNotNull(result);
        }
    }

    @Test
    public void putUserByIdDisableFirstCache() {
        testService.putUserByIdDisableFirstCache(118_118);
        sleep(2);
        Collection<Cache> caches = cacheManager.getCache("user:info:3-0-2");
        String key = "118118";
        for (Cache cache : caches) {
            User result = cache.get(key, User.class);
            Assert.assertNotNull(result);

            result = ((LayeringCache) cache).getFirstCache().get(key, User.class);
            Assert.assertNull(result);

            result = ((LayeringCache) cache).getSecondCache().get(key, User.class);
            Assert.assertNotNull(result);
        }
    }

    /**
     * 测试刷新二级缓存，同步更新一级缓存
     */
    @Test
    public void testRefreshSecondCacheSyncFistCache() {
        User user = new User();
        user.setUserId(316_1);
        user.setAge(31);
        Long llen1 = redisClient.llen(GlobalConfig.getMessageRedisKey());
        // 初始化缓存
        User user1 = testService.refreshSecondCacheSyncFistCache(user);
        sleep(4);
        // 刷新二级缓存，数据没有变化
        User user2 = testService.refreshSecondCacheSyncFistCache(user);
        Assert.assertEquals(user1.getAge(), user2.getAge());
        sleep(1);
        Long llen2 = redisClient.llen(GlobalConfig.getMessageRedisKey());
        Assert.assertEquals(llen1, llen2);

        logger.info("============================================================================");

        sleep(4);
        user.setAge(33);
        // 刷新二级缓存，数据发生变化，同步刷新一级缓存
        user1 = testService.refreshSecondCacheSyncFistCache(user);
        sleep(1);
        user2 = testService.refreshSecondCacheSyncFistCache(user);
        Assert.assertEquals(user1.getAge(), 31);
        Assert.assertEquals(user2.getAge(), 33);
        Long llen3 = redisClient.llen(GlobalConfig.getMessageRedisKey());
        Assert.assertEquals((long) llen1, llen3 - 1);
    }

    /**
     * 测试刷新二级缓存，同步更新一级缓存
     */
    @Test
    public void testRefreshSecondCacheSyncFistCacheOldNull() {
        User user = new User();
        user.setUserId(316_2);
        Long llen1 = redisClient.llen(GlobalConfig.getMessageRedisKey());
        // 初始化缓存
        User user1 = testService.refreshSecondCacheSyncFistCacheNull(null);
        sleep(4);
        // 刷新二级缓存，数据没有变化
        User user2 = testService.refreshSecondCacheSyncFistCacheNull(null);
        Assert.assertNull(user1);
        Assert.assertNull(user2);
        sleep(1);
        Long llen2 = redisClient.llen(GlobalConfig.getMessageRedisKey());
        Assert.assertEquals(llen1, llen2);


        logger.info("============================================================================");

        sleep(4);
        // 刷新二级缓存，数据发生变化，同步刷新一级缓存
        user1 = testService.refreshSecondCacheSyncFistCacheNull(user);
        sleep(1);
        user2 = testService.refreshSecondCacheSyncFistCacheNull(user);
        Assert.assertEquals(user2.getAge(), user.getAge());
        Long llen3 = redisClient.llen(GlobalConfig.getMessageRedisKey());
        Assert.assertEquals((long) llen1, llen3 - 1);
    }

    /**
     * 测试刷新二级缓存，同步更新一级缓存
     */
    @Test
    public void testSerializer() {
        User user = new User();
        KryoRedisSerializer kryoRedisSerializer = new KryoRedisSerializer();
        FastJsonRedisSerializer fastJsonRedisSerializer = new FastJsonRedisSerializer();
        JacksonRedisSerializer jacksonRedisSerializer = new JacksonRedisSerializer();
        JdkRedisSerializer jdkRedisSerializer = new JdkRedisSerializer();
        ProtostuffRedisSerializer protostuffRedisSerializer = new ProtostuffRedisSerializer();


        int count = 1_000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.set("Serializer:KryoRedisSerializer", user, 10, TimeUnit.MINUTES, kryoRedisSerializer);
        }
        long kryoSet = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.set("Serializer:fastJsonRedisSerializer", user, 10, TimeUnit.MINUTES, fastJsonRedisSerializer);
        }
        long fastJsonSet = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.set("Serializer:jacksonRedisSerializer", user, 10, TimeUnit.MINUTES, jacksonRedisSerializer);
        }
        long jacksonSet = System.currentTimeMillis() - start;
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.set("Serializer:jdkRedisSerializer", user, 10, TimeUnit.MINUTES, jdkRedisSerializer);
        }
        long jdkSet = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.set("Serializer:protostuffRedisSerializer", user, 10, TimeUnit.MINUTES, protostuffRedisSerializer);
        }
        long protostufSet = System.currentTimeMillis() - start;


        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.get("Serializer:KryoRedisSerializer", User.class, kryoRedisSerializer);
        }
        long kryoGet = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.get("Serializer:fastJsonRedisSerializer", User.class, fastJsonRedisSerializer);
        }
        long fastJsonGet = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.get("Serializer:jacksonRedisSerializer", User.class, jacksonRedisSerializer);
        }
        long jacksonGet = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.get("Serializer:jdkRedisSerializer", User.class, jdkRedisSerializer);
        }
        long jdkGet = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            redisClient.get("Serializer:protostuffRedisSerializer", User.class, protostuffRedisSerializer);
        }
        long protostufGet = System.currentTimeMillis() - start;


        System.out.println("KryoRedisSerializer:" + kryoRedisSerializer.serialize(user).length + " b");
        System.out.println("fastJsonRedisSerializer:" + fastJsonRedisSerializer.serialize(user).length + " b");
        System.out.println("jacksonRedisSerializer:" + jacksonRedisSerializer.serialize(user).length + " b");
        System.out.println("jdkRedisSerializer:" + jdkRedisSerializer.serialize(user).length + " b");
        System.out.println("protostuffRedisSerializer:" + protostuffRedisSerializer.serialize(user).length + " b");
        System.out.println();

        System.out.println("KryoRedisSerializer serialize:" + kryoSet + " ms");
        System.out.println("fastJsonRedisSerializer serialize:" + fastJsonSet + " ms");
        System.out.println("jacksonRedisSerializer serialize:" + jacksonSet + " ms");
        System.out.println("jdkRedisSerializer serialize:" + jdkSet + " ms");
        System.out.println("protostuffRedisSerializer serialize:" + protostufSet + " ms");
        System.out.println();

        System.out.println("KryoRedisSerializer deserialize:" + kryoGet + " ms");
        System.out.println("fastJsonRedisSerializer deserialize:" + fastJsonGet + " ms");
        System.out.println("jacksonRedisSerializer deserialize:" + jacksonGet + " ms");
        System.out.println("jdkRedisSerializer deserialize:" + jdkGet + " ms");
        System.out.println("protostuffRedisSerializer deserialize:" + protostufGet + " ms");
    }

    private String systemInfo() {
        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        //获取CPU
        double cpuLoad = osmxb.getSystemLoadAverage();
        int percentCpuLoad = (int) (cpuLoad * 100);
        //获取内存
        double totalvirtualMemory = osmxb.getSystemCpuLoad();
        double freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize();
        double value = freePhysicalMemorySize / totalvirtualMemory;
        int percentMemoryLoad = (int) ((1 - value) * 100);

        return String.format("CPU = %s,Mem = %s", percentCpuLoad, percentMemoryLoad);
    }

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("java.util.logging.config.file", ClassLoader.getSystemResource("log4j.properties").getPath());
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}

