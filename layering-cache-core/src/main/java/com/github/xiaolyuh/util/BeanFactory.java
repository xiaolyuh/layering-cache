package com.github.xiaolyuh.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean 工厂类
 * @author yuhao.wang3
 */
public class BeanFactory {
    private static Logger logger = LoggerFactory.getLogger(BeanFactory.class);

    /**
     * bean 容器
     */
    private static ConcurrentHashMap<Class, Object> beanContainer = new ConcurrentHashMap<>();

    public static <T> T getBean(Class<T> aClass) {
        return (T) beanContainer.computeIfAbsent(aClass, aClass1 -> {
            try {
                return aClass1.newInstance();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            return null;
        });
    }
}
