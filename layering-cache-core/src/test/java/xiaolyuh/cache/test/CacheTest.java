package xiaolyuh.cache.test;

import com.xiaolyuh.cache.Cache;
import com.xiaolyuh.cache.LayeringCache;
import com.xiaolyuh.manager.CacheManager;
import com.xiaolyuh.setting.FirstCacheSetting;
import com.xiaolyuh.setting.LayeringCacheSetting;
import com.xiaolyuh.setting.SecondaryCacheSetting;
import com.xiaolyuh.support.ExpireMode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import xiaolyuh.cache.config.CacheConfig;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

// SpringJUnit4ClassRunner再Junit环境下提供Spring TestContext Framework的功能。
@RunWith(SpringJUnit4ClassRunner.class)
//// @ContextConfiguration用来加载配置ApplicationContext，其中classes用来加载配置类
@ContextConfiguration(classes = {CacheConfig.class})
public class CacheTest {
    private Logger logger = LoggerFactory.getLogger(CacheTest.class);

    @Autowired
    private CacheManager cacheManager;

    private LayeringCacheSetting layeringCacheSetting1;
    private LayeringCacheSetting layeringCacheSetting2;

    @Before
    public void testGetCache() {
        // 测试 CacheManager getCache方法
        FirstCacheSetting firstCacheSetting1 = new FirstCacheSetting(10, 1000, 6, TimeUnit.SECONDS, ExpireMode.WRITE);
        SecondaryCacheSetting secondaryCacheSetting1 = new SecondaryCacheSetting(30, 10, TimeUnit.SECONDS, true);
        layeringCacheSetting1 = new LayeringCacheSetting(firstCacheSetting1, secondaryCacheSetting1);

        FirstCacheSetting firstCacheSetting2 = new FirstCacheSetting(10, 1000, 5, TimeUnit.SECONDS, ExpireMode.WRITE);
        SecondaryCacheSetting secondaryCacheSetting2 = new SecondaryCacheSetting(3000, 14, TimeUnit.SECONDS, true);
        layeringCacheSetting2 = new LayeringCacheSetting(firstCacheSetting2, secondaryCacheSetting2);

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
        String cacheName = "cache:name";
        LayeringCache cache1 = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting1);
        LayeringCache cache2 = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting2);
        cache1.get("cache:key1", () -> initCache(String.class));
        // 测试一级缓存值及过期时间
        String str1 = cache1.getFirstCache().get("cache:key1", String.class);
        String st2 = cache1.getFirstCache().get("cache:key1", () ->initCache(String.class));
        logger.info("========================:{}", str1);
        Assert.assertTrue(str1.equals(st2));
        Assert.assertTrue(str1.equals(initCache(String.class)));
        sleep(6);
        Assert.assertNull(cache1.getFirstCache().get("cache:key1", String.class));

        cache1.get("cache:key1", () -> initCache(String.class));
    }

    private <T> T initCache(Class<T> t) {
        logger.info("加载缓存");
        return (T) "test";
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}

