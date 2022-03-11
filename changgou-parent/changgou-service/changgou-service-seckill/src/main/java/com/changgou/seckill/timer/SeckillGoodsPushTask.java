package com.changgou.seckill.timer;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import entity.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;

/****
 * @Author:henzhang
 * @Description: 定时将秒杀任务存入到redis缓存中.
 *****/
@Component
public class SeckillGoodsPushTask {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /****
     * 每30秒执行一次
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void loadGoodsPushRedis() {
        /**
         * 首先查询出当前需要展示的时间菜单
         *
         * 1. 秒杀库存 > 0
         * 2. 审核状态 : 已审核
         * 3. 开始时间 start_time , 结束时间 end_time
         * 时间菜单的开始时间 <= start_time , end_time <= (时间菜单的开始时间+2hours)
         */

        // 时间菜单
        List<Date> dateMenus = DateUtil.getDateMenus();

        // 求每个时间区间的秒杀商品
        for (Date dateMenu : dateMenus) {
            String timeSpace = "SeckillGoods_" + DateUtil.date2Str(dateMenu);  // 格式 : yyyyMMddHH
            /**
             * 1. 秒杀库存 > 0
             * 2. 审核状态 : 已审核
             * 3. 开始时间 start_time , 结束时间 end_time
             * 时间菜单的开始时间 <= start_time , end_time <= 时间菜单的开始时间+2 hours
             */
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andGreaterThan("stockCount", 0);
            criteria.andEqualTo("status", "1");
            criteria.andGreaterThanOrEqualTo("startTime", dateMenu);
            criteria.andLessThanOrEqualTo("endTime", DateUtil.addDateHour(dateMenu, 2));

            /**
             * 排除掉已经存入到redis中的seckillGoods
             * 1. 求出当前命名空间下已有的商品id
             * 2. 查询时排除掉这些已存在的商品数据
             */
            Set keys = redisTemplate.boundHashOps(timeSpace).keys();
            if (keys != null && keys.size() > 0) {
                criteria.andNotIn("id", keys);
            }

            // 查询数据
            List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(example);
            System.out.println("查询到的数据数据条数 :" + seckillGoods.size());

            for (SeckillGoods seckillGood : seckillGoods) {
                System.out.println("商品id : " + seckillGood.getId() + " 存入到redis " + timeSpace);
                // 插入到redis
                redisTemplate.boundHashOps(timeSpace).put(seckillGood.getId(), seckillGood);
                // 给每个商品做个队列
                Long[] ids = pushIds(seckillGood.getStockCount(), seckillGood.getId());
                redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillGood.getId()).leftPushAll(ids);
            }

        }
    }

    /***
     * 将商品ID存入到数组中
     * @param len:长度
     * @param id :值
     * @return
     */
    public Long[] pushIds(int len, Long id) {
        Long[] ids = new Long[len];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = id;
        }
        return ids;
    }
}
