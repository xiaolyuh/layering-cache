package com.github.xiaolyuh.stats.extend;

import com.github.xiaolyuh.stats.CacheStatsInfo;

import java.util.List;

/**
 * 缓存统计信息上报扩展类
 *
 * @author olafwang
 */
public interface CacheStatsReportService {

    /**
     * 上报缓存统计信息
     *
     * @param cacheStatsInfos {@link CacheStatsInfo}
     */
    void reportCacheStats(List<CacheStatsInfo> cacheStatsInfos);
}