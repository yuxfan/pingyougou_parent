package com.jlb.core.service.cart;

import com.alibaba.dubbo.config.annotation.Service;
import com.jlb.core.dao.item.ItemDao;
import com.jlb.core.pojo.item.Item;
import com.jlb.core.pojo.order.OrderItem;
import com.jlb.core.vo.Cart;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class CartServiceImpl implements CartService{

    @Resource
    private ItemDao itemDao;

    @Resource
    private RedisTemplate redisTemplate;
    /**
     * 添加商品到购物车
     * @param cartList:购物车列表
     * @param itemId:商品id
     * @param num:商品数量
     * @return
     * Cart:包含sellerId,sellerName,orderItemList(订单列表)
     * CartList:购物车列表
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1,根据sku id查询sku商品信息
        Item item = itemDao.selectByPrimaryKey(itemId);
        if(item==null){
            throw  new RuntimeException("商品不存在");
        }
        //审核状态
        if (!"1".equals(item.getStatus())){
            throw  new RuntimeException("商品状态无效");
        }
        //2,获取商家id
        String sellerId = item.getSellerId();
        //3,根据商家id判断购物车中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);
        //4,如果购物车列表中不存在该商家的购物车
        if (cart==null){
            //4.1,新建购物车对象
                cart=new Cart();
                cart.setSellerId(sellerId);
                cart.setSellerName(item.getSeller());  //商家名称
            //4.2,将新建的购物车对象添加到购物车明细列表
            OrderItem orderItem = createOrderItem(item, num);
            List orderItemList=new ArrayList();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            cartList.add(cart);
        }else{   //5,如果购物车列表中存在该商家的购物车对象
            //判断该购物车明细列表中是否包含该商品
            OrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if (orderItem==null){   //5.1,如果没有,
                //新增商品明细对象到商品明细列表中(订单列表)
                orderItem=createOrderItem(item,num);
                cart.getOrderItemList().add(orderItem);
            }else {   //5.2,如果有,
                //更新商品明细对象的数量和金额
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
                //5.3,判断如果该商品明细对象数量小于1时,将其从商品明细列表中移除
                if (orderItem.getNum()<1){
                    cart.getOrderItemList().remove(orderItem);
                }
                //5.4,判断如果该商品明细列表数量小于等于0时,将其从购物车列表中移除
                if (cart.getOrderItemList().size()<=0){
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    /**
     * 根据商品id查询订单列表(订单明细列表)
     * @param orderItemList
     * @param itemId
     * @return
     */
    private OrderItem searchOrderItemByItemId(List<OrderItem> orderItemList, Long itemId) {
        //遍历订单明细列表
        for (OrderItem orderItem : orderItemList) {
            //根据id判断订单存在,则返回
            if (orderItem.getItemId().longValue() == itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 根据商家id查询购物车(Cart:购物车对象)
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }

    /**
     * 创建订单明细对象
     * @param item
     * @param num
     * @return
     */
    private OrderItem createOrderItem(Item item, Integer num) {
        //判断最小购买数量为1
        if (num<=0){
            num=1;
        }
        OrderItem orderItem=new OrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setTitle(item.getTitle());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));  //订单小计
        return orderItem;
    }

    /**
     * 保存购物车到Redis中 (redis不需要经过mysql)
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向Redis中存入购物车信息:"+username);
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    /**
     * 从Redis中查找购物车(如果购物车为空,新建一个,否则返回)
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从Redis中获取购物车信息:"+username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList==null){   //判空
            cartList=new ArrayList<>();
        }
        return cartList;
    }

    /**
     * 合并购物车实现思路：
     * 循环一个购物车 ，根据购物车中的商品ID和数量 添加到另一个购物车中
     * @param cartList1   : 购物车1
     * @param cartList2   : 购物车2
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        System.out.println("合并购物车");
        for (Cart cart : cartList2) {
            //遍历订单明细列表(一个商家的订单==一个订单明细列表)
            for (OrderItem orderItem : cart.getOrderItemList()) {
                cartList1=addGoodsToCartList(cartList2,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList1;
    }


}
