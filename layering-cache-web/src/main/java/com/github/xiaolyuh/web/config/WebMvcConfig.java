package com.github.xiaolyuh.web.config;

import com.github.xiaolyuh.web.interceptor.LoginInterceptor;
import com.github.xiaolyuh.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private UserService userService;

    @Bean
    public WebMvcConfig getMyWebMvcConfig() {
        WebMvcConfig webMvcConfig = new WebMvcConfig() {
            //注册拦截器
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new LoginInterceptor(userService))
                        .addPathPatterns("/**")
                        .excludePathPatterns("/login**", "/user/submit-login", "/toLogin", "/redis/redis-config", "/css/**", "/js/**", "/fonts/**", "/i/**");
            }
        };
        return webMvcConfig;
    }
}