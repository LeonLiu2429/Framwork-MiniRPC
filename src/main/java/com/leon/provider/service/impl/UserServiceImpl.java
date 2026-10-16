package com.leon.provider.service.impl;

import com.leon.framework.annotation.RpcService;
import com.leon.api.UserService;

@RpcService(UserService.class)
public class UserServiceImpl implements UserService {

    @Override
    public String getUserById(Long id) {
        return "根据ID查询用户：" + id;
    }

    @Override
    public String getUserById(String username) {
        return "根据用户名查询：" + username;
    }

    @Override
    public String getUserName() {
        return "Leon";
    }

    @Override
    public void printWelcome() {
        System.out.println("欢迎使用 Mini RPC");
    }

    public String unknownMethod(){
        System.out.println("fail2");
        return "";
    }
}