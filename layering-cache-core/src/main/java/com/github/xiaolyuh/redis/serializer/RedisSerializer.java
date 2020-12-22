package com.github.xiaolyuh.redis.serializer;

import org.springframework.lang.Nullable;

/**
 * redis序列化
 *
 * @author olafwang
 * @since 2020/6/29 3:25 下午
 */
public interface RedisSerializer {
    /**
     * 将给定对象序列化为二进制数据。
     *
     * @param value 需要序列化的对象.允许为 {@literal null}.
     * @param <T>   T
     * @return 返回对象的二进制数据. 允许为 {@literal null}.
     * @throws SerializationException 序列化异常
     */
    @Nullable
    <T> byte[] serialize(T value) throws SerializationException;

    /**
     * 将给定的二进制数据中反序列化对象。
     *
     * @param bytes      给定的二进制数据. 允许为 {@literal null}.
     * @param resultType 返回值类型
     * @param <T>        T
     * @return 反序列化后的对象.允许为 {@literal null}.
     * @throws SerializationException 序列化异常
     */
    @Nullable
    <T> T deserialize(@Nullable byte[] bytes, Class<T> resultType) throws SerializationException;
}
