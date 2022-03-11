package com.changgou.search.controller;

import com.changgou.search.feign.SkuFeign;
import com.changgou.search.pojo.SkuInfo;
import entity.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/****
 * @Author:henzhang
 * @Description:
 *****/
@Controller
@RequestMapping("/search")
public class SkuController {


    @Autowired
    private SkuFeign skuFeign;

    /**
     * 实现搜索调用
     *
     * @param searchMap
     * @return
     */
    @RequestMapping(value = "/list")
    public String search(@RequestParam(required = false) Map<String, String> searchMap, Model model) {
        Map<String, Object> resultMap = skuFeign.search(searchMap);
        model.addAttribute("result", resultMap);

        // 将条件存储，用于页面回显数据
        model.addAttribute("searchMap", searchMap);

        // 计算分页
        Page<SkuInfo> pageInfo = new Page<SkuInfo>(
                Long.parseLong(resultMap.get("total").toString()),
                Integer.parseInt(resultMap.get("pageNumber").toString()) + 1, //pageNumber从0开始，页面从1开始
                Integer.parseInt(resultMap.get("pageSize").toString())
        );
        model.addAttribute("pageInfo", pageInfo);

        // 获取上次请求地址
        String[] urls = url(searchMap);
        model.addAttribute("url", urls[0]);
        model.addAttribute("sortUrl", urls[1]);
        return "search";
    }

    /**
     * 拼接组装用户请求的url地址
     * <p>
     * 1. 获取用户每次请求的url地址
     * 2. 页面需要在这次请求的地址上面添加额外的搜索条件
     * http://localhost:18086/search/list
     * http://localhost:18086/search/list?keywords=华为
     * http://localhost:18086/search/list?keywords=华为&category=笔记本
     * http://localhost:18086/search/list?keywords=华为&category=笔记本&spec_颜色=红
     */
    public String[] url(Map<String, String> searchMap) {
        String url = "/search/list";     //基本地址
        String sortUrl = "/search/list"; //排序地址
        if (searchMap != null && searchMap.size() > 0) {
            url += "?";
            sortUrl += "?";
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                String key = entry.getKey();

                //跳过分页参数
                if (key.equalsIgnoreCase("pageNum")) {
                    continue;
                }

                String value = entry.getValue();

                url += key + "=" + value + "&";

                // 排序参数，则跳过
                if (key.equalsIgnoreCase("sortField") || key.equalsIgnoreCase("sortRule")) {
                    continue;
                }
                sortUrl += key + "=" + value + "&";

            }
            // 去掉最后一个&
            url = url.substring(0, url.length() - 1);
            sortUrl = sortUrl.substring(0, sortUrl.length() - 1);
        }
        return new String[]{url, sortUrl};
    }
}
