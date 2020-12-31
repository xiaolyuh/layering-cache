package com.github.xiaolyuh.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 二级缓存配置项
 *
 * @author yuhao.wang
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SecondaryCache {
    /**
     * 缓存有效时间
     *
     * @return long
     */
    long expireTime() default 5;

    /**
     * 缓存主动在失效前强制刷新缓存的时间
     * 建议是： preloadTime = expireTime * 0.2
     *
     * @return long
     */
    long preloadTime() default 1;

    /**
     * 时间单位 {@link TimeUnit}
     *
     * @return TimeUnit
     */
    TimeUnit timeUnit() default TimeUnit.HOURS;

    /**
     * 是否强制刷新（直接执行被缓存方法），默认是false
     *
     * @return boolean
     */
    boolean forceRefresh() default false;

    /**
     * 是否允许缓存NULL值
     *
     * @return boolean
     */
    boolean isAllowNullValue() default false;

    /**
     * 非空值和null值之间的时间倍率，默认是1。isAllowNullValue=true才有效
     * <p>
     * 如配置缓存的有效时间是200秒，倍率这设置成10，
     * 那么当缓存value为null时，缓存的有效时间将是20秒，非空时为200秒
     * </p>
     *
     * @return int
     */
    int magnification() default 1;
}
