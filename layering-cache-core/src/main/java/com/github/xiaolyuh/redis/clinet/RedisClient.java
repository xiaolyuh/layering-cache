package com.github.xiaolyuh.redis.clinet;

import com.github.xiaolyuh.listener.RedisMessageListener;
import com.github.xiaolyuh.redis.serializer.RedisSerializer;
import com.github.xiaolyuh.util.StringUtils;

import io.lettuce.core.KeyValue;
import io.lettuce.core.ScriptOutputType;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis 客户端
 *
 * @author olafwang
 */
public interface RedisClient {

    /**
     * 获取RedisClient实例
     *
     * @param redisProperties redis配置
     * @return RedisClient
     */
    static RedisClient getInstance(RedisProperties redisProperties) {
        RedisClient redisClient;
        if (StringUtils.isNotBlank(redisProperties.getCluster())) {
            redisClient = new ClusterRedisClient(redisProperties);
        } else if (StringUtils.isNotBlank(redisProperties.getSentinelNodes())) {
            redisClient = new SentinelRedisClient(redisProperties);
        } else {
            redisClient = new SingleRedisClient(redisProperties);
        }
        return redisClient;
    }

    /**
     * 通过key获取储存在redis中的value,自动转对象
     *
     * @param key        key
     * @param resultType 返回值类型对应的Class对象
     * @param <T>        返回值类型
     * @return 成功返回value 失败返回null
     * @author manddoxli
     */
    <T> T get(String key, Class<T> resultType);

    /**
     * 通过key获取储存在redis中的value,自动转对象
     *
     * @param key                  key
     * @param resultType           返回值类型对应的Class对象
     * @param valueRedisSerializer 指定序列化器
     * @param <T>                  返回值类型
     * @return 成功返回value 失败返回null
     * @author manddoxli
     */
    <T> T get(String key, Class<T> resultType, RedisSerializer valueRedisSerializer);

    /**
     * 通过key获取储存在redis中的value,自动转对象
     *
     * @param keys        keys
     * @param resultType 返回值类型对应的Class对象
     * @param <T>        返回值类型
     * @return List<KeyValue<String,Object>>  Key - Value
     */
     <T> List<KeyValue<String,Object>> getAll(List<String> keys, Class<T> resultType);

    /**
     * 通过key获取储存在redis中的value,自动转对象
     *
     * @param keys                  key
     * @param resultType           返回值类型对应的Class对象
     * @param valueRedisSerializer 指定序列化器
     * @param <T>                  返回值类型
     * @return List<KeyValue<String,Object>>  Key - Value
     */
     <T> List<KeyValue<String,Object>> getAll(List<String> keys, Class<T> resultType,RedisSerializer valueRedisSerializer);

    /**
     * <p>
     * 向redis存入key和value,并释放连接资源
     * </p>
     * <p>
     * 如果key已经存在 则覆盖
     * </p>
     *
     * @param key   key
     * @param value value
     * @return 成功 返回OK 失败返回 0
     */
    String set(String key, Object value);

    /**
     * <p>
     * 向redis存入key和value,并释放连接资源
     * </p>
     * <p>
     * 如果key已经存在 则覆盖
     * </p>
     *
     * @param key   key
     * @param value value
     * @param time  时间
     * @param unit  时间单位
     * @return 成功 返回OK 失败返回 0
     */
    String set(String key, Object value, long time, TimeUnit unit);


    /**
     *
     * 批量向redis存入key和value,如果key已经存在 则覆盖
     *
     * @param keyValues  {@link KeyValue}
     * @param time  时间
     * @param unit  时间单位
     * @return 成功 返回OK 失败返回 0
     */
    List<String> batchSet(List<KeyValue<String,Object>> keyValues, long time, TimeUnit unit);

    /**
     * <p>
     * 向redis存入key和value,并释放连接资源
     * </p>
     * <p>
     * 如果key已经存在 则覆盖
     * </p>
     *
     * @param key                  key
     * @param value                value
     * @param time                 时间
     * @param unit                 时间单位
     * @param valueRedisSerializer 指定序列化器
     * @return 成功 返回OK 失败返回 0
     */
    String set(String key, Object value, long time, TimeUnit unit, RedisSerializer valueRedisSerializer);

    /**
     * Set the string value as value of the key. The string can't be longer than 1073741824 bytes (1
     * GB).
     *
     * @param key   key
     * @param value value
     * @param time  expire time in the units of <code>expx</code>
     * @return Status code reply
     */
    String setNxEx(final String key, final Object value, final long time);

