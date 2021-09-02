package com.changgou.goods.service;


import com.changgou.goods.pojo.Brand;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface BrandService {

    List<Brand> findAll();

    Brand findById(Integer id);

    void add(Brand brand);

    void update(Brand brand);

    void delete(Integer id);

    /**
     * 多条件搜索 (无分页)
     *
     * @param brand
     * @return
     */
    List<Brand> findList(Brand brand);

    /**
     * 分页
     */
    PageInfo<Brand> findList(Integer page, Integer pageSize);

    /**
     * 分页 + 多条件搜索
     *
     * @param page
     * @param pageSize
     * @param brand
     * @return
     */
    PageInfo<Brand> findList(Integer page, Integer pageSize, Brand brand);

}
