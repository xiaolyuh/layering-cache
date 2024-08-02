package com.github.xiaolyuh.manager;

import com.github.xiaolyuh.cache.Cache;
import com.github.xiaolyuh.listener.RedisMessageListener;
import com.github.xiaolyuh.listener.RedisMessagePullTask;
import com.github.xiaolyuh.redis.clinet.RedisClient;
import com.github.xiaolyuh.setting.LayeringCacheSetting;
import com.github.xiaolyuh.stats.StatsService;
import com.github.xiaolyuh.stats.extend.CacheStatsReportService;
import com.github.xiaolyuh.stats.extend.DefaultCacheStatsReportServiceImpl;
import com.github.xiaolyuh.util.BeanFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;

/**
 * 公共的抽象 {@link CacheManager} 的实现.
 *
 * @author yuhao.wang3
 */
public abstract class AbstractCacheManager implements CacheManager, InitializingBean, DisposableBean {
    private Logger logger = LoggerFactory.getLogger(AbstractCacheManager.class);

    /**
     * 缓存容器
     * 外层key是cache_name
     * 里层key是[一级缓存有效时间-二级缓存有效时间]
     */
    private final ConcurrentMap<String, ConcurrentMap<String, Cache>> cacheContainer = new ConcurrentHashMap<>(16);

    /**
     * 缓存名称容器
     */
    private volatile Set<String> cacheNames = new LinkedHashSet<>();

    /**
     * CacheManager 容器
     */
    static Set<AbstractCacheManager> cacheManagers = new LinkedHashSet<>();

    /**
     * 上报缓存统计信息
     */
    CacheStatsReportService cacheStatsReportService = new DefaultCacheStatsReportServiceImpl();

    /**
     * 是否开启统计
     */
    private boolean stats = true;

    /**
     * redis 客户端
     */
    RedisClient redisClient;

    public static Set<AbstractCacheManager> getCacheManager() {
        return cacheManagers;
    }

    @Override
    public Collection<Cache> getCache(String name) {
        ConcurrentMap<String, Cache> cacheMap = this.cacheContainer.get(name);
        if (CollectionUtils.isEmpty(cacheMap)) {
            return Collections.emptyList();
        }
        return cacheMap.values();
    }

    // Lazy cache initialization on access
    @Override
    public Cache getCache(String name, LayeringCacheSetting layeringCacheSetting) {
        // 第一次获取缓存Cache，如果有直接返回,如果没有加锁往容器里里面放Cache
        ConcurrentMap<String, Cache> cacheMap = this.cacheContainer.get(name);
        if (!CollectionUtils.isEmpty(cacheMap)) {
            Cache cache = cacheMap.get(layeringCacheSetting.getInternalKey());
            if (cache != null) {
                return cache;
            }
        }

        // 第二次获取缓存Cache，加锁往容器里里面放Cache
        synchronized (this.cacheContainer) {
            cacheMap = this.cacheContainer.get(name);
            if (!CollectionUtils.isEmpty(cacheMap)) {
                // 从容器中获取缓存
                Cache cache = cacheMap.get(layeringCacheSetting.getInternalKey());
                if (cache != null) {
                    return cache;
                }
            } else {
                cacheMap = new ConcurrentHashMap<>(16);
                cacheContainer.put(name, cacheMap);
                // 更新缓存名称
                updateCacheNames(name);
            }

            // 新建一个Cache对象
            Cache cache = getMissingCache(name, layeringCacheSetting);
            if (cache != null) {
                // 装饰Cache对象
                cache = decorateCache(cache);
                // 将新的Cache对象放到容器
                cacheMap.put(layeringCacheSetting.getInternalKey(), cache);
                if (cacheMap.size() > 1) {
                    logger.warn("缓存名称为 {} 的缓存,存在两个不同的过期时间配置，请一定注意保证缓存的key唯一性，否则会出现缓存过期时间错乱的情况", name);
                }
            }

            return cache;
        }
    }

    @Override
    public Collection<String> getCacheNames() {
        return this.cacheNames;
    }

    /**
     * 更新缓存名称容器
     *
     * @param name 需要添加的缓存名称
     */
    private void updateCacheNames(String name) {
        cacheNames.add(name);
    }

    /**
     * 获取Cache对象的装饰示例
     *
     * @param cache 需要添加到CacheManager的Cache实例
     * @return 装饰过后的Cache实例
     */
    protected Cache decorateCache(Cache cache) {
        return cache;
    }

    /**
     * 根据缓存名称在CacheManager中没有找到对应Cache时，通过该方法新建一个对应的Cache实例
     *
     * @param name                 缓存名称
     * @param layeringCacheSetting 缓存配置
     * @return {@link Cache}
     */
    protected abstract Cache getMissingCache(String name, LayeringCacheSetting layeringCacheSetting);

    /**
     * 获取缓存容器
     *
     * @return 返回缓存容器
     */
    public ConcurrentMap<String, ConcurrentMap<String, Cache>> getCacheContainer() {
        return cacheContainer;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //  redis pull 消息任务
        BeanFactory.getBean(RedisMessagePullTask.class).init(this);
        //  redis pub/sub 监听器
        BeanFactory.getBean(RedisMessageListener.class).init(this);

        BeanFactory.getBean(StatsService.class).setCacheManager(this);
        if (getStats()) {
            // 采集缓存命中率数据
            BeanFactory.getBean(StatsService.class).syncCacheStats();
        }
    }

    @Override
    public void destroy() throws Exception {
        BeanFactory.getBean(StatsService.class).shutdownExecutor();
    }

    public boolean getStats() {
        return stats;
    }

    public void setStats(boolean stats) {
        this.stats = stats;
    }

    public CacheStatsReportService getCacheStatsReportService() {
        return cacheStatsReportService;
    }

    public void setCacheStatsReportService(CacheStatsReportService cacheStatsReportService) {
        this.cacheStatsReportService = cacheStatsReportService;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public RedisClient getRedisClient() {
        return redisClient;
    }

    public void setRedisClient(RedisClient redisClient) {
        this.redisClient = redisClient;
    }
}
