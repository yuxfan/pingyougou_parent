package com.jlb.core.controller.vo;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jlb.core.entity.PageResult;
import com.jlb.core.entity.Result;
import com.jlb.core.pojo.good.Goods;
import com.jlb.core.service.vo.GoodsVoService;
import com.jlb.core.vo.GoodsVo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("goods")
public class GoodsVoController {

    @Reference
    private GoodsVoService goodsVoService;

    @RequestMapping("/add.do")
    public Result add(@RequestBody GoodsVo goodsVo){
        try {
            //设置商家id
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            goodsVo.getGoods().setSellerId(sellerId);
            goodsVoService.add(goodsVo);
            return new Result(true,"保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"保存失败");
        }
    }


    //查询当前商家的商品列表
    @RequestMapping("/search.do")
    public PageResult search(Integer page,Integer rows,@RequestBody Goods goods){
        //设置商家id
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(sellerId);
        return goodsVoService.search(page,rows,goods);
    }

    //商品管理修改之回显
    @RequestMapping("/findOne.do")
    public GoodsVo findOne(Long id){
        return goodsVoService.findOne(id);
    }

    //商品管理修改之更新
    @RequestMapping("/update.do")
    //@requestbody 不要忘  不要忘  不要忘
    public Result update(@RequestBody GoodsVo goodsVo){
        try {
            goodsVoService.update(goodsVo);
            return new Result(true,"更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"更新失败");

        }
    }
}
