package com.leon.framework.proxy;

import com.leon.framework.client.RpcClient;
import java.lang.reflect.Proxy;
import java.util.Objects;

public class RpcProxyFactory {

    @SuppressWarnings("unchecked")
    public static <T> T getProxy(
            Class<T> serviceInterface,
            RpcClient client
    ) {

        Objects.requireNonNull(
                serviceInterface,
                "服务接口不能为空"
        );

        Objects.requireNonNull(
                client,
                "RPC客户端不能为空"
        );

        if (!serviceInterface.isInterface()) {
            throw new IllegalArgumentException(
                    "JDK动态代理要求传入接口"
            );
        }

        RpcInvocationHandler handler =
                new RpcInvocationHandler(
                        serviceInterface,
                        client
                );

        Object proxy = Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class<?>[]{serviceInterface},
                handler
        );

        return (T) proxy;
    }
}