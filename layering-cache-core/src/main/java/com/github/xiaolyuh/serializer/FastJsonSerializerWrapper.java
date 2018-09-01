package com.github.xiaolyuh.serializer;

import com.github.xiaolyuh.support.Type;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.List;
import java.util.Set;

/**
 * 序列化包装类
 *
 * @author yuhao.wang
 */
public class FastJsonSerializerWrapper {
    private Object content;

    private String type;

    public FastJsonSerializerWrapper() {
    }

    public FastJsonSerializerWrapper(Object content) {
        this.content = content;

        if (content == null) {
            this.type = Type.NULL.name();
            return;
        }

        if (content instanceof String || content instanceof Integer
                || content instanceof Long || content instanceof Double
                || content instanceof Float || content instanceof Boolean
                || content instanceof Byte || content instanceof Character
                || content instanceof Short) {

            this.type = Type.STRING.name();
            return;
        }

        if (content instanceof List) {
            this.type = Type.LIST.name();
            return;
        }

        if (content instanceof Set) {
            this.type = Type.SET.name();
            return;
        }

        if (content.getClass().isArray()) {
            throw new SerializationException("FastJsonRedisSerializer 序列化不支持枚数组型");
        }

        if (content.getClass().isEnum()) {
            throw new SerializationException("FastJsonRedisSerializer 序列化不支持枚举类型");
        }

        this.type = Type.OBJECT.name();
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}