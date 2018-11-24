package com.jlb.core.service;

import com.jlb.core.pojo.seller.Seller;
import com.jlb.core.service.seller.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Set;


/*自定义用户登录认证类
*/
public class UserDetailServiceImpl implements UserDetailsService {
    //手动注入
    private SellerService sellerService;
    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }
    //判断用户是否存在
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //根据username获取商家信息
        Seller seller = sellerService.findOne(username);
        //只有审核通过的商家才可以登录
        if (seller != null && "1".equals(seller.getStatus())){
            Set<GrantedAuthority> authorities = new HashSet<>();
            //添加访问权限   GrantedAuthority:授予权限
            SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_SELLER");
            authorities.add(simpleGrantedAuthority);
            //user是UserDetails接口的实现类,实现了封装
          User user=new User(username,seller.getPassword(),authorities);
          return user;
        }
        return null;
    }
}
