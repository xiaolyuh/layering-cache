package com.github.xiaolyuh.cache.test;

import com.alibaba.fastjson.JSON;
import com.github.xiaolyuh.cache.Cache;
import com.github.xiaolyuh.cache.LayeringCache;
import com.github.xiaolyuh.cache.config.CacheConfig;
import com.github.xiaolyuh.cache.redis.RedisCache;
import com.github.xiaolyuh.cache.redis.RedisCacheKey;
import com.github.xiaolyuh.listener.RedisPubSubMessage;
import com.github.xiaolyuh.listener.RedisPubSubMessageType;
import com.github.xiaolyuh.manager.AbstractCacheManager;
import com.github.xiaolyuh.manager.CacheManager;
import com.github.xiaolyuh.redis.clinet.RedisClient;
import com.github.xiaolyuh.setting.FirstCacheSetting;
import com.github.xiaolyuh.setting.LayeringCacheSetting;
import com.github.xiaolyuh.setting.SecondaryCacheSetting;
import com.github.xiaolyuh.stats.CacheStats;
import com.github.xiaolyuh.support.ExpireMode;
import com.github.xiaolyuh.support.LayeringCacheRedisLock;
import com.github.xiaolyuh.util.GlobalConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

// SpringJUnit4ClassRunner再Junit环境下提供Spring TestContext Framework的功能。
@RunWith(SpringJUnit4ClassRunner.class)
// @ContextConfiguration用来加载配置ApplicationContext，其中classes用来加载配置类
@ContextConfiguration(classes = {CacheConfig.class})
public class CacheCoreTest {
    private Logger logger = LoggerFactory.getLogger(CacheCoreTest.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisClient redisClient;

    private LayeringCacheSetting layeringCacheSetting1;
    private LayeringCacheSetting layeringCacheSetting2;
    private LayeringCacheSetting layeringCacheSetting4;
    private LayeringCacheSetting layeringCacheSetting5;
    private LayeringCacheSetting layeringCacheSetting6;
    private LayeringCacheSetting layeringCacheSetting7;

    @Before
    public void testGetCache() {
        // 测试 CacheManager getCache方法
        FirstCacheSetting firstCacheSetting1 = new FirstCacheSetting(10, 1000, 4, TimeUnit.SECONDS, ExpireMode.WRITE);
        SecondaryCacheSetting secondaryCacheSetting1 = new SecondaryCacheSetting(10, 4, TimeUnit.SECONDS, true, true, 1);
        layeringCacheSetting1 = new LayeringCacheSetting(firstCacheSetting1, secondaryCacheSetting1, "", true);

        // 二级缓存可以缓存null,时间倍率是1
        FirstCacheSetting firstCacheSetting2 = new FirstCacheSetting(10, 1000, 5, TimeUnit.SECONDS, ExpireMode.WRITE);
        SecondaryCacheSetting secondaryCacheSetting2 = new SecondaryCacheSetting(3000, 14, TimeUnit.SECONDS, true, true, 1);
        layeringCacheSetting2 = new LayeringCacheSetting(firstCacheSetting2, secondaryCacheSetting2, "", true);

        // 二级缓存可以缓存null,时间倍率是10
        FirstCacheSetting firstCacheSetting4 = new FirstCacheSetting(10, 1000, 5, TimeUnit.SECONDS, ExpireMode.WRITE);
        SecondaryCacheSetting secondaryCacheSetting4 = new SecondaryCacheSetting(100, 70, TimeUnit.SECONDS, true, true, 10);
        layeringCacheSetting4 = new LayeringCacheSetting(firstCacheSetting4, secondaryCacheSetting4, "", true);


        // 二级缓存不可以缓存null
        FirstCacheSetting firstCacheSetting5 = new FirstCacheSetting(10, 1000, 5, TimeUnit.SECONDS, ExpireMode.WRITE);
        SecondaryCacheSetting secondaryCacheSetting5 = new SecondaryCacheSetting(10, 7, TimeUnit.SECONDS, true, false, 1);
        layeringCacheSetting5 = new LayeringCacheSetting(firstCacheSetting5, secondaryCacheSetting5, "", true);

        // pull message
        FirstCacheSetting firstCacheSetting6 = new FirstCacheSetting(10, 1000, 5, TimeUnit.MINUTES, ExpireMode.WRITE);
        SecondaryCacheSetting secondaryCacheSetting6 = new SecondaryCacheSetting(10, 7, TimeUnit.MINUTES, true, false, 1);
        layeringCacheSetting6 = new LayeringCacheSetting(firstCacheSetting6, secondaryCacheSetting6, "", true);

        // 禁用一级缓存
        FirstCacheSetting firstCacheSetting7 = new FirstCacheSetting(10, 1000, 5, TimeUnit.SECONDS, ExpireMode.WRITE);
        SecondaryCacheSetting secondaryCacheSetting7 = new SecondaryCacheSetting(10, 7, TimeUnit.SECONDS, true, false, 1);
        layeringCacheSetting7 = new LayeringCacheSetting(firstCacheSetting7, secondaryCacheSetting7, "", false);

        String cacheName = "cache:name";
        Cache cache1 = cacheManager.getCache(cacheName, layeringCacheSetting1);
        Cache cache2 = cacheManager.getCache(cacheName, layeringCacheSetting1);
        Assert.assertEquals(cache1, cache2);

        Cache cache3 = cacheManager.getCache(cacheName, layeringCacheSetting2);
        Collection<Cache> caches = cacheManager.getCache(cacheName);
        Assert.assertTrue(caches.size() == 2);
        Assert.assertNotEquals(cache1, cache3);
    }

    @Test
    public void testCacheExpiration() {
        // 测试 缓存过期时间
        String cacheName = "cache:name:cluster";
        String cacheKey1 = "cache:key1";
        LayeringCache cache1 = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting1);
        cache1.get(cacheKey1, () -> initCache(String.class));
        // 测试一级缓存值及过期时间
        String str1 = cache1.getFirstCache().get(cacheKey1, String.class);
        String st2 = cache1.getFirstCache().get(cacheKey1, () -> initCache(String.class));
        logger.debug("========================:{}", str1);
        Assert.assertTrue(str1.equals(st2));
        Assert.assertTrue(str1.equals(initCache(String.class)));

        RedisCacheKey redisCacheKey = ((RedisCache) cache1.getSecondCache()).getRedisCacheKey(cacheKey1);
        Long ttl = redisClient.getExpire(redisCacheKey.getKey());
        logger.debug("========================ttl 1:{}", ttl);
        sleep(4);
        Assert.assertNull(cache1.getFirstCache().get(cacheKey1, String.class));
        // 看日志是不是走了二级缓存
        cache1.get(cacheKey1, () -> initCache(String.class));
        ttl = redisClient.getExpire(redisCacheKey.getKey());
        logger.debug("========================ttl 2:{}", ttl);

        // 测试二级缓存
        str1 = cache1.getSecondCache().get(cacheKey1, String.class);
        st2 = cache1.getSecondCache().get(cacheKey1, () -> initCache(String.class));
        Assert.assertTrue(st2.equals(str1));
        Assert.assertTrue(str1.equals(initCache(String.class)));
        sleep(4);
        // 看日志是不是走了自动刷新

        ttl = redisClient.getExpire(redisCacheKey.getKey());
        logger.debug("========================ttl 3:{}", ttl);
        cache1.get(cacheKey1, () -> initCache(String.class));
        sleep(1);
        ttl = redisClient.getExpire(redisCacheKey.getKey());
        logger.debug("========================ttl 4:{}", ttl);

        sleep(6);
        ttl = redisClient.getExpire(redisCacheKey.getKey());
        logger.debug("========================ttl 5:{}", ttl);
        Assert.assertNotNull(cache1.getSecondCache().get(cacheKey1));

        sleep(5);
        ttl = redisClient.getExpire(redisCacheKey.getKey());
        logger.debug("========================ttl 6:{}", ttl);
        Assert.assertNull(cache1.getSecondCache().get(cacheKey1));
        redisClient.delete(redisCacheKey.getKey());
    }

