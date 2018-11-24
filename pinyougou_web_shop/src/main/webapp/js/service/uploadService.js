app.service("uploadService",function($http){
	
	this.uploadFile = function(){
		// 向后台传递数据:
		var formData = new FormData();
		// 向formData中添加数据:
		formData.append("file",file.files[0]);
		
		return $http({
			method:'post',
			url:'../upload/uploadFile.do',
			data:formData,
			//设置undefined,浏览器会自动设置multipart/form-data
			headers:{'Content-Type':undefined} ,// Content-Type : text/html  text/plain
			//将附件进行序列化
			transformRequest: angular.identity
		});
	}
});