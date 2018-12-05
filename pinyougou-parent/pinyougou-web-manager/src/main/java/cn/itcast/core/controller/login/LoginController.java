package cn.itcast.core.controller.login;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {
    /**
     * 回显用户名
     * @return
     */
    @RequestMapping("/showName.do")
    public Map<String,String> login(){
        Map<String,String> map = new HashMap<>();

        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String name = principal.getUsername();
        map.put("username",name);
        return map;
    }
}
