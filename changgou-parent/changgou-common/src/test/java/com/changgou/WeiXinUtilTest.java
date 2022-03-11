package com.changgou;

import com.github.wxpay.sdk.WXPayUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/****
 * @Author:henzhang
 * @Description:
 *****/
public class WeiXinUtilTest {


    /**
     * 1. 随机字符串
     * 2. 将map 转换成 xml 字符串
     * 3. 将map 转换成 xml 字符串, 并且生成签名
     * 4. 将xml 字符串 转换成 map
     */
    @Test
    public void testDemo() throws Exception {
        String s = WXPayUtil.generateNonceStr();
        System.out.println("随机字符串: " + s);

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("id", "NUM1");
        dataMap.put("title", "畅购商城杯具支付");
        dataMap.put("money", "998");
        String mapXml = WXPayUtil.mapToXml(dataMap);
        System.out.println(mapXml);

        String changGou = WXPayUtil.generateSignedXml(dataMap, "changgou");
        System.out.println(changGou);

        Map<String, String> mapResult = WXPayUtil.xmlToMap(changGou);
        System.out.println(mapResult);
    }
}
