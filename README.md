# layering-cache
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.xiaolyuh/layering-cache/badge.svg)](https://search.maven.org/artifact/com.github.xiaolyuh/layering-cache/)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

# 简介
layering-cache是一个支持分布式环境的多级缓存框架，主要解决在高并发下数据快速读取的问题。整体采用了分层架构设计的思路，来保证整个框架的扩展性；采用了面向切面的设计模式，来解决了缓存和业务代码耦合性。

它使用Caffeine作为一级本地缓存，redis作为二级集中式缓存。

一级缓存和二级缓存的数据一致性是通过推和拉两种模式相结合的方式来保证。
- 推主要是基于redis的pub/sub机制
- 拉主要是基于消息队列和记录消费消息的偏移量来实现的。

# 支持
- 支持缓存命中率的监控统计，统计数据上报支持自定义扩展
- 内置dashboard，支持对缓存的管理和缓存命中率的查看
- 支持缓存过期时间在注解上直接配置
- 支持缓存的自动刷新（当缓存命中并发现二级缓存将要过期时，会开启一个异步线程刷新缓存）
- 缓存Key支持SpEL表达式
- Redis支持Kryo、FastJson、Jackson、Jdk和Protostuff序列化，默认使用Protostuff序列化，并支持自定义的序列化
- 支持同一个缓存名称设置不同的过期时间
- 支持只使用一级缓存或者只使用二级缓存
- Redis支持单机、集群、Sentinel三种客户端

# 优势
1. 提供缓存命中率的监控统计，统计数据上报支持自定义扩展
2. 支持本地缓存和集中式两级缓存
3. 接入成本和使用成本都非常低
4. 无缝集成Spring、Spring boot
5. 内置dashboard使得缓存具备可运维性
6. 通过缓存空值来解决缓存穿透问题、通过异步加载缓存的方式来解决缓存击穿和雪崩问题


# 文档

[中文文档](https://github.com/xiaolyuh/layering-cache/wiki/%E6%96%87%E6%A1%A3)

# Redis序列化方式对比
[Redis序列化同一个User对象对比](https://github.com/xiaolyuh/layering-cache/wiki/Redis%E5%BA%8F%E5%88%97%E5%8C%96%E6%96%B9%E5%BC%8F%E5%AF%B9%E6%AF%94)

||size|serialize(get 10W次)|deserialize(set 10W次)|serialize(cpu)|deserialize(cpu)|
---|---|---|---|---|---
Kryo|273 b|82919 ms|90917 ms|8%|12%
FastJson|329 b|15405 ms|18886 ms|12%|13%
Jackson|473 b|16066 ms|16140 ms|15%|14%
Jdk|1036 b|17344 ms|24917 ms|14%|13%
Protostuff|282 b|14295 ms|14355 ms|15%|13%

# 打开监控统计功能

[打开监控统计功能](https://github.com/xiaolyuh/layering-cache/wiki/%E7%9B%91%E6%8E%A7%E7%BB%9F%E8%AE%A1%E5%8A%9F%E8%83%BD)

# 重要提示
- layering-cache支持同一个缓存名称设置不同的过期时间，但是一定要保证key唯一，否则会出现缓存过期时间错乱的情况
- 删除缓存的时候会将同一个缓存名称的不同的过期时间的缓存都删掉
- 在集成layering-cache之前还需要添加以下的依赖，主要是为了减少jar包冲突([依赖jar列表](https://github.com/xiaolyuh/layering-cache/wiki/%E4%BE%9D%E8%B5%96jar%E5%88%97%E8%A1%A8))。
- redis的key序列化方式必须StringRedisSerializer

# 更新日志

[更新日志](https://github.com/xiaolyuh/layering-cache/wiki/%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97)

# 实现原理
[实现原理](https://github.com/xiaolyuh/layering-cache/wiki/%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86)

# 作者信息

作者博客：https://xiaolyuh.blog.csdn.net/

作者邮箱： xiaolyuh@163.com  

github 地址：https://github.com/xiaolyuh/layering-cache


# 捐赠
项目的发展离不开你的支持，请作者喝杯咖啡吧！

![微信-支付宝](https://img-blog.csdnimg.cn/20200218152559645.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3hpYW9seXVoMTIz,size_16,color_FFFFFF,t_70)

# 技术支持
添加微信记得备注 ```layering-cache```。

![微信](https://img-blog.csdnimg.cn/2020122516245862.png)

# 特别感谢
感谢何一睿同学贡献的```@BatchCacheable```批量缓存注解


