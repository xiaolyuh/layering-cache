package com.xiaolyuh.annotation;

import com.xiaolyuh.support.ExpireMode;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 一级缓存配置项
 *
 * @author yuhao.wang
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface FirstCache {
    /**
     * 缓存初始Size
     */
    int initialCapacity() default 10;

    /**
     * 缓存最大Size
     */
    int maximumSize() default 5000;

    /**
     * 缓存有效时间
     */
    int expireTime() default 9;

    /**
     * 缓存时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;

    /**
     * 缓存失效模式
     *
     * @see ExpireMode
     */
    ExpireMode expireMode() default ExpireMode.WRITE;
}
