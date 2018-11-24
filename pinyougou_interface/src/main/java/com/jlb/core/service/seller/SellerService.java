package com.jlb.core.service.seller;

import com.jlb.core.entity.PageResult;
import com.jlb.core.pojo.seller.Seller;

public interface SellerService {
    void add(Seller seller);
    //分页查询
    PageResult search(Integer page,Integer rows,Seller seller);
    //根据id查询  回显
    Seller findOne(String sellerId);
    //更新复选框状态
    void updateStatus(String sellerId,String status);
}
