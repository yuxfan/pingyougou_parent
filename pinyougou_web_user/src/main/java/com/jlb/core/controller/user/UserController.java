package com.jlb.core.controller.user;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jlb.core.entity.Result;
import com.jlb.core.pojo.user.User;
import com.jlb.core.service.user.UserService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.PhoneFormatCheckUtils;


@RestController
@RequestMapping("user")
public class UserController {

    @Reference
    private UserService userService;

    @RequestMapping("/add.do")
    public Result add(@RequestBody User user,String smscode){
        //判断验证码是否正确
        if(!userService.checkSmsCode(user.getPhone(),smscode)){
            return new Result( false,"验证码输入错误！");
        }
        try {
            userService.add(user);
            return new Result( true,"保存成功");
        }  catch (Exception e) {
            e.printStackTrace();
            return new Result( false,"保存失败");
        }
    }

    /**
     * 发送验证码
     * @param phone
     * @return
     */
    @RequestMapping("/sendCode.do")
    public Result sendCode(String phone){
        //判断手机号码格式
        if (!PhoneFormatCheckUtils.isPhoneLegal(phone)){
            return new Result(false,"号码格式不正确");
        }
        try {
            userService.createSmsCode(phone);  //生成验证码
            return new Result(true,"发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"发送失败");
            }
    }

}
