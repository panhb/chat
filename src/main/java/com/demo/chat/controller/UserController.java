package com.demo.chat.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.demo.chat.model.po.UserPO;
import com.demo.chat.utils.SessionUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hongbo.pan
 * @date 2023/6/28
 */
@RestController
@RequestMapping("/user/")
public class UserController {

    // 测试登录，浏览器访问： http://localhost:8080/user/doLogin?username=zhang&password=123456
    @RequestMapping("doLogin")
    public String doLogin(String username, String password) {
        // 此处仅作模拟示例，真实项目需要从数据库中查询数据进行比对
        if("zhang".equals(username) && "123456".equals(password)) {
            StpUtil.login(10001);
            UserPO po = new UserPO();
            po.setUsername(username);
            po.setPassword(password);
            SessionUtil.setUser(po);
            return "登录成功";
        }
        return "登录失败";
    }

    // 查询登录状态，浏览器访问： http://localhost:8080/user/isLogin
    @RequestMapping("isLogin")
    public String isLogin() {
        UserPO bo = SessionUtil.getUser();
        return bo.getUsername() + "当前会话是否登录：" + StpUtil.isLogin();
    }

}

