//订单服务层
app.service('orderService',function($http){

    //保存订单
    this.submitOrder=function (order) {
        return $http.post("/order/add.do",order);
    }

});