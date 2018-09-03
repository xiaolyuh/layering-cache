package com.github.xiaolyuh.util.service;

import com.github.xiaolyuh.util.support.InitServletData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
     * @param request         {@link HttpServletRequest}
     */
    public void checkSecrity(InitServletData initServletData, HttpServletRequest request) {

    }

    public void login(InitServletData initServletData, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String usernameParam = request.getParameter(InitServletData.PARAM_NAME_USERNAME);
        String passwordParam = request.getParameter(InitServletData.PARAM_NAME_PASSWORD);
        if (initServletData.getUsername().equals(usernameParam) &&
                initServletData.getPassword().equals(passwordParam)) {
            request.getSession().setAttribute(SESSION_USER_KEY, initServletData.getUsername());
            response.getWriter().print("success");
        } else {
            response.getWriter().print("error");
        }
    }

}
