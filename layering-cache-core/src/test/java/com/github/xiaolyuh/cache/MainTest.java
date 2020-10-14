package com.github.xiaolyuh.cache;

import com.alibaba.fastjson.JSON;
import com.github.xiaolyuh.support.NullValue;

/**
 * @author olafwang
 * @since 2020/9/25 4:10 下午
 */
public class MainTest {
    public static void main(String[] args) {
        double b = 0.0f/0.0;
        System.out.println(Double.isNaN(b));
        System.out.println(Double.isInfinite(b));
        System.out.println(Double.isInfinite(1.0));
        System.out.println(JSON.toJSONString(null));
        System.out.println(JSON.toJSONString(new NullValue()));
    }
}
