package com.github.xiaolyuh.web.controller;

import com.github.xiaolyuh.web.service.UserService;
import com.github.xiaolyuh.web.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/index")
    public String index() {
        return "index";
    }

    @RequestMapping("/toLogin")
    public String toLogin() {
        return "login";
    }

    @RequestMapping("/user/login-out")
    @ResponseBody
    public Result loginOut(String token) {
        userService.loginOut(token);
        return Result.success();
    }

    @RequestMapping("/user/submit-login")
    @ResponseBody
    public Result login(String loginUsername, String loginPassword) {
        String token = UUID.randomUUID().toString();
        if (userService.login(loginUsername, loginPassword, token)) {
            return Result.success(token);
        }
        return Result.error("用户名或密码错误");
    }
}