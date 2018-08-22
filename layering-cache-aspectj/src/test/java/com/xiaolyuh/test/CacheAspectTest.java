package com.xiaolyuh.test;

import com.alibaba.fastjson.JSON;
import com.xiaolyuh.config.CacheConfig;
import com.xiaolyuh.domain.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testGetUserNameLongParam() {
        long userId = 111;

        User user = testService.getUser(userId);
        user = testService.getUser(userId);
        sleep(4);
        user = testService.getUser(userId);
        sleep(4);
        user = testService.getUser(userId);
        sleep(10);

        Object result = redisTemplate.opsForValue().get("user:info:113:113");
        Assert.assertNull(result);

        user = testService.getUser(userId);
        Assert.assertNotNull(user);
    }

    @Test
    public void testGetUserNameArrayAndLongParam() {
        String[] lastName = {"w", "y", "h"};
        long userId = 122;

        User user = testService.getUser(userId, lastName);
        user = testService.getUser(userId, lastName);
        sleep(4);
        user = testService.getUser(userId, lastName);
        sleep(4);
        user = testService.getUser(userId, lastName);
        sleep(10);
        Object result = redisTemplate.opsForValue().get("user:info:113:113");
        Assert.assertNull(result);

        user = testService.getUser(userId, lastName);
        Assert.assertNotNull(user);
    }

    @Test
    public void testGetUserNameObjectParam() {
        User user = new User();
        user.setUserId(113);
        user.setAge(31);
        user.setLastName(new String[]{"w", "y", "h"});

        user = testService.getUser(user);
        user = testService.getUser(user);
        sleep(4);
        user = testService.getUser(user);
        sleep(4);
        user = testService.getUser(user);
        sleep(10);
        Object result = redisTemplate.opsForValue().get("user:info:113:113");
        Assert.assertNull(result);

        user = testService.getUser(user);
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
        sleep(4);
        user = testService.getUser(user, user.getAge());
        sleep(4);
        user = testService.getUser(user, user.getAge());
        sleep(10);
        Object result = redisTemplate.opsForValue().get("user:info:114:114");
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

        sleep(4);
        user = testService.getNullUser(userId);
        sleep(4);
        user = testService.getNullUser(userId);
        sleep(11);
        Object result = redisTemplate.opsForValue().get("user:info:115:115");
        Assert.assertNull(result);

        user = testService.getNullUser(userId);
        Assert.assertNull(user);
    }

    @Test
    public void testPutUser() {
        long userId = 116;
        testService.putUser(userId);
        User user = testService.getUser(userId);
        logger.debug(JSON.toJSONString(user));
        Assert.assertNotNull(user);
    }

    @Test
    public void testPutNullUser() {
        long userId = 117;
        testService.putNullUser(userId);
        User user = testService.getUser(userId);
        logger.debug(JSON.toJSONString(user));
        Assert.assertNull(user);
    }

    @Test
    public void testEvictUser() {
        long userId = 118;
        User user = testService.putUser(userId);
        sleep(2);
        testService.evictUser(userId);
        sleep(2);
        Object result = redisTemplate.opsForValue().get("user:info:118:118");
        Assert.assertNull(result);
    }

    @Test
    public void testEvictAllUser() {
        testService.putUserById(119);
        testService.putUserById(120);
        testService.putUserById(121);
        sleep(5);
        testService.evictAllUser();
        sleep(2);
        Object result1 = redisTemplate.opsForValue().get("user:info:119:119");
        Object result2 = redisTemplate.opsForValue().get("user:info:121:121");
        Assert.assertNull(result1);
        Assert.assertNull(result2);
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}

