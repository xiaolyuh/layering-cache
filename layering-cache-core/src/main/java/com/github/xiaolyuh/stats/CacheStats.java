package com.github.xiaolyuh.stats;

import java.io.Serializable;
import java.util.concurrent.atomic.LongAdder;

/**
 * 缓存统计信息实体类
 *
 * @author yuhao.wang3
 */
public final class CacheStats implements Serializable {
    /**
     * 请求缓存总数
     */
    private LongAdder cacheRequestCount;

    /**
     * 请求被缓存方法总数
     */
    private LongAdder cachedMethodRequestCount;

    /**
     * 请求被缓存方法总耗时(毫秒)
     */
    private LongAdder cachedMethodRequestTime;

    public CacheStats() {
        this.cacheRequestCount = new LongAdder();
        this.cachedMethodRequestCount = new LongAdder();
        this.cachedMethodRequestTime = new LongAdder();
    }

    /**
     * 自增请求缓存总数
     *
     * @param add 自增数量
     */
    public void addCacheRequestCount(long add) {
        cacheRequestCount.add(add);
    }

    /**
     * 自增请求被缓存方法总数
     *
     * @param add 自增数量
     */
    public void addCachedMethodRequestCount(long add) {
        cachedMethodRequestCount.add(add);
    }

    /**
     * 自增请求被缓存方法总耗时(毫秒)
     *
     * @param time 自增数量
     */
    public void addCachedMethodRequestTime(long time) {
        cachedMethodRequestTime.add(time);
    }

    public LongAdder getCacheRequestCount() {
        return cacheRequestCount;
    }

    public void setCacheRequestCount(LongAdder cacheRequestCount) {
        this.cacheRequestCount = cacheRequestCount;
    }

    public LongAdder getCachedMethodRequestCount() {
        return cachedMethodRequestCount;
    }

    public void setCachedMethodRequestCount(LongAdder cachedMethodRequestCount) {
        this.cachedMethodRequestCount = cachedMethodRequestCount;
    }

    public LongAdder getCachedMethodRequestTime() {
        return cachedMethodRequestTime;
    }

    public void setCachedMethodRequestTime(LongAdder cachedMethodRequestTime) {
        this.cachedMethodRequestTime = cachedMethodRequestTime;
    }


    public long getAndResetCacheRequestCount() {
        long lodValue = cacheRequestCount.longValue();
        cacheRequestCount.reset();
        return lodValue;
    }

    public long getAndResetCachedMethodRequestCount() {
        long lodValue = cachedMethodRequestCount.longValue();
        cachedMethodRequestCount.reset();
        return lodValue;
    }

    public long getAndResetCachedMethodRequestTime() {
        long lodValue = cachedMethodRequestTime.longValue();
        cachedMethodRequestTime.reset();
        return lodValue;
    }

}