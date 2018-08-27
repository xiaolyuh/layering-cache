# 多级缓存 layering-cache

[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper)

layering-cache是在Spring Cache基础上扩展而来的一个缓存框架，主要目的是在使用注解的时候支持配置过期时间。layering-cache其实是一个两级缓存，一级缓存使用Caffeine作为本地缓存，二级缓存使用redis作为集中式缓存。并且基于redis的Pub/Sub做缓存的删除，所以他是一个适用于分布式环境下的一个缓存系统。

# 支持
- 支持缓存过期时间在注解上直接配置
- 支持二级缓存的自动刷新（当缓存命中并发现缓存将要过期时会开启一个异步线程刷新缓存）
- 刷新缓存分为强刷新和软刷新，强刷新直接调用缓存方法，软刷新直接改缓存的时间 
- 缓存名称和Key都支持SpEL表达式
- 新增FastJsonRedisSerializer，KryoRedisSerializer序列化，重写String序列化。
- 支持同一个缓存名称设置不同的过期时间

# 集成
在集成layering-cache之前还需要添加以下的依赖，主要是为了减少jar包冲突。
```xml  
<dependency>
	<groupId>org.springframework.data</groupId>
	<artifactId>spring-data-redis</artifactId>
	<version>1.8.3.RELEASE</version>
</dependency>

<dependency>
	<groupId>redis.clients</groupId>
	<artifactId>jedis</artifactId>
	<version>2.9.0</version>
</dependency>

<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-core</artifactId>
	<version>4.3.18.RELEASE</version>
</dependency>

<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-aop</artifactId>
	<version>4.3.18.RELEASE</version>
</dependency>

<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-context</artifactId>
	<version>4.3.18.RELEASE</version>
</dependency>

<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>fastjson</artifactId>
	<version>1.2.31</version>
</dependency>

<dependency>
	<groupId>com.esotericsoftware</groupId>
	<artifactId>kryo-shaded</artifactId>
	<version>3.0.3</version>
</dependency>

<dependency>
	<groupId>org.aspectj</groupId>
	<artifactId>aspectjweaver</artifactId>
	<version>1.8.10</version>
</dependency>
```  
## 集成 Spring 4.x
1. 引入layering-cache
```xml
<dependency>
    <groupId>com.xiaolyuh</groupId>
    <artifactId>layering-cache-aspectj</artifactId>
    <version>${layering.version}</version>
</dependency>
```
2. 声明RedisTemplate
```java
import FastJsonRedisSerializer;
import StringRedisSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@PropertySource({"classpath:application.properties"})
public class RedisConfig {

    @Value("${spring.redis.database:0}")
    private int database;

    @Value("${spring.redis.host:192.168.83.128}")
    private String host;

    @Value("${spring.redis.password:}")
    private String password;

    @Value("${spring.redis.port:6378}")
    private int port;

    @Value("${spring.redis.pool.max-idle:200}")
    private int maxIdle;

    @Value("${spring.redis.pool.min-idle:10}")
    private int minIdle;

    @Value("${spring.redis.pool.max-active:80}")
    private int maxActive;

    @Value("${spring.redis.pool.max-wait:-1}")
    private int maxWait;


    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxTotal(maxActive);
        jedisPoolConfig.setMaxWaitMillis(maxWait);

        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(jedisPoolConfig);
        jedisConnectionFactory.setDatabase(database);
        jedisConnectionFactory.setHostName(host);
        jedisConnectionFactory.setPassword(password);
        jedisConnectionFactory.setPort(port);
        jedisConnectionFactory.setUsePool(true);
        return jedisConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class, "com.xxx.");

        // 设置值（value）的序列化采用FastJsonRedisSerializer。
        redisTemplate.setHashValueSerializer(fastJsonRedisSerializer);
        redisTemplate.setValueSerializer(fastJsonRedisSerializer);
        // 设置键（key）的序列化采用StringRedisSerializer。
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
```

3. 声明CacheManager和LayeringAspect
```java
/**
 * 多级缓存配置
 *
 * @author yuhao.wang3
 */
@Configuration
@EnableAspectJAutoProxy
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisTemplate<String, Object> redisTemplate) {
        return new LayeringCacheManager(redisTemplate);
    }

    @Bean
    public LayeringAspect layeringAspect() {
        return new LayeringAspect();
    }
}
```

## 集成 Spring Boot
引入layering-cache 就可以了
```xml
<dependency>
    <groupId>com.xiaolyuh</groupId>
    <artifactId>layering-cache-starter</artifactId>
    <version>${layering.version}</version>
</dependency>
```

# 使用
## 注解形式
直接在需要缓冲的方法上加上Cacheable、CacheEvict、CachePut注解。

