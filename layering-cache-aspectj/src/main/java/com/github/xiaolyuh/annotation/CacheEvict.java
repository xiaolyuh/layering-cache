package com.github.xiaolyuh.annotation;

import com.github.xiaolyuh.support.CacheMode;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 删除缓存
 *
 * @author olafwang
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CacheEvict {

    /**
     * 别名 {@link #cacheNames}.
     *
     * @return String[]
     */
    @AliasFor("cacheNames")
    String[] value() default {};

    /**
     * 缓存名称
     *
     * @return String[]
     */
    @AliasFor("value")
    String[] cacheNames() default {};

    /**
     * 缓存key，支持SpEL表达式
     * <ul>
     * <li>{@code #root.method}, {@code #root.target}, and {@code #root.caches} for
     * references to the {@link java.lang.reflect.Method method}, target object, and
     * affected cache(s) respectively.</li>
     * <li>Shortcuts for the method name ({@code #root.methodName}) and target class
     * ({@code #root.targetClass}) are also available.
     * <li>Method arguments can be accessed by index. For instance the second argument
     * can be accessed via {@code #root.args[1]}, {@code #p1} or {@code #a1}. Arguments
     * can also be accessed by name if that information is available.</li>
     * </ul>
     *
     * @return String
     */
    String key() default "";

    /**
     * 缓存模式(只使用一级缓存或者二级缓存)
     *
     * @return boolean
     */
    CacheMode cacheMode() default CacheMode.ALL;

    /**
     * 是否删除缓存中所有数据
     * <p>默认情况下是只删除关联key的缓存数据
     * <p>注意：当该参数设置成 {@code true} 时 {@link #key} 参数将无效
     *
     * @return boolean
     */
    boolean allEntries() default false;


}
