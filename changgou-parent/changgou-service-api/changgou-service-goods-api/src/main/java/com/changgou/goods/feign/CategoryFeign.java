package com.changgou.goods.feign;

import com.changgou.goods.pojo.Category;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/****
 * @Author:henzhang
 * @Description:
 *****/

@FeignClient(name = "goods")
@RequestMapping(value = "/category")
public interface CategoryFeign {

    @GetMapping("/{id}")
    Result<Category> findById(@PathVariable Integer id);
}
