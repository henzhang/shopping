package com.changgou.order.mq.queue;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/****
 * @Author:henzhang
 * @Description: 延时队列操作
 *****/
@Configuration
public class QueueConfig {

    /**
     * 创建queue1 , 延时队列 ，会过期，过期后，将数据发送给queue2
     *
     * @Bean (name = " ") bean 的 属性中name默认是方法名
     */
    @Bean
    public Queue orderDelayQueue() {
        return QueueBuilder.durable("orderDelayQueue")
                .withArgument("x-dead-letter-exchange", "orderListenerExchange") // 消息超时进入死信队列，绑定死信队列交换机
                .withArgument("x-dead-letter-routing-key", "orderListenerQueue") // 绑定指定的routing-key
                .build();
    }

    /**
     * 创建queue2
     */
    @Bean
    public Queue orderListenerQueue() {
        return new Queue("orderListenerQueue", true);
    }

    /**
     * 创建交换机
     */
    @Bean
    public Exchange orderListenerExchange() {
        return new DirectExchange("orderListenerExchange");
    }

    /**
     * 队列queue2绑定交换机
     */
    @Bean
    public Binding orderListenerBinding(Queue orderListenerQueue, Exchange orderListenerExchange) {
        return BindingBuilder.bind(orderListenerQueue)
                .to(orderListenerExchange)
                .with("orderListenerQueue").noargs();
    }

}
