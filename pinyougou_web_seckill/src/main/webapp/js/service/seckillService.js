//秒杀服务模块
app.service('seckillService',function ($http) {

    //查询秒杀商品列表(读取列表数据绑定到表单中)
    this.findList=function () {
        return $http.get('seckillGoods/findList.do');
    }

    //根据id从Redis中查询商品详情
    this.findOne=function (id) {
        return $http.get('seckillGoods/findOneFromRedis.do?id='+id);
    }

    //秒杀下单
    this.submitOrder=function (seckillId) {
        return $http.get('seckillOrder/submitOrder.do?seckillId='+seckillId);
    }
});