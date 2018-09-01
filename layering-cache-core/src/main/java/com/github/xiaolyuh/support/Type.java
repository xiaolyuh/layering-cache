package com.github.xiaolyuh.support;

/**
 * 对象类型
 *
 * @author yuhao.wang3
 */
public enum Type {
    /**
     * null
     */
    NULL("null"),

    /**
     * string
     */
    STRING("string"),

    /**
     * object
     */
    OBJECT("Object 对象"),

    /**
     * List集合
     */
    LIST("List集合"),

    /**
     * Set集合
     */
    SET("Set集合"),

    /**
     * 数组
     */
    ARRAY("数组"),

    /**
     * 枚举
     */
    ENUM("枚举"),

    /**
     * 其他类型
     */
    OTHER("其他类型");

    private String label;

    Type(String label) {
        this.label = label;
    }

    public static Type parse(String name) {
        for (Type type : Type.values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return OTHER;
    }
}