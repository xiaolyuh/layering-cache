package com.github.xiaolyuh.demo;

import com.github.xiaolyuh.cache.config.EnableLayeringCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableLayeringCache
public class LayeringCacheStartDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LayeringCacheStartDemoApplication.class, args);
    }
}
