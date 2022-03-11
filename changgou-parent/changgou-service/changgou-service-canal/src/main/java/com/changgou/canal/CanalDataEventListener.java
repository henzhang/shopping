package com.changgou.canal;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.content.feign.ContentFeign;
import com.changgou.content.pojo.Content;
import com.changgou.item.feign.PageFeign;
import com.xpand.starter.canal.annotation.*;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/****
 * @Author:henzhang
 * @Description: 实现 mysql 数据库监听
 *****/

@CanalEventListener
public class CanalDataEventListener {

    @Autowired
    private ContentFeign contentFeign;

    @Autowired
    private PageFeign pageFeign;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * test :
     * <p>
     * 1. 增加数据监听
     *
     * @param eventType 当前操作的类型 增加数据
     * @param rowData   发生变更的一行数据
     *                  <p>
     *                  rowData.getAfterColumnsList() : 增加、修改   (获取变更之后的数据)
     *                  rowData.getBeforeColumnsList() : 删除、修改  (获取变更之前的数据)
     */
    @InsertListenPoint
    public void onEventInsert(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        rowData.getAfterColumnsList().forEach((c) -> System.out.println("列名: " + c.getName() + " 新增后的数据::   " + c.getValue()));
    }

    /***
     * 2. 修改数据监听
     *
     * @param rowData
     */
    @UpdateListenPoint
    public void onEventUpdate(CanalEntry.RowData rowData) {
        rowData.getBeforeColumnsList().forEach((c) -> System.out.println("列名: : " + c.getName() + " 修改前的数据::   " + c.getValue()));
        rowData.getAfterColumnsList().forEach((c) -> System.out.println("列名: : " + c.getName() + " 修改后的数据::   " + c.getValue()));
    }

    /***
     * 3. 删除数据监听
     *
     * @param rowData
     */
    @DeleteListenPoint
    public void onEventDelete(CanalEntry.RowData rowData) {
        rowData.getBeforeColumnsList().forEach((c) -> System.out.println("列名: : " + c.getName() + " 删除前的数据::   " + c.getValue()));
    }


    /***
     *
     * 自定义数据监听
     * @param eventType
     * @param rowData
     *
     * 监测广告表的数据变化，当发生变化时候，更新到redis中.
     */
    @ListenPoint(destination = "example",   // 指实例的地址
            schema = "changgou_content",    // 指监听的数据库
            table = {"tb_content_category", "tb_content"},  // 指监听的表
            eventType = {
                    CanalEntry.EventType.UPDATE,
                    CanalEntry.EventType.INSERT,
                    CanalEntry.EventType.DELETE    // 指监听的类型（增删改查）
            })
    public void onEventCustomUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        //1.获取列名 为category_id的值
        String categoryId = getColumnValue(eventType, rowData);
        //2.调用feign 获取该分类下的所有的广告集合
        Result<List<Content>> byCategory = contentFeign.findByCategory(Long.valueOf(categoryId));
        //3.使用redisTemplate存储到redis中
        List<Content> data = byCategory.getData();//List
        stringRedisTemplate.boundValueOps("content_" + categoryId).set(JSON.toJSONString(data));
    }

    private String getColumnValue(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        // 是delete 操作
        if (eventType == CanalEntry.EventType.DELETE) {
            for (CanalEntry.Column temp : rowData.getBeforeColumnsList()) {
                if (temp.getName().equalsIgnoreCase("category_id")) {
                    return temp.getValue();
                }
            }
        } else {
            // 是 update or insert 操作
            for (CanalEntry.Column temp : rowData.getAfterColumnsList()) {
                if (temp.getName().equalsIgnoreCase("category_id")) {
                    return temp.getValue();
                }
            }
        }
        return null;
    }


    /**
     * 监听商品数据库的tb_spu的数据变化,当数据变化的时候生成静态页或者删除静态页
     *
     * @param eventType
     * @param rowData
     */
    @ListenPoint(
            destination = "example",
            schema = "changgou_goods",
            table = "tb_spu",
            eventType = {CanalEntry.EventType.INSERT, CanalEntry.EventType.UPDATE, CanalEntry.EventType.DELETE}
    )
    public void onEventCustomSpu(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        if (eventType == CanalEntry.EventType.DELETE) {
            String spuId = "";
            List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
            for (CanalEntry.Column column : beforeColumnsList) {
                if (column.getName().equals("id")) {
                    spuId = column.getValue();
                    break;
                }
            }
            // 根据 spuId 删除静态页

        } else {
            String spuId = "";
            List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
            for (CanalEntry.Column column : afterColumnsList) {
                if (column.getName().equals("id")) {
                    spuId = column.getValue();
                    break;
                }
            }
            //更新  生成静态页
            pageFeign.createHtml(Long.valueOf(spuId));
        }

    }


}
