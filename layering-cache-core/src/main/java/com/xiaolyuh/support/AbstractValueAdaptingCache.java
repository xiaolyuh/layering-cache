/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xiaolyuh.support;

import com.xiaolyuh.cache.Cache;
import org.springframework.util.Assert;

import java.util.concurrent.Callable;


/**
 * Cache 接口的抽象实现类，对公共的方法做了一写实现，如是否允许存NULL值
 * <p>
 * <p>如果允许为NULL值，则需要在内部将NULL替换成{@link NullValue#INSTANCE} 对象
 *
 * @author yuhao.wang3
 */
public abstract class AbstractValueAdaptingCache implements Cache {

    /**
     * 是否允许为NULL
     */
    private final boolean allowNullValues;

    /**
     * 缓存名称
     */
    private final String name;


    /**
     * 通过构造方法执行配置
     *
     * @param allowNullValues 是否允许为NULL
     */
    protected AbstractValueAdaptingCache(boolean allowNullValues, String name) {
        Assert.notNull(name, "缓存名称不能为NULL");
        this.allowNullValues = allowNullValues;
        this.name = name;
    }

    /**
     * 获取是否允许存NULL值
     *
     * @return true:允许，false:不允许
     */
    public final boolean isAllowNullValues() {
        return this.allowNullValues;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object get(Object key) {
        Object value = lookup(key);
        return fromStoreValue(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Class<T> type) {
        Object value = fromStoreValue(lookup(key));
        if (value != null && type != null && !type.isInstance(value)) {
            throw new IllegalStateException("Cached value is not of required type [" + type.getName() + "]: " + value);
        }
        return (T) value;
    }

    /**
     * 底层真正去查询缓存的方法
     *
     * @param key 缓存key
     * @return 缓存key对应的值
     */
    protected abstract Object lookup(Object key);


    /**
     * Convert the given value from the internal store to a user value
     * returned from the get method (adapting {@code null}).
     *
     * @param storeValue the store value
     * @return the value to return to the user
     */
    protected Object fromStoreValue(Object storeValue) {
        if (this.allowNullValues && storeValue == NullValue.INSTANCE) {
            return null;
        }
        return storeValue;
    }

    /**
     * Convert the given user value, as passed into the put method,
     * to a value in the internal store (adapting {@code null}).
     *
     * @param userValue the given user value
     * @return the value to store
     */
    protected Object toStoreValue(Object userValue) {
        if (this.allowNullValues && userValue == null) {
            return NullValue.INSTANCE;
        }
        return userValue;
    }


    /**
     * {@link #get(Object, Callable)} 方法加载缓存值的包装异常
     */
    public class LoaderCacheValueException extends RuntimeException {

        private final Object key;

        public LoaderCacheValueException(Object key, Callable<?> loader, Throwable ex) {
            super(String.format("Value for key '%s' could not be loaded using '%s'", key, loader), ex);
            this.key = key;
        }

        public Object getKey() {
            return this.key;
        }
    }
}
