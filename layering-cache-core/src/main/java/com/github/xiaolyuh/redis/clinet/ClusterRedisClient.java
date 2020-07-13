package com.github.xiaolyuh.redis.clinet;

import com.github.xiaolyuh.listener.RedisMessageListener;
import com.github.xiaolyuh.redis.serializer.KryoRedisSerializer;
import com.github.xiaolyuh.redis.serializer.RedisSerializer;
import com.github.xiaolyuh.redis.serializer.SerializationException;
import com.github.xiaolyuh.redis.serializer.StringRedisSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.util.SafeEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 集群版redis缓存
 *
 * @author olafwang
 */
public class ClusterRedisClient implements RedisClient {
    Logger logger = LoggerFactory.getLogger(ClusterRedisClient.class);
    /**
     * 默认key序列化方式
     */
    private RedisSerializer keySerializer = new StringRedisSerializer();

    /**
     * 默认value序列化方式
     */
    private RedisSerializer valueSerializer = new KryoRedisSerializer(Object.class);

    private JedisCluster cluster;

    public ClusterRedisClient(RedisProperties properties) {

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(properties.getMaxTotal());
        jedisPoolConfig.setMaxIdle(properties.getMaxIdle());
        jedisPoolConfig.setMaxWaitMillis(properties.getMaxWaitMillis());
        jedisPoolConfig.setTestOnBorrow(properties.getTestOnBorrow());

        String password = properties.getPassword();

        cluster = new JedisCluster(properties.getHostAndPorts(), 5000, 5000, 5, password, jedisPoolConfig);
    }

    @Override
    public Object get(String key) {
        try {
            return cluster.get(getKeySerializer().serialize(key));
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public <T> T get(String key, Class<T> t) {
        return (T) get(key);
    }

    @Override
    public String set(String key, Object value) {
        try {
            return cluster.set(getKeySerializer().serialize(key), getValueSerializer().serialize(value));
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public String set(String key, Object value, long time, TimeUnit unit) {
        try {
            return cluster.setex(getKeySerializer().serialize(key), (int) unit.toSeconds(time), getValueSerializer().serialize(value));
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public String set(String key, Object value, String nxxx, String expx, long time) {
        try {
            return cluster.set(getKeySerializer().serialize(key), getValueSerializer().serialize(value), SafeEncoder.encode(nxxx), SafeEncoder.encode(expx), time);
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Long delete(String... keys) {
        if (Objects.nonNull(keys) && keys.length > 0) {
            try {
                final byte[][] bkeys = new byte[keys.length][];
                for (int i = 0; i < keys.length; i++) {
                    bkeys[i] = getKeySerializer().serialize(keys[i]);
                }
                return cluster.del(bkeys);
            } catch (SerializationException e) {
                throw e;
            } catch (Exception e) {
                throw new RedisClientException(e.getMessage(), e);
            }
        }
        return 0L;
    }


    @Override
    public Long delete(Set<String> keys) {

        return delete(keys.toArray(new String[0]));
    }

    @Override
    public Boolean hasKey(String key) {
        try {
            return cluster.exists(getKeySerializer().serialize(key));
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Long expire(String key, long timeout, TimeUnit timeUnit) {
        try {
            return cluster.expire(getKeySerializer().serialize(key), (int) timeUnit.toSeconds(timeout));
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Long getExpire(String key) {
        try {
            return cluster.ttl(getKeySerializer().serialize(key));
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisClientException(e.getMessage(), e);
        }

    }

    @Override
    public Set<String> scan(String pattern) {
        Set<String> keys = new HashSet<>();
        ScanParams params = new ScanParams();
        params.count(1000);
        params.match(pattern);
        String cursor = "0";
        try {
            do {
                ScanResult<String> results = cluster.scan(cursor, params);
                keys.addAll(results.getResult());
                cursor = results.getCursor() + "";
            } while (!"0".equals(cursor));
            return keys;
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisClientException(e.getMessage(), e);
        }

    }

    @Override
    public Object eval(String script, List<String> keys, List<String> args) {
        try {
            return cluster.eval(script, keys, args);
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Long publish(String channel, String message) {
        try {
            return cluster.publish(channel, message);
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public void subscribe(RedisMessageListener messageListener, String... channel) {
        try {
            cluster.subscribe(messageListener, channel);
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public RedisSerializer<Object> getKeySerializer() {
        return keySerializer;
    }

    @Override
    public RedisSerializer<Object> getValueSerializer() {
        return valueSerializer;
    }

    @Override
    public void setKeySerializer(RedisSerializer keySerializer) {
        this.keySerializer = keySerializer;
    }

    @Override
    public void setValueSerializer(RedisSerializer valueSerializer) {
        this.valueSerializer = valueSerializer;
    }
}