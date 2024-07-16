package com.github.xiaolyuh.listener;

import com.alibaba.fastjson.JSON;
import com.github.xiaolyuh.cache.Cache;
import com.github.xiaolyuh.cache.LayeringCache;
import com.github.xiaolyuh.manager.AbstractCacheManager;
import com.github.xiaolyuh.redis.clinet.RedisClient;
import com.github.xiaolyuh.support.LayeringCacheRedisLock;
import com.github.xiaolyuh.util.BeanFactory;
import com.github.xiaolyuh.util.GlobalConfig;
import com.github.xiaolyuh.util.StringUtils;
import io.lettuce.core.ScriptOutputType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * 拉消息模式
 *
 * @author yuhao.wang
 */
public class RedisMessageService {
    private static final Logger logger = LoggerFactory.getLogger(RedisMessageService.class);

    /**
     * 本地消息偏移量
     */
    private static final AtomicLong OFFSET = new AtomicLong(-1);

    /**
     * 最后一次处理推消息的时间搓，忽略并发情况下的误差，只保证可见性即可
     */
    private static volatile long LAST_PUSH_TIME = 0L;

    /**
     * 最后一次处理拉消息的时间搓，忽略并发情况下的误差，只保证可见性即可
     */
    private static volatile long LAST_PULL_TIME = 0L;

    /**
     * pub/sub 重连间隔时间
     */
    public static final long RECONNECTION_TIME = 10 * 1000;

    /**
     * 缓存管理器
     */
    private AbstractCacheManager cacheManager;

    public static final String LUA_SCRIPT =
            "local messageKey = KEYS[1] " +
                    "local oldOffset = tonumber(ARGV[1]) " +
                    "local maxOffset = redis.call('llen', messageKey) - 1 " +
                    "if maxOffset < 0 then " +
                    "    return { maxOffset, {} } " +
                    "end " +
                    "if oldOffset >= maxOffset then " +
                    "    return { maxOffset, {} } " +
                    "end " +
                    "local messages = redis.call('lrange', messageKey, 0, maxOffset - oldOffset - 1) " +
                    "return { maxOffset, messages }";

    public RedisMessageService init(AbstractCacheManager cacheManager) {
        this.cacheManager = cacheManager;
        return this;
    }

    /**
     * 拉消息
     */
    public void pullMessage() {
        RedisClient redisClient = cacheManager.getRedisClient();
        String messageRedisKey = GlobalConfig.getMessageRedisKey();
        List<byte[]> messages = null;
        synchronized (messageRedisKey) {
            long oldOffset = OFFSET.get();
            List<String> keys = Collections.singletonList(messageRedisKey);
            List<String> args = Collections.singletonList(String.valueOf(oldOffset));
            // issues/85 解决llen和lrange的原子性
            List<Object> result = (List<Object>) redisClient.eval(LUA_SCRIPT, ScriptOutputType.MULTI, keys, args);
            if (CollectionUtils.isEmpty(result) || result.size() < 2) {
                throw new RuntimeException("拉取清除一级缓存的消息失败，Lua表达式执行错误");
            }
            long maxOffset = (Long) result.get(0);
            messages = (List<byte[]>) result.get(1);
            // 没有消息
            if (maxOffset < 0 || CollectionUtils.isEmpty(messages)) {
                return;
            }
            // 更新本地消息偏移量
            OFFSET.set(maxOffset > 0 ? maxOffset : 0);
            RedisMessageService.updateLastPullTime();
        }

        for (byte[] messageByteArray : messages) {
            // 将字节数组转换为字符串
            String message = GlobalConfig.GLOBAL_REDIS_SERIALIZER.deserialize(messageByteArray, String.class);
            if (logger.isDebugEnabled()) {
                logger.debug("redis 通过PULL方式处理本地缓，消息内容：{}", message);
            }
            if (StringUtils.isBlank(message)) {
                continue;
            }

            RedisPubSubMessage redisPubSubMessage = JSON.parseObject(message, RedisPubSubMessage.class);
            // 根据缓存名称获取多级缓存，可能有多个
            Collection<Cache> caches = cacheManager.getCache(redisPubSubMessage.getCacheName());
            for (Cache cache : caches) {
                // 判断缓存是否是多级缓存
                if (cache instanceof LayeringCache) {
                    // 删除二级缓存
                    removeSecondCache(redisClient, redisPubSubMessage, (LayeringCache) cache);

                    // 删除一级缓存数据
                    removeFirstCache(redisPubSubMessage, (LayeringCache) cache);
                }
            }
        }
    }

