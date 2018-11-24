package com.jlb.core.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jlb.core.entity.Result;
import com.jlb.core.service.seckill.SeckillOrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("seckillOrder")
public class SeckillOrderController {

    @Reference
    private SeckillOrderService seckillOrderService;

    /**
     * 提交订单(秒杀订单)
     * @param seckillId
     * @param
     * @return
     */
    @RequestMapping("/submitOrder.do")
    public Result submitOrder(Long seckillId){
        //获取用户名
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(userId)){
            return new Result(false,"请先登录");
        }

        try {
            //提交订单
            seckillOrderService.submitOrder(seckillId,userId);
            return new Result(true,"提交成功");
        }catch (RuntimeException re){
            re.printStackTrace();
        return new Result(false,re.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"提交失败");
        }
    }
}
