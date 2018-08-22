package com.xiaolyuh.support;

/**
 * 缓存模式
 *
 * @author yuhao.wang3
 */
public enum CacheMode {
    /**
     * 只开启一级缓存
     */
    ONLY_FIRST("只是用一级缓存"),

    /**
     * 只开启二级缓存
     */
    ONLY_SECOND("只是使用二级缓存"),

    /**
     * 同时开启一级缓存和二级缓存
     */
    ALL("同时开启一级缓存和二级缓存");

    private String label;

    CacheMode(String label) {
        this.label = label;
    }
}
