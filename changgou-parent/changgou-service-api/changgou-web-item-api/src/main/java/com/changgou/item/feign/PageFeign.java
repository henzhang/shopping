package com.changgou.item.feign;

import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/****
 * @Author:henzhang
 * @Description:
 *****/
@FeignClient(name = "item")
@RequestMapping("/page")
public interface PageFeign {

    /**
     * 根据SpuId 生成静态页
     *
     * @param id
     * @return
     */
    @RequestMapping("/createHtml/{id}")
    Result createHtml(@PathVariable(name = "id") Long id);
}
