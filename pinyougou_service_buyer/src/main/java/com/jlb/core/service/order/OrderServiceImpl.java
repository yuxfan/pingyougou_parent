package com.jlb.core.service.order;

import com.alibaba.dubbo.config.annotation.Service;
import com.jlb.core.dao.item.ItemDao;
import com.jlb.core.dao.log.PayLogDao;
import com.jlb.core.dao.order.OrderDao;
import com.jlb.core.dao.order.OrderItemDao;
import com.jlb.core.pojo.item.Item;
import com.jlb.core.pojo.log.PayLog;
import com.jlb.core.pojo.order.Order;
import com.jlb.core.pojo.order.OrderItem;
import com.jlb.core.pojo.order.OrderQuery;
import com.jlb.core.vo.Cart;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import util.IdWorker;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private OrderItemDao orderItemDao;

    @Resource
    private PayLogDao payLogDao;

    @Resource
    private OrderDao orderDao;

    @Resource
    private ItemDao itemDao;

    @Resource
    private IdWorker idWorker;

    /**
     * 将购物车保存到订单表和订单明细表中
     * @param order
     */
    @Override
    public void add(Order order) {
        //1,从Redis中读取购物车数据
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
        if (cartList==null){
            return;
        }
        //查询购物车的商品库存是否充足,执行数量加减
        for (Cart cart : cartList) {
            for (OrderItem orderItem : cart.getOrderItemList()){
                Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                if (item.getNum()>=orderItem.getNum()){  //充足
                    item.setStockCount(item.getStockCount()+orderItem.getNum());  //预扣减+订单数量
                    item.setNum(item.getNum()-orderItem.getNum());  //库存数量-订单数量
                    itemDao.updateByPrimaryKey(item);
                }else {
                    throw new RuntimeException("库存不足");
                }
            }
        }
        //2,保存订单和订单明细
        String outTradeNo = idWorker.nextId()+"";  //支付订单编号

        long total_money=0;  //订单总金额
        //订单
        for (Cart cart : cartList){
            Order tborder=new Order();
            long orderId = idWorker.nextId();//订单号是需要自动生成的
            tborder.setOrderId(orderId);  //订单编号
            tborder.setStatus("1");  //支付状态
            tborder.setPaymentType(order.getPaymentType()); //支付类型
            tborder.setCreateTime(new Date());  //订单创建时间
            tborder.setUserId(order.getUserId());  //用户id
            tborder.setReceiverAreaName(order.getReceiverAreaName());  //收货人地址
            tborder.setReceiverMobile(order.getReceiverMobile());  //收货人手机
            tborder.setReceiver(order.getReceiver());  //收货人
            tborder.setSourceType(order.getSourceType());  //订单来源
            tborder.setSellerId(cart.getSellerId());  //商家id
            tborder.setOutTradeNo(outTradeNo);

            double money=0;  //金额
            //订单明细列表
            for (OrderItem orderItem : cart.getOrderItemList()){
                orderItem.setId(idWorker.nextId());  //订单明细表id
                orderItem.setOrderId(orderId);  //订单id
                orderItemDao.insert(orderItem);

                money += orderItem.getTotalFee().doubleValue();  //订单详细列表总金额
            }
            //订单总金额
            total_money += (long) (money*100);
            //支付金额
            tborder.setPayment(new BigDecimal(money));
            orderDao.insert(tborder);
         }
        //3,判断是否微信支付,并添加到支付日志中
        if ("1".equals(order.getPaymentType())){   //支付方式  微信
            PayLog payLog=new PayLog();   //支付日志=支付订单
            payLog.setOutTradeNo(outTradeNo);   //支付订单编号
            payLog.setCreateTime(new Date());  //日志创建时间
            payLog.setTotalFee(total_money);  //订单金额
            payLog.setUserId(order.getUserId());// 用户id
            payLog.setTradeState("0");  //未支付

            payLogDao.insert(payLog);
            //存入Redis中,用来读取支付状态(速度快,效率高)
            redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);
        }
        //4,清除购物车中的数据记录
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());
    }

    /**
     * 从Redis中查询支付日志
     * @param userId
     * @return
     */
    @Override
    public PayLog searchPayLogFromRedis(String userId) {
        return (PayLog) redisTemplate.boundHashOps("payLog").get(userId);
    }

    /**
     * 更新支付日志状态码
     * @param out_trade_no   支付订单编号
     * @param transaction_id  :交易号码
     */
    @Override
    @Transactional
    public void updateOrderStatus(String out_trade_no, String transaction_id) {
        //1,修改支付日志状态
        PayLog payLog = payLogDao.selectByPrimaryKey(out_trade_no);
        payLog.setPayTime(new Date());  //支付时间
        payLog.setTransactionId(transaction_id);   //微信支付订单号
        payLog.setTradeState("1");  //支付状态:0  未支付   1  已支付
        payLogDao.updateByPrimaryKey(payLog);   //更新

            //2,修改关联的订单状态
        OrderQuery query=new OrderQuery();
        query.createCriteria().andOutTradeNoEqualTo(out_trade_no);
        Order order=new Order();
        order.setStatus("2");  //订单  2 已支付
        order.setPaymentTime(new Date());  //支付时间

        orderDao.updateByExampleSelective(order,query);
        //3,清除缓存中的日志状态
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
    }


}
