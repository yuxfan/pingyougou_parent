//购物车控制层
app.controller('cartController',function($scope,$location,cartService,addressService,orderService){
    $scope.loginname="";//当前登陆名
//返回购物车列表
	//初始化
	$scope.init=function(){
        $scope.cartList=cartService.getCartList();   //获取本地购物车列表
		var itemId=$location.search()["itemId"];  //search拿到的是所有参数的数组
		var num = $location.search()["num"];
		if (itemId != null && num != null){   //如果有参数
			$scope.addGoodsToCartList(itemId,num);   //添加到购物车
		}else{
            $scope.findCartList();  //查询购物车
		}
	}

	//从Redis查询购物车
	$scope.findCartList=function(){
        $scope.cartList=cartService.getCartList();   //获取本地购物车列表
        cartService.findCartList($scope.cartList).success(function (response) {
            $scope.cartList=response.data;
        	if (response.loginname!=""){  //如果用户登录,删除本地购物车
        		cartService.removeCartList();
			}
            $scope.loginname=response.loginname;   //显示用户名
        });
	}

	//添加商品到购物车
	$scope.addGoodsToCartList=function (itemId, num) {
        $scope.cartList=cartService.getCartList();
        cartService.addGoodsToCartList($scope.cartList,itemId,num).success(function (response) {
			if (response.success) {
                $scope.cartList=response.data;
         //     alert("购物车")
				if (response.loginname!=""){
                    $scope.findCartList();    //查询购物车,目的是合并
                }else{
                    cartService.setCartList(response.data);   //保存购物车到localstorage
                }
                $scope.loginname=response.loginname;  //显示用户名
			}
        });
    }

	//计算总数量和总金额
	$scope.$watch('cartList',function (newValue,oldValue){
		$scope.totalValue=cartService.sum(newValue);   //求合计数
	});


    //查询当前用户的地址列表
    $scope.findAddressList=function () {
        addressService.findListByLoginUser().success(function (response) {
                $scope.addressList=response;
                //默认地址选择
					for (var i = 0; i < $scope.addressList.length; i++) {
						//判断出来地址默认为1的,赋值初始化的
						if ( $scope.addressList[i].isDefault=='1'){
							$scope.address=$scope.addressList[i];
                            break;
                        }
					}
            });
    	}

    	//选择地址
	$scope.selectAddress=function(address){
    	$scope.address=address;
	}
	//判断是否当前选中的地址
	$scope.isSelectedAddress=function(address){
    	if ($scope.address == address) {
    		return true;
		}else{
            return false;
        }
	}

	//支付方式
	$scope.order={paymentType:'1'};
	//选择支付方式
    $scope.selectPayType=function (type) {
		$scope.order.paymentType=type;
    }

    //保存订单
	$scope.submitOrder=function () {
    	$scope.order.receiverAreaName = $scope.address.address;  //收货人地址
	//	alert("dizhi")
		$scope.order.receiverMobile = $scope.address.mobile;    //电话
		$scope.order.receiver = $scope.address.contact;    //联系人
        orderService.submitOrder($scope.order).success(function (response) {
		if (response.flag){
		//	alert("订单保存");
			if ($scope.order.paymentType=="1"){  //微信支付
				location.href="pay.html";
			}else {
				location.href="submitordersuccess.html"
			}
		} 
        });
    }
});
