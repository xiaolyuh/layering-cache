package com.github.xiaolyuh.util;

import com.github.xiaolyuh.support.MdcThreadPoolTaskExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池
 *
 * @author yuhao.wang3
 */
public class ThreadTaskUtils {
    private static MdcThreadPoolTaskExecutor refreshTaskExecutor = null;
    private static MdcThreadPoolTaskExecutor deleteTaskExecutor = null;

    static {
        refreshTaskExecutor = new MdcThreadPoolTaskExecutor();
        // 核心线程数
        refreshTaskExecutor.setCorePoolSize(8);
        // 最大线程数
        refreshTaskExecutor.setMaxPoolSize(64);
        // 队列最大长度
        refreshTaskExecutor.setQueueCapacity(1000);
        // 线程池维护线程所允许的空闲时间(单位秒)
        refreshTaskExecutor.setKeepAliveSeconds(120);
        refreshTaskExecutor.setThreadNamePrefix("layering-cache-refresh-thread");
        /*
         * 线程池对拒绝任务(无限程可用)的处理策略
         * ThreadPoolExecutor.AbortPolicy:丢弃任务并抛出RejectedExecutionException异常。
         * ThreadPoolExecutor.DiscardPolicy：也是丢弃任务，但是不抛出异常。
         * ThreadPoolExecutor.DiscardOldestPolicy：丢弃队列最前面的任务，然后重新尝试执行任务（重复此过程）
         * ThreadPoolExecutor.CallerRunsPolicy：由调用线程处理该任务,如果执行器已关闭,则丢弃.
         */
        refreshTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        refreshTaskExecutor.initialize();


        deleteTaskExecutor = new MdcThreadPoolTaskExecutor();
        // 核心线程数
        deleteTaskExecutor.setCorePoolSize(8);
        // 最大线程数
        deleteTaskExecutor.setMaxPoolSize(64);
        // 队列最大长度
        deleteTaskExecutor.setQueueCapacity(1000);
        // 线程池维护线程所允许的空闲时间(单位秒)
        deleteTaskExecutor.setKeepAliveSeconds(120);
        deleteTaskExecutor.setThreadNamePrefix("layering-cache-delete-thread");
        deleteTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        deleteTaskExecutor.initialize();
    }

    public static void refreshCacheRun(Runnable runnable) {
        refreshTaskExecutor.execute(runnable);
    }

    public static void deleteCacheRun(Runnable runnable) {
        deleteTaskExecutor.execute(runnable);
    }
}
