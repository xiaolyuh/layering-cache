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

package com.xiaolyuh.manager;

import com.xiaolyuh.cache.Cache;
import com.xiaolyuh.listener.RedisMessageListener;
import com.xiaolyuh.setting.LayeringCacheSetting;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 公共的抽象 {@link CacheManager} 的实现.
 *
 * @author yuhao.wang3
 */
public abstract class AbstractCacheManager implements CacheManager, InitializingBean {

    /**
     * redis pub/sub 容器
     */
    private final RedisMessageListenerContainer container = new RedisMessageListenerContainer();

    /**
     * redis pub/sub 监听器
     */
    private RedisMessageListener messageListener = new RedisMessageListener();

    /**
     * 缓存容器
     */
    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>(16);

    /**
     * 缓存名称容器
     */
    private volatile Set<String> cacheNames = new LinkedHashSet<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        messageListener.setCacheManager(this);
        container.setConnectionFactory(getRedisTemplate().getConnectionFactory());
    }

    @Override
    public Cache getCache(String name) {
        return this.cacheMap.get(name);
    }

    // Lazy cache initialization on access
    @Override
    public Cache getCache(String name, LayeringCacheSetting layeringCacheSetting) {
        Cache cache = this.cacheMap.get(name);
        if (cache != null) {
            return cache;
        } else {
            // Fully synchronize now for missing cache creation...
            synchronized (this.cacheMap) {
                cache = this.cacheMap.get(name);
                if (cache == null) {
                    cache = getMissingCache(name, layeringCacheSetting);
                    if (cache != null) {
                        cache = decorateCache(cache);
                        addMessageListener(name);
                        this.cacheMap.put(name, cache);
                        updateCacheNames(name);
                    }
                }
                return cache;
            }
        }
    }

    @Override
    public Collection<String> getCacheNames() {
        return this.cacheNames;
    }

    /**
     * 更新缓存名称容器
     *
     * @param name 需要添加的缓存名称
     */
    private void updateCacheNames(String name) {
        cacheNames.add(name);
    }


    /**
     * 获取Cache对象的装饰示例
     *
     * @param cache 需要添加到CacheManager的Cache实例
     * @return 装饰过后的Cache实例
     */
    protected Cache decorateCache(Cache cache) {
        return cache;
    }

    /**
     * 根据缓存名称在CacheManager中没有找到对应Cache时，通过该方法新建一个对应的Cache实例
     *
     * @param name 缓存名称
     * @return {@link Cache}
     */
    protected abstract Cache getMissingCache(String name, LayeringCacheSetting layeringCacheSetting);

    /**
     * 获取缓存容器
     *
     * @return 返回缓存容器
     */
    protected ConcurrentMap<String, Cache> getCacheMap() {
        return cacheMap;
    }

    /**
     * 获取redis客户端
     *
     * @return {@link RedisTemplate}
     */
    protected abstract RedisTemplate<String, Object> getRedisTemplate();

    /**
     * 添加消息监听
     *
     * @param name 缓存名称
     */
    protected void addMessageListener(String name) {
        container.addMessageListener(messageListener, new ChannelTopic(name));
    }
}
