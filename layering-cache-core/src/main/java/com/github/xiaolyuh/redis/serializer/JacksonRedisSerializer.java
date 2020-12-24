package com.github.xiaolyuh.redis.serializer;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Arrays;

/**
 * JackJson 序列化方式
 *
 * @author yuhao.wang
 */
public class JacksonRedisSerializer extends AbstractRedisSerializer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    {
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Override
    public <T> byte[] serialize(T value) throws SerializationException {
        if (value == null) {
            return SerializationUtils.EMPTY_ARRAY;
        }
        try {
            return this.objectMapper.writeValueAsBytes(value);
        } catch (Exception e) {
            throw new SerializationException(String.format("JacksonRedisSerializer 序列化异常: %s, 【%s】", e.getMessage(), JSON.toJSONString(value)), e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> resultType) throws SerializationException {
        if (SerializationUtils.isEmpty(bytes)) {
            return null;
        }

        if (Arrays.equals(getNullValueBytes(), bytes)) {
            return null;
        }

        try {
            return this.objectMapper.readValue(bytes, 0, bytes.length, resultType);
        } catch (Exception e) {
            throw new SerializationException(String.format("JacksonRedisSerializer 反序列化异常: %s, 【%s】", e.getMessage(), JSON.toJSONString(bytes)), e);
        }

    }
}