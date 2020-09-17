package com.github.xiaolyuh.util;

/**
 * 全局配置
 *
 * @author yuhao.wang3
 */
public class GlobalConfig {
    public static final String MESSAGE_KEY = "layering-cache:message-key:%s";

    public static String NAMESPACE = "";

    public static void setNamespace(String namespace) {
        GlobalConfig.NAMESPACE = namespace;
    }

    public static String getMessageRedisKey() {
        return String.format(MESSAGE_KEY, GlobalConfig.NAMESPACE);
    }
}
