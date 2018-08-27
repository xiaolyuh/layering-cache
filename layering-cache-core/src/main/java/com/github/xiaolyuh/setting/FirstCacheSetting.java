package com.github.xiaolyuh.setting;

import com.github.xiaolyuh.support.ExpireMode;

import java.util.concurrent.TimeUnit;

/**
 * 一级缓存配置项
 *
 * @author yuhao.wang
 */
public class FirstCacheSetting {

    /**
     * 缓存初始Size
     */
    private int initialCapacity;

    /**
     * 缓存最大Size
     */
    private int maximumSize;

    /**
     * 缓存有效时间
     */
    private int expireTime;

    /**
     * 缓存时间单位
     */
    private TimeUnit timeUnit;

    /**
     * 缓存失效模式{@link ExpireMode}
     */
    private ExpireMode expireMode;

    /**
     * 是否允许存NULL值
     */
    private boolean allowNullValues;

    /**
     * @param initialCapacity 缓存初始Size
     * @param maximumSize     缓存最大Size
     * @param expireTime      缓存有效时间
     * @param timeUnit        缓存时间单位 {@link TimeUnit}
     * @param expireMode      缓存失效模式{@link ExpireMode}
     */
    public FirstCacheSetting(int initialCapacity, int maximumSize, int expireTime, TimeUnit timeUnit, ExpireMode expireMode) {
        this.initialCapacity = initialCapacity;
        this.maximumSize = maximumSize;
        this.expireTime = expireTime;
        this.timeUnit = timeUnit;
        this.expireMode = expireMode;
        this.allowNullValues = true;
    }

    public int getInitialCapacity() {
        return initialCapacity;
    }

    public int getMaximumSize() {
        return maximumSize;
    }

    public int getExpireTime() {
        return expireTime;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public ExpireMode getExpireMode() {
        return expireMode;
    }

    public boolean isAllowNullValues() {
        return allowNullValues;
    }
}