    @Test
    public void testGetCacheNullUserAllowNullValueTrue() {
        logger.info("测试二级缓存允许为NULL，NULL值时间倍率是10");
        // 测试 缓存过期时间
        String cacheName = "cache:name:118_1";
        String cacheKey1 = "cache:key1:118_1";
        LayeringCache cache1 = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting4);
        cache1.get(cacheKey1, () -> initNullCache());
        // 测试一级缓存值不能缓存NULL
        String str1 = cache1.getFirstCache().get(cacheKey1, String.class);
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache1.getFirstCache().getNativeCache();
        Assert.assertTrue(str1 == null);
        Assert.assertTrue(0 == nativeCache.asMap().size());

        // 测试二级缓存可以存NULL值，NULL值时间倍率是10
        String st2 = cache1.getSecondCache().get(cacheKey1, String.class);
        RedisCacheKey redisCacheKey = ((RedisCache) cache1.getSecondCache()).getRedisCacheKey(cacheKey1);
        Long ttl = redisClient.getExpire(redisCacheKey.getKey());
        Assert.assertTrue(redisClient.hasKey(redisCacheKey.getKey()));
        Assert.assertTrue(st2 == null);
        Assert.assertTrue(ttl <= 10);
        sleep(5);
        st2 = cache1.getSecondCache().get(cacheKey1, String.class);
        Assert.assertTrue(st2 == null);
        cache1.getSecondCache().get(cacheKey1, () -> initNullCache());
        sleep(1);
        ttl = redisClient.getExpire(redisCacheKey.getKey());
        Assert.assertTrue(ttl <= 10 && ttl > 5);

