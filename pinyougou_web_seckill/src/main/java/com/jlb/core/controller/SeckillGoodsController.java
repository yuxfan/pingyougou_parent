package com.jlb.core.controller;

import com.jlb.core.pojo.seckill.SeckillGoods;
import com.alibaba.dubbo.config.annotation.Reference;
import com.jlb.core.service.seckill.SeckillGoodsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("seckillGoods")
public class SeckillGoodsController {

    @Reference
    private SeckillGoodsService seckillGoodsService;

    /**
     * 查询秒杀商品列表
     * @return
     */
    @RequestMapping("/findList.do")
    public List<SeckillGoods> findList(){
        return seckillGoodsService.findList();
    }

    /**
     * 根据id从Redis中查询商品详情
     * @param id
     * @return
     */
    @RequestMapping("/findOneFromRedis.do")
    public SeckillGoods findOneFromRedis(Long id){
        return seckillGoodsService.findOneFromRedis(id);
    }
}
