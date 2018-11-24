package com.jlb.core.service.user;

import com.alibaba.dubbo.config.annotation.Service;
import com.jlb.core.dao.user.UserDao;
import com.jlb.core.pojo.user.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.jms.Destination;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserDao userDao;

    @Resource
    private JmsTemplate jmsTemplate;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private Destination smscodeDestination;


    /**
     * 用户注册(添加)
     * @param user
     */
    @Transactional
    @Override
    public void add(User user) {
        user.setCreated(new Date()); //创建日期
        user.setUpdated(new Date());  //修改日期
        //密码加密  (使用bcrypt(data too long 异常)提示数据太长,换MD5就好了)
        /*BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String newpassword = bCryptPasswordEncoder.encode(user.getPassword());*/
        String newpassword = DigestUtils.md5Hex(user.getPassword());
        user.setPassword(newpassword);
        userDao.insert(user);
    }

    /**
     * 发送短信验证码
     * @param phone
     */
    @Override
    public void createSmsCode(String phone) {
        //1.生成一个短信验证码  6位数字   7
        long code= (long)(Math.random()*1000000);
        if(code<100000){
            code=code+100000;
        }
        System.out.println("验证码："+code);
        //2.存入redis
        redisTemplate.boundValueOps("smscode_"+phone ).set(code+"",5, TimeUnit.MINUTES);
        //3.发送到消息队列
        HashMap map=new HashMap();
        map.put("mobile",phone);
        map.put("smscode",code+"");
        jmsTemplate.convertAndSend(smscodeDestination,map);
    }

    /**
     * 验证短信验证码(判断输入的和系统发送的是否一致)
     * @param phone
     * @param smscode
     * @return
     */
    @Override
    public boolean checkSmsCode(String phone, String smscode){
        //取出系统的验证码
        String syscode =(String)redisTemplate.boundValueOps("smscode_" + phone).get();

        if(smscode.equals(syscode) && syscode!=null){
            return true;
        }else{
            return false;
        }
    }
}
