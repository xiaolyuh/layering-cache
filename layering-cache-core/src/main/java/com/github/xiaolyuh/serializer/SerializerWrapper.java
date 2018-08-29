package com.github.xiaolyuh.serializer;

/**
 * 序列化包装类
 *
 * @author yuhao.wang
 */
public class SerializerWrapper {
    private Object conent;

    public SerializerWrapper() {
    }

    public SerializerWrapper(Object conent) {
        this.conent = conent;
    }

    public Object getConent() {
        return conent;
    }

    public void setConent(Object conent) {
        this.conent = conent;
    }
}