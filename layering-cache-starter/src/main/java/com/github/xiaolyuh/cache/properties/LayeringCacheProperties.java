package com.github.xiaolyuh.cache.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author yuhao.wang3
 */
@ConfigurationProperties("spring.cache.layering-cache")
public class LayeringCacheProperties {
    /**
     * 启动 LayeringCacheServlet.
     */
    private boolean utilViewServletEnabled = false;
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

    public boolean isUtilViewServletEnabled() {
        return utilViewServletEnabled;
    }

    public void setUtilViewServletEnabled(boolean utilViewServletEnabled) {
        this.utilViewServletEnabled = utilViewServletEnabled;
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

}
