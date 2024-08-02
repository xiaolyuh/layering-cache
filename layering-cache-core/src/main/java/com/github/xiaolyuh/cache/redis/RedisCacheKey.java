package com.github.xiaolyuh.cache.redis;

import com.github.xiaolyuh.redis.serializer.RedisSerializer;
import com.github.xiaolyuh.redis.serializer.StringRedisSerializer;
import java.util.Arrays;
import org.springframework.util.Assert;

/**
 * redis key 生成
 *
 * @author yuhao.wang3
 */
public class RedisCacheKey {

    /**
     * 前缀序列化器
     */
    private final RedisSerializer prefixSerializer1 = new StringRedisSerializer();

    /**
     * 缓存key
     */
    private final Object keyElement;

    /**
     * 缓存名称
     */
    private String cacheName;

    /**
     * 是否使用缓存前缀
     */
    private boolean usePrefix = true;

    /**
     * RedisTemplate 的key序列化器
     */
    private final RedisSerializer serializer;

    /**
     * 缓存预加载时间
     */
    private long preloadTime;

    /**
     * @param keyElement 缓存key
     * @param serializer RedisSerializer
     */
    public RedisCacheKey(Object keyElement, RedisSerializer serializer) {

        Assert.notNull(keyElement, "缓存key不能为NULL");
        Assert.notNull(serializer, "key的序列化器不能为NULL");
        this.keyElement = keyElement;
        this.serializer = serializer;
    }

    /**
     * 获取缓存key
     *
     * @return String
     */
    public String getKey() {

        return new String(getKeyBytes());
    }

    /**
     * 获取key的byte数组
     *
     * @return byte[]
     */
    public byte[] getKeyBytes() {

        byte[] rawKey = serializeKeyElement();
        if (!usePrefix) {
            return rawKey;
        }
        byte[] prefix = getPrefix();
        byte[] prefixedKey = Arrays.copyOf(prefix, prefix.length + rawKey.length);
        System.arraycopy(rawKey, 0, prefixedKey, prefix.length, rawKey.length);

        return prefixedKey;
    }

    private byte[] serializeKeyElement() {

        if (serializer == null && keyElement instanceof byte[]) {
            return (byte[]) keyElement;
        }

        return serializer.serialize(keyElement);
    }

    /**
     * 获取缓存前缀，默认缓存前缀是":"
     *
     * @return byte[]
     */
    public byte[] getPrefix() {
        return prefixSerializer1.serialize((cacheName.concat(":")));
    }

    /**
     * 设置缓存名称
     *
     * @param cacheName cacheName
     * @return RedisCacheKey
     */
    public RedisCacheKey cacheName(String cacheName) {
        this.cacheName = cacheName;
        return this;
    }

    /**
     * 设置预加载时间
     *
     * @param preloadTime preloadTime
     * @return RedisCacheKey
     */
    public RedisCacheKey preloadTime(long preloadTime) {
        this.preloadTime = preloadTime;
        return this;
    }

    /**
     * 设置是否使用缓存前缀，默认使用
     *
     * @param usePrefix usePrefix
     * @return RedisCacheKey
     */
    public RedisCacheKey usePrefix(boolean usePrefix) {
        this.usePrefix = usePrefix;
        return this;
    }

    public Object getKeyElement() {
        return keyElement;
    }

    public long getPreloadTime() {
        return preloadTime;
    }
}