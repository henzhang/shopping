package com.changgou.goods.dao;


import com.changgou.goods.pojo.Brand;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/**
 * DAO 使用通用mapper
 * dao 使用通用mapper， dao接口需要继承 tk.mybatis.mapper.common.Mapper
 *
 * add data :
 * Mapper.insert()
 * Mapper.insertSelective()
 *
 * update data :
 * Mapper.update(T)
 * Mapper.updateByPrimaryKey(T)
 *
 * query data :
 * Mapper.selectByPrimaryKey(ID)
 * Mapper.select(T)

 */
@Component
public interface BrandMapper extends Mapper<Brand> {
}
