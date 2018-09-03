package com.github.xiaolyuh.util.service;

import com.alibaba.fastjson.JSON;
import com.github.xiaolyuh.cache.Cache;
import com.github.xiaolyuh.cache.LayeringCache;
import com.github.xiaolyuh.manager.AbstractCacheManager;
import com.github.xiaolyuh.manager.CacheManager;
import com.github.xiaolyuh.setting.LayeringCacheSetting;
import com.github.xiaolyuh.support.Lock;
import com.github.xiaolyuh.util.servlet.LayeringCacheServlet;
import com.github.xiaolyuh.util.support.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 统计服务
 *
 * @author yuhao.wang3
 */
public class StatsService {
    private static Logger logger = LoggerFactory.getLogger(LayeringCacheServlet.class);

    private static final String CACHE_STATS = ":cache_stats";

    /**
     * 定时任务线程池
     */
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(50);

    /**
     * 获取缓存统计list
     *
     * @param response {@link HttpServletResponse}
     */
    public void listCacheStats(HttpServletResponse response) throws IOException {
        logger.debug("获取缓存统计数据");
        List<CacheStats> statsList = new ArrayList<>();
        Set<AbstractCacheManager> cacheManagers = AbstractCacheManager.getCacheManager();

        for (AbstractCacheManager cacheManager : cacheManagers) {
            RedisTemplate<String, Object> redisTemplate = cacheManager.getRedisTemplate();
            Collection<String> cacheNames = ((CacheManager) cacheManager).getCacheNames();
            for (String cacheName : cacheNames) {
                // 获取Cache
                Collection<Cache> caches = cacheManager.getCache(cacheName);
                for (Cache cache : caches) {
                    LayeringCache layeringCache = (LayeringCache) cache;
                    LayeringCacheSetting layeringCacheSetting = layeringCache.getLayeringCacheSetting();
                    // 加锁并增量缓存统计数据，缓存key=缓存名称加 + 内部缓存名 + 固定后缀
                    String redisKey = cacheName + layeringCacheSetting.getInternalKey() + CACHE_STATS;
                    CacheStats cacheStats = (CacheStats) redisTemplate.opsForValue().get(redisKey);
                    if (!Objects.isNull(cacheStats)) {
                        statsList.add(cacheStats);
                    }
                }
            }
        }

        response.getWriter().write(JSON.toJSONString(statsList));
    }

    /**
     * 同步缓存统计list
     */
    public void syncCacheStats() {
        executor.scheduleWithFixedDelay(() -> {
            logger.debug("执行缓存统计数据采集定时任务");
            Set<AbstractCacheManager> cacheManagers = AbstractCacheManager.getCacheManager();
            for (AbstractCacheManager abstractCacheManager : cacheManagers) {
                // 获取CacheManager
                CacheManager cacheManager = ((CacheManager) abstractCacheManager);
                RedisTemplate<String, Object> redisTemplate = cacheManager.getRedisTemplate();
                Collection<String> cacheNames = cacheManager.getCacheNames();
                for (String cacheName : cacheNames) {
                    // 获取Cache
                    Collection<Cache> caches = cacheManager.getCache(cacheName);
                    for (Cache cache : caches) {
                        LayeringCache layeringCache = (LayeringCache) cache;
                        LayeringCacheSetting layeringCacheSetting = layeringCache.getLayeringCacheSetting();
                        // 加锁并增量缓存统计数据，缓存key=缓存名称加 + 内部缓存名 + 固定后缀
                        String redisKey = cacheName + layeringCacheSetting.getInternalKey() + CACHE_STATS;
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
                                cacheStats.setFirstCacheRequestCount(cacheStats.getFirstCacheRequestCount() + firstCacheStats.getAndResetCacheRequestCount());
                                cacheStats.setFirstCacheMissCount(cacheStats.getFirstCacheMissCount() + firstCacheStats.getAndResetCachedMethodRequestCount());
                                cacheStats.setSecondCacheRequestCount(cacheStats.getSecondCacheRequestCount() + secondCacheStats.getAndResetCacheRequestCount());
                                cacheStats.setSecondCacheMissCount(cacheStats.getSecondCacheMissCount() + secondCacheStats.getAndResetCachedMethodRequestCount());

                                // 将缓存统计数据写到redis
                                redisTemplate.opsForValue().set(redisKey, cacheStats, 1, TimeUnit.HOURS);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            }

            //  初始时间间隔是5分
        }, 5, 5, TimeUnit.MINUTES);
    }

    /**
     * 关闭线程池
     */
    public void shutdownExecutor() {
        executor.shutdown();
    }
}
