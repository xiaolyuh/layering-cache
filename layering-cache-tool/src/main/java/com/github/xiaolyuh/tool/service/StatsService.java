package com.github.xiaolyuh.tool.service;

import com.alibaba.fastjson.JSON;
import com.github.xiaolyuh.cache.Cache;
import com.github.xiaolyuh.cache.LayeringCache;
import com.github.xiaolyuh.manager.AbstractCacheManager;
import com.github.xiaolyuh.manager.CacheManager;
import com.github.xiaolyuh.setting.LayeringCacheSetting;
import com.github.xiaolyuh.support.Lock;
import com.github.xiaolyuh.tool.servlet.LayeringCacheServlet;
import com.github.xiaolyuh.tool.support.CacheStats;
import com.github.xiaolyuh.tool.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 统计服务
 *
 * @author yuhao.wang3
 */
public class StatsService {
    private static Logger logger = LoggerFactory.getLogger(LayeringCacheServlet.class);

    /**
     * 缓存统计数据前缀
     */
    private static final String CACHE_STATS_KEY_PREFIX = "layering-cache:cache_stats:xiaolyuh:";

    /**
     * 定时任务线程池
     */
    private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(50);

    /**
     * 数字格式化
     */
    private static DecimalFormat df = new DecimalFormat("#.00");

    /**
     * 获取缓存统计list
     *
     * @param cacheNameParam 缓存名称
     */
    public List<CacheStats> listCacheStats(RedisTemplate<String, Object> redisTemplate, String cacheNameParam) throws IOException {
        logger.debug("获取缓存统计数据");

        List<CacheStats> statsList = new ArrayList<>();
        Set<AbstractCacheManager> cacheManagers = AbstractCacheManager.getCacheManager();

        for (AbstractCacheManager cacheManager : cacheManagers) {
            Collection<String> cacheNames = ((CacheManager) cacheManager).getCacheNames();
            for (String cacheName : cacheNames) {
                if (StringUtils.isNotBlank(cacheNameParam) && !cacheName.startsWith(cacheNameParam)) {
                    continue;
                }

                // 获取Cache
                Collection<Cache> caches = cacheManager.getCache(cacheName);
                for (Cache cache : caches) {
                    LayeringCache layeringCache = (LayeringCache) cache;
                    LayeringCacheSetting layeringCacheSetting = layeringCache.getLayeringCacheSetting();
                    // 加锁并增量缓存统计数据，缓存key=固定前缀 + 缓存名称加 + 内部缓存名
                    String redisKey = CACHE_STATS_KEY_PREFIX + cacheName + layeringCacheSetting.getInternalKey();
                    CacheStats cacheStats = (CacheStats) redisTemplate.opsForValue().get(redisKey);
                    if (!Objects.isNull(cacheStats)) {
                        statsList.add(cacheStats);
                    }
                }
            }
        }

        return statsList.stream().sorted(Comparator.comparing(CacheStats::getHitRate)).collect(Collectors.toList());
    }

