# layering-cache
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.xiaolyuh/layering-cache/badge.svg)](https://search.maven.org/artifact/com.github.xiaolyuh/layering-cache/)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

# 简介
layering-cache是一个支持分布式环境的多级缓存框架，使用方式和spring-cache类似。一级缓存使用Caffeine作为本地缓存，二级缓存使用redis作为集中式缓存。一级缓存和二级缓存的数据一致性是通过推和拉两种模式相结合的方式来实现的。推主要是基于redis的pub/sub机制，拉主要是基于消息队列和记录消费消息的偏移量来实现的。

# 支持
- 支持缓存命中率的监控统计，统计数据上报支持自定义扩展
- 内置dashboard，支持对缓存的管理和缓存命中率的查看
- 支持缓存过期时间在注解上直接配置
- 支持二级缓存的自动刷新（当缓存命中并发现缓存将要过期时会开启一个异步线程刷新缓存）
- 缓存Key支持SpEL表达式
- Redis默认使用KryoRedisSerializer序列化，并支持自定义的序列化
- 支持同一个缓存名称设置不同的过期时间
- 支持禁用一级缓存，只使用二级缓存
- 通过允许存空值来解决缓存穿透问题

# 优势
1. 提供缓存命中率的监控统计，统计数据上报支持自定义扩展
2. 支持本地缓存和集中式两级缓存
3. 接入成本和使用成本都非常低
4. 支持Spring、Spring boot
5. 内置dashboard使得缓存具备可运维性

# 文档

[中文文档](https://github.com/xiaolyuh/layering-cache/wiki/%E6%96%87%E6%A1%A3)
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
添加微信记得备注 ```layering-cache```

![微信](https://github.com/xiaolyuh/layering-cache/images/wechat.jpeg)



