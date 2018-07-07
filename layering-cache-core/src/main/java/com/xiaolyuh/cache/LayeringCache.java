package com.xiaolyuh.cache;

import com.alibaba.fastjson.JSON;
import com.xiaolyuh.support.AbstractValueAdaptingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * 多级缓存
 *
 * @author yuhao.wang
 */
public class LayeringCache extends AbstractValueAdaptingCache {
    Logger logger = LoggerFactory.getLogger(LayeringCache.class);

    /**
     * 一级缓存
     */
    private Cache firstCache;

    /**
     * 二级缓存
     */
    private Cache secondCache;

    /**
     * 是否使用一级缓存， 默认true
     */
    private boolean useFirstCache = true;

    /**
     * 创建一个多级缓存对象
     *
     * @param firstCache  一级缓存
     * @param secondCache 二级缓存
     */
    public LayeringCache(Cache firstCache, Cache secondCache) {
        this(firstCache, secondCache, true, secondCache.getName());
    }

    /**
     * 创建一个多级缓存对象
     *
     * @param firstCache    一级缓存
     * @param secondCache   二级缓存
     * @param useFirstCache 是否使用一级缓存，默认是
     * @param name          缓存名称
     */
    public LayeringCache(Cache firstCache, Cache secondCache, boolean useFirstCache, String name) {
        super(true, name);
        this.firstCache = firstCache;
        this.secondCache = secondCache;
        this.useFirstCache = useFirstCache;
    }

    @Override
    public LayeringCache getNativeCache() {
        return this;
    }

    @Override
    public Object get(Object key) {
        Object result = null;
        if (useFirstCache) {
            result = firstCache.get(key);
            logger.info("查询一级缓存。 key:{},返回值是:{}", key, JSON.toJSONString(result));
        }
        if (result == null) {
            result = secondCache.get(key);
            firstCache.putIfAbsent(key, result);
            logger.info("查询二级缓存,并将数据放到一级缓存。 key:{},返回值是:{}", key, JSON.toJSONString(result));
        }
        return fromStoreValue(result);
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        if (useFirstCache) {
            Object result = firstCache.get(key);
            logger.info("查询一级缓存。 key:{},返回值是:{}", key, JSON.toJSONString(result));
            // 有可能是缓存的是 @{link NullValue.INSTANCE}值，所以需要双重if判断
            if (result != null) {
                result = fromStoreValue(result);
                // 判断返回值类型
                if (result != null && type != null && !type.isInstance(result)) {
                    throw new IllegalStateException("缓存的值不是需要的 [" + type.getName() + "] 类型: " + result);
                }
                return (T) result;
            }
        }

        T result = secondCache.get(key, type);
        firstCache.putIfAbsent(key, result);
        logger.info("查询二级缓存,并将数据放到一级缓存。 key:{},返回值是:{}", key, JSON.toJSONString(result));
        return result;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        if (useFirstCache) {
            Object result = firstCache.get(key);
            logger.info("查询一级缓存。 key:{},返回值是:{}", key, JSON.toJSONString(result));
            if (result != null) {
                return (T) fromStoreValue(result);
            }
        }

        T result = secondCache.get(key, valueLoader);
        firstCache.putIfAbsent(key, result);
        logger.info("查询二级缓存,并将数据放到一级缓存。 key:{},返回值是:{}", key, JSON.toJSONString(result));
        return result;
    }

    @Override
    public void put(Object key, Object value) {
        if (useFirstCache) {
            firstCache.put(key, value);
        }
        secondCache.put(key, value);
    }

    @Override
    public Object putIfAbsent(Object key, Object value) {
        if (useFirstCache) {
            firstCache.putIfAbsent(key, value);
        }
        return secondCache.putIfAbsent(key, value);
    }

    @Override
    public void evict(Object key) {
        // 删除的时候要先删除二级缓存再删除一级缓存，否则有并发问题
        secondCache.evict(key);
        // 删除一级缓存
        if (useFirstCache) {
            // TODO 删除一级缓存需要用到redis的Pub/Sub（订阅/发布）模式，否则集群中其他服服务器节点的一级缓存数据无法删除
//            Map<String, Object> message = new HashMap<>();
//            message.put("cacheName", name);
//            message.put("key", key);
//            // 创建redis发布者
//            RedisPublisher redisPublisher = new RedisPublisher(redisOperations, ChannelTopicEnum.REDIS_CACHE_DELETE_TOPIC.getChannelTopic());
//            // 发布消息
//            redisPublisher.publisher(message);
        }
    }

    @Override
    public void clear() {
        // 删除的时候要先删除二级缓存再删除一级缓存，否则有并发问题
        secondCache.clear();
        if (useFirstCache) {
            // TODO 清除一级缓存需要用到redis的订阅/发布模式，否则集群中其他服服务器节点的一级缓存数据无法删除
//            Map<String, Object> message = new HashMap<>();
//            message.put("cacheName", name);
//            // 创建redis发布者
//            RedisPublisher redisPublisher = new RedisPublisher(redisOperations, ChannelTopicEnum.REDIS_CACHE_CLEAR_TOPIC.getChannelTopic());
//            // 发布消息
//            redisPublisher.publisher(message);
        }
    }

}
