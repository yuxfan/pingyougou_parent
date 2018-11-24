package com.jlb.core.service.order;

import com.jlb.core.pojo.log.PayLog;
import com.jlb.core.pojo.order.Order;

public interface OrderService {

    public void add(Order order);

    public PayLog searchPayLogFromRedis(String userId);

    public void updateOrderStatus(String out_trade_no,String transaction_id);
}
