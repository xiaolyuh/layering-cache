package com.github.xiaolyuh.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.xiaolyuh.support.NullValue;
import com.github.xiaolyuh.support.SerializationException;
import com.github.xiaolyuh.support.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.nio.charset.Charset;

/**
 * @author yuhao.wang
 */
public class FastJsonRedisSerializer<T> implements RedisSerializer<T> {
    private Logger logger = LoggerFactory.getLogger(FastJsonRedisSerializer.class);

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private Class<T> clazz;

    /**
     * 允许所有包的序列化和反序列化，不推荐
     * @param clazz Class
     */
    @Deprecated
    public FastJsonRedisSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
        try {
            ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        } catch (Throwable e) {
            logger.warn("fastjson 版本太低，反序列化有被攻击的风险", e);
        }
        logger.warn("fastjson 反序列化有被攻击的风险，推荐使用白名单的方式，详情参考：https://www.jianshu.com/p/a92ecc33fd0d");
    }

    /**
     * 指定小范围包的序列化和反序列化，具体原因可以参考：
     * <P>https://www.jianshu.com/p/a92ecc33fd0d</P>
     *
     * @param clazz    clazz
     * @param packages 白名单包名，如:"com.xxx."
     */
    public FastJsonRedisSerializer(Class<T> clazz, String... packages) {
        super();
        this.clazz = clazz;
        try {
            ParserConfig.getGlobalInstance().addAccept("com.github.xiaolyuh.");
            if (packages != null && packages.length > 0) {
                for (String packageName : packages) {
                    ParserConfig.getGlobalInstance().addAccept(packageName);
                }
            }
        } catch (Throwable e) {
            logger.warn("fastjson 版本太低，反序列化有被攻击的风险", e);
        }
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        try {
            return JSON.toJSONString(new FastJsonSerializerWrapper(t), SerializerFeature.WriteClassName).getBytes(DEFAULT_CHARSET);
        } catch (Exception e) {
            logger.error("FastJsonRedisSerializer 序列化异常: {}", e.getMessage(), e);
            throw new SerializationException("FastJsonRedisSerializer 序列化异常: " + e.getMessage(), e);
        }

    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (SerializationUtils.isEmpty(bytes)) {
            return null;
        }

        try {
            String str = new String(bytes, DEFAULT_CHARSET);
            FastJsonSerializerWrapper wrapper = JSON.parseObject(str, FastJsonSerializerWrapper.class);
            switch (Type.parse(wrapper.getType())) {
                case STRING:
                    return (T) wrapper.getContent();
                case OBJECT:
                case SET:

                    if (wrapper.getContent() instanceof NullValue) {
                        return null;
                    }

                    return (T) wrapper.getContent();

                case LIST:

                    return (T) ((JSONArray) wrapper.getContent()).toJavaList(clazz);

                case NULL:

                    return null;
                default:
                    throw new SerializationException("不支持反序列化的对象类型: " + wrapper.getType());
            }
        } catch (Exception e) {
            logger.error("FastJsonRedisSerializer 反序列化异常:{}", e.getMessage(), e);
            throw new SerializationException("FastJsonRedisSerializer 反序列化异常: " + e.getMessage(), e);
        }
    }
}