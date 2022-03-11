package com.changgou.httpclient;

import entity.HttpClient;
import org.junit.Test;

import java.io.IOException;

/****
 * @Author:henzhang
 * @Description:
 *****/
public class HttpClientTest {

    @Test
    public void testDemo() throws IOException {
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";

        HttpClient httpClient = new HttpClient(url);
        String xml = "<xml><name>张三</name></xml>";
        httpClient.setXmlParam(xml);
        httpClient.setHttps(true);
        httpClient.post();

        String content = httpClient.getContent();
        System.out.println(content);
    }
}
