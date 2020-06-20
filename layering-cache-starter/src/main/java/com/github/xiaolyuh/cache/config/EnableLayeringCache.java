package com.github.xiaolyuh.cache.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({LayeringCacheAutoConfig.class})
public @interface EnableLayeringCache {

}