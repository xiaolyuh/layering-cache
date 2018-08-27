package com.github.xiaolyuh.annotation;

import com.github.xiaolyuh.support.KeyGenerator;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 将对应数据放到缓存中
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CachePut {

    /**
     * 别名 {@link #cacheNames}.
     */
    @AliasFor("cacheNames")
    String[] value() default {};

    /**
     * 缓存名称
     */
    @AliasFor("value")
    String[] cacheNames() default {};

    /**
     * 缓存key，支持SpEL表达式
     * <p>The SpEL expression evaluates against a dedicated context that provides the
     * following meta-data:
     * <ul>
     * <li>{@code #result} for a reference to the result of the method invocation. For
     * supported wrappers such as {@code Optional}, {@code #result} refers to the actual
     * object, not the wrapper</li>
     * <li>{@code #root.method}, {@code #root.target}, and {@code #root.caches} for
     * references to the {@link java.lang.reflect.Method method}, target object, and
     * affected cache(s) respectively.</li>
     * <li>Shortcuts for the method name ({@code #root.methodName}) and target class
     * ({@code #root.targetClass}) are also available.
     * <li>Method arguments can be accessed by index. For instance the second argument
     * can be accessed via {@code #root.args[1]}, {@code #p1} or {@code #a1}. Arguments
     * can also be accessed by name if that information is available.</li>
     * </ul>
     */
    String key() default "";

    /**
     * The bean name of the custom {@link KeyGenerator}
     * to use.
     * <p>Mutually exclusive with the {@link #key} attribute.
     */
    @Deprecated
    String keyGenerator() default "";

    /**
     * 一级缓存配置
     */
    FirstCache firstCache() default @FirstCache();

    /**
     * 二级缓存配置
     */
    SecondaryCache secondaryCache() default @SecondaryCache();
}
