package com.jlb.core.controller.spec;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jlb.core.service.spec.SpecificationService;
import com.jlb.core.entity.PageResult;
import com.jlb.core.entity.Result;
import com.jlb.core.pojo.specification.Specification;
import com.jlb.core.vo.SpecificationVo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("specification")
public class SpecificationController {
    //禁用resource  数据展示不出来
    @Reference    //关联  引用
   private SpecificationService specificationService;

    /**
     * 条件分页查询
     */
    @RequestMapping("/search.do")
    public PageResult search(Integer page, Integer rows, @RequestBody Specification specification){
        return specificationService.search(page,rows,specification);
    }

    //增加
    @RequestMapping("/add.do")
    public Result add(@RequestBody SpecificationVo specificationVo){
        try {
            specificationService.add(specificationVo);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }

    //根据id查询回显数据
    @RequestMapping("/findOne.do")
    public SpecificationVo findOne(Long id){
        return specificationService.findOne(id);
    }

    //更新
    @RequestMapping("/update.do")
    public Result update(@RequestBody SpecificationVo specificationVo){
        try {
            specificationService.update(specificationVo);
            return new Result(true, "更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "更新失败");
        }
    }


    //删除
    @RequestMapping("/delete.do")
    public Result dele(Long[] ids){
        try {
            specificationService.dele(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

    //分类模板之规格选项回显
    @RequestMapping("/selectOptionList.do")
    public List<Map<String,String>> selectOptionList(){
        return specificationService.selectOptionList();
    }
}
