package com.xiaolyuh.test;

import com.xiaolyuh.config.CacheConfig;
import com.xiaolyuh.domain.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

// SpringJUnit4ClassRunner再Junit环境下提供Spring TestContext Framework的功能。
@RunWith(SpringJUnit4ClassRunner.class)
// @ContextConfiguration用来加载配置ApplicationContext，其中classes用来加载配置类
@ContextConfiguration(classes = {CacheConfig.class})
public class CacheAspectTest {
    private Logger logger = LoggerFactory.getLogger(CacheAspectTest.class);

    @Autowired
    private TestService testService;

    @Test
    public void testGetUserName1() {
        long userId = 123;

        testService.getUserName(userId);
        testService.getUserName(userId);
        sleep(4);
        testService.getUserName(userId);
        sleep(4);
        testService.getUserName(userId);
        sleep(10);
        testService.getUserName(userId);
    }

    @Test
    public void testGetUserName2() {
        String[] lastName = {"w", "y", "h"};
        long userId = 122;

        testService.getUserName(userId, lastName);
        testService.getUserName(userId, lastName);
        sleep(4);
        testService.getUserName(userId, lastName);
        sleep(4);
        testService.getUserName(userId, lastName);
        sleep(10);
        testService.getUserName(userId, lastName);
    }

    @Test
    public void testGetUserName3() {
        User user = new User();
        user.setUserId(111);
        user.setAge(31);
        user.setLastName(new String[]{"w", "y", "h"});

        testService.getUserName(user);
        testService.getUserName(user);
        sleep(4);
        testService.getUserName(user);
        sleep(4);
        testService.getUserName(user);
        sleep(10);
        testService.getUserName(user);
    }

    @Test
    public void testGetUserName4() {
        User user = new User();
        user.setUserId(111);
        user.setAge(31);
        user.setLastName(new String[]{"w", "y", "h"});

        testService.getUserName(user, user.getAge());
        testService.getUserName(user, user.getAge());
        sleep(4);
        testService.getUserName(user, user.getAge());
        sleep(4);
        testService.getUserName(user, user.getAge());
        sleep(10);
        testService.getUserName(user, user.getAge());
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

