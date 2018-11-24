package com.jlb.core.controller.template;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jlb.core.pojo.template.TypeTemplate;
import com.jlb.core.service.template.TypeTemplateService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("typeTemplate")
public class TypeTemplateController {

    @Reference
    private TypeTemplateService typeTemplateService;

    /*
    * 查询三级分类下的品牌列表
    * */
    @RequestMapping("/findOne.do")
    public TypeTemplate findone(Long id){

        return typeTemplateService.findOne(id);
    }

    //查询分类下的规格列表
    @RequestMapping("/findBySpecList.do")
    public List<Map> findBySpecList(Long id){
        return typeTemplateService.findBySpecList(id);
    }



}
