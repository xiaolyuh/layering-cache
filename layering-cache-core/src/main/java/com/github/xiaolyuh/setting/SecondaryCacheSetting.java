package com.github.xiaolyuh.setting;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * 二级缓存配置项
 *
 * @author yuhao.wang
 */
public class SecondaryCacheSetting implements Serializable {
    /**
     * 缓存有效时间
     */
    private long expiration;

    /**
     * 缓存主动在失效前强制刷新缓存的时间
     */
    private long preloadTime;

    /**
     * 时间单位 {@link TimeUnit}
     */
    private TimeUnit timeUnit;

    /**
     * 是否强制刷新（走数据库），默认是false
     */
    private boolean forceRefresh = false;

    /**
     * 是否使用缓存名称作为 redis key 前缀
     */
    private boolean usePrefix;

    public SecondaryCacheSetting() {
    }

    /**
     * @param expiration   缓存有效时间
     * @param preloadTime  缓存刷新时间
     * @param timeUnit     时间单位 {@link TimeUnit}
     * @param forceRefresh 是否强制刷新
     */
    public SecondaryCacheSetting(long expiration, long preloadTime, TimeUnit timeUnit, boolean forceRefresh) {
        this.expiration = expiration;
        this.preloadTime = preloadTime;
        this.timeUnit = timeUnit;
        this.forceRefresh = forceRefresh;
        this.usePrefix = true;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public long getPreloadTime() {
        return preloadTime;
    }

    public void setPreloadTime(long preloadTime) {
        this.preloadTime = preloadTime;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public boolean isForceRefresh() {
        return forceRefresh;
    }

    public void setForceRefresh(boolean forceRefresh) {
        this.forceRefresh = forceRefresh;
    }

    public boolean isUsePrefix() {
        return usePrefix;
    }

    public void setUsePrefix(boolean usePrefix) {
        this.usePrefix = usePrefix;
    }
}
