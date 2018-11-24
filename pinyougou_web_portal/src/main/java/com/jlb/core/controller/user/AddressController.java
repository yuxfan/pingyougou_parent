package com.jlb.core.controller.user;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jlb.core.pojo.address.Address;
import com.jlb.core.service.user.AddresService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("address")
public class AddressController {

    @Reference
   private AddresService addresService;
    /**
     * 根据用户查询地址列表
     * @param
     * @return
     */
    @RequestMapping("/findListByLoginUser.do")
    public List<Address> findListByLoginUser(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return addresService.findListByLoginName(username);
    }
}
