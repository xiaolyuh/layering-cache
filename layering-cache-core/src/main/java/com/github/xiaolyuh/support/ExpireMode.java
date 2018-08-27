package com.github.xiaolyuh.support;

/**
 * 缓存失效模式
 *
 * @author yuhao.wang3
 */
public enum ExpireMode {
    /**
     * 每写入一次重新计算一次缓存的有效时间
     */
    WRITE("最后一次写入后到期失效"),

    /**
     * 每访问一次重新计算一次缓存的有效时间
     */
    ACCESS("最后一次访问后到期失效");

    private String label;

    ExpireMode(String label) {
        this.label = label;
    }
}