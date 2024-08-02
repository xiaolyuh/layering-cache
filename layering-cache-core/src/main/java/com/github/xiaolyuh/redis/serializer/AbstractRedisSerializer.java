package com.github.xiaolyuh.redis.serializer;

import com.github.xiaolyuh.support.NullValue;
import java.util.Objects;

/**
 * 序列化方式的抽象实现
 *
 * @author yuhao.wang
 */
public abstract class AbstractRedisSerializer implements RedisSerializer {
    private byte[] nullValueBytes;

    /**
     * 获取空值的序列化值
     *
     * @return byte[]
     */
    public byte[] getNullValueBytes() {
        if (Objects.isNull(nullValueBytes)) {
            synchronized (this) {
                nullValueBytes = serialize(NullValue.INSTANCE);
            }
        }
        return nullValueBytes;
    }
}