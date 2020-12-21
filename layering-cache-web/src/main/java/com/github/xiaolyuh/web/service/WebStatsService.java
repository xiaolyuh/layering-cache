package com.github.xiaolyuh.web.service;

import com.github.xiaolyuh.redis.clinet.RedisClient;
import com.github.xiaolyuh.stats.CacheStatsInfo;
import com.github.xiaolyuh.stats.StatsService;
import com.github.xiaolyuh.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 统计服务
 *
 * @author yuhao.wang3
 */
@Service
public class WebStatsService {
    private static Logger logger = LoggerFactory.getLogger(WebStatsService.class);

    /**
     * 获取缓存统计list
     *
     * @param redisClient    redisClient
     * @param cacheNameParam 缓存名称
     * @return List&lt;CacheStatsInfo&gt;
     */
    public List<CacheStatsInfo> listCacheStats(RedisClient redisClient, String cacheNameParam) {
        logger.debug("获取缓存统计数据");

        Set<String> layeringCacheKeys = redisClient.scan(StatsService.CACHE_STATS_KEY_PREFIX + "*");
        if (CollectionUtils.isEmpty(layeringCacheKeys)) {
            return Collections.emptyList();
        }
        // 遍历找出对应统计数据
        List<CacheStatsInfo> statsList = new ArrayList<>();
        for (String key : layeringCacheKeys) {
            if (StringUtils.isNotBlank(cacheNameParam) && !key.startsWith(StatsService.CACHE_STATS_KEY_PREFIX + cacheNameParam)) {
                continue;
            }

            try {
                CacheStatsInfo cacheStats = redisClient.get(key, CacheStatsInfo.class);
                if (!Objects.isNull(cacheStats)) {
                    statsList.add(cacheStats);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return statsList.stream().sorted(Comparator.comparing(CacheStatsInfo::getHitRate)).collect(Collectors.toList());
    }


    /**
     * 重置缓存统计数据
     */
    public void resetCacheStat(RedisClient redisClient) {
        Set<String> layeringCacheKeys = redisClient.scan(StatsService.CACHE_STATS_KEY_PREFIX + "*");

        for (String key : layeringCacheKeys) {
            resetCacheStat(redisClient, key);
        }
    }

    /**
     * 重置缓存统计数据
     *
     * @param redisKey redisKey
     */
    public void resetCacheStat(RedisClient redisClient, String redisKey) {
        try {
            CacheStatsInfo cacheStats = redisClient.get(redisKey, CacheStatsInfo.class);
            if (Objects.nonNull(cacheStats)) {
                cacheStats.clearStatsInfo();
                // 将缓存统计数据写到redis
                redisClient.set(redisKey, cacheStats, 24, TimeUnit.HOURS);
            }
        } catch (Exception e) {
            redisClient.delete(redisKey);
        }
    }

}
