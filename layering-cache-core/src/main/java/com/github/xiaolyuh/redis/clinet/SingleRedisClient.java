package com.github.xiaolyuh.redis.clinet;

import com.alibaba.fastjson.JSON;
import com.github.xiaolyuh.listener.RedisMessageListener;
import com.github.xiaolyuh.redis.serializer.KryoRedisSerializer;
import com.github.xiaolyuh.redis.serializer.RedisSerializer;
import com.github.xiaolyuh.redis.serializer.SerializationException;
import com.github.xiaolyuh.redis.serializer.StringRedisSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.util.SafeEncoder;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 单机版Redis客户端
 *
 * @author olafwang
 */
public class SingleRedisClient implements RedisClient {
    Logger logger = LoggerFactory.getLogger(SingleRedisClient.class);
    /**
     * 默认key序列化方式
     */
    private RedisSerializer keySerializer = new StringRedisSerializer();

    /**
     * 默认value序列化方式
     */
    private RedisSerializer valueSerializer = new KryoRedisSerializer(Object.class);

    private JedisPool jedisPool;

    public SingleRedisClient(RedisProperties properties) {
        String ip = properties.getHost();
        int port = properties.getPort();
        int database = properties.getDatabase();
        String password = properties.getPassword();

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(properties.getMaxTotal());
        jedisPoolConfig.setMaxIdle(properties.getMaxIdle());
        jedisPoolConfig.setMaxWaitMillis(properties.getMaxWaitMillis());
        jedisPoolConfig.setTestOnBorrow(properties.getTestOnBorrow());
        jedisPoolConfig.setMinIdle(properties.getMinIdle());
        logger.info("layering-cache redis配置" + JSON.toJSONString(properties));
        jedisPool = new JedisPool(jedisPoolConfig, ip, port, 10000, password, database);
    }

    @Override
    public Object get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            byte[] results = jedis.get(getKeySerializer().serialize(key));
            return getValueSerializer().deserialize(results);
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
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.set(getKeySerializer().serialize(key), getValueSerializer().serialize(value));
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public String set(String key, Object value, long time, TimeUnit unit) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setex(getKeySerializer().serialize(key), (int) unit.toSeconds(time), getValueSerializer().serialize(value));
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public String set(String key, Object value, String nxxx, String expx, long time) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.set(getKeySerializer().serialize(key), getValueSerializer().serialize(value), SafeEncoder.encode(nxxx), SafeEncoder.encode(expx), time);
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Long delete(String... keys) {
        if (Objects.nonNull(keys) && keys.length > 0) {
            try (Jedis jedis = jedisPool.getResource()) {
                final byte[][] bkeys = new byte[keys.length][];
                for (int i = 0; i < keys.length; i++) {
                    bkeys[i] = getKeySerializer().serialize(keys[i]);
                }
                return jedis.del(bkeys);
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
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(getKeySerializer().serialize(key));
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Long expire(String key, long timeout, TimeUnit timeUnit) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.expire(getKeySerializer().serialize(key), (int) timeUnit.toSeconds(timeout));
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Long getExpire(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ttl(getKeySerializer().serialize(key));
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
        try (Jedis jedis = jedisPool.getResource()) {
            do {
                ScanResult<String> results = jedis.scan(cursor, params);
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
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.eval(script, keys, args);
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Long publish(String channel, String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.publish(channel, message);
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisClientException(e.getMessage(), e);
        }

    }

    @Override
    public void subscribe(RedisMessageListener messageListener, String... channels) {
        try (Jedis jedis = jedisPool.getResource()) {
            logger.info("layering-cache和redis创建订阅关系，订阅频道【{}】", Arrays.toString(channels));
            jedis.subscribe(messageListener, channels);
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