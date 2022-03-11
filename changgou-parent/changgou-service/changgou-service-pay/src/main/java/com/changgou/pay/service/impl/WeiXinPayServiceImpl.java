package com.changgou.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.pay.service.WeiXinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.HttpClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/****
 * @Author:henzhang
 * @Description:
 *****/
@Service
public class WeiXinPayServiceImpl implements WeiXinPayService {

    @Value("${weixin.appid}")
    private String appid;
    @Value("${weixin.partner}")
    private String partner;
    @Value("${weixin.partnerkey}")
    private String partnerkey;
    @Value("${weixin.notifyurl}")
    private String notifyurl;


    /**
     * 获取二维码
     *
     * @param parameterMap
     * @return
     */
    @Override
    public Map createNative(Map<String, String> parameterMap) {
        try {
            //1、封装参数
            Map param = new HashMap();
            param.put("appid", appid);                              //应用ID
            param.put("mch_id", partner);                           //商户ID号
            param.put("nonce_str", WXPayUtil.generateNonceStr());   //随机数
            param.put("body", "畅购");                                //订单描述
            param.put("out_trade_no", parameterMap.get("outTradeNo"));  //商户订单号
            param.put("total_fee", parameterMap.get("totalFee"));      //交易金额 单位：分
            param.put("spbill_create_ip", "127.0.0.1");           //终端IP
            param.put("notify_url", notifyurl);                    //回调地址
            param.put("trade_type", "NATIVE");                     //交易类型

            //获取自定义参数    (如果是秒杀订单，则需要传递username)
            String exchange = parameterMap.get("exchange");
            String routingkey = parameterMap.get("routingkey");
            String username = parameterMap.get("username");

            Map<String, String> attachMap = new HashMap<String, String>();
            attachMap.put("exchange", exchange);
            attachMap.put("routingkey", routingkey);
            if (!StringUtils.isEmpty(username)) {
                attachMap.put("username", username);
            }
            String attach = JSON.toJSONString(attachMap);
            param.put("attach", attach);


            //2、将参数转成xml字符，并携带签名
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);

            ///3、执行请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();

            //4、获取参数
            String content = httpClient.getContent();
            Map<String, String> stringMap = WXPayUtil.xmlToMap(content);
            System.out.println("stringMap:" + stringMap);

            //5、获取部分页面所需参数
//            Map<String,String> dataMap = new HashMap<String,String>();
//            dataMap.put("code_url",stringMap.get("code_url"));
//            dataMap.put("out_trade_no",out_trade_no);
//            dataMap.put("total_fee",total_fee);

            return stringMap;

        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    public Map queryPayStatus(String outTradeNo) {
        try {
            //1.封装参数
            Map param = new HashMap();
            param.put("appid", appid);                            //应用ID
            param.put("mch_id", partner);                         //商户号
            param.put("out_trade_no", outTradeNo);              //商户订单编号
            param.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符

            //2、将参数转成xml字符，并携带签名
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);

            //3、发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();

            //4、获取返回值，并将返回值转成Map
            String content = httpClient.getContent();
            return WXPayUtil.xmlToMap(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
