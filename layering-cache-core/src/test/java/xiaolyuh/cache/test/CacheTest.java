package xiaolyuh.cache.test;

import com.xiaolyuh.cache.Cache;
import com.xiaolyuh.manager.CacheManager;
import com.xiaolyuh.setting.FirstCacheSetting;
import com.xiaolyuh.setting.LayeringCacheSetting;
import com.xiaolyuh.setting.SecondaryCacheSetting;
import com.xiaolyuh.support.ExpireMode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import xiaolyuh.cache.config.CacheConfig;
import xiaolyuh.cache.config.RedisConfig;

import java.util.concurrent.TimeUnit;


// SpringJUnit4ClassRunner再Junit环境下提供Spring TestContext Framework的功能。
@RunWith(SpringJUnit4ClassRunner.class)
//// @ContextConfiguration用来加载配置ApplicationContext，其中classes用来加载配置类
@ContextConfiguration(classes = {RedisConfig.class, CacheConfig.class})
@PropertySource("classpath:application.properties")
public class CacheTest {
    private Logger logger = LoggerFactory.getLogger(CacheTest.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void contextTest() {

        FirstCacheSetting firstCacheSetting = new FirstCacheSetting(10, 1000, 10, TimeUnit.SECONDS, ExpireMode.WRITE);
        SecondaryCacheSetting secondaryCacheSetting = new SecondaryCacheSetting(60, 15, TimeUnit.SECONDS, true);
        LayeringCacheSetting layeringCacheSetting = new LayeringCacheSetting(firstCacheSetting, secondaryCacheSetting);
        Cache cache = cacheManager.getCache("test:cache:name", layeringCacheSetting);

        cache.get("cache:key", () -> initCache(String.class));
        cache.get("cache:key", () -> initCache(String.class));


    }

    private <T> T initCache(Class<T> t) {
        logger.info("加载缓存");
        return (T) "test";
    }

}
