package com.github.xiaolyuh.test;

import com.alibaba.fastjson.JSON;
import com.github.xiaolyuh.config.CacheConfig;
import com.github.xiaolyuh.domain.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

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
        sleep(5);
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
        sleep(5);
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
        sleep(5);
        user = testService.getUser(user);
        sleep(4);
        user = testService.getUser(user);
        sleep(11);
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
        sleep(5);
        user = testService.getUser(user, user.getAge());
        sleep(4);
        user = testService.getUser(user, user.getAge());
        sleep(11);
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

        sleep(5);
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
    public void testGetUserNoParam() {
        User user = testService.getUserNoParam();
        Assert.assertNotNull(user);
        user = testService.getUserNoParam();
        Assert.assertNotNull(user);

        sleep(5);
        testService.getUserNoParam();
        sleep(4);
        testService.getUserNoParam();
        sleep(11);
        Object result = redisTemplate.opsForValue().get("user:info:{params:[]}");
        Assert.assertNull(result);

        user = testService.getUserNoParam();
        Assert.assertNotNull(user);
    }

    @Test
    public void testGetString() {
        String user = testService.getString(211);
        Assert.assertNotNull(user);
        user = testService.getString(211);
        Assert.assertNotNull(user);
        sleep(5);
        user = testService.getString(211);
        Assert.assertNotNull(user);
    }

    @Test
    public void testGetInt() {
        Integer user = testService.getInt(212);
        Assert.assertNotNull(user);
        user = testService.getInt(212);
        Assert.assertNotNull(user);
        sleep(5);
        user = testService.getInt(212);
        Assert.assertNotNull(user);
    }

    @Test
    public void testGetLong() {
        Long user = testService.getLong(213);
        Assert.assertNotNull(user);
        user = testService.getLong(213);
        Assert.assertNotNull(user);
        sleep(5);
        testService.getLong(213);
    }

    @Test
    public void testGetArray() {
        long[] array = testService.getArray(214);
        Assert.assertNotNull(array);
        array = testService.getArray(214);
        Assert.assertEquals(array.length, 3);
        sleep(5);
        array = testService.getArray(214);
        Assert.assertEquals(array.length, 3);
    }

    @Test
    public void testGetList() {
        List<String> list = testService.getList(215);
        Assert.assertNotNull(list);
        list = testService.getList(215);
        Assert.assertEquals(list.size(), 3);
        sleep(5);
        list = testService.getList(215);
        Assert.assertEquals(list.size(), 3);

    }

    @Test
    public void testGetListObject() {
        List<User> list = testService.getListObject(216);
        Assert.assertNotNull(list);
        list = testService.getListObject(216);
        Assert.assertEquals(list.size(), 3);
        sleep(5);
        list = testService.getListObject(216);
        Assert.assertEquals(list.size(), 3);

    }

    @Test
    public void testGetException() {
        List<User> list = null;
        try {
            list = testService.getException(217);
        } catch (Exception e) {
            Assert.assertNotNull(e);
            return;
        }
        Assert.assertTrue(false);

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
    public void testPutUserNoParam() {
        User user = testService.putUserNoParam();
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
        sleep(3);
        testService.evictUser(userId);
        sleep(3);
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
        sleep(3);
        Object result1 = redisTemplate.opsForValue().get("user:info:119:119");
        Object result2 = redisTemplate.opsForValue().get("user:info:121:121");
        Assert.assertNull(result1);
        Assert.assertNull(result2);
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

    private void sleep(int time) {
        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}

