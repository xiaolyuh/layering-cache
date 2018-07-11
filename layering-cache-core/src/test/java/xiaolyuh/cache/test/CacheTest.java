package xiaolyuh.cache.test;

import com.xiaolyuh.manager.CacheManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import xiaolyuh.cache.config.CacheConfig;
import xiaolyuh.cache.config.RedisConfig;


// SpringJUnit4ClassRunner再Junit环境下提供Spring TestContext Framework的功能。
@RunWith(SpringJUnit4ClassRunner.class)
//// @ContextConfiguration用来加载配置ApplicationContext，其中classes用来加载配置类
@ContextConfiguration(classes = {RedisConfig.class, CacheConfig.class})
@PropertySource("classpath:application.properties")
public class CacheTest {
    @Autowired
    private CacheManager cacheManager;

    @Test
    public void contextTest() {
        System.out.println(cacheManager);
    }

}
