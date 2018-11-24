package com.jlb.core.service.cart;

import com.jlb.core.pojo.address.Address;
import com.jlb.core.vo.Cart;

import java.util.List;

public interface CartService {
    /**
     * 添加商品到购物车
     * @param cartList:购物车列表
     * @param itemId:商品id
     * @param num:商品数量
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);

    /**
     * 保存购物车到Redis中
     * @param username
     * @param cartList
     */
    public void saveCartListToRedis(String username,List<Cart> cartList);

    /**
     * 从Redis中查找购物车
     * @param username
     * @return
     */
    public List<Cart> findCartListFromRedis(String username);

    /**
     * 合并购物车
     * @param cartList1
     * @param cartList2
     * @return
     */
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);


}
