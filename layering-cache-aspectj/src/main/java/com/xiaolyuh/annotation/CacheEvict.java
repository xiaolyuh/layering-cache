package com.xiaolyuh.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 删除缓存
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CacheEvict {

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
	 * The bean name of the custom {@link com.xiaolyuh.support.KeyGenerator}
	 * to use.
	 * <p>Mutually exclusive with the {@link #key} attribute.
	 */
	@Deprecated
	String keyGenerator() default "";

    /**
     * 是否删除缓存中所有数据
     * <p>默认情况下是只删除关联key的缓存数据
     * <p>注意：当该参数设置成 {@code true} 时 {@link #key} 参数将无效
     */
	boolean allEntries() default false;


}
