package com.github.xiaolyuh.tool.servlet;


import com.alibaba.fastjson.JSON;
import com.github.xiaolyuh.manager.AbstractCacheManager;
import com.github.xiaolyuh.redis.clinet.RedisClient;
import com.github.xiaolyuh.stats.CacheStatsInfo;
import com.github.xiaolyuh.tool.service.CacheService;
import com.github.xiaolyuh.tool.service.UserService;
import com.github.xiaolyuh.tool.support.IPRange;
import com.github.xiaolyuh.tool.support.InitServletData;
import com.github.xiaolyuh.tool.support.Result;
import com.github.xiaolyuh.tool.support.URLConstant;
import com.github.xiaolyuh.tool.util.Utils;
import com.github.xiaolyuh.util.BeanFactory;
import com.github.xiaolyuh.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 统计的Servlet
 *
 * @author yuhao.wang3
 */
public class LayeringCacheServlet extends HttpServlet {
    private static Logger logger = LoggerFactory.getLogger(LayeringCacheServlet.class);

    private InitServletData initServletData = new InitServletData();

    private RedisClient redisClient;

    @Override
    public void init() throws ServletException {
        initAuthEnv();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String contextPath = request.getContextPath();
        String servletPath = request.getServletPath();
        String requestURI = request.getRequestURI();

        response.setCharacterEncoding("utf-8");

        // root context
        if (contextPath == null) {
            contextPath = "";
        }
        String uri = contextPath + servletPath;
        String path = requestURI.substring(contextPath.length() + servletPath.length());

        // 权限校验
        String ip = request.getRemoteAddr();
        boolean security = BeanFactory.getBean(UserService.class).checkSecurity(initServletData, ip, path);
        if (!security) {
            returnResourceFile("/nopermit.html", uri, response);
            return;
        }

        // 登录校验
        String token = request.getParameter(InitServletData.PARAM_NAME_TOKEN);
        boolean isLogin = BeanFactory.getBean(UserService.class).checkLogin(path, redisClient, token);
        if (!isLogin) {
            returnResourceFile("/login.html", uri, response);
            return;
        }

        // 登录
        if (URLConstant.USER_SUBMIT_LOGIN.equals(path)) {
            String usernameParam = request.getParameter(InitServletData.PARAM_NAME_USERNAME);
            String passwordParam = request.getParameter(InitServletData.PARAM_NAME_PASSWORD);
            token = UUID.randomUUID().toString();
            boolean success = BeanFactory.getBean(UserService.class).login(initServletData, usernameParam, passwordParam, redisClient, token);

            if (success) {
                response.getWriter().write(JSON.toJSONString(Result.success(token)));
            } else {
                response.getWriter().write(JSON.toJSONString(Result.error("用户名或密码错误")));
            }
            return;
        }

        // 退出登录
        if (URLConstant.USER_LOGIN_OUT.equals(path)) {
            boolean success = BeanFactory.getBean(UserService.class).loginOut(redisClient, token);

            if (success) {
                response.getWriter().write(JSON.toJSONString(Result.success()));
            } else {
                response.getWriter().write(JSON.toJSONString(Result.error("用户名或密码错误")));
            }
            return;
        }

        // 重置缓存统计数据
        if (URLConstant.RESET_CACHE_STAT.equals(path)) {
            if (!initServletData.getEnableUpdate()) {
                response.getWriter().write(JSON.toJSONString(Result.error("你没有开启更新数据的权限")));
                return;
            }
            Set<AbstractCacheManager> cacheManagers = AbstractCacheManager.getCacheManager();
            for (AbstractCacheManager cacheManager : cacheManagers) {
                cacheManager.resetCacheStat();
            }
            response.getWriter().write(JSON.toJSONString(Result.success()));
            // 刷新session
            BeanFactory.getBean(UserService.class).refreshSession(redisClient, token);
            return;
        }

        // 缓存统计列表
        if (URLConstant.CACHE_STATS_LIST.equals(path)) {
            String cacheName = request.getParameter("cacheName");
            Set<AbstractCacheManager> cacheManagers = AbstractCacheManager.getCacheManager();
            List<CacheStatsInfo> statsList = new ArrayList<>();
            for (AbstractCacheManager cacheManager : cacheManagers) {
                List<CacheStatsInfo> cacheStats = cacheManager.listCacheStats(cacheName);
                if (!CollectionUtils.isEmpty(cacheStats)) {
                    statsList.addAll(cacheStats);
                }
            }
            response.getWriter().write(JSON.toJSONString(Result.success(statsList)));
            // 刷新session
            BeanFactory.getBean(UserService.class).refreshSession(redisClient, token);
            return;
        }

        // 删除缓存
        if (URLConstant.CACHE_STATS_DELETE_CACHW.equals(path)) {
            if (!initServletData.getEnableUpdate()) {
                response.getWriter().write(JSON.toJSONString(Result.error("你没有开启更新数据的权限")));
                return;
            }

            String cacheNameParam = request.getParameter("cacheName");
            String internalKey = request.getParameter("internalKey");
            String key = request.getParameter("key");
            BeanFactory.getBean(CacheService.class).deleteCache(cacheNameParam, internalKey, key);
            response.getWriter().write(JSON.toJSONString(Result.success()));
            // 刷新session
            BeanFactory.getBean(UserService.class).refreshSession(redisClient, token);
            return;
        }

        // find file in http.resources path
        returnResourceFile(path, uri, response, request, token);
    }

