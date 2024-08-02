package com.github.xiaolyuh.redis.serializer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.springframework.util.Assert;

/**
 * 必须重写序列化器，否则@Cacheable注解的key会报类型转换错误
 *
 * @author yuhao.wang
 */
public class StringRedisSerializer implements RedisSerializer {

    private final Charset charset;

    public StringRedisSerializer() {
        this(StandardCharsets.UTF_8);
    }

    public StringRedisSerializer(Charset charset) {
        Assert.notNull(charset, "Charset must not be null!");
        this.charset = charset;
    }

    @Override
    public <T> byte[] serialize(T value) throws SerializationException {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return ((String) value).getBytes(charset);
        }
        throw new UnsupportedOperationException("String序列化方式不支持其他数据类型的序列化");
    }

    @Override
    public String deserialize(byte[] bytes, Class resultType) throws SerializationException {

        return (bytes == null ? null : new String(bytes, charset));
    }
}
