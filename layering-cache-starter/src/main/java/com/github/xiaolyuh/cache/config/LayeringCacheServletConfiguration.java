package com.github.xiaolyuh.cache.config;

import com.github.xiaolyuh.cache.properties.LayeringCacheProperties;
import com.github.xiaolyuh.tool.servlet.LayeringCacheServlet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import javax.servlet.Servlet;

/**
 * @author yuhao.wang3
 */
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "spring.layering-cache.layering-cache-servlet-enabled", havingValue = "true", matchIfMissing = false)
public class LayeringCacheServletConfiguration {
    @Bean
    public ServletRegistrationBean<Servlet> layeringCacheStatViewServletRegistrationBean(LayeringCacheProperties properties) {
        ServletRegistrationBean<Servlet> registrationBean = new ServletRegistrationBean<>();
        registrationBean.setServlet(new LayeringCacheServlet());
        registrationBean.addUrlMappings(!StringUtils.isEmpty(properties.getUrlPattern()) ? properties.getUrlPattern() : "/layering-cache/*");
        registrationBean.addInitParameter("loginUsername", StringUtils.isEmpty(properties.getLoginUsername()) ? "admin" : properties.getLoginUsername());
        registrationBean.addInitParameter("loginPassword", StringUtils.isEmpty(properties.getLoginPassword()) ? "admin" : properties.getLoginPassword());
        registrationBean.addInitParameter("enableUpdate", properties.isEnableUpdate()+"");
        if (!StringUtils.isEmpty(properties.getAllow())) {
            registrationBean.addInitParameter("allow", properties.getAllow());
        }
        if (!StringUtils.isEmpty(properties.getDeny())) {
            registrationBean.addInitParameter("deny", properties.getDeny());
        }
        return registrationBean;
    }
}
