package com.github.xiaolyuh.test;

import com.github.xiaolyuh.annotation.*;
import com.github.xiaolyuh.domain.User;
import com.github.xiaolyuh.support.CacheMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class TestService {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Cacheable(value = "user:info", key = "#userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3,
                    forceRefresh = true, timeUnit = TimeUnit.SECONDS, isAllowNullValue = true))
    public User getUserById(long userId) {
        logger.debug("测试正常配置的缓存方法，参数是基本类型");
        User user = new User();
        user.setUserId(userId);
        user.setAge(31);
        user.setLastName(new String[]{"w", "y", "h"});
        return user;
    }

    @Cacheable(value = "user:info", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getUserNoKey(long userId, String[] lastName) {
        logger.debug("测试没有配置key的缓存方法，参数是基本类型和数组的缓存缓存方法");
        User user = new User();
        user.setUserId(userId);
        user.setAge(31);
        user.setLastName(lastName);
        return user;
    }

    @Cacheable(value = "user:info",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS), ignoreException = false,
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getUserObjectPram(User user) {
        logger.debug("测试没有配置key的缓存方法，参数是复杂对象");
        return user;
    }

    @Cacheable(value = "user:info", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getUser(User user, int age) {
        logger.debug("测试没有配置key的缓存方法，参数是复杂对象和基本量类型");
        user.setAge(age);
        return user;
    }


    @Cacheable(value = "user:info", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getUserNoParam() {
        logger.debug("测试没有配置key的缓存方法，没有参数");
        User user = new User();
        user.setUserId(223);
        user.setAge(31);
        user.setLastName(new String[]{"w", "y", "h"});
        return user;
    }

    @Cacheable(value = "user:info", key = "#user.userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getNullObjectPram(User user) {
        logger.debug("测试参数是NULL对象，不忽略异常");
        return user;
    }

    @Cacheable(value = "user:info",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getNullObjectPramIgnoreException(User user) {
        logger.debug("测试参数是NULL对象，忽略异常");
        return user;
    }

    @Cacheable(value = "user:info", key = "#userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true,
                    timeUnit = TimeUnit.SECONDS, isAllowNullValue = true, magnification = 1))
    public User getNullUser(Long userId) {
        logger.debug("缓存方法返回NULL");
        return null;
    }

    @Cacheable(value = "user:info", key = "#userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 100, preloadTime = 70, forceRefresh = true,
                    timeUnit = TimeUnit.SECONDS, isAllowNullValue = true, magnification = 10))
    public User getNullUserAllowNullValueTrueMagnification(Long userId) {
        logger.debug("缓存方法返回NULL");
        return null;
    }

    @Cacheable(value = "user:info", key = "#userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 7, forceRefresh = true,
                    timeUnit = TimeUnit.SECONDS, isAllowNullValue = false))
    public User getNullUserAllowNullValueFalse(Long userId) {
        logger.debug("缓存方法返回NULL");
        return null;
    }

    @Cacheable(value = "user:info", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public String getString(long userId) {
        logger.debug("缓存方法返回字符串");
        return "User";
    }

    @Cacheable(value = "user:info", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public int getInt(long userId) {
        logger.debug("缓存方法返回基本类型");
        return 111;
    }

    @Cacheable(value = "user:info", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public Long getLong(long userId) {
        logger.debug("缓存方法返回包装类型");
        return 1111L;
    }

    @Cacheable(value = "user:info", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public double getDouble(long userId) {
        logger.debug("缓存方法返回包装类型");
        return 111.2;
    }

    @Cacheable(value = "user:info", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public float getFloat(long userId) {
        logger.debug("缓存方法返回包装类型");
        return 11.311F;
    }

    @Cacheable(value = "user:info", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public BigDecimal getBigDecimal(long userId) {
        logger.debug("缓存方法返回包装类型");
        return new BigDecimal(33.33);
    }

    @Cacheable(value = "user:info", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public Date getDate(long userId) {
        logger.debug("缓存方法返回Date类型");
        return new Date();
    }


    @Cacheable(value = "user:info", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public CacheMode getEnum(long userId) {
        logger.debug("缓存方法返回枚举");
        return CacheMode.ONLY_FIRST;
    }

    @Cacheable(value = "user:info", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public long[] getArray(long userId) {
        logger.debug("缓存方法返回数组");
        return new long[]{111, 222, 333};
    }

    @Cacheable(value = "user:info", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User[] getObjectArray(long userId) {
        logger.debug("缓存方法返回数组");
        User user = new User();
        return new User[]{user, user, user};
    }

    @Cacheable(value = "user:info", ignoreException = false,
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

    @Cacheable(value = "user:info", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public LinkedList<String> getLinkedList(long userId) {
        logger.debug("调用方法获取数组");
        LinkedList<String> list = new LinkedList<>();
        list.add("111");
        list.add("112");
        list.add("113");

        return list;
    }

    @Cacheable(value = "user:info", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public List<User> getListObject(long userId) {
        logger.debug("调用方法获取数组");
        List<User> list = new ArrayList<>();
        User user = new User();
        list.add(user);
        list.add(user);
        list.add(user);

        return list;
    }

    @Cacheable(value = "user:info", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public Set<String> getSet(long userId) {
        logger.debug("调用方法获取数组");
        Set<String> set = new HashSet<>();
        set.add("111");
        set.add("112");
        set.add("113");
        return set;
    }

    @Cacheable(value = "user:info", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public Set<User> getSetObject(long userId) {
        logger.debug("调用方法获取数组");
        Set<User> set = new HashSet<>();
        User user = new User();
        set.add(user);
        return set;
    }

    @Cacheable(value = "user:info", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public List<User> getException(long userId) {
        logger.debug("缓存测试方法");

        throw new RuntimeException("缓存测试方法");
    }


    @CachePut(value = "user:info", key = "#userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User putUser(long userId) {
        return new User();
    }

    @CachePut(value = "user:info",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS), ignoreException = false,
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User putUserNoParam() {
        User user = new User();
        return user;
    }

    @CachePut(value = "user:info:118", key = "#userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true,
                    timeUnit = TimeUnit.SECONDS, isAllowNullValue = true))
    public User putNullUser1118(long userId) {

        return null;
    }

    @Cacheable(value = "user:info:118", key = "#userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3,
                    forceRefresh = true, timeUnit = TimeUnit.SECONDS, isAllowNullValue = true))
    public User getUserById118(long userId) {
        logger.debug("1.1.8版本测试正常配置的缓存方法，参数是基本类型");
        User user = new User();
        user.setUserId(userId);
        user.setAge(31);
        user.setLastName(new String[]{"w", "y", "h"});
        return user;
    }

    @CachePut(value = "user:info", key = "#userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 40, preloadTime = 30, forceRefresh = true,
                    timeUnit = TimeUnit.SECONDS, isAllowNullValue = true, magnification = 10))
    public User putNullUserAllowNullValueTrueMagnification(long userId) {

        return null;
    }

    @CachePut(value = "user:info", key = "#userId", ignoreException = false,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 7, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User putNullUserAllowNullValueFalse(long userId) {

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

    @CacheEvict(value = "user:info", key = "#userId", ignoreException = false)
    public void evictUser(long userId) {

    }

    @CacheEvict(value = "user:info", allEntries = true, ignoreException = false)
    public void evictAllUser() {
    }
}
