package com.changgou.goods.service.impl;

import com.changgou.goods.dao.ParaMapper;
import com.changgou.goods.dao.TemplateMapper;
import com.changgou.goods.pojo.Para;
import com.changgou.goods.pojo.Template;
import com.changgou.goods.service.ParaService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author henzhang
 */
@Service
public class ParaServiceImpl implements ParaService {
    @Autowired
    private ParaMapper paraMapper;

    @Autowired
    private TemplateMapper templateMapper;


    /**
     * 分页+条件搜索
     *
     * @param para
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Para> findPage(Para para, int page, int size) {
        //分页
        PageHelper.startPage(page, size);
        //搜索条件构建
        Example example = createExample(para);
        //执行搜索
        return new PageInfo<Para>(paraMapper.selectByExample(example));
    }

    private Example createExample(Para para) {
        Example example = new Example(Para.class);
        Example.Criteria criteria = example.createCriteria();
        if (para != null) {
            // id
            if (!StringUtils.isEmpty(para.getId())) {
                criteria.andEqualTo("id", para.getId());
            }
            // 名称
            if (!StringUtils.isEmpty(para.getName())) {
                criteria.andLike("name", "%" + para.getName() + "%");
            }
            // 选项
            if (!StringUtils.isEmpty(para.getOptions())) {
                criteria.andEqualTo("options", para.getOptions());
            }
            // 排序
            if (!StringUtils.isEmpty(para.getSeq())) {
                criteria.andEqualTo("seq", para.getSeq());
            }
            // 模板ID
            if (!StringUtils.isEmpty(para.getTemplateId())) {
                criteria.andEqualTo("templateId", para.getTemplateId());
            }
        }
        return example;
    }

    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Para> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        return new PageInfo<Para>(paraMapper.selectAll());
    }

    /**
     * 条件查询
     *
     * @param para
     * @return
     */
    @Override
    public List<Para> findList(Para para) {
        Example example = createExample(para);
        return paraMapper.selectByExample(example);
    }

    @Override
    public void delete(Integer id) {
        //根据ID查询
        Para para = paraMapper.selectByPrimaryKey(id);
        //修改模板统计数据
        updateParaNum(para, -1);
        paraMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void update(Para para) {
        paraMapper.updateByPrimaryKey(para);
    }

    @Override
    public void add(Para para) {
        paraMapper.insert(para);
        updateParaNum(para, 1);
    }

    @Override
    public Para findById(Integer id) {
        return paraMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Para> findAll() {
        return paraMapper.selectAll();
    }

    /**
     * 修改模板统计数据
     *
     * @param para:操作的参数
     * @param count:变更的数量
     */
    public void updateParaNum(Para para, int count) {
        //修改模板数量统计
        Template template = templateMapper.selectByPrimaryKey(para.getTemplateId());
        template.setParaNum(template.getParaNum() + count);
        templateMapper.updateByPrimaryKeySelective(template);
    }
}
