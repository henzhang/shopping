package com.changgou.order.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Map;

/****
 * @Author:henzhang
 * @Description:
 *****/
@Component
@Configuration
@RabbitListener(queues = {"${mq.pay.queue.order}"})
public class OrderPayMessageListener {

    @Autowired
    private OrderService orderService;

    /***
     * 接收消息
     */
    @RabbitHandler
    public void consumeMessage(String msg) throws Exception {
        //支付结果
        Map<String, String> result = JSON.parseObject(msg, Map.class);
        System.out.println("监听到的结果 : " + result);
        // 通信结果
        String return_code = result.get("return_code");
        // 业务结果
        String result_code = result.get("result_code");

        if (return_code.equalsIgnoreCase("success")) {
            //获取订单号
            String outTradeNo = result.get("out_trade_no");

            //业务结果  result_code=SUCCESS/FAIL，修改订单状态
            if (result_code.equalsIgnoreCase("success")) {
                if (outTradeNo != null) {
                    //修改订单状态  out_trade_no
                    orderService.updateStatus(outTradeNo, result.get("time_end"), result.get("transaction_id"));
                }
            } else {
                //订单删除  支付失败 关闭支付 取消订单 回滚库存
                orderService.deleteOrder(outTradeNo);
            }
        }

    }
}
