package cn.itcast.core.service.user;

import cn.itcast.core.pojo.user.User;

public interface UserService {
    /**
     * 用户手机验证码
     * @param phone
     */
    public void sendCode(String phone);

    /**
     * 用户注册
     * @param smscode
     * @param user
     */
    public void add(String smscode,User user);
}
