package com.changgou.pay.mq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


/****
 * @Author:henzhang
 * @Description:
 *****/

@Configuration
public class MQConfig {


    /**
     * 读取配置文件中的对象
     */
    @Autowired
    private Environment env;

    /**
     * 创建队列
     */
    @Bean
    public Queue orderQueue() {
        return new Queue(env.getProperty("mq.pay.queue.order"), true);
    }


    /**
     * 创建交换机
     */
    @Bean
    public Exchange orderExchange() {
        return new DirectExchange(env.getProperty("mq.pay.exchange.order"), true, false);
    }


    /**
     * 队列绑定交换机
     *
     * @return
     */
    @Bean
    public Binding basicBinding(Queue orderQueue, Exchange orderExchange) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(env.getProperty("mq.pay.routing.key")).noargs();
    }


    /************************************秒杀队列创建************************************/


    /**
     * 创建队列
     */
    @Bean
    public Queue orderSecKillQueue() {
        return new Queue(env.getProperty("mq.pay.queue.seckillorder"), true);
    }


    /**
     * 创建交换机
     */
    @Bean
    public Exchange orderSecKillExchange() {
        return new DirectExchange(env.getProperty("mq.pay.exchange.seckillorder"), true, false);
    }


    /**
     * 队列绑定交换机
     *
     * @return
     */
    @Bean
    public Binding basicSecKillBinding(Queue orderSecKillQueue, Exchange orderSecKillExchange) {
        return BindingBuilder.bind(orderSecKillQueue).to(orderSecKillExchange).with(env.getProperty("mq.pay.routing.seckillkey")).noargs();
    }


}
