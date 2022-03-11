package com.changgou.pay.service;

import java.util.Map;

/****
 * @Author:henzhang
 * @Description:
 *****/
public interface WeiXinPayService {

    /**
     * 获取二维码
     */
    Map createNative(Map<String, String> parameterMap);

    /***
     * 查询订单状态
     */
    Map queryPayStatus(String outTradeNo);
}
