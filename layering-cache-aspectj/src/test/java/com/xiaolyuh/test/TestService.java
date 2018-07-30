package com.xiaolyuh.test;

import com.xiaolyuh.annotation.Cacheable;
import com.xiaolyuh.annotation.FirstCache;
import com.xiaolyuh.annotation.SecondaryCache;
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
            secondaryCache = @SecondaryCache(expiration = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public Object getUserName(long userId) {
        logger.info("调用方法获取用户名称");
        return "xiaolyuh";
    }

    @Cacheable(value = "'user:info' + ':' + #userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expiration = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public Object getUserName(long userId, String[] lastName) {
        logger.info("调用方法获取用户名称");
        return "xiaolyuh";
    }

    @Cacheable(value = "'user:info' + ':' + #user.userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expiration = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public Object getUserName(User user) {
        logger.info("调用方法获取用户名称");
        return "xiaolyuh";
    }

    @Cacheable(value = "'user:info' + ':' + #user.userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expiration = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public Object getUserName(User user, int age) {
        logger.info("调用方法获取用户名称");
        return "xiaolyuh";
    }

}
