package com.jlb.core.service.seckill;

import com.jlb.core.pojo.seckill.SeckillOrder;

public interface SeckillOrderService {

    /**
     * 根据秒杀商品id和用户id提交订单
     *
     * @param seckillId
     * @param userId
     */
    public void submitOrder(Long seckillId, String userId);

    /**
     * 根据用户查询订单
     *
     * @param userId
     * @return
     */
    public SeckillOrder searchOrderFromRdis(String userId);

    /**
     * 支付成功把订单保存到数据库
     * @param userId
     * @param orderId
     * @param transactionId  : 交易流水号
     */
    public void saveOrderFromRedisToDB(String userId, Long orderId, String transactionId);

    /**
     * 超时订单处理
     * @param userId
     * @param orderId
     */
    public void deleteOrderFromRedis(String userId,Long orderId);
}