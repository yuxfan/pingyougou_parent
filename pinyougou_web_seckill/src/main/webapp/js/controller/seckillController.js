//秒杀controller
app.controller("seckillController",function($scope,$location,$interval,seckillService){
    //读取列表数据绑定到表单中
    $scope.findList=function () {
        seckillService.findList().success(function (response) {
           $scope.list=response;
        });
    }

    //根据id从Redis中查询商品详情
    $scope.findOne=function (id) {
        //$location.search()['id']:  从地址栏中获取参数(返回的是一个数组类型的map对象)
        seckillService.findOne($location.search()['id']).success(function (response) {
           $scope.entity=response;
            //秒杀倒计时
            //allsecond:从现在到结束的总秒数
           allSecond = Math.floor((new Date($scope.entity.endTime).getTime()-new Date().getTime())/1000);
           time=$interval(function () {
                if (allSecond > 0) {
                    allSecond--;
                    //转换时间字符串
                    $scope.timeStr=convertTimeStr(allSecond);

                }else{
                    $interval.cancel(time);
                    alert("秒杀服务已结束");
                }
           },1000);
        });
    }

    //转换秒为 天小时分钟秒格式 XXX 天 10:22:33
    convertTimeStr=function () {
        //天数
        var days=Math.floor(allSecond/(60*60*24));
        //小时
        var hours=Math.floor((allSecond-days*60*60*24)/(60*60));
        //分钟
        var minute=Math.floor((allSecond-days*60*60*24-hours*60*60)/60);
        //秒
        var second=allSecond-days*60*60*24-hours*60*60-minute*60;

        var timeStr="";
        if (days>0){
            timeStr=days+"天";
        }
        return timeStr+hours+":"+minute+":"+second;
    }

    //提交订单(秒杀下单)
    $scope.submitOrder=function(){
        seckillService.submitOrder($scope.entity.id).success(function(response){
                if(response.flag){
                    alert("下单成功，请在 1 分钟内完成支付");
                    location.href="pay.html";
                }else{
                  //  alert("出错了")
                    alert(response.message);
                }
            });
    }
});