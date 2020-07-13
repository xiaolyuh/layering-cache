package com.github.xiaolyuh.web.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.xiaolyuh.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务
 *
 * @author yuhao.wang3
 */
@Service
public class UserService {
    private static Cache<String, Object> manualCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();


    @Value("${layering-cache.web.user-name}")
    private String userName;

    @Value("${layering-cache.web.password}")
    private String password;

    /**
     * 登录校验
     *
     * @param token 唯一标示
     */
    public boolean checkLogin(String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }
        // 检查是否登录
        if (!isLogin(token)) {
            return false;
        }

        return true;
    }

    /**
     * 登录
     *
     * @param usernameParam 用户名
     * @param passwordParam 密码
     * @param token         唯一标示
     * @return
     * @throws IOException
     */
    public boolean login(String usernameParam, String passwordParam, String token) {
        if (userName.equals(usernameParam) && password.equals(passwordParam)) {
            manualCache.put(token, userName);
            return true;
        }
        return false;
    }

    /**
     * 是否登录
     *
     * @param token 唯一标示
     * @return
     */
    public boolean isLogin(String token) {
        if (Objects.nonNull(manualCache.getIfPresent(token))) {
            refreshSession(token);
            return true;
        }
        return false;
    }

    /**
     * 退出
     *
     * @param token 唯一标示
     * @return boolean
     */
    public boolean loginOut(String token) {
        manualCache.invalidate(token);
        return true;
    }

    /**
     * 刷新session
     *
     * @param token 唯一标示
     * @return boolean
     */
    public boolean refreshSession(String token) {
        manualCache.put(token, token);
        return true;
    }
}
