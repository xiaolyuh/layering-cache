package com.github.xiaolyuh.test;

import com.github.xiaolyuh.annotation.CacheEvict;
import com.github.xiaolyuh.annotation.CachePut;
import com.github.xiaolyuh.annotation.Cacheable;
import com.github.xiaolyuh.annotation.Caching;
import com.github.xiaolyuh.annotation.FirstCache;
import com.github.xiaolyuh.annotation.SecondaryCache;
import com.github.xiaolyuh.domain.User;
import com.github.xiaolyuh.support.CacheMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TestService {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Cacheable(value = "user:info", key = "#userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3,
                    forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getUserById(long userId) {
        logger.debug("测试正常配置的缓存方法，参数是基本类型");
        User user = new User();
        user.setUserId(userId);
        user.setAge(31);
        user.setLastName(new String[]{"w", "y", "h"});
        return user;
    }

    @Cacheable(value = "user:info",
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
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getUserObjectPram(User user) {
        logger.debug("测试没有配置key的缓存方法，参数是复杂对象");
        return user;
    }

    @Cacheable(value = "user:info",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getUser(User user, int age) {
        logger.debug("测试没有配置key的缓存方法，参数是复杂对象和基本量类型");
        user.setAge(age);
        return user;
    }


    @Cacheable(value = "user:info",
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

    @Cacheable(value = "user:info", key = "#user.userId",
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

    @Cacheable(value = "user:info", key = "#userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true,
                    timeUnit = TimeUnit.SECONDS, magnification = 1))
    public User getNullUser(Long userId) {
        logger.debug("缓存方法返回NULL");
        return null;
    }

    @Cacheable(value = "user:info", key = "#userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 100, preloadTime = 70, forceRefresh = true,
                    timeUnit = TimeUnit.SECONDS, magnification = 10))
    public User getNullUserAllowNullValueTrueMagnification(Long userId) {
        logger.debug("缓存方法返回NULL");
        return null;
    }

    @Cacheable(value = "user:info", key = "#userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 7, forceRefresh = true,
                    timeUnit = TimeUnit.SECONDS))
    public User getNullUserAllowNullValueFalse(Long userId) {
        logger.debug("缓存方法返回NULL");
        return null;
    }

    @Cacheable(value = "user:info",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public String getString(long userId) {
        logger.debug("缓存方法返回字符串");
        return "User";
    }

    @Cacheable(value = "user:info",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public int getInt(long userId) {
        logger.debug("缓存方法返回基本类型");
        return 111;
    }

    @Cacheable(value = "user:info",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public Long getLong(long userId) {
        logger.debug("缓存方法返回包装类型");
        return 1111L;
    }

    @Cacheable(value = "user:info",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public double getDouble(long userId) {
        logger.debug("缓存方法返回包装类型");
        return 111.2;
    }

    @Cacheable(value = "user:info",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public float getFloat(long userId) {
        logger.debug("缓存方法返回包装类型");
        return 11.311F;
    }

    @Cacheable(value = "user:info",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public BigDecimal getBigDecimal(long userId) {
        logger.debug("缓存方法返回包装类型");
        return new BigDecimal(33.33);
    }

    @Cacheable(value = "user:info",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public Date getDate(long userId) {
        logger.debug("缓存方法返回Date类型");
        return new Date();
    }


    @Cacheable(value = "user:info",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public CacheMode getEnum(long userId) {
        logger.debug("缓存方法返回枚举");
        return CacheMode.FIRST;
    }

    @Cacheable(value = "user:info",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public long[] getArray(long userId) {
        logger.debug("缓存方法返回数组");
        return new long[]{111, 222, 333};
    }

    @Cacheable(value = "user:info",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User[] getObjectArray(long userId) {
        logger.debug("缓存方法返回数组");
        User user = new User();
        return new User[]{user, user, user};
    }

    @Cacheable(value = "user:info",
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

    @Cacheable(value = "user:info",
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

    @Cacheable(value = "user:info",
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

    @Cacheable(value = "user:info",
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

    @Cacheable(value = "user:info",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public Set<User> getSetObject(long userId) {
        logger.debug("调用方法获取数组");
        Set<User> set = new HashSet<>();
        User user = new User();
        set.add(user);
        return set;
    }

    @Cacheable(value = "user:info",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public List<User> getException(long userId) {
        logger.debug("缓存测试方法");

        throw new RuntimeException("缓存测试方法");
    }


    @CachePut(value = "user:info", key = "#userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User putUser(long userId) {
        return new User();
    }

    @CachePut(value = "user:info",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User putUserNoParam() {
        User user = new User();
        return user;
    }

    @CachePut(value = "user:info:118", key = "#userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true,
                    timeUnit = TimeUnit.SECONDS))
    public User putNullUser1118(long userId) {

        return null;
    }

    @CachePut(value = "user:info",
            firstCache = @FirstCache(expireTime = 40, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 100, preloadTime = 30, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User putUserNoKey(long userId, String[] lastName, User user) {
        return user;
    }

    @Cacheable(value = "user:info:118", key = "#userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3,
                    forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getUserById118(long userId) {
        logger.debug("1.1.8版本测试正常配置的缓存方法，参数是基本类型");
        User user = new User();
        user.setUserId(userId);
        user.setAge(31);
        user.setLastName(new String[]{"w", "y", "h"});
        return user;
    }

    @CachePut(value = "user:info", key = "#userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 40, preloadTime = 30, forceRefresh = true,
                    timeUnit = TimeUnit.SECONDS, magnification = 10))
    public User putNullUserAllowNullValueTrueMagnification(long userId) {

        return null;
    }

    @CachePut(value = "user:info", key = "#userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 7, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User putNullUserAllowNullValueFalse(long userId) {

        return null;
    }


    @CachePut(value = "user:info", key = "#userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 100, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User putUserById(long userId) {
        User user = new User();
        user.setUserId(userId);
        user.setAge(311);
        user.setLastName(new String[]{"w", "y", "h"});

        return user;
    }

    @CacheEvict(value = "user:info", key = "#userId")
    public void evictUser(long userId) {

    }

    @CacheEvict(value = "user:info")
    public void evictUserNoKey(long userId, String[] lastName, User user) {

    }

    @CacheEvict(value = "user:info", allEntries = true)
    public void evictAllUser() {
    }


    @Cacheable(value = "user:info:118:3-0-2", key = "#userId", cacheMode = CacheMode.SECOND,
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3,
                    forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User getUserById118DisableFirstCache(long userId) {
        logger.debug("3.0.2 测试禁用一级缓存");
        User user = new User();
        user.setUserId(userId);
        user.setAge(31);
        user.setLastName(new String[]{"w", "y", "h"});
        return user;
    }

    @CachePut(value = "user:info:3-0-2", key = "#userId", cacheMode = CacheMode.SECOND,
            secondaryCache = @SecondaryCache(expireTime = 100, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User putUserByIdDisableFirstCache(long userId) {
        logger.debug("3.0.2 测试禁用一级缓存");
        User user = new User();
        user.setUserId(userId);
        user.setAge(311);
        user.setLastName(new String[]{"w", "y", "h"});

        return user;
    }

    @Cacheable(value = "user:info:3-1-6", key = "#user.userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 9, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User refreshSecondCacheSyncFistCache(User user) {
        logger.debug("测试刷新二级缓存，同步更新一级缓存");
        return user;
    }

    @Cacheable(value = "user:info:3-1-6", key = "361-2",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 7, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User refreshSecondCacheSyncFistCacheNull(User user) {
        logger.debug("测试刷新二级缓存，同步更新一级缓存");
        return user;
    }

    @Cacheable(value = "user:info:3-4-0", key = "#userId", cacheMode = CacheMode.FIRST,
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 7, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public User disableSecondCachePutIfAbsent(long userId) {
        logger.debug("3.4.0 测试禁用二级缓存");
        User user = new User();
        user.setUserId(userId);
        user.setAge(311);
        user.setLastName(new String[]{"w", "y", "h"});
        return user;
    }

    public static final AtomicInteger COUNT = new AtomicInteger();

    @Caching(evict = {@CacheEvict(value = "user:info:caching:3-4-0", key = "'evict'+#userId"), @CacheEvict(value = "user:info:caching", allEntries = true)},
            put = {@CachePut(value = "user:info:caching:3-4-0", key = "'put'+#userId",
                    secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, timeUnit = TimeUnit.SECONDS))},
            cacheable = {@Cacheable(value = "user:info:caching:3-4-0", key = "#userId",
                    firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
                    secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 7, timeUnit = TimeUnit.SECONDS))})
    public User cachingAll(long userId) {
        COUNT.incrementAndGet();
        logger.debug("3.4.0 Caching 所有注解都包含");
        User user = new User();
        user.setUserId(userId);
        user.setAge(311);
        user.setLastName(new String[]{"w", "y", "h"});
        return user;
    }

    @Caching
    public User caching(long userId) {
        COUNT.incrementAndGet();
        logger.debug("3.4.0 Caching");
        User user = new User();
        user.setUserId(userId);
        user.setAge(311);
        user.setLastName(new String[]{"w", "y", "h"});
        return user;
    }

    @Caching(evict = {@CacheEvict(value = "user:info:caching:3-4-0", key = "'evict'+#userId")},
            put = {@CachePut(value = "user:info:caching:3-4-0", key = "'put'+#userId",
                    secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, timeUnit = TimeUnit.SECONDS))})
    public User cachingEvictPut(long userId) {
        COUNT.incrementAndGet();
        logger.debug("3.4.0 EvictPut 删除");
        User user = new User();
        user.setUserId(userId);
        user.setAge(311);
        user.setLastName(new String[]{"w", "y", "h"});
        return user;
    }

    @Caching(evict = {@CacheEvict(value = "user:info:caching:3-4-0", key = "'evict'+#userId")},
            cacheable = {@Cacheable(value = "user:info:caching:3-4-0", key = "#userId",
                    firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
                    secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 7, timeUnit = TimeUnit.SECONDS))})
    public User cachingEvictCacheable(long userId) {
        COUNT.incrementAndGet();
        logger.debug("3.4.0 EvictCacheable 删除");
        User user = new User();
        user.setUserId(userId);
        user.setAge(311);
        user.setLastName(new String[]{"w", "y", "h"});
        return user;
    }

    @Caching(put = {@CachePut(value = "user:info:caching:3-4-0", key = "'put'+#userId")},
            cacheable = {@Cacheable(value = "user:info:caching:3-4-0", key = "#userId",
                    firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
                    secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 7, timeUnit = TimeUnit.SECONDS))})
    public User cachingPutCacheable(long userId) {
        COUNT.incrementAndGet();
        logger.debug("3.4.0 PutCacheable 删除");
        User user = new User();
        user.setUserId(userId);
        user.setAge(311);
        user.setLastName(new String[]{"w", "y", "h"});
        return user;
    }

    @Caching(evict = {@CacheEvict(value = "user:info:caching:evict:3-4-0", key = "#userId")})
    public User cachingEvict(long userId) {
        COUNT.incrementAndGet();
        logger.debug("3.4.0 Evict 删除");
        User user = new User();
        user.setUserId(userId);
        user.setAge(311);
        user.setLastName(new String[]{"w", "y", "h"});
        return user;
    }

    @Caching(put = {@CachePut(value = "user:info:caching:put:3-4-0", key = "#userId",
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, timeUnit = TimeUnit.SECONDS))})
    public User cachingPut(long userId) {
        COUNT.incrementAndGet();
        logger.debug("3.4.0 Put 删除");
        User user = new User();
        user.setUserId(userId);
        user.setAge(311);
        user.setLastName(new String[]{"w", "y", "h"});
        return user;
    }

    @Caching(cacheable = {@Cacheable(value = "user:info:caching:cacheable:3-4-0", key = "#userId",
            firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
            secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 7, timeUnit = TimeUnit.SECONDS))})
    public User cachingCacheable(long userId) {
        COUNT.incrementAndGet();
        logger.debug("3.4.0 Cacheable 删除");
        User user = new User();
        user.setUserId(userId);
        user.setAge(311);
        user.setLastName(new String[]{"w", "y", "h"});
        return user;
    }
}
