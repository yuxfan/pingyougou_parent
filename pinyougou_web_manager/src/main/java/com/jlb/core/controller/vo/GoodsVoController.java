package com.jlb.core.controller.vo;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jlb.core.entity.PageResult;
import com.jlb.core.entity.Result;
import com.jlb.core.pojo.good.Goods;
import com.jlb.core.service.page.ItemPageService;
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

    //运营商商品待审核之 分页查询
    @RequestMapping("/search.do")
    public PageResult searchByManager(Integer page, Integer rows,@RequestBody Goods goods){
        return goodsVoService.searchByManager(page,rows,goods);
    }

    //商品审核   根据id来审核商品状态
    @RequestMapping("/updateStatus.do")
    public Result updateStatus(Long[] ids,String status){
        try {
            goodsVoService.updateStatus(ids,status);
            for (Long id : ids) {
                itemPageService.genItemHtml(id);
            }
            return new Result(true,"操作成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"操作失败");
        }
    }


    //商品审核 之 删除(状态更新)
    @RequestMapping("/delete.do")
    public Result delete(Long[] ids){
        try {
            goodsVoService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

    @Reference
    private ItemPageService itemPageService;
    @RequestMapping("/genHtml")
    private void genHtml(Long goodsId) throws Exception {
        itemPageService.genItemHtml(goodsId);
    }
}
