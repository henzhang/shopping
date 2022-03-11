package com.changgou.order.mq.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/****
 * @Author:henzhang
 * @Description: 过期消息监听
 *****/
@Component
@RabbitListener(queues = "orderListenerQueue")
public class DelayMessageListener {

    /***
     * 延时队列监听
     * @param msg
     */
    @RabbitHandler
    public void msg(@Payload Object msg) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("监听消息的时间:" + dateFormat.format(new Date()));
        System.out.println("收到信息:" + msg);
    }
}
