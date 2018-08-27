package com.github.xiaolyuh.listener;

/**
 * redis pub/sub 消息
 *
 * @author yuhao.wang3
 */
public class RedisPubSubMessage {
    /**
     * 缓存名称
     */
    private String cacheName;

    /**
     * 缓存key
     */
    private Object key;

    /**
     * 消息类型
     */
    private RedisPubSubMessageType messageType;

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public RedisPubSubMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(RedisPubSubMessageType messageType) {
        this.messageType = messageType;
    }
}