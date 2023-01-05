package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
@Autowired
    private UserService userService;
@Autowired
private RedisTemplate redisTemplate;
    /**
     * 接受请求发送验证码
     * http://localhost:8080/user/sendMsg
     * {phone:.....}
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
public R<String>  get(@RequestBody User user, HttpSession session){
    //获取邮箱
    String phone = user.getPhone();
    //生成验证码
    String code = ValidateCodeUtils.generateValidateCode(4).toString();
    log.info(code);
    //使用邮箱服务发送验证码
    userService.sendMsg(phone,"点餐验证","您的验证码为"+code+"请妥善保存");
    //将生成的验证码保存到session中
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(phone,code);
        return R.success("验证码发送完成");
}

    /**
     * 用户登录
     * http://localhost:8080/user/login
     * @param
     * @param session
     * @return
     */
      @PostMapping("/login")
        public R<User> login(@RequestBody Map map, HttpSession session){
          //获取邮箱
          String phone = map.get("phone").toString();
          //获取验证码
          String code = map.get("code").toString();
          //判断验证码
          Object sessionCode = redisTemplate.opsForValue().get(phone);
          if (sessionCode!=null&&sessionCode.equals(code)){
              //判断账号是否注册
              LambdaQueryWrapper<User>  queryWrapper=new LambdaQueryWrapper<>();
              queryWrapper.eq(User::getPhone,phone);
              User user = userService.getOne(queryWrapper);
              if (user==null){
                  user=new User();
                  user.setPhone(phone);
                  user.setStatus(1);
                  userService.save(user);
              }
              session.setAttribute("user",user.getId());

              //登录成功，删除redis中缓存的验证码
              redisTemplate.delete(phone);
              return R.success(user);
          }
          return R.error("登录失败");
        }
}
