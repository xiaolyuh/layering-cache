package com.github.xiaolyuh.redis.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.IOUtils;

import java.util.Arrays;

/**
 * FastJson 序列化方式
 *
 * @author yuhao.wang
 */
public class FastJsonRedisSerializer extends AbstractRedisSerializer {
    private static final ParserConfig DEFAULT_REDIS_CONFIG = new ParserConfig();

    static {
        DEFAULT_REDIS_CONFIG.setAutoTypeSupport(true);
    }

    @Override
    public <T> byte[] serialize(T value) throws SerializationException {
        if (value == null) {
            return SerializationUtils.EMPTY_ARRAY;
        }

        try {
            return JSON.toJSONBytes(value, SerializerFeature.WriteClassName);
        } catch (Exception e) {
            throw new SerializationException(String.format("FastJsonRedisSerializer 序列化异常: %s, 【%s】", e.getMessage(), JSON.toJSONString(value)), e);
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
            return JSON.parseObject(new String(bytes, IOUtils.UTF8), resultType, DEFAULT_REDIS_CONFIG, new Feature[0]);
        } catch (Exception e) {
            throw new SerializationException(String.format("FastJsonRedisSerializer 反序列化异常: %s, 【%s】", e.getMessage(), JSON.toJSONString(bytes)), e);
        }
    }
}