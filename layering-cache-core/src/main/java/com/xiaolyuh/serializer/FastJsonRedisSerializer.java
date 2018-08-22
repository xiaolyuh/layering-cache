package com.xiaolyuh.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.Charset;

/**
 * @param <T>
 * @author yuhao.wang
 */
public class FastJsonRedisSerializer<T> implements RedisSerializer<T> {
    private Logger logger = LoggerFactory.getLogger(KryoRedisSerializer.class);

    private static final byte[] EMPTY_ARRAY = new byte[0];
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private Class<T> clazz;

    public FastJsonRedisSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
        ParserConfig.getGlobalInstance().addAccept("com.xiaolyuh.");
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return SerializationUtils.EMPTY_ARRAY;
        }

        try {
            return JSON.toJSONString(t, SerializerFeature.WriteClassName).getBytes(DEFAULT_CHARSET);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SerializationException("FastJsonRedisSerializer 序列化异常: " + e.getMessage(), e);
        }

    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (SerializationUtils.isEmpty(bytes)) {
            return null;
        }

        try {
            String str = new String(bytes, DEFAULT_CHARSET);
            return JSON.parseObject(str, clazz);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SerializationException("FastJsonRedisSerializer 反序列化异常: " + e.getMessage(), e);
        }
    }

}