package com.github.xiaolyuh.web.vo;

import com.github.xiaolyuh.stats.CacheStatsInfo;

import java.util.List;

/**
 * @author olafwang
 * @since 2020/9/25 3:07 下午
 */
public class CacheStatsInfoVo {
    List<CacheStatsInfo> cacheStats;

    long time;

    public List<CacheStatsInfo> getCacheStats() {
        return cacheStats;
    }

    public void setCacheStats(List<CacheStatsInfo> cacheStats) {
        this.cacheStats = cacheStats;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
