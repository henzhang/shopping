package com.changgou.pay.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.pay.service.WeiXinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/****
 * @Author:henzhang
 * @Description:
 *****/

@RestController
@RequestMapping(value = "/weixin/pay")
@CrossOrigin
public class WeiXinPayController {
    @Autowired
    private WeiXinPayService weiXinPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /***
     * 创建二维码
     * 普通订单 :
     *         exchange: exchange.order
     *         routingkey : queue.order
     *
     * 秒杀订单 :
     *         exchange: exchange.seckillorder
     *         routingkey : queue.seckillorder
     *
     * exchange + routingkey -> json -> attach
     *
     * @return
     */
    @RequestMapping(value = "/create/native")
    public Result createNative(@RequestParam Map<String, String> parameterMap) {
        Map<String, String> resultMap = weiXinPayService.createNative(parameterMap);
        return new Result(true, StatusCode.OK, "创建二维码预付订单成功！", resultMap);
    }

    /***
     * 查询支付状态
     * @return
     */
    @GetMapping(value = "/status/query")
    public Result queryStatus(String outTradeNo) {
        Map<String, String> resultMap = weiXinPayService.queryPayStatus(outTradeNo);
        return new Result(true, StatusCode.OK, "查询状态成功！", resultMap);
    }

    /**
     * 支付结果通知回调方法
     */
    @RequestMapping(value = "/notify/url")
    public String notifyUrl(HttpServletRequest request) {
        ServletInputStream inputStream;
        try {
            inputStream = request.getInputStream();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }

            inputStream.close();
            outputStream.close();

            byte[] bytes = outputStream.toByteArray();
            // 将支付回调数据转换成xml字符串
            String xmlResult = new String(bytes, "UTF-8");
            //将xml字符串转换成Map结构
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlResult);
            System.out.println(resultMap);

            // 获取自定义参数
            String attach = resultMap.get("attach");
            Map<String, String> attachMap = JSON.parseObject(attach, Map.class);

            //发送支付结果给mq
            rabbitTemplate.convertAndSend(attachMap.get("exchange"), attachMap.get("routingkey"), JSON.toJSONString(resultMap));
//            rabbitTemplate.convertAndSend("exchange.order","queue.order", JSON.toJSONString(resultMap));

            //响应数据设置
            Map respMap = new HashMap();
            respMap.put("return_code", "SUCCESS");
            respMap.put("return_msg", "OK");
            return WXPayUtil.mapToXml(respMap);

        } catch (Exception e) {
            //记录错误日志
            e.printStackTrace();
        }
        return null;
    }
}
