package com.changgou.order.service;

import com.changgou.order.pojo.OrderItem;

import java.util.List;

/****
 * @Author:henzhang
 * @Description:
 *****/
public interface CartService {

    /**
     * 加入购物车
     */
    void add(Integer number, Long id, String username);

    /**
     * 查询购物车
     */
    List<OrderItem> list(String username);

}
