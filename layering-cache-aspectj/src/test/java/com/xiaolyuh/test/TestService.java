package com.xiaolyuh.test;

import com.xiaolyuh.annotation.*;
import com.xiaolyuh.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TestService {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Cacheable(value = "'user:info' + ':' + #userId", key = "#userId",
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

    @Cacheable(value = "'user:info' + ':' + #userId",
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
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getUser(User user) {
        logger.debug("调用方法获取用户名称");
        return user;
    }

    @Cacheable(value = "'user:info' + ':' + #user.userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getUser(User user, int age) {
        logger.debug("调用方法获取用户名称");
        user.setAge(age);
        return user;
    }

    @Cacheable(value = "'user:info' + ':' + #userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getNullUser(long userId) {
        logger.debug("调用方法获取用户名称返回NULL");
        return null;
    }

    @CachePut(value = "'user:info' + ':' + #userId", key = "#userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User putUser(long userId) {
        User user = new User();
        user.setUserId(userId);
        user.setAge(31);
        user.setLastName(new String[]{"w", "y", "h"});

        return user;
    }

    @CachePut(value = "'user:info' + ':' + #userId", key = "#userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User putNullUser(long userId) {

        return null;
    }

    @CachePut(value = "user:info", key = "#userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User putUserById(long userId) {
        User user = new User();
        user.setUserId(userId);
        user.setAge(311);
        user.setLastName(new String[]{"w", "y", "h"});

        return user;
    }

    @CacheEvict(value = "'user:info' + ':' + #userId", key = "#userId")
    public void evictUser(long userId) {

    }

    @CacheEvict(value = "user:info", allEntries = true)
    public void evictAllUser() {
    }
}
