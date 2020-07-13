package com.github.xiaolyuh.redis.clinet;

import com.github.xiaolyuh.util.StringUtils;
import lombok.Data;
import redis.clients.jedis.HostAndPort;

import java.util.HashSet;
import java.util.Set;

@Data
public class RedisProperties {
    Integer database = 0;
    String cluster = "";
    String host = "localhost";
    Integer port = 6379;
    String password = null;

    /**
     * 最大连接数
     */
    Integer maxTotal = 20;
    /**
     * 最大空闲连接数
     */
    Integer maxIdle = 20;

    /**
     * 最小连接数
     */
    Integer minIdle = 15;

    /**
     * 获取连接时的最大等待毫秒数,小于零:阻塞不确定的时间
     */
    Long maxWaitMillis = 1000L;
    /**
     * 在获取连接的时候检查有效性
     */
    Boolean testOnBorrow = false;

    public Set<HostAndPort> getHostAndPorts(){
        Set<HostAndPort> hostAndPorts = new HashSet<>();
        if(StringUtils.isEmpty(cluster)){
            HostAndPort hap = new HostAndPort(host,port);
            hostAndPorts.add(hap);
        }else{
            String[] adds = cluster.split(",");
            for (String add : adds) {
                hostAndPorts.add(parse(add));
            }
        }
        return hostAndPorts;
    }

    public HostAndPort parse(String hapStr){
        String[] hap = hapStr.trim().split(":");
        return new HostAndPort(hap[0],Integer.parseInt(hap[1]));
    }
}