package com.changgou.item.service;

/****
 * @Author:henzhang
 * @Description:
 *****/
public interface PageService {
    /**
     * 根据商品的ID 生成静态页
     *
     * @param spuId
     */
    void createPageHtml(Long spuId);
}
