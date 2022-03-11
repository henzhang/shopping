package com.shopping.controller;

import com.shopping.moudle.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

/****
 * @Author:henzhang
 * @Description: thymeleaf模版引擎
 *****/

// @RestController = @Controller + @ResponseBody (响应json数据)      现在需要响应页面
@Controller
@RequestMapping(value = "/test")
public class TestController {

    @RequestMapping(value = "/hello")
    public String hello(Model model) {
        model.addAttribute("message", "hello thymeleaf ! ");

        // 集合定义
        List<User> users = new ArrayList<>();
        users.add(new User(1,"张三","深圳"));
        users.add(new User(2,"李四","北京"));
        users.add(new User(3,"王五","武汉"));
        model.addAttribute("users",users);

        //Map定义
        Map<String,Object> dataMap = new HashMap<String,Object>();
        dataMap.put("No","123");
        dataMap.put("address","深圳");
        model.addAttribute("dataMap",dataMap);

        //存储一个数组
        String[] names = {"张三","李四","王五"};
        model.addAttribute("names",names);

        //日期
        model.addAttribute("now",new Date());

        //if条件
        model.addAttribute("age",22);

        return "demo1";
    }

}
