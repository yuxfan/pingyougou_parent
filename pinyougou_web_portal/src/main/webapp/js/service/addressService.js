//地址服务层
app.service('addressService',function($http){

    //根据当前用户查询地址列表
    this.findListByLoginUser=function () {
        return $http.get("address/findListByLoginUser.do");
    }

});