- Cacheable注解
```java
@Cacheable(value = "'user:info' + ':' + #user.userType",
		firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
		secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
public User getUser(User user) {
	logger.debug("调用方法获取用户名称");
	return user;
}
```
- CachePut注解
```java
@CachePut(value = "'user:info' + ':' + #userType", key = "#userId",
		firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
		secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
public User putUser(long userId) {
	User user = new User();
	user.setUserId(userId);
	user.setAge(31);
	user.setLastName(new String[]{"w", "y", "h"});

	return user;
}
```
- CacheEvict注解
```java
@CacheEvict(value = "'user:info' + ':' + #userType", key = "#userId")
public void evictUser(long userId) {

}

@CacheEvict(value = "user:info", allEntries = true)
public void evictAllUser() {
}
```
## 直接使用API
```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CacheConfig.class})
public class CacheCoreTest {
    private Logger logger = LoggerFactory.getLogger(CacheCoreTest.class);

    @Autowired
    private CacheManager cacheManager;

    @Test
    public void testCacheExpiration() {
        FirstCacheSetting firstCacheSetting = new FirstCacheSetting(10, 1000, 4, TimeUnit.SECONDS, ExpireMode.WRITE);
        SecondaryCacheSetting secondaryCacheSetting = new SecondaryCacheSetting(10, 4, TimeUnit.SECONDS, true);
        LayeringCacheSetting layeringCacheSetting = new LayeringCacheSetting(firstCacheSetting, secondaryCacheSetting);

        String cacheName = "cache:name";
        String cacheKey = "cache:key1";
        LayeringCache cache = (LayeringCache) cacheManager.getCache(cacheName, layeringCacheSetting);
        cache.get(cacheKey, () -> initCache(String.class));
        cache.put(cacheKey, "test");
        cache.evict(cacheKey);
        cache.clear();
    }

    private <T> T initCache(Class<T> t) {
        logger.debug("加载缓存");
        return (T) "test";
    }
}
```

# 文档
## @Cacheable
表示用的方法的结果是可以被缓存的，当该方法被调用时先检查缓存是否命中，如果没有命中再调用被缓存的方法，并将其返回值放到缓存中。

名称|默认值|说明
---|---|---
value|空字符串数组|缓存名称，cacheNames的别名
cacheNames|空字符串数组|缓存名称，支持SpEL表达式
key|空字符串|缓存key，支持SpEL表达式
firstCache||一级缓存配置
secondaryCache|| 二级缓存配置
~~keyGenerator~~||key生成器,暂时不支持配置

## @FirstCache
一级缓存配置项

名称|默认值|说明
---|---|---
initialCapacity|10|缓存初始Size
maximumSize|5000|缓存最大Size
expireTime|9|缓存有效时间
timeUnit|TimeUnit.MINUTES|时间单位，默认分钟
expireMode|ExpireMode.WRITE| 缓存失效模式，ExpireMode.WRITE：最后一次写入后到期失效，ExpireMode.ACCESS：最后一次访问后到期失效


## @SecondaryCache
二级缓存配置项

名称|默认值|说明
---|---|---
expireTime|5|缓存有效时间
preloadTime|1|缓存主动在失效前强制刷新缓存的时间，建议是 expireTime * 0.2
timeUnit|TimeUnit.HOURS|时间单位，默认小时
forceRefresh|false| 是否强制刷新（直接执行被缓存方法）

## @CachePut
将数据放到缓存中

名称|默认值|说明
---|---|---
value|空字符串数组|缓存名称，cacheNames的别名
cacheNames|空字符串数组|缓存名称，支持SpEL表达式
key|空字符串|缓存key，支持SpEL表达式
firstCache||一级缓存配置
secondaryCache|| 二级缓存配置
~~keyGenerator~~||key生成器,暂时不支持配置

## @CacheEvict
删除缓存

名称|默认值|说明
---|---|---
value|空字符串数组|缓存名称，cacheNames的别名
cacheNames|空字符串数组|缓存名称，支持SpEL表达式
key|空字符串|缓存key，支持SpEL表达式
allEntries|false|是否删除缓存中所有数据，默认情况下是只删除关联key的缓存数据，当该参数设置成  true 时 key 参数将无效
~~keyGenerator~~||key生成器,暂时不支持配置

# 更新日志

# 重要提示
- layering-cache支持同一个缓存名称设置不同的过期时间，但是一定要保证key唯一，否则会出现缓存过期时间错乱的情况
- layering-cache缓存名称支持SpEL表达式，但是一定要保证缓存名称中表达式的取值范围在一个很小的范围之类，如枚举等，不能使用userId之类。
- 删除缓存的时候会将同一个缓存名称的不同的过期时间的缓存都删掉

# 实现原理


# 作者信息

作者博客：https://www.jianshu.com/u/4e6e80b98daa

作者邮箱： xiaolyuh@163.com  

github 地址：https://github.com/wyh-chenfeng/layering-cache


# 捐赠
项目的发展离不开你的支持，请作者喝杯咖啡吧！
![微信.png](https://upload-images.jianshu.io/upload_images/6464086-553feada56e87976.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![支付宝.png](https://upload-images.jianshu.io/upload_images/6464086-b5931fed21a137c6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![扫描领支付宝红包.png](https://upload-images.jianshu.io/upload_images/6464086-4c07fc47862dab24.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