    private void initAuthEnv() {
        String paramUserName = getInitParameter(InitServletData.PARAM_NAME_USERNAME);
        if (!StringUtils.isEmpty(paramUserName)) {
            this.initServletData.setUsername(paramUserName);
        }

        String paramPassword = getInitParameter(InitServletData.PARAM_NAME_PASSWORD);
        if (!StringUtils.isEmpty(paramPassword)) {
            this.initServletData.setPassword(paramPassword);
        }

        try {
            String paramEnableUpdate = getInitParameter(InitServletData.PARAM_NAME_ENABLE_UPDATE);
            if (!StringUtils.isEmpty(paramPassword)) {
                this.initServletData.setEnableUpdate(parseStringToBoolean(paramEnableUpdate));
            }
        } catch (Exception e) {
            logger.error("initParameter config error, enableUpdate : {}", getInitParameter(InitServletData.PARAM_NAME_ENABLE_UPDATE), e);
        }

        try {
            String param = getInitParameter(InitServletData.PARAM_NAME_ALLOW);
            this.initServletData.setAllowList(parseStringToIP(param));
        } catch (Exception e) {
            logger.error("initParameter config error, allow : {}", getInitParameter(InitServletData.PARAM_NAME_ALLOW), e);
        }

        try {
            String param = getInitParameter(InitServletData.PARAM_NAME_DENY);
            this.initServletData.setDenyList(parseStringToIP(param));
        } catch (Exception e) {
            logger.error("initParameter config error, deny : {}", getInitParameter(InitServletData.PARAM_NAME_DENY), e);
        }

        // 获取redisTemplate
        redisClient = getRedisClient();
    }

    private List<IPRange> parseStringToIP(String ipStr) {
        List<IPRange> ipList = new ArrayList<>();
        if (ipStr != null && ipStr.trim().length() != 0) {
            ipStr = ipStr.trim();
            String[] items = ipStr.split(",");

            for (String item : items) {
                if (item == null || item.length() == 0) {
                    continue;
                }

                IPRange ipRange = new IPRange(item);
                ipList.add(ipRange);
            }
        }

        return ipList;
    }

    protected void returnResourceFile(String fileName, String uri, HttpServletResponse response)
            throws ServletException,
            IOException {

        returnResourceFile(fileName, uri, response, null, null);
    }

    protected void returnResourceFile(String fileName, String uri, HttpServletResponse response, HttpServletRequest request, String token)
            throws ServletException,
            IOException {

        String filePath = getFilePath(fileName);

        if (filePath.endsWith(".html")) {
            if (request != null && StringUtils.isNotBlank(token)) {
                request.setAttribute("token", token);
            }
            response.setContentType("text/html; charset=utf-8");
        }
        if (fileName.endsWith(".jpg") || fileName.endsWith(".png")) {
            byte[] bytes = Utils.readByteArrayFromResource(filePath);
            if (bytes != null) {
                response.getOutputStream().write(bytes);
            }

            return;
        }

        String text = Utils.readFromResource(filePath);
        if (StringUtils.isEmpty(text)) {
            response.sendRedirect(uri + "/index.html");
            return;
        }
        if (fileName.endsWith(".css")) {
            response.setContentType("text/css;charset=utf-8");
        } else if (fileName.endsWith(".js")) {
            response.setContentType("text/javascript;charset=utf-8");
        }
        response.getWriter().write(text);
    }

    private boolean parseStringToBoolean(String enableUpdate) {

        return Boolean.parseBoolean(enableUpdate);
    }

    private RedisClient getRedisClient() {
        Set<AbstractCacheManager> cacheManagers = AbstractCacheManager.getCacheManager();
        for (AbstractCacheManager cacheManager : cacheManagers) {
            return cacheManager.getRedisClient();
        }
        return null;
    }

    protected String getFilePath(String fileName) {
        return InitServletData.RESOURCE_PATH + fileName;
    }
}
