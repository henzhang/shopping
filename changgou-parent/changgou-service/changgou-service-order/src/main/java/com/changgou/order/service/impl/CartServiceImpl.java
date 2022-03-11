package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/****
 * @Author:henzhang
 * @Description:
 *****/

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SpuFeign spuFeign;


    @Override
    public void add(Integer number, Long id, String username) {
        // 特殊情况处理 .
        if (number <= 0) {
            // 当number<=0时，移除redis中的相关购物条目数据
            redisTemplate.boundHashOps("Cart_" + username).delete(id);

            // 如果此时购物车数据为null ， 则移除购物车
            Long size = redisTemplate.boundHashOps("Cart_" + username).size();
            if (size == null || size <= 0) {
                redisTemplate.delete("Cart_" + username);
            }
            return;
        }

        // 1. 查询商品的详情
        Result<Sku> skuResult = skuFeign.findById(id);
        Sku sku = skuResult.getData();
        Result<Spu> spuResult = spuFeign.findById(sku.getSpuId());
        Spu spu = spuResult.getData();

        // 2. 将加入购物车的商品信息封装到 OrderItem (这是订单的实体类，但是其属性包括了所有的购物车条目属性，所以可以复用)
        OrderItem orderItem = createOrderItem(number, sku, spu);

        // 3. 将购物车的数据存入到redis中
        redisTemplate.boundHashOps("Cart_" + username).put(id, orderItem);

    }

    @Override
    public List<OrderItem> list(String username) {
        // values() 获取指定命名空间下所有数据
        return redisTemplate.boundHashOps("Cart_" + username).values();
    }

    /**
     * 创建一个OrderItem 对象
     *
     * @param number
     * @param sku
     * @param spu
     * @return
     */
    private OrderItem createOrderItem(Integer number, Sku sku, Spu spu) {
        OrderItem orderItem = new OrderItem();
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        orderItem.setSpuId(spu.getId());
        orderItem.setSkuId(sku.getId());
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(number);
        orderItem.setMoney(number * orderItem.getPrice());
        orderItem.setImage(spu.getImage());
        return orderItem;
    }
}
