package com.github.xiaolyuh.redis.serializer;

import com.alibaba.fastjson.JSON;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * JDK 序列化方式
 *
 * @author yuhao.wang
 */
public class JdkRedisSerializer extends AbstractRedisSerializer {
    @Override
    public <T> byte[] serialize(T value) throws SerializationException {
        if (value == null) {
            return SerializationUtils.EMPTY_ARRAY;
        }

        if (!(value instanceof Serializable)) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " requires a Serializable payload but received an object of type [" + value.getClass().getName() + "]");
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {

            objectOutputStream.writeObject(value);
            objectOutputStream.flush();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new SerializationException(String.format("JdkRedisSerializer 序列化异常: %s, 【%s】", e.getMessage(), JSON.toJSONString(value)), e);
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

        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
             ObjectInputStream stream = new ObjectInputStream(byteStream)) {
            return (T) stream.readObject();
        } catch (Exception e) {
            throw new SerializationException(String.format("JdkRedisSerializer 反序列化异常: %s, 【%s】", e.getMessage(), JSON.toJSONString(bytes)), e);
        }
    }
}