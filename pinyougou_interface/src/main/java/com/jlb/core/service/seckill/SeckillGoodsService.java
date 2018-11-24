package com.jlb.core.service.seckill;

import com.jlb.core.pojo.seckill.SeckillGoods;

import java.util.List;

public interface SeckillGoodsService {
    /**
     * 查询秒杀商品列表
     * @return
     */
    public List<SeckillGoods> findList();

    /**
     * 根据id从Redis在中取商品详情
     * @param id
     * @return
     */
    public SeckillGoods findOneFromRedis(Long id);


}
