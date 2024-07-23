package com.qlh.server.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @Author:liwenbo
 * @Date:2024/7/22 11:28
 * @Description:
 **/

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        HttpSession session = request.getSession();
//        Object user = session.getAttribute("user");
//        if (user == null) {
//            // 用户未登录，重定向到登录页面
//            response.sendRedirect("/user/login");
//            return false;
//        }
        return true;
    }
}