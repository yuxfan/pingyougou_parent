package com.jlb.core.vo;

import com.jlb.core.pojo.order.OrderItem;

import java.io.Serializable;
import java.util.List;

public class Cart implements Serializable {

    private String sellerId;   //商家id

    private String sellerName;  //商家名称

    private List<OrderItem> orderItemList;   //订单列表   一个商家对应一个订单列表

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}
