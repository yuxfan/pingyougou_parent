package com.jlb.core.service.seckill;

import com.alibaba.dubbo.config.annotation.Service;
import com.jlb.core.dao.seckill.SeckillGoodsDao;
import com.jlb.core.dao.seckill.SeckillOrderDao;
import com.jlb.core.pojo.seckill.SeckillGoods;
import com.jlb.core.pojo.seckill.SeckillOrder;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import util.IdWorker;

import javax.annotation.Resource;
import java.util.Date;


@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Resource
    private SeckillGoodsDao seckillGoodsDao;

    @Resource
    private SeckillOrderDao seckillOrderDao;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private IdWorker idWorker;

    /**
     * 提交订单
     * 1,从缓存中查询出商品,并做校验,如果为空,返回提示信息,
     * 2,如果不为空,扣减库存(-1,+1),重新存入缓存
     * 3,校验:如果商品数量等于0,更新数据库,删除缓存中的数据
     * 4,保存订单到缓存
     * @param seckillId
     * @param userId
     */
    @Override
    @Transactional
    public void submitOrder(Long seckillId, String userId) {
        //从Redis中查询商品
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
        if (seckillGoods == null || seckillGoods.getNum()<=0){
            throw new RuntimeException("商品已抢完,请等待下一轮");
        }

        //扣减库存
        seckillGoods.setNum(seckillGoods.getNum()-1);
        seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
        //重新放入缓存中
        redisTemplate.boundHashOps("seckillGoods").put(seckillId,seckillGoods);

        //如果商品被抢完了
        if (seckillGoods.getNum()==0){
            seckillGoodsDao.updateByPrimaryKey(seckillGoods);
            //清除Redis中的数据
            redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
        }

        //保存订单(Redis)
        long orderId = idWorker.nextId();
        SeckillOrder seckillOrder =new SeckillOrder();
        seckillOrder.setId(orderId);   //订单id
        seckillOrder.setSeckillId(seckillId);  //商品id
        seckillOrder.setMoney(seckillGoods.getCostPrice());   //金额
        seckillOrder.setCreateTime(new Date());   //创建时间
        seckillOrder.setUserId(userId);   //用户id
        seckillOrder.setSellerId(seckillGoods.getSellerId());  //商家id
        seckillOrder.setPayTime(new Date());  //支付日期
        seckillOrder.setStatus("0");   //支付状态
        //存入缓存中(键 :  用户id)
        redisTemplate.boundHashOps("seckillOrder").put(userId,seckillOrder);
    }

    /**
     * 根据用户查询订单
     * @param userId
     * @return
     */
    @Override
    public SeckillOrder searchOrderFromRdis(String userId) {
        return (SeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    /**
     * 支付成功把订单保存到数据库,并清除redis中的缓存
     * @param userId
     * @param orderId
     * @param transactionId  : 交易流水号
     */
    @Override
    public void saveOrderFromRedisToDB(String userId, Long orderId, String transactionId) {
        System.out.println("saveOrderFromRedisToDB"+userId);
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        if (seckillOrder==null){
            throw new RuntimeException("订单不存在");
        }
        //如果与传递过来的订单号不相等
        if (seckillOrder.getId().longValue()!=orderId.longValue()){
            throw new RuntimeException("订单不相符");
        }
        seckillOrder.setTransactionId(transactionId);
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setStatus("1");
        //保存到数据库
        seckillOrderDao.insert(seckillOrder);
        //从缓存中删除
        redisTemplate.boundHashOps("seckillOrder").delete(userId);
    }

    /**
     * 超时订单处理(超时未付款,释放订单,增加库存)
     * @param userId
     * @param orderId
     */
    @Override
    public void deleteOrderFromRedis(String userId, Long orderId) {
        //从redis中获取订单
        SeckillOrder seckillOrder = (SeckillOrder)redisTemplate.boundHashOps("seckillOrder").get(userId);
        if (seckillOrder!=null && seckillOrder.getId().longValue()==orderId.longValue()){
            //恢复库存
            //从缓存中取出秒杀商品
            SeckillGoods seckillGoods = (SeckillGoods)redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
            //不为空
            if (seckillGoods!=null){
                seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
                seckillGoods.setNum(seckillGoods.getNum()+1);
                //恢复到缓存中
                redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(),seckillGoods);
            }else {
                //如果被秒光,不再存到缓存中,直接在数据库中更新
                seckillGoodsDao.selectByPrimaryKey(seckillOrder.getSeckillId());
                if (seckillGoods!=null) {
                    seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
                    seckillGoods.setNum(seckillGoods.getNum() + 1);
                    seckillGoodsDao.updateByPrimaryKey(seckillGoods);
                }
            }
            //从缓存中直接删除
            redisTemplate.boundHashOps("seckillOrder").delete(userId);
        }

    }
}
