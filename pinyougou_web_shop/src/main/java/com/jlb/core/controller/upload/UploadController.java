package com.jlb.core.controller.upload;

import com.jlb.core.entity.Result;
import com.jlb.core.utils.upload.FastDFSClient;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("upload")
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    //1、商品添加之图片上传
    @RequestMapping("/uploadFile.do")
    public Result uploadFile(MultipartFile file) throws Exception {
        try {
        //添加配置文件
        String conf="classpath:fastDfs/fstf_client.conf";
        //创建客户端
        FastDFSClient fastDFSClient = new FastDFSClient(conf);
        //获取原始的文件名  相当于切割文件名  获取到的是 .jpg
        String fileName = file.getOriginalFilename();
        //获取扩展名
        String extName = FilenameUtils.getExtension(fileName);
        //获得上传文件的路径  转换成二进制字节码上传
        String path = fastDFSClient.uploadFile(file.getBytes(), extName, null);
        //最后访问的路径
       String url= FILE_SERVER_URL + path;
       //保存成功,就跳转到这个路径
        return new Result(true,url);
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"保存失败");
        }
    }
}
