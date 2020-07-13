package com.github.xiaolyuh.web.interceptor;

import com.alibaba.fastjson.JSON;
import com.github.xiaolyuh.web.service.UserService;
import com.github.xiaolyuh.web.utils.Result;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author olafwang
 * @since 2020/7/10 3:00 下午
 */
public class LoginInterceptor implements HandlerInterceptor {
    public static final String PARAM_NAME_TOKEN = "token";

    private UserService userService;

    public LoginInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String contextPath = request.getContextPath();
        String servletPath = request.getServletPath();
        String requestURI = request.getRequestURI();

        response.setCharacterEncoding("utf-8");

        // root context
        if (contextPath == null) {
            contextPath = "";
        }
        String path = requestURI.substring(contextPath.length() + servletPath.length());

        // 登录校验
        String token = request.getParameter(PARAM_NAME_TOKEN);

        if (!userService.checkLogin(token)) {
            response.sendRedirect("/toLogin");
//            request.getRequestDispatcher("/toLogin").forward(request, response);
//            response.getWriter().write(JSON.toJSONString(Result.error("请登录")));
            return false;
        }

        return true;
    }

}
