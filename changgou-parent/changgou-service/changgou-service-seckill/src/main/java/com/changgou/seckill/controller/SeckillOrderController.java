package com.changgou.seckill.controller;

import com.changgou.seckill.service.SeckillOrderService;
import entity.Result;
import entity.SeckillStatus;
import entity.StatusCode;
import entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/****
 * @Author:henzhang
 * @Description:
 *****/

@RestController
@RequestMapping("/seckillOrder")
@CrossOrigin
public class SeckillOrderController {

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 添加秒杀订单
     */
    @RequestMapping(value = "/add")
    public Result add(String time, Long id) {
        String username = "szitheima";
        seckillOrderService.add(id, time, username);
        return new Result(true, StatusCode.OK, "正在排队...");
    }

    /****
     * 查询抢购
     * @return
     */
    @RequestMapping(value = "/query")
    public Result queryStatus() {
        String username = TokenDecode.getUserInfo().get("username"); //获取用户名

        SeckillStatus seckillStatus = seckillOrderService.queryStatus(username);
        if (seckillStatus != null) {
            return new Result(true, seckillStatus.getStatus(), "抢购状态", seckillStatus);
        }
        return new Result(false, StatusCode.NOTFOUNDERROR, "没有抢购信息");
    }

}