    /**
     * <p>
     * 删除指定的key,也可以传入一个包含key的数组
     * </p>
     *
     * @param keys 一个key 也可以使 string 数组
     * @return 返回删除成功的个数
     */
    Long delete(String... keys);

    /**
     * <p>
     * 删除一批key
     * </p>
     *
     * @param keys key的Set集合
     * @return 返回删除成功的个数
     */
    Long delete(Set<String> keys);


    /**
     * <p>
     * 判断key是否存在
     * </p>
     *
     * @param key key
     * @return true OR false
     */
    Boolean hasKey(String key);

    /**
     * <p>
     * 为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。
     * </p>
     *
     * @param key      key
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     * @return 成功返回1 如果存在 和 发生异常 返回 0
     */
    Boolean expire(String key, long timeout, TimeUnit timeUnit);


    /**
     * <p>
     *
     * 批量给 keys 设置生存时间，当 keys 过期时(生存时间为 0 )，它会被自动删除。
     * </p>
     *
     * @param keys      keys
     * @param ttl  过期时间
     * @param timeUnit 时间单位
     * @return 成功返回1 如果存在 和 发生异常 返回 0
     */
    List<Boolean> batchExpire(List<String> keys, long ttl, TimeUnit timeUnit);


    /**
     * <p>
     * 以秒为单位，返回给定 key 的剩余生存时间
     * </p>
     *
     * @param key key
     * @return 当 key 不存在时或没有设置剩余生存时间时，返回 -1 。否则，以秒为单位，返回 key
     * 的剩余生存时间。 发生异常 返回 0
     */
    Long getExpire(String key);

    /**
     * <p>
     * 以秒为单位，返回给定 keys 的剩余生存时间
     * </p>
     *
     * @param keys keys
     * @return 批量返回对应key的剩余生存时间。当 key 不存在时或没有设置剩余生存时间时，返回 -1 。否则，以秒为单位，返回 key
     * 的剩余生存时间。 发生异常 返回 0
     */
    List<Long> getExpireBatch(List<String> keys);

    /**
     * <p>
     * 查询符合条件的key
     * </p>
     *
     * @param pattern 表达式
     * @return 返回符合条件的key
     */
    Set<String> scan(String pattern);

    /**
     * <p>
     * 通过key向list头部添加字符串
     * </p>
     *
     * @param key                  key
     * @param valueRedisSerializer 指定序列化器
     * @param values               可以使一个string 也可以使string数组
     * @return 返回list的value个数
     */
    Long lpush(String key, RedisSerializer valueRedisSerializer, String... values);

    /**
     * <p>
     * 通过key返回list的长度
     * </p>
     *
     * @param key key
     * @return long
     */
    Long llen(String key);

    /**
     * <p>
     * 通过key获取list指定下标位置的value
     * </p>
     * <p>
     * 如果start 为 0 end 为 -1 则返回全部的list中的value
     * </p>
     *
     * @param key                  key
     * @param start                起始位置
     * @param end                  结束位置
     * @param valueRedisSerializer 指定序列化器
     * @return List
     */
    List<String> lrange(String key, long start, long end, RedisSerializer valueRedisSerializer);

    /**
     * 执行Lua脚本
     *
     * @param script Lua 脚本
     * @param keys   参数
     * @param args   参数值
     * @return 返回结果
     */
    Object eval(String script, List<String> keys, List<String> args);


    /**
     * 执行Lua脚本
     *
     * @param script     Lua 脚本
     * @param returnType Lua 脚本返回值类型
     * @param keys       参数
     * @param args       参数值
     * @return 返回结果
     */
    Object eval(String script, ScriptOutputType returnType, List<String> keys, List<String> args);


    /**
     * 发送消息
     *
     * @param channel 发送消息的频道
     * @param message 消息内容
     * @return Long
     */
    Long publish(String channel, String message);

    /**
     * 绑定监听器
     *
     * @param messageListener 消息监听器
     * @param channel         信道
     */
    void subscribe(RedisMessageListener messageListener, String... channel);

    /**
     * key序列化方式
     *
     * @return the key {@link RedisSerializer}.
     */
    RedisSerializer getKeySerializer();

    /**
     * value序列化方式
     *
     * @return the value {@link RedisSerializer}.
     */
    RedisSerializer getValueSerializer();

    /**
     * 设置key的序列化方式
     *
     * @param keySerializer {@link RedisSerializer}
     */
    void setKeySerializer(RedisSerializer keySerializer);

    /**
     * 设置value的序列化方式
     *
     * @param valueSerializer {@link RedisSerializer}
     */
    void setValueSerializer(RedisSerializer valueSerializer);

}