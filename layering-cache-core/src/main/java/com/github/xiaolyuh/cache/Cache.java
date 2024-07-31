package com.github.xiaolyuh.cache;

import com.github.xiaolyuh.manager.CacheManager;
import com.github.xiaolyuh.stats.CacheStats;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * 缓存的顶级接口
 *
 * @author yuhao.wang
 */
public interface Cache {

    /**
     * 返回缓存名称
     *
     * @return String
     */
    String getName();

    /**
     * 返回真实Cache对象
     *
     * @return Object
     */
    Object getNativeCache();

    /**
     * 根据KEY返回缓存中对应的值，并将其返回类型转换成对应类型，如果对应key不存在返回NULL
     *
     * @param key        缓存key
     * @param resultType 返回值类型
     * @param <T>        Object
     * @return 缓存key对应的值
     */
    <T> T get(String key, Class<T> resultType);

    /**
     * 根据KEY返回缓存中对应的值，并将其返回类型转换成对应类型，如果对应key不存在则调用valueLoader加载数据
     *
     * @param key         缓存key
     * @param resultType  返回值类型
     * @param valueLoader 加载缓存的回调方法
     * @param <T>         Object
     * @return 缓存key对应的值
     */
    <T> T get(String key, Class<T> resultType, Callable<T> valueLoader);

    /**
     * 根据KEY返回缓存中对应的值，并将其返回类型转换成对应类型。只返回缓存中存在的KEY-VALUE对
     *
     * @param keys       缓存keys
     * @param resultType 返回值类型
     * @param <K>        键的类型
     * @param <V>        值的类型
     * @return 缓存key对应的值
     */
    <K, V> Map<K, V> getAll(List<String> keys, Class<V> resultType);

    /**
     * 根据KEY返回缓存中对应的值，并将其返回类型转换成对应类型，如果对应key不存在则调用valueLoader加载数据
     *
     * @param keys        缓存keys
     * @param resultType  返回值类型
     * @param valueLoader 加载缓存的回调方法
     * @param <K>         键的类型
     * @param <V>         值的类型
     * @return 缓存key对应的值
     */
    <K, V> Map<K, V> getAll(List<String> keys, Class<V> resultType, Function<String[], Object> valueLoader);

    /**
     * 将对应key-value放到缓存，如果key原来有值就直接覆盖
     *
     * @param key   缓存key
     * @param value 缓存的值
     */
    void put(String key, Object value);

    /**
     * 如果缓存key没有对应的值就将值put到缓存，如果有就直接返回原有的值
     * <p>就相当于:
     * <pre><code>
     * Object existingValue = cache.get(key);
     * if (existingValue == null) {
     *     cache.put(key, value);
     *     return null;
     * } else {
     *     return existingValue;
     * }
     * </code></pre>
     * except that the action is performed atomically. While all out-of-the-box
     * {@link CacheManager} implementations are able to perform the put atomically,
     * the operation may also be implemented in two steps, e.g. with a check for
     * presence and a subsequent put, in a non-atomic way. Check the documentation
     * of the native cache implementation that you are using for more details.
     *
     * @param key        缓存key
     * @param value      缓存key对应的值
     * @param resultType 返回值类型
     * @param <T>        T
     * @return 因为值本身可能为NULL，或者缓存key本来就没有对应值的时候也为NULL，
     * 所以如果返回NULL就表示已经将key-value键值对放到了缓存中
     * @since 4.1
     */
    <T> T putIfAbsent(String key, Object value, Class<T> resultType);

    /**
     * 在缓存中删除对应的key
     *
     * @param key 缓存key
     */
    void evict(String key);

    /**
     * 在缓存中删除对应的keys
     *
     * @param keys 缓存keys
     */
    void evictAll(List<String> keys);

    /**
     * 清楚缓存
     */
    void clear();

    /**
     * 获取统计信息
     *
     * @return {@link CacheStats}
     */
    CacheStats getCacheStats();

    /**
     * 缓存预估size
     *
     * @return 预估大小
     */
    default long estimatedSize() {
        return 0;
    }
}
