
package com.leon.provider.service.impl;

import com.leon.api.HelloService;

public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {

        System.out.println(
                "服务端执行 sayHello，参数: " + name);

        return "Hello, " + name;
    }
}
