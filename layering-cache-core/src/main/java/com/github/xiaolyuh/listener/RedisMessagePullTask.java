package com.github.xiaolyuh.listener;

import com.github.xiaolyuh.manager.AbstractCacheManager;
import com.github.xiaolyuh.util.BeanFactory;
import com.github.xiaolyuh.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * redis消息拉模式
 *
 * @author yuhao.wang
 */
public class RedisMessagePullTask {
    private static final Logger log = LoggerFactory.getLogger(RedisMessagePullTask.class);

    /**
     * 定时任务线程池
     */
    private static final ScheduledThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(3, new NamedThreadFactory("layering-cache-pull-message"));

    /**
     * redis消息处理器
     */
    private RedisMessageService redisMessageService;

    public void init(AbstractCacheManager cacheManager) {
        Random random = new Random();
        int initialDelay = Math.abs(random.nextInt()) % 23 + 1;
        int delay = Math.abs(random.nextInt()) % 7 + 1;
        log.info("一级缓存拉模式同步消息每隔{}秒，执行一次", delay);
        redisMessageService = BeanFactory.getBean(RedisMessageService.class).init(cacheManager);
        // 1. 服务启动同步最新的偏移量
        BeanFactory.getBean(RedisMessageService.class).syncOffset();
        // 2. 启动PULL TASK
        startPullTask(initialDelay, delay);
        // 3. 启动重置本地偏消息移量任务
        clearMessageQueueTask();
        // 4. 重连检测
        reconnectionTask(initialDelay, delay);
    }

    /**
     * 启动PULL TASK
     */
    private void startPullTask(int initialDelay, int delay) {
        EXECUTOR.scheduleWithFixedDelay(() -> {
            try {
                redisMessageService.pullMessage();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("layering-cache PULL 方式清楚一级缓存异常：{}", e.getMessage(), e);
            }
            //  初始时间间隔是7秒
        }, initialDelay, delay, TimeUnit.SECONDS);
    }

    /**
     * 启动清空消息队列的任务
     */
    private void clearMessageQueueTask() {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 3);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long initialDelay = System.currentTimeMillis() - cal.getTimeInMillis();
        initialDelay = initialDelay > 0 ? initialDelay : 1;
        // 每天晚上凌晨3:00执行任务
        EXECUTOR.scheduleWithFixedDelay(() -> {
            try {
                redisMessageService.clearMessageQueue();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("layering-cache 重置本地消息偏移量异常：{}", e.getMessage(), e);
            }
        }, initialDelay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);

    }

    /**
     * 启动重连pub/sub检查
     */
    private void reconnectionTask(int initialDelay, int delay) {
        EXECUTOR.scheduleWithFixedDelay(() -> redisMessageService.reconnection(),
                initialDelay, delay, TimeUnit.SECONDS);
    }
}
