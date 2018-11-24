//购物车服务层
app.service('cartService',function($http){
//购物车列表
    //保存购物车列表
    this.setCartList=function (cartList) {
        //转成json字符串
        localStorage.setItem("cartList",JSON.stringify(cartList));
    }

    //获取列表(返回一个购物车列表)
	this.getCartList=function () {
		var cartListStr = localStorage.getItem("cartList");
		if (cartListStr==null){
			return [];
		} else{
			//转成json对象
			return JSON.parse(cartListStr);
		}
    }

    //添加商品到购物车
	this.addGoodsToCartList=function (cartList, itemId, num) {
	return $http.post('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num,cartList);
    }

    //购物车列表
    this.findCartList=function (cartList) {
        return $http.post('cart/findCartList.do',cartList);
    }

    //计算总数量和总金额
	this.sum=function (cartList) {
        var totalValue={totalNum:0,totalMoney:0};
        for (var i=0;i<cartList.length;i++) {
            var cart = cartList[i];
            for (var j=0;j<cart.orderItemList.length;j++) {
                var orderItem =cart.orderItemList[j];
             //   alert("orderItem.num:"+ orderItem.num );
                totalValue.totalNum += orderItem.num;     //数量累加
                totalValue.totalMoney += orderItem.totalFee;  //金额累加
            }
        }
        return totalValue;
    }

    //合并后清除本地购物车
    this.removeCartList=function () {
        localStorage.removeItem("cartList")
    }
});