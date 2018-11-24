package com.jlb.core.service.pay;

import java.util.HashMap;

public interface WeiXinPayService {
    /**
     * 生成微信支付二维码
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    public HashMap createNative(String out_trade_no, String total_fee);

    /**
     * 查询支付状态
     * @param out_trade_no
     * @return
     */
    public HashMap queryPayStatus(String out_trade_no);

    /**
     * 循环查询订单支付状态
     * @param out_trade_no
     * @return
     */
    public HashMap queryPayStatusWhile(String out_trade_no);
}
