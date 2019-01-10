package com.github.xiaolyuh.demo.controller;

import com.alibaba.fastjson.JSON;
import com.github.xiaolyuh.demo.entity.Person;
import com.github.xiaolyuh.demo.utils.OkHttpClientUtil;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CacheControllerTest {

    String host = "http://localhost:8081/";

    @Test
//    @Ignore
    public void testPut() throws IOException {

        Person person = new Person(1, "name1", 12, "address1");
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , JSON.toJSONString(person));
        String post = OkHttpClientUtil.post(host + "put", requestBody);
        System.out.println("返回结果：" + post);
    }

    @Test
    @Ignore
    public void cacheable() throws IOException {

        Person person = new Person(1, "name1", 12, "address1");
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , JSON.toJSONString(person));
        String post = OkHttpClientUtil.post(host + "able", requestBody);
        System.out.println("返回结果：" + post);
    }

    @Test
    @Ignore
    public void testEvit() throws IOException {
        Person person = new Person(1, "name1", 12, "address1");
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , JSON.toJSONString(person));
        String post = OkHttpClientUtil.post(host + "evit", requestBody);
        System.out.println("返回结果：" + post);
    }

}