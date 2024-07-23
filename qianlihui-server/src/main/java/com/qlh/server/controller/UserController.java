package com.qlh.server.controller;

import com.qlh.server.domain.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @Author:liwenbo
 * @Date:2024/7/22 11:29
 * @Description:
 **/

@Controller
@RequestMapping("/user")
public class UserController {

    @RequestMapping("/login")
    public String index() {
        System.out.println("login 123");

        return "user/login";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(User user) {
        System.out.println(user.toString());
        return "user/login";
    }

    @RequestMapping(value = "/toRegister", method = RequestMethod.GET)
    public String toRegister(User user) {
        System.out.println(user.toString());
        return "user/register";
    }
}
