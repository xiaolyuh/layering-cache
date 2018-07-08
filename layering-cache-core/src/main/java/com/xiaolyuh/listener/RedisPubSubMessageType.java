package com.xiaolyuh.listener;

/**
 * 消息类型
 *
 * @author yuhao.wang3
 */
public enum RedisPubSubMessageType {
    /**
     * 删除缓存
     */
    EVICT("最后一次写入后到期失效"),

    /**
     * 清空缓存
     */
    CLEAR("最后一次访问后到期失效");

    private String label;

    RedisPubSubMessageType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}