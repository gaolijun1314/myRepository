package cn.itcast.core.service;

import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.seller.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Set;

public class UserDetailServiceImpl implements UserDetailsService {

    /*springSecrity.xml中已经配置了bean,不需要再加reference注解*/
    private SellerService sellerService;

    /*需要提供set方法才可以在配置文件总注入*/
    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //调用service获取一个用户
        Seller seller = sellerService.findOne(username);

        //必须是通过审核的商家才可以登录
        if (seller != null && "1".equals(seller.getStatus())) {

            //获取当前用户的密码
            String password = seller.getPassword();

            Set<GrantedAuthority> authorities = new HashSet<>();
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_SELLER");
            authorities.add(authority);
            User user = new User(username, password, authorities);
            return user;
        }
        return null;
    }
}