    /**
     * 删除一级缓存数据
     *
     * @param redisPubSubMessage RedisPubSubMessage
     * @param cache              LayeringCache
     */
    private void removeFirstCache(RedisPubSubMessage redisPubSubMessage, LayeringCache cache) {
        switch (redisPubSubMessage.getMessageType()) {
            case EVICT:
                // 获取一级缓存，并删除一级缓存数据
                cache.getFirstCache().evict(redisPubSubMessage.getKey());
                logger.info("删除一级缓存 {} 数据,key={}", redisPubSubMessage.getCacheName(), redisPubSubMessage.getKey());
                break;

            case CLEAR:
                // 获取一级缓存，并删除一级缓存数据
                cache.getFirstCache().clear();
                logger.info("清除一级缓存 {} 数据", redisPubSubMessage.getCacheName());
                break;

            default:
                logger.error("接收到没有定义的消息数据");
                break;
        }
    }

    /**
     * 删除二级缓存
     *
     * @param redisClient        RedisClient
     * @param redisPubSubMessage RedisPubSubMessage
     * @param cache              LayeringCache
     */
    private void removeSecondCache(RedisClient redisClient, RedisPubSubMessage redisPubSubMessage, LayeringCache cache) {
        if (!RedisPubSubMessage.SOURCE.equals(redisPubSubMessage.getSource())) {
            // 只有通过管理页面过来的操作才需要删除二级缓存
            return;
        }
        String lockKey = RedisPubSubMessageType.EVICT.equals(redisPubSubMessage.getMessageType()) ? redisPubSubMessage.getKey() : redisPubSubMessage.getCacheName();
        LayeringCacheRedisLock redisLock = new LayeringCacheRedisLock(redisClient, lockKey + "_remove_lock");
        try {
            if (redisLock.lock()) {
                switch (redisPubSubMessage.getMessageType()) {
                    case EVICT:
                        cache.getSecondCache().evict(redisPubSubMessage.getKey());
                        logger.info("删除二级缓存 {} 数据,key={}", redisPubSubMessage.getCacheName(), redisPubSubMessage.getKey());
                        break;
                    case CLEAR:
                        cache.getSecondCache().clear();
                        logger.info("清除二级级缓存 {} 数据", redisPubSubMessage.getCacheName());
                        break;
                    default:
                        logger.error("接收到没有定义的消息数据");
                        break;
                }
                // 防止消息传输的时间差
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            redisLock.unlock();
        }
    }

    /**
     * 清空消息队列
     */
    public void clearMessageQueue() {
        LayeringCacheRedisLock lock = new LayeringCacheRedisLock(cacheManager.getRedisClient(), GlobalConfig.getMessageRedisKey(), 60);
        if (lock.lock()) {
            // 清空消息，直接删除key（不可以调换顺序）
            cacheManager.getRedisClient().delete(GlobalConfig.getMessageRedisKey());
        }
        // 重置偏移量
        OFFSET.getAndSet(-1);
    }

    /**
     * 同步offset
     */
    public void syncOffset() {
        // 1. 同步offset
        long maxOffset = cacheManager.getRedisClient().llen(GlobalConfig.getMessageRedisKey()) - 1;
        if (maxOffset < 0) {
            return;
        }
        OFFSET.getAndSet(maxOffset > 0 ? maxOffset : 0);
    }

    /**
     * 启动重连pub/sub检查
     */
    public void reconnection() {
        long time = LAST_PULL_TIME - LAST_PUSH_TIME;
        if (time >= RECONNECTION_TIME) {
            try {
                RedisMessageService.updateLastPushTime();
                //  redis pub/sub 监听器
                BeanFactory.getBean(RedisMessageListener.class).init(cacheManager);

            } catch (Exception e) {
                logger.error("layering-cache 清楚一级缓存异常：{}", e.getMessage(), e);
            }
        }
    }


    /**
     * 更新最后一次处理拉消息的时间
     */
    public static void updateLastPullTime() {
        LAST_PULL_TIME = System.currentTimeMillis();
    }

    /**
     * 更新最后一次处理推消息的时间
     */
    public static void updateLastPushTime() {
        LAST_PUSH_TIME = System.currentTimeMillis();
    }
}
