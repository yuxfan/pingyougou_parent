package com.jlb.core.controller.item;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jlb.core.pojo.item.ItemCat;
import com.jlb.core.service.item.ItemCatService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("itemCat")
public class ItemCatController {
    @Reference
    private ItemCatService itemCatService;

    //根据父id查询三级分类
    @RequestMapping("/findByParentId.do")
    public List<ItemCat> findByParentId(Long parentId){
        return itemCatService.findByParentId(parentId);
    }

    //查询商品审核的所有分类列表
    @RequestMapping("/findAll.do")
    public List<ItemCat> findAll(){
    return itemCatService.findAll();
    }
}
