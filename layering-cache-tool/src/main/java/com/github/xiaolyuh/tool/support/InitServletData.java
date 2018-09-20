package com.github.xiaolyuh.tool.support;

import java.util.ArrayList;
import java.util.List;

/**
 * Servlet 初始化数据
 *
 * @author yuhao.wang3
 */
public class InitServletData {

    public static final String PARAM_NAME_NAMESPACE = "namespace";
    public static final String PARAM_NAME_USERNAME = "loginUsername";
    public static final String PARAM_NAME_PASSWORD = "loginPassword";
    public static final String PARAM_NAME_ALLOW = "allow";
    public static final String PARAM_NAME_DENY = "deny";
    public static final String PARAM_NAME_SYNC_CACHE_STATS_DELAY = "syncCacheStatsDelay";

    /**
     * 命名空间（一般是服务名）
     */
    private String namespace;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 白名单
     */
    private List<IPRange> allowList = new ArrayList<>();

    /**
     * 黑名单（优先级高于白名单）
     */
    private List<IPRange> denyList = new ArrayList<>();

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

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
