package com.changgou.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/****
 * @Author:henzhang
 * @Description: 秒杀订单mq监听
 *****/

@Component
@RabbitListener(queues = "${mq.pay.queue.seckillorder}")
public class SeckillMessageListener {

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 消息监听
     *
     * @param message
     */
    @RabbitHandler
    public void getMessage(String message) {
        try {
            // 支付信息转为map
            Map<String, String> resultMap = JSON.parseObject(message, Map.class);

            String return_code = resultMap.get("return_code"); // 通信标识
            String result_code = resultMap.get("result_code"); // 业务结果
            String out_trade_no = resultMap.get("out_trade_no"); // 订单号
            String attach = resultMap.get("attach"); //自定义数据
            Map<String, String> attachMap = JSON.parseObject(attach, Map.class);

            // return_code 通信标识 success
            if (return_code.equals("SUCCESS")) {

                // result_code 业务结果 success
                if (result_code.equals("SUCCESS")) {
                    // 修改订单状态 清理用户排队信息
                    seckillOrderService.updatePayStatus(attachMap.get("username"),
                            attachMap.get("transaction_id"),
                            attachMap.get("time_end"));
                } else {
                    // fail -> 删除订单 -> 回滚库存
                    seckillOrderService.deleteOrder(attachMap.get("username"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
