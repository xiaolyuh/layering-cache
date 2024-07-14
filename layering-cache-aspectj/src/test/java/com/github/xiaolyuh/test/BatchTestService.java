package com.github.xiaolyuh.test;

import com.github.xiaolyuh.annotation.BatchCacheable;
import com.github.xiaolyuh.annotation.Cacheable;
import com.github.xiaolyuh.annotation.FirstCache;
import com.github.xiaolyuh.annotation.SecondaryCache;
import com.github.xiaolyuh.domain.User;
import com.github.xiaolyuh.support.CacheMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BatchTestService
 *
 * @author heyirui
 * @date 2024/7/11
 */
public class BatchTestService {
    Logger logger = LoggerFactory.getLogger(getClass());

    @BatchCacheable(value = "user:info", keys = "#users.![userId]", depict = "用户信息缓存",
        firstCache = @FirstCache(expireTime = 10, timeUnit = TimeUnit.SECONDS),
        secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3,timeUnit = TimeUnit.SECONDS))
    public List<User> getUserByIds(List<User> users) {
        logger.debug("测试正常配置的批量缓存方法");

        ArrayList<User> res = new ArrayList<>();
        for (int i = 0; i < Math.min(users.size(),2) ; i++) {
            User user = new User();
            user.setUserId(users.get(i).getUserId());
            user.setAge(i);
            user.setLastName(new String[]{"w", "y", Integer.toString(i)});
            res.add(user);
        }
        return res;
    }

    @BatchCacheable(value = "user:info", keys = "#users.![userId]", depict = "用户信息缓存",
        firstCache = @FirstCache(expireTime = 10, timeUnit = TimeUnit.SECONDS),
        secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3,timeUnit = TimeUnit.SECONDS))
    public List<User> getUserByIdsReturnNone(List<User> users) {
        logger.debug("测试批量缓存方法返回空数组");
        return new ArrayList<>();
    }

    @BatchCacheable(value = "user:info:01", keys = "#users.![userId]", cacheMode = CacheMode.FIRST,
        firstCache = @FirstCache(expireTime = 10, timeUnit = TimeUnit.SECONDS),
        secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 7, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public List<User> getUserByIdDisableFirstCache(List<User> users) {
        logger.debug("user:info:01 测试禁用二级缓存");
        ArrayList<User> res = new ArrayList<>();
        for (int i = 0; i < Math.min(users.size(),2) ; i++) {
            User user = new User();
            user.setUserId(users.get(i).getUserId());
            user.setAge(i);
            user.setLastName(new String[]{"w", "y", Integer.toString(i)});
            res.add(user);
        }
        return res;
    }

    @BatchCacheable(value = "user:info:02", keys = "#users.![userId]", cacheMode = CacheMode.SECOND,
        firstCache = @FirstCache(expireTime = 10, timeUnit = TimeUnit.SECONDS),
        secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public List<User> getUserByIdDisableSecondCache(List<User> users) {
        logger.debug("user:info:01 测试禁用二级缓存");
        ArrayList<User> res = new ArrayList<>();
        for (int i = 0; i < Math.min(users.size(),2) ; i++) {
            User user = new User();
            user.setUserId(users.get(i).getUserId());
            user.setAge(i);
            user.setLastName(new String[]{"w", "y", Integer.toString(i)});
            res.add(user);
        }
        return res;
    }


    @BatchCacheable(value = "user:info:all", keys = "#users.![userId]",
        firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
        secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 9, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
    public List<User> batchRefreshSecondCacheSyncFistCache(List<User> users) {
        logger.debug("测试刷新二级缓存，同步更新一级缓存");
        return users;
    }
}
