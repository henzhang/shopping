package com.changgou.search.service;

import java.util.Map;

/****
 * @Author:henzhang
 * @Description:
 *****/
public interface SkuService {


    /***
     * 搜索
     * @param searchMap
     * @return
     */
    Map search(Map<String, String> searchMap);

    /***
     * 导入SKU数据
     */
    void importSku();
}
