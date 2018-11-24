package com.jlb.core.service;

import com.jlb.core.entity.PageResult;
import com.jlb.core.pojo.good.Brand;

import java.util.List;
import java.util.Map;

public interface BrandService {

    List<Brand> findAll() ;

    PageResult findPage(Integer pageNum, Integer pageSize) throws Exception;

    PageResult search(Integer pageNum, Integer pageSize, Brand brand) throws Exception;

    void add(Brand brand) throws Exception;

    Brand findOne(Long id) throws Exception;

    void update(Brand brand);

    void delete(Long[] ids);

    //查询分类模板关联的品牌列表
    List<Map<String,String>> selectOptionList();

}
