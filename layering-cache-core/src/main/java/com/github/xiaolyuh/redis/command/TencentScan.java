package com.github.xiaolyuh.redis.command;

import io.lettuce.core.dynamic.Commands;
import io.lettuce.core.dynamic.annotation.Command;
import java.util.List;

/**
 * 腾讯云redis scan命令
 *
 * @author olafwang
 */
public interface TencentScan extends Commands {

    @Command("scan ?0 match ?1 count ?2 ?3")
    List<Object> scan(long cursor, String pattern, int count, String nodeId);
}