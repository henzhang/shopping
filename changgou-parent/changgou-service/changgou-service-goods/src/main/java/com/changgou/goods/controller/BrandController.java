package com.changgou.goods.controller;

import com.changgou.goods.pojo.Brand;
import com.changgou.goods.service.BrandService;
import com.github.pagehelper.PageInfo;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 跨域: A域名访问B域名的数据
 * 域名或者请求端口或协议不一致的时候，就算跨域
 */
@RestController
@RequestMapping(value = "/brand")
@CrossOrigin //跨域
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * 查询所有品牌
     */
    @GetMapping
    public Result<List<Brand>> findAll() {
        List<Brand> brands = brandService.findAll();
        return new Result<List<Brand>>(true, StatusCode.OK, "查询品牌集合成功！", brands);
    }

    /**
     * 根据主键id查询
     */
    @GetMapping(value = "/{id}")
    public Result<Brand> findById(@PathVariable(value = "id") Integer id) {
        Brand brand = brandService.findById(id);
        return new Result<Brand>(true, StatusCode.OK, "根据ID查询品牌成功！", brand);
    }

    /**
     * 增加品牌
     *
     * @param brand
     */
    @PostMapping
    public Result add(@RequestBody Brand brand) {
        brandService.add(brand);
        return new Result(true, StatusCode.OK, "增加品牌成功！");
    }


    /**
     * 修改品牌
     *
     * @param id
     * @param brand
     * @return
     */
    @PutMapping(value = "/{id}")
    public Result update(@PathVariable(value = "id") Integer id, @RequestBody Brand brand) {
        brand.setId(id);
        brandService.update(brand);
        return new Result(true, StatusCode.OK, "修改品牌成功！");
    }


    /**
     * 删除品牌
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}")
    public Result delete(@PathVariable(value = "id") Integer id) {
        brandService.delete(id);
        return new Result(true, StatusCode.OK, "删除品牌成功！");
    }

    /***
     * 多条件搜索品牌数据
     * @param brand
     * @return
     */
    @PostMapping(value = "/search")
    public Result<List<Brand>> findList(@RequestBody(required = false) Brand brand) {
        List<Brand> list = brandService.findList(brand);
        return new Result<List<Brand>>(true, StatusCode.OK, "多条件查询成功", list);
    }


    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}")
    public Result<PageInfo<Brand>> findList(@PathVariable(value = "page") Integer page,
                                            @PathVariable(value = "size") Integer size) {
        PageInfo<Brand> pageInfo = brandService.findList(page, size);
        return new Result<PageInfo<Brand>>(true, StatusCode.OK, "分页查询成功", pageInfo);
    }

    /**
     * 分页 + 多条件
     *
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}")
    public Result<PageInfo<Brand>> findList(@RequestBody Brand brand, @PathVariable(value = "page") Integer page,
                                            @PathVariable(value = "size") Integer size) {
        PageInfo<Brand> pageInfo = brandService.findList(page, size, brand);
        return new Result<PageInfo<Brand>>(true, StatusCode.OK, "分页查询成功", pageInfo);
    }

}
