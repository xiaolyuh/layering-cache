package com.github.xiaolyuh.annotation;

import com.github.xiaolyuh.support.CacheMode;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

/**
 * 表示调用的方法的结果是可以被缓存的。
 * 通过缓存key队列批量获取缓存数据。
 * 当该方法被调用时先检查缓存是否命中，如果没有命中再调用被缓存的方法，并将其返回值放到缓存中。
 * 这里的value和key都支持SpEL 表达式
 *
 * @author rayon177
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BatchCacheable {

    /**
     * 别名是 {@link #cacheNames}.
     *
     * @return String[]
     */
    @AliasFor("cacheNames")
    String[] value() default {};

    /**
     * 缓存名称，支持SpEL表达式
     *
     * @return String[]
     */
    @AliasFor("value")
    String[] cacheNames() default {};

    /**
     * 描述
     *
     * @return String
     */
    String depict() default "";

    /**
     * 缓存keys，需要是SpEL表达式。这个表达式会用于从参数和返回值中获取缓存keys
     * 该表达式需要返回一个List对象
     *
     * @return String
     */
    String keys() ;

    /**
     * 生成缓存的条件，默认空全部缓存。支持SpEL表达式
     * @return
     */
    String condition() default "";

    /**
     * 缓存模式(只使用一级缓存或者二级缓存)
     *
     * @return boolean
     */
    CacheMode cacheMode() default CacheMode.ALL;

    /**
     * 一级缓存配置
     *
     * @return FirstCache
     */
    FirstCache firstCache() default @FirstCache();

    /**
     * 二级缓存配置
     *
     * @return SecondaryCache
     */
    SecondaryCache secondaryCache() default @SecondaryCache();
}
