package com.jlb.core.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jlb.core.entity.Result;
import com.jlb.core.pojo.order.Order;
import com.jlb.core.service.order.OrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("order")
public class OrderController {

    @Reference
    private OrderService orderService;

    @RequestMapping("/add.do")
    public Result add(@RequestBody Order order){
        //获取用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        order.setUserId(username);
        try {
            orderService.add(order);
            return new Result(true,"保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"保存失败");

        }
    }
}
