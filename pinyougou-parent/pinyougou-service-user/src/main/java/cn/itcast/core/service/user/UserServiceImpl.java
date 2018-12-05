package cn.itcast.core.service.user;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.md5.MD5Util;
import cn.itcast.core.pojo.user.User;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.jms.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private JmsTemplate jmsTemplate;
    @Resource
    private Destination smsDestination;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private UserDao userDao;

    /**
     * 用户手机验证码
     *
     * @param phone
     */
    @Override
    public void sendCode(final String phone) {
        //生成六位数的验证码
        final String code = RandomStringUtils.randomNumeric(6);
        System.out.println(code);

        //将生成的验证码放到redis缓存中,用于用户注册时校验验证码
        redisTemplate.boundValueOps(phone).set(code);

        //设置过期时间
        redisTemplate.boundValueOps(phone).expire(5, TimeUnit.MINUTES);
        System.out.println(code);

        //将数据发送到mq
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("phoneNumbers", phone);
                mapMessage.setString("signName", "阮文");
                mapMessage.setString("templateCode", "SMS_140720901");
                mapMessage.setString("templateParam", "{\"code\":\"" + code + "\"}");

                return mapMessage;
            }
        });
    }

    /**
     * 用户注册
     *
     * @param smscode
     * @param user
     */
    @Transactional
    @Override
    public void add(String smscode, User user) {

        String code = redisTemplate.boundValueOps(user.getPhone()).get();
        if (code != null && code.equals(smscode)) {
            //创建时间和更新时间
            user.setCreated(new Date());
            user.setUpdated(new Date());
            //密码加密
            String md5Encode = MD5Util.MD5Encode(user.getPassword(), "");
            user.setPassword(md5Encode);

            userDao.insertSelective(user);
        } else {
            throw new RuntimeException("验证码有误");
        }

    }
}
