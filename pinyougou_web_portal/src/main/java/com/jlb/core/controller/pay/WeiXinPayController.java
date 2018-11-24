package com.jlb.core.controller.pay;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jlb.core.entity.Result;
import com.jlb.core.pojo.log.PayLog;
import com.jlb.core.service.order.OrderService;
import com.jlb.core.service.pay.WeiXinPayService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("pay")
public class WeiXinPayController {

    @Reference(timeout = 1000*60*6)
    private WeiXinPayService weiXinPayService;

    @Reference
    private OrderService orderService;


    /**
     * 生成微信支付二维码
     * 返回订单号和金额
     * @return
     */
    @RequestMapping("/createNative.do")
    public HashMap createNative(){
        /*IdWorker idworker=new IdWorker();
        return weiXinPayService.createNative(idworker.nextId()+"","1");*/
        //获用户名
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //根据用户名从Redis中查询
        PayLog payLog = orderService.searchPayLogFromRedis(userId);
        if (payLog!=null){    //如果不等于空,返回生成的二维码,携带订单号和总金额
            return weiXinPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        }else{
            return new HashMap();
        }
    }

    /**
     * 循环查询订单支付状态
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus.do")
    public Result queryPayStatus(String out_trade_no){
        HashMap map = weiXinPayService.queryPayStatusWhile(out_trade_no);
        if (map==null){   //如果没有支付
            return  new Result(false,"二维码超时");
        }else {
            if ("SUCCESS".equals(map.get("trade_state"))){   //trade_state:交易状态
                //更新订单状态
                orderService.updateOrderStatus(out_trade_no,(String) map.get("transaction_id"));
                return new Result(true,"支付成功");
            }else {
                return new Result(false,"支付失败");

            }
        }
    }
}
