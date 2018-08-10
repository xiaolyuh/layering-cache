package com.xiaolyuh.test;

import com.alibaba.fastjson.JSON;
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

        testService.getUser(userId);
        testService.getUser(userId);
        sleep(4);
        testService.getUser(userId);
        sleep(4);
        testService.getUser(userId);
        sleep(10);
        testService.getUser(userId);
    }

    @Test
    public void testGetUserName2() {
        String[] lastName = {"w", "y", "h"};
        long userId = 122;

        testService.getUser(userId, lastName);
        testService.getUser(userId, lastName);
        sleep(4);
        testService.getUser(userId, lastName);
        sleep(4);
        testService.getUser(userId, lastName);
        sleep(10);
        testService.getUser(userId, lastName);
    }

    @Test
    public void testGetUserName3() {
        User user = new User();
        user.setUserId(111);
        user.setAge(31);
        user.setLastName(new String[]{"w", "y", "h"});

        testService.getUser(user);
        testService.getUser(user);
        sleep(4);
        testService.getUser(user);
        sleep(4);
        testService.getUser(user);
        sleep(10);
        testService.getUser(user);
    }

    @Test
    public void testGetUserName4() {
        User user = new User();
        user.setUserId(111);
        user.setAge(31);
        user.setLastName(new String[]{"w", "y", "h"});

        testService.getUser(user, user.getAge());
        testService.getUser(user, user.getAge());
        sleep(4);
        testService.getUser(user, user.getAge());
        sleep(4);
        testService.getUser(user, user.getAge());
        sleep(10);
        testService.getUser(user, user.getAge());
    }

    @Test
    public void testPutUser() {
        long userId = 122;
        testService.putUser(userId);
        User user = testService.getUser(userId);
        logger.debug(JSON.toJSONString(user));
    }

    @Test
    public void testEvictUser() {
        long userId = 122;
        testService.putUser(userId);
        sleep(2);
        testService.evictUser(userId);
        sleep(2);
    }

    @Test
    public void testEvictAllUser() {
        testService.putUserById(122);
        testService.putUserById(123);
        testService.putUserById(124);
        sleep(5);
        testService.evictAllUser();
        sleep(2);
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}

