package com.jlb.core.controller.cart;

import com.alibaba.dubbo.config.annotation.Reference;

import com.jlb.core.cart.LoginResult;
import com.jlb.core.service.cart.CartService;
import com.jlb.core.vo.Cart;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("cart")
public class CartController {

    @Reference
    private CartService cartService;

    /**
     * 添加商品到购物车
     * @param cartList : 购物车列表
     * @param itemId ;商品id(sku的id)
     * @param num :商品数量
     * @return
     */
    @RequestMapping("/addGoodsToCartList.do")
    public LoginResult addGoodsToCartList(@RequestBody List<Cart> cartList, Long itemId, Integer num){
        //获取用户名,判断是否用用户登录
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("登录名为:"+username);  //未登录:anonymousUser
        if ("anonymousUser".equals(username)){  //如果没登录.
            username="";
        }
        try {
        if (!"".equals(username)){ //如果已登录,存入Redis中
            //合并时先查询Redis中的购物车
            List<Cart> cartListFromRedis = cartService.findCartListFromRedis(username);
            //然后将本地添加到Redis中
            List<Cart> carts = cartService.addGoodsToCartList(cartListFromRedis, itemId, num);
            cartService.saveCartListToRedis(username,carts);
            return new LoginResult(true,username,carts);
            }else {
                List<Cart> carts = cartService.addGoodsToCartList(cartList, itemId, num);
                return new LoginResult(true,username,carts);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new LoginResult(false,username,"添加失败");
        }
    }

    /**
     * 从Redis中获取购物车
     * @param cartList
     * @return
     */
    @RequestMapping("/findCartList.do")
    public LoginResult findCartListFromRedis(@RequestBody List<Cart> cartList){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(username)){    //如果未登录,返回本地的购物车
        return new LoginResult(true,"",cartList);
        }else {     //如果已登录,返回Redis中的购物车
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            //合并购物车
            if (cartList.size()>0){
                cartList_redis= cartService.mergeCartList(cartList,cartList_redis);
                cartService.saveCartListToRedis(username,cartList_redis);  //保存到Redis中
            }
            return new LoginResult(true,username,cartList_redis);
        }
    }

}
