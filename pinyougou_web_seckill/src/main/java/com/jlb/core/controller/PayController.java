package com.jlb.core.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jlb.core.entity.Result;
import com.jlb.core.pojo.seckill.SeckillOrder;
import com.jlb.core.service.pay.WeiXinPayService;
import com.jlb.core.service.seckill.SeckillOrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("pay")
public class PayController {

    @Reference
    private SeckillOrderService seckillOrderService;

    @Reference
    private WeiXinPayService weiXinPayService;

    @RequestMapping("/createNative.do")
    public HashMap createNative(){
        //获取用户名
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //从Redis中查询订单
        SeckillOrder seckillOrder = seckillOrderService.searchOrderFromRdis(userId);
        //订单存在
        if (seckillOrder!=null){
            //付款金额
            long fen = (long) (seckillOrder.getMoney().doubleValue() * 100);
            //返回二维码(订单编号和金额)
            return weiXinPayService.createNative(seckillOrder.getId()+"",fen+"");
        }else {
            return new HashMap();
        }
    }

    /**
     *根据支付状态保存订单到数据库
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus.do")
    public Result queryPayStatus(String out_trade_no){

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //轮询订单的支付状态
        HashMap map = weiXinPayService.queryPayStatusWhile(out_trade_no);
        if (map==null){   //如果没有订单
            return new Result(false,"支付失败");
        }else{
            if ("SUCCESS".equals(map.get("trade_state"))){  //交易状态
                //保存数据库,并删除缓存
                seckillOrderService.saveOrderFromRedisToDB(userId,Long.valueOf(out_trade_no),(String) map.get("transaction_id"));
                return new Result(true,"支付成功");
            }else {
                //超时未处理订单,从缓存中删除,更新数据库
                seckillOrderService.deleteOrderFromRedis(userId,Long.valueOf(out_trade_no));
                return new Result(false,"二维码超时");
            }
        }
    }
}
