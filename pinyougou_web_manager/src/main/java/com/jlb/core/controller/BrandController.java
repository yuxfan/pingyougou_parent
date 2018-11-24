package com.jlb.core.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jlb.core.entity.PageResult;
import com.jlb.core.entity.Result;
import com.jlb.core.pojo.good.Brand;
import com.jlb.core.service.BrandService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController   //发送的请求都是json数据   可以理解为responsebody和controller的合体
@RequestMapping("brand")
public class BrandController {
    @Reference   //关联,引用
    private BrandService brandService;

    //查询全部品牌
    @RequestMapping("findAll")
    @ResponseBody
    public List<Brand> findAll(){
        List<Brand> brands = brandService.findAll();
        return brands;
    }

    //分页查询
    @RequestMapping("findPage")
    public PageResult findPage(Integer pageNum , Integer pageSize) throws Exception {
        PageResult pageResult = brandService.findPage(pageNum, pageSize);
        return pageResult;
    }
    //条件查询(模糊查询和根据首字母查询)   因为请求过来的是字符串,需要转换成对象,所以参数需要加requestbody注解来转换
    @RequestMapping("search")
    public PageResult search(Integer pageNum , Integer pageSize, @RequestBody Brand brand) throws Exception {
       return brandService.search(pageNum,pageSize,brand);
    }

    @RequestMapping("/add.do")
    //提交请求是json字符串,需要转成对象, 所以需要requestbody
    public Result add(@RequestBody Brand brand){
        try {
            brandService.add(brand);
            return new Result(true,"保存成功");   //返回result集合
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"保存失败");  //返回result集合
        }
    }

    //更新之 先查询(回显)
    @RequestMapping("/findOne.do")
    public Brand findOne(Long id) throws Exception {
        return brandService.findOne(id);
    }

    //品牌更新
    //更新传入的是brand对象,返回的是result对象(flag,message)
    @RequestMapping("/update.do")
    public Result update(@RequestBody Brand brand){
        try {
            brandService.update(brand);
            return new Result(true,"更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"更新失败");
        }
    }

    //删除品牌列表
    @RequestMapping("/delete.do")
    public Result delete(Long[] ids){
        try {
            brandService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

    //查询分类模板关联的品牌列表
    @RequestMapping("/selectOptionList.do")
    public List<Map<String, String>> selectOptionList(){
         return brandService.selectOptionList();
    }

}
