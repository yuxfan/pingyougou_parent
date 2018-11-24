package com.jlb.core.service.spec;

import com.jlb.core.entity.PageResult;
import com.jlb.core.pojo.specification.Specification;
import com.jlb.core.vo.SpecificationVo;

import java.util.List;
import java.util.Map;

public interface SpecificationService {
    PageResult search(Integer page, Integer rows, Specification specification);

    void add(SpecificationVo specificationVo);

    SpecificationVo findOne(Long id);

    //更新规格
    void update(SpecificationVo specificationVo);

    void dele(Long[] ids);

    //分类模板之规格选项回显
    List<Map<String,String>> selectOptionList();
}
