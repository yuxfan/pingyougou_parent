package com.jlb.core.service.vo;

import com.jlb.core.entity.PageResult;
import com.jlb.core.pojo.good.Goods;
import com.jlb.core.vo.GoodsVo;

public interface GoodsVoService {

    void add(GoodsVo goodsVo);

    //分页条件查询
    PageResult search(Integer page,Integer rows,Goods goods);

    //商品管理修改之回显
    GoodsVo findOne(Long id);

    //商品管理修改之更新
    void update(GoodsVo goodsVo);

    //运营商商品待审核之 分页查询
    PageResult searchByManager(Integer page,Integer rows,Goods goods);

    //商品审核
    void updateStatus(Long[] ids,String status);

    //商品审核之批量删除
    void delete(Long[] ids);
}
