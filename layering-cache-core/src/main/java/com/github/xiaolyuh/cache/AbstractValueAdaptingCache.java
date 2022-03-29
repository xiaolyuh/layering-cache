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

package com.github.xiaolyuh.cache;

import com.alibaba.fastjson.JSON;
import com.github.xiaolyuh.listener.RedisPubSubMessage;
import com.github.xiaolyuh.listener.RedisPubSubMessageType;
import com.github.xiaolyuh.listener.RedisPublisher;
import com.github.xiaolyuh.redis.clinet.RedisClient;
import com.github.xiaolyuh.stats.CacheStats;
import com.github.xiaolyuh.support.CacheMode;
import com.github.xiaolyuh.support.NullValue;
import org.springframework.util.Assert;

import java.util.concurrent.Callable;


/**
 * Cache 接口的抽象实现类，对公共的方法做了一写实现，如是否允许存NULL值
 * <p>如果允许为NULL值，则需要在内部将NULL替换成{@link NullValue#INSTANCE} 对象
 * *
 *
 * @author yuhao.wang3
 */
public abstract class AbstractValueAdaptingCache implements Cache {

    /**
     * 缓存名称
     */
    private final String name;

    /**
     * 是否开启统计功能
     */
    private boolean stats;

    /**
     * 缓存统计类
     */
    private CacheStats cacheStats = new CacheStats();

    /**
     * 缓存模式
     */
    final CacheMode cacheMode;

    /**
     * 通过构造方法设置缓存配置
     *
     * @param stats 是否开启监控统计
     * @param name  缓存名称
     */
    protected AbstractValueAdaptingCache(boolean stats, String name, CacheMode cacheMode) {
        Assert.notNull(name, "缓存名称不能为NULL");
        this.stats = stats;
        this.name = name;
        this.cacheMode = cacheMode;
    }

    /**
     * 获取是否允许存NULL值
     *
     * @return true:允许，false:不允许
     */
    public abstract boolean isAllowNullValues();

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Convert the given value from the internal store to a user value
     * returned from the get method (adapting {@code null}).
     *
     * @param storeValue the store value
     * @return the value to return to the user
     */
    protected Object fromStoreValue(Object storeValue) {
        if (isAllowNullValues() && storeValue instanceof NullValue) {
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
        if (isAllowNullValues() && userValue == null) {
            return NullValue.INSTANCE;
        }
        return userValue;
    }


    /**
     * {@link #get(String, Class, Callable)} 方法加载缓存值的包装异常
     */
    public class LoaderCacheValueException extends RuntimeException {

        private final Object key;

        public LoaderCacheValueException(Object key, Throwable ex) {
            super(String.format("加载key为 %s 的缓存数据,执行被缓存方法异常", JSON.toJSONString(key)), ex);
            this.key = key;
        }

        public Object getKey() {
            return this.key;
        }
    }

    /**
     * 获取是否开启统计
     *
     * @return true：开启统计，false：关闭统计
     */
    public boolean isStats() {
        return stats;
    }

    /**
     * 获取统计信息
     *
     * @return CacheStats
     */
    @Override
    public CacheStats getCacheStats() {
        return cacheStats;
    }

    public void setCacheStats(CacheStats cacheStats) {
        this.cacheStats = cacheStats;
    }

    public void deleteFirstCacheByKey(String key, RedisClient redisClient) {
        // 删除一级缓存
        if (CacheMode.ALL.equals(cacheMode)) {
            // 删除一级缓存需要用到redis的Pub/Sub（订阅/发布）模式，否则集群中其他服服务器节点的一级缓存数据无法删除
            RedisPubSubMessage message = new RedisPubSubMessage();
            message.setCacheName(getName());
            message.setKey(key);
            message.setMessageType(RedisPubSubMessageType.EVICT);
            // 发布消息
            RedisPublisher.publisher(redisClient, message);
        }
    }

    public void clearFirstCache(RedisClient redisClient) {
        // 清除一级缓存
        if (CacheMode.ALL.equals(cacheMode)) {
            // 清除一级缓存需要用到redis的订阅/发布模式，否则集群中其他服服务器节点的一级缓存数据无法删除
            RedisPubSubMessage message = new RedisPubSubMessage();
            message.setCacheName(getName());
            message.setMessageType(RedisPubSubMessageType.CLEAR);
            // 发布消息
            RedisPublisher.publisher(redisClient, message);
        }
    }
}
