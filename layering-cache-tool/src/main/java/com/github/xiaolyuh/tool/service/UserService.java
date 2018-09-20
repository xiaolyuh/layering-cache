package com.github.xiaolyuh.tool.service;

import com.github.xiaolyuh.tool.support.IPAddress;
import com.github.xiaolyuh.tool.support.IPRange;
import com.github.xiaolyuh.tool.support.InitServletData;
import com.github.xiaolyuh.tool.support.URLConstant;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 用户服务
 *
 * @author yuhao.wang3
 */
public class UserService {
    private static final String SESSION_USER_KEY = "layering-cache-user";

    /**
     * 权限校验
     *
     * @param initServletData {@link InitServletData}
     * @param ip              IP地址
     * @param path            path
     */
    public boolean checkSecurity(InitServletData initServletData, String ip, String path) {
        // 不需要权限的就可以访问的资源
        if (isIgnoreSource(path)) {
            return true;
        }

        boolean ipV6 = ip != null && ip.indexOf(':') != -1;
        if (ipV6) {
            return "0:0:0:0:0:0:0:1".equals(ip);
        }

        // 检查是否是授权IP
        if (CollectionUtils.isEmpty(initServletData.getDenyList()) && CollectionUtils.isEmpty(initServletData.getAllowList())) {
            return true;
        }

        IPAddress ipAddress = new IPAddress(ip);

        for (IPRange range : initServletData.getDenyList()) {
            if (range.isIPAddressInRange(ipAddress)) {
                return false;
            }
        }

        if (initServletData.getAllowList().size() > 0) {
            for (IPRange range : initServletData.getAllowList()) {
                if (range.isIPAddressInRange(ipAddress)) {
                    return true;
                }
            }

            return false;
        }

        return true;
    }

    /**
     * 登录校验
     *
     * @param path    请求地址
     * @param session {@link HttpSession}
     */
    public boolean checkLogin(String path, HttpSession session) {
        // 不需要权限的就可以访问的资源
        if (isIgnoreSource(path)) {
            return true;
        }
        
        // 检查是否登录
        if (!isLogin(session)) {
            return false;
        }

        return true;
    }

    /**
     * 登录
     *
     * @param initServletData 服务启动初始化参数
     * @param usernameParam   用户名
     * @param passwordParam   密码
     * @param session         {@link HttpSession}
     * @return
     * @throws IOException
     */
    public boolean login(InitServletData initServletData, String usernameParam, String passwordParam, HttpSession session) throws IOException {
        if (initServletData.getUsername().equals(usernameParam) &&
                initServletData.getPassword().equals(passwordParam)) {
            session.setAttribute(SESSION_USER_KEY, initServletData.getUsername());
            return true;
        }
        return false;
    }

    /**
     * 是否登录
     *
     * @param session session
     * @return
     */
    public boolean isLogin(HttpSession session) {
        return session != null && session.getAttribute(SESSION_USER_KEY) != null;
    }

    /**
     * 忽略权限检查的资源
     *
     * @param path 地址
     * @return boolean
     */
    public boolean isIgnoreSource(String path) {
        // 不需要权限的就可以访问的资源
        return URLConstant.USER_LOGIN_PAGE.equals(path)
                || URLConstant.USER_SUBMIT_LOGIN.equals(path)
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/fonts/")
                || path.startsWith("/i/");
    }

    /**
     * 退出
     *
     * @param session {@link HttpSession}
     * @return boolean
     */
    public boolean loginOut(HttpSession session) {
        session.invalidate();
        return true;
    }
}
