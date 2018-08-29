package com.github.xiaolyuh.annotation;

import com.github.xiaolyuh.support.KeyGenerator;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 表示调用的方法（或类中的所有方法）的结果是可以被缓存的。
 * 当该方法被调用时先检查缓存是否命中，如果没有命中再调用被缓存的方法，并将其返回值放到缓存中。
 * 这里的value和key都支持SpEL 表达式
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Cacheable {

    /**
     * 别名是 {@link #cacheNames}.
     */
    @AliasFor("cacheNames")
    String[] value() default {};

    /**
     * 缓存名称，支持SpEL表达式
     *
     * @return
     */
    @AliasFor("value")
    String[] cacheNames() default {};

    /**
     * 缓存key，支持SpEL表达式
     * <p>The SpEL expression evaluates against a dedicated context that provides the
     * following meta-data:
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
     * 是否忽略在操作缓存中遇到的异常，如反序列化异常，默认true。
     * <p>true: 有异常会输出warn级别的日志，并直接执行被缓存的方法（缓存将失效）<p/>
     * <p>false:有异常会输出error级别的日志，并抛出异常<p/>
     *
     * @return
     */
    boolean ignoreException() default true;

    /**
     * 一级缓存配置
     */
    FirstCache firstCache() default @FirstCache();

    /**
     * 二级缓存配置
     */
    SecondaryCache secondaryCache() default @SecondaryCache();
}
