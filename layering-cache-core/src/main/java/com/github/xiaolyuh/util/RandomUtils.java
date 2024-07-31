package com.github.xiaolyuh.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * RandomUtils
 *
 * @author heyirui
 */
public class RandomUtils {


    /**
     *   生成带有随机浮动的预加载时间
     * @param basePreloadTime basePreloadTime
     * @return 带有随机浮动的预加载时间
     */
    public static long getRandomPreloadTime(long basePreloadTime) {
        double randomFactor = ThreadLocalRandom.current().nextDouble(0.9, 1.1);
        return (long) (basePreloadTime * randomFactor);
    }

}
