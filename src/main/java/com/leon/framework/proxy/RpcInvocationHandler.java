
package com.leon.framework.proxy;

import com.leon.framework.client.RpcClient;
import com.leon.framework.protocol.RpcRequest;
import com.leon.framework.protocol.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class RpcInvocationHandler
        implements InvocationHandler {

    private final Class<?> serviceInterface;
    private final RpcClient rpcClient;

    public RpcInvocationHandler(
            Class<?> serviceInterface,
            RpcClient rpcClient) {

        this.serviceInterface = serviceInterface;
        this.rpcClient = rpcClient;
    }

    @Override
    public Object invoke(
            Object proxy,
            Method method,
            Object[] args) throws Throwable {

        // 1. 特殊处理 Object 类的方法
        if (method.getDeclaringClass() == Object.class) {

            return switch (method.getName()) {

                case "toString" ->
                        "RpcProxy(" +
                                serviceInterface.getName() + ")";

                case "hashCode" ->
                        System.identityHashCode(proxy);

                case "equals" ->
                        proxy == args[0];

                default -> throw new UnsupportedOperationException(
                        "不支持的 Object 方法");
            };
        }

        // 2. 构造 RPC 请求
        RpcRequest request = new RpcRequest(
                serviceInterface.getName(),
                method.getName(),
                method.getParameterTypes(),
                args == null ? new Object[0] : args
        );

        // 3. 通过 Netty 发送请求
        CompletableFuture<RpcResponse> future =
                rpcClient.sendRequestAsync(request);

        // 4. 检查远程调用结果
        future.thenAccept(res -> {
            if (res.isSuccess()) {
                System.out.println("结果：" + res.getResult());
            } else {
                System.out.println("调用失败：" + res.getErrorMessage());
            }
        }).exceptionally(error -> {
            System.err.println("框架异常：" + error.getMessage());
            return null;
        });
        return future.get().getResult();
    }
}
