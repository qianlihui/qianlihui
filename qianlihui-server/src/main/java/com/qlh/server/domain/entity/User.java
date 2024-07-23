package com.qlh.server.domain.entity;

import lombok.Data;

/**
 * @Author:liwenbo
 * @Date:2024/7/15 17:18
 * @Description:
 **/
@Data
public class User {

    private String phone;

    private String name;

    private Integer age;

    private String nickname;

    private String password;
}
