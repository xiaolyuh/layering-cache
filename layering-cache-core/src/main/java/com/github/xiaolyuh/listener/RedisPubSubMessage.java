package com.github.xiaolyuh.listener;

import java.io.Serializable;

/**
 * redis pub/sub 消息
 *
 * @author yuhao.wang3
 */
public class RedisPubSubMessage implements Serializable {
    public static final String SOURCE = "web-manage";

    /**
     * 缓存名称
     */
    private String cacheName;

    /**
     * 缓存key
     */
    private String key;

    /**
     * 消息类型
     */
    private RedisPubSubMessageType messageType;

    /**
     * 消息来源
     */
    private String source;

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public RedisPubSubMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(RedisPubSubMessageType messageType) {
        this.messageType = messageType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}