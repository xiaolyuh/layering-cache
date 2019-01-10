package com.github.xiaolyuh.demo.utils;

import com.alibaba.fastjson.JSON;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.remoting.RemoteAccessException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OkHttpClient工具
 *
 * @author yuhao.wang3
 */
public abstract class OkHttpClientUtil {
    private static final Logger logger = LoggerFactory.getLogger(OkHttpClientUtil.class);

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();


    /**
     * 发起post请求，不做任何签名
     *
     * @param url         发送请求的URL
     * @param requestBody 请求体
     * @throws IOException
     */
    public static String post(String url, RequestBody requestBody) throws IOException {
        Request request = new Request.Builder()
                //请求的url
                .url(url)
                .post(requestBody)
                .build();

        //创建/Call
        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            logger.error("访问外部系统异常 {}: {}", url, JSON.toJSONString(response));
            throw new RemoteAccessException("访问外部系统异常 " + url);
        }
        return response.body().string();
    }

    /**
     * 发起post请求，不做任何签名 宽松的参数构造
     *
     * @param url     发送请求的URL
     * @param builder 请求体
     * @throws IOException
     */
    public static String post(String url, Request.Builder builder) throws IOException {

        Request request = builder.url(url).build();
        //创建/Call
        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            logger.error("访问外部系统异常 {}: {}", url, JSON.toJSONString(response));
            throw new RemoteAccessException("访问外部系统异常 " + url);
        }
        return response.body().string();
    }


    public static String post(String url, Map<String, ?> param, Map<String, String> header) throws Exception {
        // 生成requestBody
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , JSON.toJSONString(param));

        Request.Builder builder = new Request.Builder()
                //请求的url
                .url(url)
                .post(requestBody);

        for (String key : header.keySet()) {
            builder.header(key, header.get(key));
        }

        Request request = builder.build();

        //创建/Call
        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            logger.error("访问外部系统异常 {}: {}", url, JSON.toJSONString(response));
            throw new RemoteAccessException("访问外部系统异常 " + url);
        }
        return response.body().string();
    }

    public static String get(String url) throws IOException {
        Request request = new Request.Builder()
                //请求的url
                .url(url)
                .get()
                .build();

        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            logger.error("访问外部系统异常 {}: {}", url, JSON.toJSONString(response));
            throw new RemoteAccessException("访问外部系统异常 " + url);
        }
        return response.body().string();
    }

}
