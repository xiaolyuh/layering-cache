/*
 * Copyright 2002-2014 the original author or authors.
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

import java.util.Collection;

/**
 * 缓存管理器
 * 允许通过缓存名称来获的对应的 {@link Cache}.
 *
 * @author yuhao.wang3
 */
public interface CacheManager {

    /**
     * 根据缓存名称返回对应的{@link Cache}.
     *
     * @param name 缓存的名称 (不能为 {@code null})
     * @return 返回对应名称的Cache, 如果没找到返回 {@code null}
     */
    Cache getCache(String name);

    /**
     * 获取所有缓存名称的集合
     *
     * @return 所有缓存名称的集合
     */
    Collection<String> getCacheNames();

}
