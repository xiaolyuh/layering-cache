package com.github.xiaolyuh.cache.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author yuhao.wang3
 */
@ConfigurationProperties("spring.layering-cache")
public class LayeringCacheProperties {

    /**
     * 是否开启缓存统计
     */
    private boolean stats = true;

    /**
     * 命名空间，必须唯一般使用服务名
     */
    private String namespace;

    /**
     * 启动 LayeringCacheServlet.
     */
    private boolean layeringCacheServletEnabled = true;

    /**
     * contextPath
     */
    private String urlPattern;

    /**
     * 白名单
     */
    private String allow;

    /**
     * 黑名单
     */
    private String deny;

    /**
     * 登录用户账号
     */
    private String loginUsername = "admin";

    /**
     * 登录用户密码
     */
    private String loginPassword = "admin";

    public boolean isLayeringCacheServletEnabled() {
        return layeringCacheServletEnabled;
    }

    public void setLayeringCacheServletEnabled(boolean layeringCacheServletEnabled) {
        this.layeringCacheServletEnabled = layeringCacheServletEnabled;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String getAllow() {
        return allow;
    }

    public void setAllow(String allow) {
        this.allow = allow;
    }

    public String getDeny() {
        return deny;
    }

    public void setDeny(String deny) {
        this.deny = deny;
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public boolean isStats() {
        return stats;
    }

    public void setStats(boolean stats) {
        this.stats = stats;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
