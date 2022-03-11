package com.changgou.seckill.task;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import entity.IdWorker;
import entity.SeckillStatus;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/****
 * @Author:henzhang
 * @Description:
 *****/

@Component
public class MultiThreadingCreateOrder {

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 多线程下单操作
     *
     * @Async 该方法会异步执行，底层是多线程
     */
    @Async
    public void createOrder() {
        //从队列中获取排队信息
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();
        if (seckillStatus != null) {

            //从队列中获取一个商品
            Object sgood = redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillStatus.getGoodsId()).rightPop();
            if (sgood == null) {
                //清理当前用户的排队信息
                clearQueue(seckillStatus);
                return;
            }

            //时间区间
            String time = seckillStatus.getTime();
            //用户登录名
            String username = seckillStatus.getUsername();
            //用户抢购商品
            Long id = seckillStatus.getGoodsId();

            // 1. 查找秒杀商品
            String namespace = "SeckillGoods_" + time;
            SeckillGoods goods = (SeckillGoods) redisTemplate.boundHashOps(namespace).get(id);

            // 2. 判断有没有库存
            if (goods == null || goods.getStockCount() <= 0) {
                throw new RuntimeException("已售罄!");
            }

            // 3. 下订单
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(idWorker.nextId());
            seckillOrder.setSeckillId(id); // 商品id
            seckillOrder.setMoney(goods.getCostPrice()); //支付金额
            seckillOrder.setUserId(username); // 用户名
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setStatus("0"); // 未支付

            /**
             * 4. 存入redis
             *
             * a. 1个用户只允许有一个未支付的秒杀订单
             * b. 订单存入redis
             * c. hash
             *    namespace : SeckillOrder
             *    key : username
             *    value : 订单信息
             */
            redisTemplate.boundHashOps("SeckillOrder").put(username, seckillOrder);

            /**
             * 5. 库存递减
             * 商品有可能是最后一个 :
             * 如果是最后一个，则将redis中的数据删除掉，并将该数据信息同步回mysql.
             */
            goods.setStockCount(goods.getStockCount() - 1);

            //获的该商品对应的商品数量
            Long size = redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillStatus.getGoodsId()).size();
            //判断当前商品是否还有库存
            if (goods.getStockCount() <= 0) {
                //同步数量
                goods.setStockCount(size.intValue());
                //并且将商品数据同步到MySQL中
                seckillGoodsMapper.updateByPrimaryKeySelective(goods);
                //如果没有库存,则清空Redis缓存中该商品
                redisTemplate.boundHashOps(namespace).delete(id);
            } else {
                //如果有库存，则直数据重置到Reids中
                redisTemplate.boundHashOps(namespace).put(id, goods);
            }

            //抢单成功，更新抢单状态,排队->等待支付
            seckillStatus.setStatus(2);  // 待付款
            seckillStatus.setOrderId(seckillOrder.getId());
            seckillStatus.setMoney(Float.parseFloat(seckillOrder.getMoney())); // 支付金额
            redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);


            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
            System.out.println("秒杀商品下单时间 ：" + simpleDateFormat.format(new Date()));

            //发送消息给延时队列
            rabbitTemplate.convertAndSend("delaySecKillQueue", (Object) JSON.toJSONString(seckillStatus), new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    message.getMessageProperties().setExpiration("10000");
                    return message;
                }
            });

            System.out.println("下单完成!");
        }

    }

    /***
     * 清理用户排队信息
     * @param seckillStatus
     */
    public void clearQueue(SeckillStatus seckillStatus) {
        //清理排队标示
        redisTemplate.boundHashOps("UserQueueCount").delete(seckillStatus.getUsername());
        //清理抢单标示
        redisTemplate.boundHashOps("UserQueueStatus").delete(seckillStatus.getUsername());

        System.out.println("abc" + "bcs");
    }
}
