package com.changgou.order.controller;

import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import entity.StatusCode;
import entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/****
 * @Author:henzhang
 * @Description: 购物车操作
 *****/
@RestController
@RequestMapping(value = "/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 加入购物车
     */
    @GetMapping(value = "/add")
    public Result add(Integer num, Long id) {

        Map<String, String> userInfo = TokenDecode.getUserInfo();
        System.out.println(userInfo);
        String username = userInfo.get("username");
        cartService.add(num, id, username);
        return new Result(true, StatusCode.OK, "加入购物车成功 !");
    }

    /**
     * 购物车列表
     */
    @GetMapping(value = "/list")
    public Result<List<OrderItem>> list() {
        //{address=null, scope=["app"], name=null, company=null, id=null, exp=2064790406, authorities=["vip","user"], jti=d9204f60-d18a-4d7c-ac31-b7cfb77b762a, client_id=changgou, username=szitheima}
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        System.out.println(userInfo);
        String username = userInfo.get("username");
        List<OrderItem> orderItemList = cartService.list(username);
        return new Result<List<OrderItem>>(true, StatusCode.OK, "查询购物车列表成功 !", orderItemList);
    }
}
