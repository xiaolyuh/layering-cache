package com.github.xiaolyuh.web.controller;

import com.github.xiaolyuh.listener.RedisPubSubMessage;
import com.github.xiaolyuh.listener.RedisPubSubMessageType;
import com.github.xiaolyuh.listener.RedisPublisher;
import com.github.xiaolyuh.util.StringUtils;
import com.github.xiaolyuh.util.ThreadTaskUtils;
import com.github.xiaolyuh.web.service.WebStatsService;
import com.github.xiaolyuh.web.utils.Result;
import com.github.xiaolyuh.web.vo.CacheStatsInfoVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/cache-stats")
public class CacheController {
    Logger logger = LoggerFactory.getLogger(CacheController.class);
    @Autowired
    private WebStatsService statsService;

    Map<String, CacheStatsInfoVo> cache = new ConcurrentHashMap<>();

    /**
     * 缓存统计列表
     */
    @RequestMapping("list")
    @ResponseBody
    public Result list(String redisClient, String cacheName) {
        if (StringUtils.isBlank(redisClient)) {
            return Result.error("未选择redis版本");
        }
        try {
            CacheStatsInfoVo statsInfoVo = cache.computeIfAbsent(redisClient + cacheName, key -> getList(redisClient, cacheName));
            refreshList(redisClient, cacheName);
            statsInfoVo.getCacheStats().forEach(cacheStatsInfo -> {
                System.out.println(cacheStatsInfo.getHitRate());
            });
            return Result.success(statsInfoVo.getCacheStats());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("配置redis失败" + e.getMessage());
        }
    }

    private CacheStatsInfoVo getList(String redisClient, String cacheName) {
        CacheStatsInfoVo statsInfoVo = new CacheStatsInfoVo();
        statsInfoVo.setTime(System.currentTimeMillis());
        statsInfoVo.setCacheStats(statsService.listCacheStats(RedisController.redisClientMap.get(redisClient), cacheName));
        return statsInfoVo;
    }

    private void refreshList(String redisClient, String cacheName) {
        String key = redisClient + cacheName;
        ThreadTaskUtils.run(() -> {
            CacheStatsInfoVo statsInfoVo = cache.get(key);
            if (Objects.isNull(statsInfoVo)) {
                cache.put(key, getList(redisClient, cacheName));
                return;
            }
            // 一分钟内缓存
            if ((System.currentTimeMillis() - statsInfoVo.getTime()) > 1000 * 60) {
                cache.put(key, getList(redisClient, cacheName));
            }
        });
    }

    /**
     * 重置缓存统计数据
     */
    @RequestMapping("reset-stats")
    @ResponseBody
    public Result resetStats(String redisClient) {
        try {
            statsService.resetCacheStat(RedisController.redisClientMap.get(redisClient));
            return Result.success();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("配置redis失败" + e.getMessage());
        }
    }

    /**
     * 删除缓存统计
     */
    @RequestMapping("delete-cache")
    @ResponseBody
    public Result deleteCache(String redisClient, String cacheName, String internalKey, String key, String nameSpace) {
        try {
            // 清除一级缓存需要用到redis的订阅/发布模式，否则集群中其他服服务器节点的一级缓存数据无法删除
            RedisPubSubMessage message = new RedisPubSubMessage();
            message.setCacheName(cacheName);
            message.setSource(RedisPubSubMessage.SOURCE);
            if (StringUtils.isBlank(key)) {
                message.setMessageType(RedisPubSubMessageType.CLEAR);
            } else {
                message.setKey(key);
                message.setMessageType(RedisPubSubMessageType.EVICT);
            }

            // 发布消息
            RedisPublisher.publisher(RedisController.redisClientMap.get(redisClient), message, nameSpace);

            return Result.success();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("配置redis失败" + e.getMessage());
        }
    }

}