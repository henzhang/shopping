package com.changgou.seckill.mq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/****
 * @Author:henzhang
 * @Description: 延时队列解决订单超时问题
 *
 * 1. 超时队列 暂时存储数据 Queue1
 * 2. 真正监听的消息队列   Queue2
 * 3. 交换机
 *
 *****/


@Configuration
public class QueueConfig {


    /**
     * 超时队列 暂时存储数据 Queue1
     */
    @Bean
    public Queue delaySecKillQueue() {
        return QueueBuilder.durable("delaySecKillQueue")
                .withArgument("x-dead-letter-exchange", "secKillExchange")  // 消息超时进入死信队列，绑定死信队列交换机
                .withArgument("x-dead-letter-routing-key", "secKillQueue")   // 绑定指定的routing-key
                .build();
    }

    /**
     * 真正监听的消息队列   Queue2
     */
    @Bean
    public Queue secKillQueue() {
        return new Queue("secKillQueue");
    }

    /**
     * 交换机
     */
    @Bean
    public Exchange secKillExchange() {
        return new DirectExchange("secKillExchange");
    }

    /**
     * 队列绑定交换机
     */
    @Bean
    public Binding secKillBinding(Queue secKillQueue, Exchange secKillExchange) {
        return BindingBuilder.bind(secKillQueue).to(secKillExchange).with("secKillQueue").noargs();
    }


}
