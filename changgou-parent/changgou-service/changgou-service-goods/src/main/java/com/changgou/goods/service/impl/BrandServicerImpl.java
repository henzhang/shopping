package com.changgou.goods.service.impl;

import com.changgou.goods.dao.BrandMapper;
import com.changgou.goods.pojo.Brand;
import com.changgou.goods.service.BrandService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.Page;
import entity.PageResult;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandServicerImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public List<Brand> findAll() {
        return brandMapper.selectAll();
    }

    @Override
    public Brand findById(Integer id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    /**
     * 方法中只要带有selective会忽略空值
     *
     * @param brand brand : name有值 letter有值
     *              Mapper.insertSelective(brand) -> sql语句 : insert into tb_brand(name,letter) values(?,?)
     *              Mapper.insert(brand) -> sql语句 : insert into tb_brand(id,name,image,letter,seq) values(?,?,?,?,?)
     */
    @Override
    public void add(Brand brand) {
        brandMapper.insertSelective(brand);
    }

    @Override
    public void update(Brand brand) {
        brandMapper.updateByPrimaryKeySelective(brand);
    }

    @Override
    public void delete(Integer id) {
        brandMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<Brand> findList(Brand brand) {
        Example example = createExample(brand);
        //根据构建的条件查询数据
        return brandMapper.selectByExample(example);
    }

    @Override
    public PageInfo<Brand> findList(Integer page, Integer pageSize) {
        /**
         * 分页查询   要求 ：后面的查询紧跟集合查询
         * 1. page 当前页
         * 2. pageSize 每页显示多少条
         */
        PageHelper.startPage(page, pageSize);
        List<Brand> brands = brandMapper.selectAll();
        return new PageInfo<Brand>(brands);
    }

    @Override
    public PageInfo<Brand> findList(Integer page, Integer pageSize, Brand brand) {
        PageHelper.startPage(page, pageSize);
        Example example = createExample(brand);
        List<Brand> brands = brandMapper.selectByExample(example);
        return new PageInfo<>(brands);
    }

    /**
     * 构建查询对象
     *
     * @param brand
     * @return
     */
    public Example createExample(Brand brand) {
        // 自定义条件搜索对象 Example
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria(); //条件构造器
        if (brand != null) {
            // 品牌名称
            if (!StringUtils.isEmpty(brand.getName())) {
                /**
                 * 1. brand的属性名
                 * 2. 占位符参数，搜索的条件
                 */
                criteria.andLike("name", "%" + brand.getName() + "%");
            }
            // 品牌图片地址
            if (!StringUtils.isEmpty(brand.getImage())) {
                criteria.andLike("image", "%" + brand.getImage() + "%");
            }
            // 品牌的首字母
            if (!StringUtils.isEmpty(brand.getLetter())) {
                criteria.andLike("letter", "%" + brand.getLetter() + "%");
            }
            // 品牌id
            if (!StringUtils.isEmpty(brand.getLetter())) {
                criteria.andEqualTo("id", brand.getId());
            }
            // 排序
            if (!StringUtils.isEmpty(String.valueOf(brand.getSeq()))) {
                criteria.andEqualTo("seq", brand.getSeq());
            }
        }
        return example;
    }


}
