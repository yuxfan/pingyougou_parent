package com.jlb.core.controller.seller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jlb.core.entity.PageResult;
import com.jlb.core.entity.Result;
import com.jlb.core.pojo.seller.Seller;
import com.jlb.core.service.seller.SellerService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("seller")
public class SellerController {

    @Reference
    private SellerService sellerService;

    //查询所有,条件查询和分页于一体
    @RequestMapping("/search.do")
    public PageResult search(Integer page, Integer rows,@RequestBody Seller seller){
       return sellerService.search(page,rows,seller);
    }

    //查询实体(待审核商家详情)
    @RequestMapping("/findOne.do")
    public Seller findOne(String id){
      return sellerService.findOne(id);
    }

    //对商家进行审核(根据id更新状态)
    @RequestMapping("/updateStatus.do")
    public Result updateStatus(String sellerId, String status){
        try {
            sellerService.updateStatus(sellerId,status);
            return new Result(true,"审核成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"审核失败");

        }
    }
}
