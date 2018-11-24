package com.jlb.core.service.template;

import com.jlb.core.entity.PageResult;
import com.jlb.core.pojo.template.TypeTemplate;

import java.util.List;
import java.util.Map;

public interface TypeTemplateService {

    PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate);

    void add(TypeTemplate typeTemplate);

    TypeTemplate findOne(Long id);

    void update(TypeTemplate typeTemplate);

    //自定义批量删除
    void delete(Long[] ids);

    //查询出模板id后,加载其下的规格及规格选项
    List<Map> findBySpecList(Long id);
}
