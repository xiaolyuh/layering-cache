package com.github.xiaolyuh.redis.serializer;

import org.springframework.lang.Nullable;

/**
 * redis序列化
 *
 * @author olafwang
 * @since 2020/6/29 3:25 下午
 */
public interface RedisSerializer<T> {
    /**
     * Serialize the given object to binary data.
     *
     * @param t object to serialize. Can be {@literal null}.
     * @return the equivalent binary data. Can be {@literal null}.
     */
    @Nullable
    byte[] serialize(@Nullable T t) throws SerializationException;

    /**
     * Deserialize an object from the given binary data.
     *
     * @param bytes object binary representation. Can be {@literal null}.
     * @return the equivalent object instance. Can be {@literal null}.
     */
    @Nullable
    T deserialize(@Nullable byte[] bytes) throws SerializationException;
}
