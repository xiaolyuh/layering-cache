package com.github.xiaolyuh.tool.support;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.ArrayList;
import java.util.List;

/**
 * Servlet 初始化数据
 *
 * @author yuhao.wang3
 */
public class InitServletData {

    public static final String PARAM_NAME_USERNAME = "loginUsername";
    public static final String PARAM_NAME_PASSWORD = "loginPassword";
    public static final String PARAM_NAME_ALLOW = "allow";
    public static final String PARAM_NAME_DENY = "deny";
    public static final String PARAM_NAME_ENABLE_UPDATE = "enableUpdate";

    public static final String RESOURCE_PATH = "http/resources";
    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 是否有更新数据权限
     */
    private Boolean enableUpdate;

    /**
     * 白名单
     */
    private List<IPRange> allowList = new ArrayList<>();

    /**
     * 黑名单（优先级高于白名单）
     */
    private List<IPRange> denyList = new ArrayList<>();

    /**
     * 采集缓存命中数据的时间间隔，至少5分钟（单位分钟）
     */
    private long syncCacheStatsDelay = 5;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<IPRange> getAllowList() {
        return allowList;
    }

    public void setAllowList(List<IPRange> allowList) {
        this.allowList = allowList;
    }

    public List<IPRange> getDenyList() {
        return denyList;
    }

    public void setDenyList(List<IPRange> denyList) {
        this.denyList = denyList;
    }

    public String getResourcePath() {
        return RESOURCE_PATH;
    }

    public long getSyncCacheStatsDelay() {
        return syncCacheStatsDelay;
    }

    public void setSyncCacheStatsDelay(long syncCacheStatsDelay) {
        this.syncCacheStatsDelay = syncCacheStatsDelay;
    }

    public Boolean getEnableUpdate() {
        return enableUpdate;
    }

    public void setEnableUpdate(Boolean enableUpdate) {
        this.enableUpdate = enableUpdate;
    }
}
