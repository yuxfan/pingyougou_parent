package com.jlb.core.service.item;

import com.jlb.core.pojo.item.ItemCat;

import java.util.List;
import java.util.Map;

public interface ItemCatService {

    List<ItemCat> findByParentId(Long parentId);

    ItemCat findOne(Long id);
    //查询所有分类列表
    List<ItemCat> findAll();

}