    /**
     * 同步缓存统计list
     */
    public void syncCacheStats(RedisTemplate<String, Object> redisTemplate) {
        // 清空统计数据
        resetCacheStat(redisTemplate);
        executor.scheduleWithFixedDelay(() -> {
            logger.debug("执行缓存统计数据采集定时任务");
            Set<AbstractCacheManager> cacheManagers = AbstractCacheManager.getCacheManager();
            for (AbstractCacheManager abstractCacheManager : cacheManagers) {
                // 获取CacheManager
                CacheManager cacheManager = ((CacheManager) abstractCacheManager);
                Collection<String> cacheNames = cacheManager.getCacheNames();
                for (String cacheName : cacheNames) {
                    // 获取Cache
                    Collection<Cache> caches = cacheManager.getCache(cacheName);
                    for (Cache cache : caches) {
                        LayeringCache layeringCache = (LayeringCache) cache;
                        LayeringCacheSetting layeringCacheSetting = layeringCache.getLayeringCacheSetting();
                        // 加锁并增量缓存统计数据，缓存key=固定前缀 + 缓存名称加 + 内部缓存名
                        String redisKey = CACHE_STATS_KEY_PREFIX + cacheName + layeringCacheSetting.getInternalKey();
                        Lock lock = new Lock(redisTemplate, redisKey, 60, 5000);
                        try {
                            if (lock.tryLock()) {
                                CacheStats cacheStats = (CacheStats) redisTemplate.opsForValue().get(redisKey);
                                if (Objects.isNull(cacheStats)) {
                                    cacheStats = new CacheStats();
                                }

                                // 设置缓存唯一标示
                                cacheStats.setCacheName(cacheName);
                                cacheStats.setInternalKey(layeringCacheSetting.getInternalKey());

                                cacheStats.setDepict(layeringCacheSetting.getDepict());
                                // 设置缓存配置信息
                                cacheStats.setLayeringCacheSetting(layeringCacheSetting);

                                // 设置缓存统计数据
                                com.github.xiaolyuh.stats.CacheStats layeringCacheStats = layeringCache.getCacheStats();
                                com.github.xiaolyuh.stats.CacheStats firstCacheStats = layeringCache.getFirstCache().getCacheStats();
                                com.github.xiaolyuh.stats.CacheStats secondCacheStats = layeringCache.getSecondCache().getCacheStats();

                                cacheStats.setRequestCount(cacheStats.getRequestCount() + layeringCacheStats.getAndResetCacheRequestCount());
                                cacheStats.setMissCount(cacheStats.getMissCount() + layeringCacheStats.getAndResetCachedMethodRequestCount());
                                cacheStats.setTotalLoadTime(cacheStats.getTotalLoadTime() + layeringCacheStats.getAndResetCachedMethodRequestTime());
                                cacheStats.setHitRate((cacheStats.getRequestCount() - cacheStats.getMissCount()) / (double) cacheStats.getRequestCount() * 100);

                                cacheStats.setFirstCacheRequestCount(cacheStats.getFirstCacheRequestCount() + firstCacheStats.getAndResetCacheRequestCount());
                                cacheStats.setFirstCacheMissCount(cacheStats.getFirstCacheMissCount() + firstCacheStats.getAndResetCachedMethodRequestCount());

                                cacheStats.setSecondCacheRequestCount(cacheStats.getSecondCacheRequestCount() + secondCacheStats.getAndResetCacheRequestCount());
                                cacheStats.setSecondCacheMissCount(cacheStats.getSecondCacheMissCount() + secondCacheStats.getAndResetCachedMethodRequestCount());

                                // 将缓存统计数据写到redis
                                redisTemplate.opsForValue().set(redisKey, cacheStats, 1, TimeUnit.HOURS);

                                logger.info("Layering Cache 统计信息：{}", JSON.toJSONString(cacheStats));
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            }

            //  初始时间间隔是1分
        }, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * 关闭线程池
     */
    public void shutdownExecutor() {
        executor.shutdown();
    }

    /**
     * 重置缓存统计数据
     */
    public void resetCacheStat(RedisTemplate<String, Object> redisTemplate) {
        Set<String> keys = redisTemplate.keys(CACHE_STATS_KEY_PREFIX + "*");
        redisTemplate.delete(keys);
    }

    /**
     * 删除缓存
     *
     * @param cacheName   缓存名称
     * @param internalKey 内部缓存名，由[一级缓存有效时间-二级缓存有效时间-二级缓存自动刷新时间]组成
     * @param key         key，可以为NULL，如果是NULL则清空缓存
     */
    public void deleteCache(String cacheName, String internalKey, String key) {
        if (StringUtils.isBlank(cacheName) || StringUtils.isBlank(internalKey)) {
            return;
        }

        Set<AbstractCacheManager> cacheManagers = AbstractCacheManager.getCacheManager();
        if (StringUtils.isBlank(key)) {
            // 清空缓存
            for (AbstractCacheManager cacheManager : cacheManagers) {
                LayeringCacheSetting layeringCacheSetting = new LayeringCacheSetting();
                layeringCacheSetting.setInternalKey(internalKey);
                Cache cache = cacheManager.getCache(cacheName, layeringCacheSetting);
                cache.clear();
            }

            return;
        }

        // 删除指定key
        for (AbstractCacheManager cacheManager : cacheManagers) {
            LayeringCacheSetting layeringCacheSetting = new LayeringCacheSetting();
            layeringCacheSetting.setInternalKey(internalKey);
            Cache cache = cacheManager.getCache(cacheName, layeringCacheSetting);
            cache.evict(key);
        }
    }
}
