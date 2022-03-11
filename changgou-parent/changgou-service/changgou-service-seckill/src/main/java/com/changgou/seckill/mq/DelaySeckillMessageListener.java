package com.changgou.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.service.SeckillOrderService;
import entity.SeckillStatus;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/****
 * @Author:henzhang
 * @Description: 秒杀订单mq监听
 *****/

@Component
@RabbitListener(queues = "secKillQueue")
public class DelaySeckillMessageListener {

    @Autowired
    private SeckillOrderService seckillOrderService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 消息监听
     *
     * @param message
     */
    @RabbitHandler
    public void getMessage(String message) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
            System.out.println("秒杀商品超时回滚时间 ：" + simpleDateFormat.format(new Date()));

            // 获取用户的排队信息
            SeckillStatus seckillStatus = JSON.parseObject(message, SeckillStatus.class);

            // 如果此时redis中没有用户的排队信息，则表示订单已经处理 ,如果有用户的排队信息, 则表示用户尚未完成支付，关闭订单(关闭微信支付)
            SeckillStatus userQueueStatus = (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(seckillStatus.getUsername());
            if (userQueueStatus == null) {
                //关闭微信支付
                //删除订单
                seckillOrderService.deleteOrder(seckillStatus.getUsername());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
