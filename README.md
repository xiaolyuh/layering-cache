# layering-cache
# layering-cache
# 简介

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.xiaolyuh/layering-cache/badge.svg)](https://search.maven.org/artifact/com.github.xiaolyuh/layering-cache/)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

layering-cache是在Spring Cache基础上扩展而来的一个缓存框架，主要目的是在使用注解的时候支持配置过期时间。layering-cache其实是一个两级缓存，一级缓存使用Caffeine作为本地缓存，二级缓存使用redis作为集中式缓存。并且基于redis的Pub/Sub做缓存的删除，所以他是一个适用于分布式环境下的一个缓存系统。

# 支持
- 支持缓存监控统计
- 支持缓存过期时间在注解上直接配置
- 支持二级缓存的自动刷新（当缓存命中并发现缓存将要过期时会开启一个异步线程刷新缓存）
- 刷新缓存分为强刷新和软刷新，强刷新直接调用缓存方法，软刷新直接改缓存的时间 
- 缓存名称和Key都支持SpEL表达式
- 新增FastJsonRedisSerializer，KryoRedisSerializer序列化，重写String序列化。
- 支持同一个缓存名称设置不同的过期时间

# 集成
## 集成 Spring 4.x
1. 引入layering-cache
- maven 方式
```xml
<dependency>
    <groupId>com.xiaolyuh</groupId>
    <artifactId>layering-cache-aspectj</artifactId>
    <version>${layering.version}</version>
</dependency>
```
- gradle 方式
```
compile 'com.github.xiaolyuh:layering-cache:1.0.8'
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
@Cacheable(value = "user:info", depict = "用户信息缓存",
		firstCache = @FirstCache(expireTime = 4, timeUnit = TimeUnit.SECONDS),
		secondaryCache = @SecondaryCache(expireTime = 10, preloadTime = 3, forceRefresh = true, timeUnit = TimeUnit.SECONDS))
public User getUser(User user) {
	logger.debug("调用方法获取用户名称");
	return user;
}
```
- CachePut注解
```java
@CachePut(value = "user:info", key = "#userId", depict = "用户信息缓存",
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
@CacheEvict(value = "user:info", key = "#userId")
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
cacheNames|空字符串数组|缓存名称
key|空字符串|缓存key，支持SpEL表达式
depict |空字符串|缓存描述（在缓存统计页面会用到）
ignoreException|true|是否忽略在操作缓存中遇到的异常，如反序列化异常
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
cacheNames|空字符串数组|缓存名称
key|空字符串|缓存key，支持SpEL表达式
depict |空字符串|缓存描述（在缓存统计页面会用到）
ignoreException|true|是否忽略在操作缓存中遇到的异常，如反序列化异常
firstCache||一级缓存配置
secondaryCache|| 二级缓存配置
~~keyGenerator~~||key生成器,暂时不支持配置

## @CacheEvict
删除缓存

名称|默认值|说明
---|---|---
value|空字符串数组|缓存名称，cacheNames的别名
cacheNames|空字符串数组|缓存名称
key|空字符串|缓存key，支持SpEL表达式
allEntries|false|是否删除缓存中所有数据，默认情况下是只删除关联key的缓存数据，当该参数设置成  true 时 key 参数将无效
ignoreException|true|是否忽略在操作缓存中遇到的异常，如反序列化异常
~~keyGenerator~~||key生成器,暂时不支持配置

# 打开监控统计功能
Layering Cache 的监控统计功能默认是开启的
## Spring 4.x
直接在声明CacheManager Bean的时候将stats设置成true。
```
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
         LayeringCacheManager layeringCacheManager = new LayeringCacheManager(redisTemplate);
        // 默认开启统计功能
        layeringCacheManager.setStats(true);
        return layeringCacheManager;
    }
   ...
}
```
## Spring Boot
在application.properties文件中添加以下配置即可
```
layering-cache.stats=true
```
# 打开内置的监控页面
Layering Cache内置提供了一个LayeringCacheServlet用于展示缓存的统计信息。

这个LayeringCacheServlet的用途包括：
- 提供监控信息展示的html页面
- 提供监控信息的JSON API
- 输出监控统计日志

日志格式：
```
Layering Cache 统计信息：{"cacheName":"people1","depict":"查询用户信息1","firstCacheMissCount":3,"firstCacheRequestCount":4575,"hitRate":99.9344262295082,"internalKey":"4000-15000-8000","layeringCacheSetting":{"depict":"查询用户信息1","firstCacheSetting":{"allowNullValues":true,"expireMode":"WRITE","expireTime":4,"initialCapacity":10,"maximumSize":5000,"timeUnit":"SECONDS"},"internalKey":"4000-15000-8000","secondaryCacheSetting":{"allowNullValues":true,"expiration":15,"forceRefresh":true,"preloadTime":8,"timeUnit":"SECONDS","usePrefix":true},"useFirstCache":true},"missCount":3,"requestCount":4575,"secondCacheMissCount":3,"secondCacheRequestCount":100,"totalLoadTime":142}
```

>- 如果项目集成了ELK之类的日志框架，那我们可以直接基于以上日志做监控和告警。
>- 统计数据每隔一分钟采集一次

## 配置 web.xml
### 配置Servlet
LayeringCacheServlet是一个标准的javax.servlet.http.HttpServlet，需要配置在你web应用中的WEB-INF/web.xml中。

```
<servlet>
    <servlet-name>layeringcachestatview</servlet-name>
    <servlet-class>com.github.xiaolyuh.tool.servlet.layeringcacheservlet</servlet-class>
    <load-on-startup>0</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>layeringcachestatview</servlet-name>
    <url-pattern>/layering-cache/*</url-pattern>
</servlet-mapping>
```
根据配置中的url-pattern来访问内置监控页面，如果是上面的配置，内置监控页面的首页是/layering-cache/index.html。例如：
http://localhost:8080/layering-cache/index.html
http://localhost:8080/xxx/layering-cache/index.html

>注意：
>- load-on-startup参数必须配置成0，否则服务启动不会自动采集统计数据。


### 配置监控页面访问密码
需要配置Servlet的 loginUsername 和 loginPassword这两个初始参数。
示例如下:
```
<!-- 配置监控信息显示页面 -->
<servlet>
    <servlet-name>LayeringCacheStatView</servlet-name>
    <servlet-class>com.github.xiaolyuh.tool.servlet.LayeringCacheServlet</servlet-class>
    <init-param>
        <!-- 用户名 -->
        <param-name>loginUsername</param-name>
        <param-value>admin</param-value>
    </init-param>
    <init-param>
        <!-- 密码 -->
        <param-name>loginPassword</param-name>
        <param-value>admin</param-value>
    </init-param>
    <load-on-startup>0</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>LayeringCacheStatView</servlet-name>
    <url-pattern>/layering-cache/*</url-pattern>
</servlet-mapping>
```
### 配置黑白名单
LayeringCacheStatView展示出来的监控信息比较敏感，是系统运行的内部情况，如果你需要做访问控制，可以配置allow和deny这两个参数。比如：

```

<servlet>
    <servlet-name>LayeringCacheStatView</servlet-name>
    <servlet-class>com.github.xiaolyuh.tool.servlet.LayeringCacheServlet</servlet-class>
    <!--配置白名单-->
    <init-param>
        <param-name>allow</param-name>
        <param-value>128.242.127.1/24,128.242.128.1</param-value>
    </init-param>
    <!--配置黑名单-->
    <init-param>
        <param-name>deny</param-name>
        <param-value>128.242.127.4</param-value>
    </init-param>
    <load-on-startup>0</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>LayeringCacheStatView</servlet-name>
    <url-pattern>/layering-cache/*</url-pattern>
</servlet-mapping>
```
>**判断规则**
>- deny优先于allow，如果在deny列表中，就算在allow列表中，也会被拒绝。
>- 如果allow没有配置或者为空，则允许所有访问
>
>**ip配置规则**
>配置的格式
>```
>  128.242.127.1,128.242.127.1/24
>```
>/24表示，前面24位是子网掩码，比对的时候，前面24位相同就匹配。
>
>**不支持IPV6**
>由于匹配规则不支持IPV6，配置了allow或者deny之后，会导致IPV6无法访问。

## Spring Boot
```
#是否开启缓存统计默认值true
spring.layering-cache.stats=true

#是否启用LayeringCacheServlet默认值true
spring.layering-cache.layering-cache-servlet-enabled=true
spring.layering-cache.url-pattern=/layering-cache/*
#用户名
spring.layering-cache.login-username=admin
#密码
spring.layering-cache.login-password=admin
##IP白名单(没有配置或者为空，则允许所有访问)
spring.layering-cache.allow=127.0.0.1,192.168.163.1/24
##IP黑名单 (存在共同时，deny优先于allow)
spring.layering-cache.deny=192.168.1.73
```

# 更新日志

# 重要提示
- layering-cache支持同一个缓存名称设置不同的过期时间，但是一定要保证key唯一，否则会出现缓存过期时间错乱的情况
- 删除缓存的时候会将同一个缓存名称的不同的过期时间的缓存都删掉
- 在集成layering-cache之前还需要添加以下的依赖，主要是为了减少jar包冲突。
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

# 实现原理
https://github.com/xiaolyuh/layering-cache/wiki

# 作者信息

作者博客：https://www.jianshu.com/u/4e6e80b98daa

作者邮箱： xiaolyuh@163.com  

github 地址：https://github.com/wyh-chenfeng/layering-cache


# 捐赠
项目的发展离不开你的支持，请作者喝杯咖啡吧！

![微信.png](https://upload-images.jianshu.io/upload_images/6464086-553feada56e87976.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![支付宝.png](https://upload-images.jianshu.io/upload_images/6464086-b5931fed21a137c6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![扫描领支付宝红包.png](https://upload-images.jianshu.io/upload_images/6464086-4c07fc47862dab24.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

