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
import com.xiaolyuh.manager.CacheManager;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Abstract base class implementing the common {@link CacheManager} methods.
 * Useful for 'static' environments where the backing caches do not change.
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 3.1
 */
public abstract class AbstractCacheManager implements CacheManager {

    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>(16);

    private volatile Set<String> cacheNames = Collections.emptySet();

    // Lazy cache initialization on access
    @Override
    public Cache getCache(String name) {
        Cache cache = this.cacheMap.get(name);
        if (cache != null) {
            return cache;
        } else {
            // Fully synchronize now for missing cache creation...
            synchronized (this.cacheMap) {
                cache = this.cacheMap.get(name);
                if (cache == null) {
                    cache = getMissingCache(name);
                    if (cache != null) {
                        cache = decorateCache(cache);
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
     * Update the exposed {@link #cacheNames} set with the given name.
     * <p>This will always be called within a full {@link #cacheMap} lock
     * and effectively behaves like a {@code CopyOnWriteArraySet} with
     * preserved order but exposed as an unmodifiable reference.
     *
     * @param name the name of the cache to be added
     */
    private void updateCacheNames(String name) {
        Set<String> cacheNames = new LinkedHashSet<String>(this.cacheNames.size() + 1);
        cacheNames.addAll(this.cacheNames);
        cacheNames.add(name);
        this.cacheNames = Collections.unmodifiableSet(cacheNames);
    }


    // Overridable template methods for cache initialization

    /**
     * Decorate the given Cache object if necessary.
     *
     * @param cache the Cache object to be added to this CacheManager
     * @return the decorated Cache object to be used instead,
     * or simply the passed-in Cache object by default
     */
    protected Cache decorateCache(Cache cache) {
        return cache;
    }

    /**
     * Return a missing cache with the specified {@code name} or {@code null} if
     * such cache does not exist or could not be created on the fly.
     * <p>Some caches may be created at runtime if the native provider supports
     * it. If a lookup by name does not yield any result, a subclass gets a chance
     * to register such a cache at runtime. The returned cache will be automatically
     * added to this instance.
     *
     * @param name the name of the cache to retrieve
     * @return the missing cache or {@code null} if no such cache exists or could be
     * created
     * @see #getCache(String)
     * @since 4.1
     */
    protected Cache getMissingCache(String name) {
        return null;
    }

    /**
     * 获取缓存容器
     *
     * @return 返回缓存容器
     */
    protected ConcurrentMap<String, Cache> getCacheMap() {
        return cacheMap;
    }

}
