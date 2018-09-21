package com.github.xiaolyuh.annotation;

import com.github.xiaolyuh.support.ExpireMode;

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
     *
     * @return int
     */
    int initialCapacity() default 10;

    /**
     * 缓存最大Size
     *
     * @return int
     */
    int maximumSize() default 5000;

    /**
     * 缓存有效时间
     *
     * @return int
     */
    int expireTime() default 9;

    /**
     * 缓存时间单位
     *
     * @return TimeUnit
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;

    /**
     * 缓存失效模式
     *
     * @return ExpireMode
     * @see ExpireMode
     */
    ExpireMode expireMode() default ExpireMode.WRITE;
}