        st2 = cache1.get(cacheKey1, String.class);
        Assert.assertTrue(st2 == null);
    }

    @Test
    public void testGetCacheNullUserAllowNullValueFalse() {
        logger.info("测试二级缓存不允许为NULL");
        // 测试 缓存过期时间
        String cacheName = "cache:name:118_2";
        String cacheKey1 = "cache:key1:118_2";
        LayeringCache cache1 = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting5);
        cache1.get(cacheKey1, () -> initNullCache());
        // 测试一级缓存值不能缓存NULL
        String str1 = cache1.getFirstCache().get(cacheKey1, String.class);
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache1.getFirstCache().getNativeCache();
        Assert.assertTrue(str1 == null);
        Assert.assertTrue(0 == nativeCache.asMap().size());

        // 测试二级缓存不可以存NULL值，NULL值时间倍率是10
        String st2 = cache1.getSecondCache().get(cacheKey1, String.class);
        RedisCacheKey redisCacheKey = ((RedisCache) cache1.getSecondCache()).getRedisCacheKey(cacheKey1);
        Assert.assertTrue(!redisClient.hasKey(redisCacheKey.getKey()));
        Assert.assertTrue(st2 == null);
    }

    @Test
    public void testGetType() throws Exception {
        // 测试 缓存过期时间
        String cacheName = "cache:name";
        String cacheKey1 = "cache:key:22";
        LayeringCache cache1 = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting1);
        cache1.get(cacheKey1, () -> null);
        String str1 = cache1.get(cacheKey1, String.class);
        Assert.assertNull(str1);
        sleep(11);
        cache1.get(cacheKey1, () -> initCache(String.class));

        str1 = cache1.get(cacheKey1, String.class);
        Assert.assertEquals(str1, initCache(String.class));
    }

    @Test
    public void testCacheEvict() throws Exception {
        // 测试 缓存过期时间
        String cacheName = "cache:name";
        String cacheKey1 = "cache:key2";
        String cacheKey2 = "cache:key3";
        LayeringCache cache1 = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting1);
        cache1.get(cacheKey1, () -> initCache(String.class));
        cache1.get(cacheKey2, () -> initCache(String.class));
        // 测试删除方法
        cache1.evict(cacheKey1);
        Thread.sleep(500);
        String str1 = cache1.get(cacheKey1, String.class);
        String str2 = cache1.get(cacheKey2, String.class);
        Assert.assertNull(str1);
        Assert.assertNotNull(str2);
        // 测试删除方法
        cache1.evict(cacheKey1);
        Thread.sleep(500);
        str1 = cache1.get(cacheKey1, () -> initCache(String.class));
        str2 = cache1.get(cacheKey2, String.class);
        Assert.assertNotNull(str1);
        Assert.assertNotNull(str2);
    }

    @Test
    public void testCacheClear() throws Exception {
        // 测试 缓存过期时间
        String cacheName = "cache:name";
        String cacheKey1 = "cache:key4";
        String cacheKey2 = "cache:key5";
        LayeringCache cache = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting1);
        cache.get(cacheKey1, () -> initCache(String.class));
        cache.get(cacheKey2, () -> initCache(String.class));
        // 测试清除方法
        cache.clear();
        Thread.sleep(500);
        String str1 = cache.get(cacheKey1, String.class);
        String str2 = cache.get(cacheKey2, String.class);
        Assert.assertNull(str1);
        Assert.assertNull(str2);
        // 测试清除方法
        cache.clear();
        Thread.sleep(500);
        str1 = cache.get(cacheKey1, () -> initCache(String.class));
        str2 = cache.get(cacheKey2, () -> initCache(String.class));
        Assert.assertNotNull(str1);
        Assert.assertNotNull(str2);
    }

    @Test
    public void testCachePut() throws Exception {
        // 测试 缓存过期时间
        String cacheName = "cache:name";
        String cacheKey1 = "cache:key6";
        LayeringCache cache = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting1);
        String str1 = cache.get(cacheKey1, String.class);
        Assert.assertNull(str1);

        cache.put(cacheKey1, "test1");
        str1 = cache.get(cacheKey1, String.class);
        Assert.assertEquals(str1, "test1");

        cache.put(cacheKey1, "test2");
        Thread.sleep(2000);
        Object value = cache.getFirstCache().get(cacheKey1);
        Assert.assertNull(value);
        str1 = cache.get(cacheKey1, String.class);
        Assert.assertEquals(str1, "test2");
    }

    @Test
    public void testPutCacheNullUserAllowNullValueTrue() {
        logger.info("测试Put二级缓存允许为NULL，NULL值时间倍率是10");
        // 测试 缓存过期时间
        String cacheName = "cache:name:118_3";
        String cacheKey1 = "cache:key1:118_3";
        LayeringCache cache1 = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting4);
        cache1.put(cacheKey1, initNullCache());
        // 测试一级缓存值不能缓存NULL
        String str1 = cache1.getFirstCache().get(cacheKey1, String.class);
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache1.getFirstCache().getNativeCache();
        Assert.assertTrue(str1 == null);
        Assert.assertTrue(0 == nativeCache.asMap().size());

        // 测试二级缓存可以存NULL值，NULL值时间倍率是10
        String st2 = cache1.getSecondCache().get(cacheKey1, String.class);
        RedisCacheKey redisCacheKey = ((RedisCache) cache1.getSecondCache()).getRedisCacheKey(cacheKey1);
        Long ttl = redisClient.getExpire(redisCacheKey.getKey());
        Assert.assertTrue(redisClient.hasKey(redisCacheKey.getKey()));
        Assert.assertTrue(st2 == null);
        Assert.assertTrue(ttl <= 10);
        sleep(5);
        st2 = cache1.getSecondCache().get(cacheKey1, String.class);
        Assert.assertTrue(st2 == null);
        cache1.getSecondCache().get(cacheKey1, () -> initNullCache());
        sleep(1);
        ttl = redisClient.getExpire(redisCacheKey.getKey());
        Assert.assertTrue(ttl <= 10 && ttl > 5);

        st2 = cache1.get(cacheKey1, String.class);
        Assert.assertTrue(st2 == null);
    }

    @Test
    public void testCacheNullUserAllowNullValueFalse() {
        logger.info("测试Put二级缓存不允许为NULL");
        // 测试 缓存过期时间
        String cacheName = "cache:name:118_4";
        String cacheKey1 = "cache:key1:118_4";
        LayeringCache cache1 = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting5);
        cache1.put(cacheKey1, initNullCache());
        // 测试一级缓存值不能缓存NULL
        String str1 = cache1.getFirstCache().get(cacheKey1, String.class);
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache1.getFirstCache().getNativeCache();
        Assert.assertTrue(str1 == null);
        Assert.assertTrue(0 == nativeCache.asMap().size());

        // 测试二级缓存不可以存NULL值，NULL值时间倍率是10
        String st2 = cache1.getSecondCache().get(cacheKey1, String.class);
        RedisCacheKey redisCacheKey = ((RedisCache) cache1.getSecondCache()).getRedisCacheKey(cacheKey1);
        Assert.assertTrue(!redisClient.hasKey(redisCacheKey.getKey()));
        Assert.assertTrue(st2 == null);
    }

    @Test
    public void testCachePutIfAbsent() throws Exception {
        // 测试 缓存过期时间
        String cacheName = "cache:name";
        String cacheKey1 = "cache:key7";
        LayeringCache cache = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting1);
        cache.putIfAbsent(cacheKey1, "test1");
        Thread.sleep(2000);
        Object value = cache.getFirstCache().get(cacheKey1);
        Assert.assertNull(value);
        String str1 = cache.get(cacheKey1, String.class);
        Assert.assertEquals(str1, "test1");

        cache.putIfAbsent(cacheKey1, "test2");
        str1 = cache.get(cacheKey1, String.class);
        Assert.assertEquals(str1, "test1");
    }


    /**
     * 测试统计
     */
    @Test
    public void testStats() {
        // 测试 缓存过期时间
        String cacheName = "cache:name:3-1-3:1";
        String cacheKey1 = "cache:key:123";
        LayeringCache cache1 = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting1);
        cache1.get(cacheKey1, () -> initCache(String.class));
        cache1.get(cacheKey1, () -> initCache(String.class));
        sleep(5);
        cache1.get(cacheKey1, () -> initCache(String.class));

        sleep(11);
        cache1.get(cacheKey1, () -> initCache(String.class));

        CacheStats cacheStats = cache1.getCacheStats();
        CacheStats cacheStats2 = cache1.getCacheStats();
        Assert.assertEquals(cacheStats.getCacheRequestCount().longValue(), cacheStats2.getCacheRequestCount().longValue());
        Assert.assertEquals(cacheStats.getCachedMethodRequestCount().longValue(), cacheStats2.getCachedMethodRequestCount().longValue());
        Assert.assertEquals(cacheStats.getCachedMethodRequestTime().longValue(), cacheStats2.getCachedMethodRequestTime().longValue());

        logger.debug("缓请求数：{}", cacheStats.getCacheRequestCount());
        logger.debug("被缓存方法请求数：{}", cacheStats.getCachedMethodRequestCount());
        logger.debug("被缓存方法请求总耗时：{}", cacheStats.getCachedMethodRequestTime());

        Assert.assertEquals(cache1.getFirstCache().estimatedSize(), 1);
        Assert.assertEquals(cacheStats.getCacheRequestCount().longValue(), 4);
        Assert.assertEquals(cacheStats.getCachedMethodRequestCount().longValue(), 2);
        Assert.assertTrue(cacheStats.getCachedMethodRequestTime().longValue() >= 0);
    }


    /**
     * scan测试
     */
    @Test
    public void testScan() {
        Set<String> keys = ((AbstractCacheManager) cacheManager).getRedisClient().scan("*");
        Assert.assertNotNull(keys);
        logger.info("key数量 {}", keys.size());
        Assert.assertTrue(keys.size() > 0);
    }

    /**
     * 通过 拉模式删除缓存
     * pull message 删除指定key
     */
    @Test
    public void testPullDeleteKey() {
        // 测试 缓存过期时间
        String cacheName = "cache:name:3-0-2";
        String cacheKey1 = "cache:key:302-1";
        LayeringCache cache1 = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting6);
        cache1.get(cacheKey1, () -> initCache(String.class));

        String cacheKey2 = "cache:key:302-2";
        LayeringCache cache2 = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting6);
        cache2.get(cacheKey2, () -> initCache(String.class));

        pushDeleteMessage(cacheName, cacheKey1);
        pushDeleteMessage(cacheName, cacheKey2);

        sleep(40);

        // 测试一级缓存值及过期时间
        String str1 = cache1.getFirstCache().get(cacheKey1, String.class);
        String str2 = cache2.getFirstCache().get(cacheKey2, String.class);
        Assert.assertNull(str1);
        Assert.assertNull(str2);
    }

    /**
     * 通过 拉模式清空缓存
     * pull message 情况缓存
     */
    @Test
    public void testPullClearKey() {
        // 测试 缓存过期时间
        String cacheName = "cache:name:3-0-2";
        String cacheKey1 = "cache:key:302-3";
        LayeringCache cache1 = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting6);
        cache1.get(cacheKey1, () -> initCache(String.class));

        String cacheKey2 = "cache:key:302-4";
        LayeringCache cache2 = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting6);
        cache2.get(cacheKey2, () -> initCache(String.class));

        pushDeleteClear(cacheName);

        sleep(40);

        // 测试一级缓存值及过期时间
        String str1 = cache1.getFirstCache().get(cacheKey1, String.class);
        String str2 = cache2.getFirstCache().get(cacheKey2, String.class);
        Assert.assertNull(str1);
        Assert.assertNull(str2);
    }

    /**
     * 测试禁用一级缓存
     */
    @Test
    public void testDisableFistCacheGet() {
        // 测试 缓存过期时间
        String cacheName = "cache:name:3-0-2";
        String cacheKey1 = "cache:key:302-5";
        LayeringCache cache1 = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting7);
        cache1.get(cacheKey1, () -> initCache(String.class));

        // 二级缓存不为空
        Object result = cache1.getSecondCache().get(cacheKey1);
        Assert.assertNotNull(result);

        // 测试禁用一级缓存，一级缓存为空
        String str1 = cache1.getFirstCache().get(cacheKey1, String.class);
        Assert.assertNull(str1);
        Object result1 = cache1.getFirstCache().get(cacheKey1);
        Assert.assertNull(result1);

        // 二级缓存不为空
        result = cache1.getSecondCache().get(cacheKey1);
        Assert.assertNotNull(result);
    }

    /**
     * 测试禁用一级缓存
     */
    @Test
    public void testDisableFistCachePut() {
        // 测试 缓存过期时间
        String cacheName = "cache:name:3-0-2";
        String cacheKey1 = "cache:key:302-6";
        LayeringCache cache1 = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting7);
        cache1.put(cacheKey1, initCache(String.class));

        // 二级缓存不为空
        Object result = cache1.getSecondCache().get(cacheKey1);
        Assert.assertNotNull(result);

        // 测试禁用一级缓存，一级缓存为空
        String str1 = cache1.getFirstCache().get(cacheKey1, String.class);
        Assert.assertNull(str1);
        Object result1 = cache1.getFirstCache().get(cacheKey1);
        Assert.assertNull(result1);

        // 二级缓存不为空
        result = cache1.getSecondCache().get(cacheKey1);
        Assert.assertNotNull(result);
    }

    /**
     * 测试禁用一级缓存
     */
    @Test
    public void testDisableFistCachePutIfAbsent() {
        // 测试 缓存过期时间
        String cacheName = "cache:name:3-0-2";
        String cacheKey1 = "cache:key:302-7";
        LayeringCache cache1 = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting7);
        cache1.putIfAbsent(cacheKey1, initCache(String.class));

        // 二级缓存不为空
        Object result = cache1.getSecondCache().get(cacheKey1);
        Assert.assertNotNull(result);

        // 测试禁用一级缓存，一级缓存为空
        String str1 = cache1.getFirstCache().get(cacheKey1, String.class);
        Assert.assertNull(str1);
        Object result1 = cache1.getFirstCache().get(cacheKey1);
        Assert.assertNull(result1);

        // 二级缓存不为空
        result = cache1.getSecondCache().get(cacheKey1);
        Assert.assertNotNull(result);
    }

    /**
     * 测试禁用一级缓存
     */
    @Test
    public void testDisableFistCacheDelete() {
        Long llen1 = ((AbstractCacheManager) cacheManager).getRedisClient().llen(GlobalConfig.getMessageRedisKey());
        // 测试 缓存过期时间
        String cacheName = "cache:name:3-0-2";
        String cacheKey1 = "cache:key:302-8";
        LayeringCache cache1 = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting7);
        cache1.evict(cacheKey1);

        sleep(5);
        Long llen2 = ((AbstractCacheManager) cacheManager).getRedisClient().llen(GlobalConfig.getMessageRedisKey());
        Assert.assertTrue(llen1 == llen2);

        // 二级缓存不为空
        Object result = cache1.getSecondCache().get(cacheKey1);
        Assert.assertNull(result);

        // 测试禁用一级缓存，一级缓存为空
        String str1 = cache1.getFirstCache().get(cacheKey1, String.class);
        Assert.assertNull(str1);
        Object result1 = cache1.getFirstCache().get(cacheKey1);
        Assert.assertNull(result1);

        // 二级缓存不为空
        result = cache1.getSecondCache().get(cacheKey1);
        Assert.assertNull(result);
    }


    /**
     * 测试禁用一级缓存
     */
    @Test
    public void testDisableFistCacheClear() {
        Long llen1 = ((AbstractCacheManager) cacheManager).getRedisClient().llen(GlobalConfig.getMessageRedisKey());
        // 测试 缓存过期时间
        String cacheName = "cache:name:3-0-2";
        String cacheKey1 = "cache:key:302-9";
        LayeringCache cache1 = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting7);
        cache1.clear();

        sleep(5);
        Long llen2 = ((AbstractCacheManager) cacheManager).getRedisClient().llen(GlobalConfig.getMessageRedisKey());
        Assert.assertTrue(llen1 == llen2);

        // 二级缓存不为空
        Object result = cache1.getSecondCache().get(cacheKey1);
        Assert.assertNull(result);

        // 测试禁用一级缓存，一级缓存为空
        String str1 = cache1.getFirstCache().get(cacheKey1, String.class);
        Assert.assertNull(str1);
        Object result1 = cache1.getFirstCache().get(cacheKey1);
        Assert.assertNull(result1);

        // 二级缓存不为空
        result = cache1.getSecondCache().get(cacheKey1);
        Assert.assertNull(result);
    }

    private void pushDeleteMessage(String cacheName, String cacheKey) {
        RedisPubSubMessage message = new RedisPubSubMessage();
        message.setCacheName(cacheName);
        message.setKey(cacheKey);
        message.setMessageType(RedisPubSubMessageType.EVICT);
        String messageJson = JSON.toJSONString(message);
        // pull 拉模式消息
        redisClient.lpush(GlobalConfig.getMessageRedisKey(), messageJson);
        redisClient.expire(GlobalConfig.getMessageRedisKey(), 25, TimeUnit.MINUTES);
    }

    private void pushDeleteClear(String cacheName) {
        RedisPubSubMessage message = new RedisPubSubMessage();
        message.setCacheName(cacheName);
        message.setMessageType(RedisPubSubMessageType.CLEAR);
        String messageJson = JSON.toJSONString(message);
        // pull 拉模式消息
        redisClient.lpush(GlobalConfig.getMessageRedisKey(), messageJson);
        redisClient.expire(GlobalConfig.getMessageRedisKey(), 25, TimeUnit.MINUTES);
    }

    /**
     * 测试锁
     */
    @Test
    public void testLock() {
        LayeringCacheRedisLock lock = new LayeringCacheRedisLock(redisClient, "test:123");
        lock.lock();
        Boolean hasKey = redisClient.hasKey("test:123_lock");
        Assert.assertTrue(hasKey);
        Boolean unlock = lock.unlock();
        Assert.assertTrue(unlock);
        hasKey = redisClient.hasKey("test:123_lock");
        Assert.assertTrue(!hasKey);
    }

    private <T> T initCache(Class<T> t) {
        logger.debug("加载缓存");
        return (T) "test";
    }

    private <T> T initNullCache() {
        logger.debug("加载缓存,空值");
        return null;
    }


    private void sleep(int time) {
        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}

