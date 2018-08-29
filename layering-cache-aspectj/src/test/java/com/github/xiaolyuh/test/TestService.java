package com.github.xiaolyuh.test;

import com.github.xiaolyuh.annotation.*;
import com.github.xiaolyuh.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TestService {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Cacheable(value = "'user:info' + ':' + #userId", key = "#userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getUser(long userId) {
        logger.debug("调用方法获取用户名称");
        User user = new User();
        user.setUserId(userId);
        user.setAge(31);
        user.setLastName(new String[]{"w", "y", "h"});
        return user;
    }

    @Cacheable(value = "'user:info' + ':' + #userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getUser(long userId, String[] lastName) {
        logger.debug("调用方法获取用户名称");
        User user = new User();
        user.setUserId(userId);
        user.setAge(31);
        user.setLastName(lastName);
        return user;
    }

    @Cacheable(value = "'user:info' + ':' + #user.userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS), ignoreException = false,
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getUser(User user) {
        logger.debug("调用方法获取用户名称");
        return user;
    }

    @Cacheable(value = "'user:info' + ':' + #user.userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getNullObjectPram(User user) {
        logger.debug("测试");
        return user;
    }

    @Cacheable(value = "'user:info' + ':' + #user.userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getNullObjectPramIgnoreException(User user) {
        logger.debug("测试");
        return user;
    }

    @Cacheable(value = "'user:info' + ':' + #user.userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getUser(User user, int age) {
        logger.debug("调用方法获取用户名称");
        user.setAge(age);
        return user;
    }

    @Cacheable(value = "user:info", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getUserNoParam() {
        logger.debug("调用方法获取用户名称");
        User user = new User();
        user.setUserId(223);
        user.setAge(31);
        user.setLastName(new String[]{"w", "y", "h"});
        return user;
    }

    @Cacheable(value = "'user:info' + ':' + #userId", key = "#userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getNullUser(Long userId) {
        logger.debug("调用方法获取用户名称返回NULL");
        return null;
    }

    @Cacheable(value = "'user:info' + ':' + #userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public String getString(long userId) {
        logger.debug("调用方法获取用户名称");
        return "User";
    }

    @Cacheable(value = "'user:info' + ':' + #userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public int getInt(long userId) {
        logger.debug("调用方法获取用户名称");
        return 111;
    }

    @Cacheable(value = "'user:info' + ':' + #userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public Long getLong(long userId) {
        logger.debug("调用方法获取用户名称");
        return 1111L;
    }

    @Cacheable(value = "'user:info' + ':' + #userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public long[] getArray(long userId) {
        logger.debug("调用方法获取数组");
        return new long[]{111, 222, 333};
    }

    @Cacheable(value = "'user:info' + ':' + #userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public List<String> getList(long userId) {
        logger.debug("调用方法获取数组");
        List<String> list = new ArrayList<>();
        list.add("111");
        list.add("112");
        list.add("113");

        return list;
    }

    @Cacheable(value = "'user:info' + ':' + #userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public List<User> getListObject(long userId) {
        logger.debug("调用方法获取数组");
        List<User> list = new ArrayList<>();
        User user = new User();
        user.setUserId(223);
        user.setAge(31);
        user.setLastName(new String[]{"w", "y", "h"});

        list.add(user);
        list.add(user);
        list.add(user);

        return list;
    }

    @Cacheable(value = "'user:info' + ':' + #userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public List<User> getException(long userId) {
        logger.debug("缓存测试方法");

        throw new RuntimeException("缓存测试方法");
    }


    @CachePut(value = "'user:info' + ':' + #userId", key = "#userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User putUser(long userId) {
        User user = new User();
        user.setUserId(userId);
        user.setAge(31);
        user.setLastName(new String[]{"w", "y", "h"});

        return user;
    }

    @CachePut(value = "user:info",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS), ignoreException = false,
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User putUserNoParam() {
        User user = new User();
        user.setUserId(222);
        user.setAge(31);
        user.setLastName(new String[]{"w", "y", "h"});

        return user;
    }

    @CachePut(value = "'user:info' + ':' + #userId", key = "#userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User putNullUser(long userId) {

        return null;
    }

    @CachePut(value = "user:info", key = "#userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User putUserById(long userId) {
        User user = new User();
        user.setUserId(userId);
        user.setAge(311);
        user.setLastName(new String[]{"w", "y", "h"});

        return user;
    }

    @CacheEvict(value = "'user:info' + ':' + #userId", key = "#userId", ignoreException = false)
    public void evictUser(long userId) {

    }

    @CacheEvict(value = "user:info", allEntries = true, ignoreException = false)
    public void evictAllUser() {
    }
}
