package com.jlb.core.controller.login;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("login")
public class LoginController {
    //显示用户名
    @RequestMapping("/showName.do")
    //object转json  是(属性:value值)   所以用map接收
    public Map<String,String> showName(){
        Map<String,String> map=new HashMap<>();
        //获取用户名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("username",name);
        return map;
    }
}
