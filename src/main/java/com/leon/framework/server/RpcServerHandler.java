
package com.leon.framework.server;

import com.leon.framework.protocol.RpcMessage;
import com.leon.framework.protocol.RpcProtocol;
import com.leon.framework.protocol.RpcRequest;
import com.leon.framework.protocol.RpcResponse;
import com.leon.framework.registry.ServiceRegistry;
import com.leon.framework.serialize.JavaSerializer;

import com.leon.framework.serialize.KryoSerializer;
import com.leon.framework.serialize.Serializer;
import com.leon.framework.serialize.SerializerFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class RpcServerHandler
        extends SimpleChannelInboundHandler<RpcMessage> {

    private final ServiceRegistry serviceRegistry;

    public RpcServerHandler(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    protected void channelRead0(
            ChannelHandlerContext ctx,
            RpcMessage message) {

        // 服务端只处理 RPC 请求
        if (message.getMessageType()
                != RpcProtocol.REQUEST) {

            ctx.close();
            return;
        }

        final Serializer serializer;

        try {
            serializer = SerializerFactory.getSerializer(
                    message.getSerializerType()
            );
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            ctx.close();
            return;
        }

        RpcResponse response;

        try {
            // 1. 反序列化 RPC 请求
            RpcRequest request = serializer.deserialize(
                    message.getBody(),
                    RpcRequest.class
            );

            // 2. 查找服务并执行方法
            Object result = invokeService(request);

            // 3. 创建成功响应
            response = RpcResponse.success(result);

        } catch (Exception e) {

            // 不向客户端暴露内部异常和堆栈信息
            System.err.println(
                    "RPC 调用失败: " + e.getMessage());

            e.printStackTrace();

            response = RpcResponse.failure(
                    "RPC 服务调用失败");
        }

        // 4. 将响应对象序列化
        byte[] responseBody =
                serializer.serialize(response);

        // 5. 封装 RPC 响应消息
        RpcMessage responseMessage = new RpcMessage(
                RpcProtocol.RESPONSE,
                serializer.getType(),
                message.getRequestId(),
                responseBody
        );

        // 6. 写回客户端
        ctx.writeAndFlush(responseMessage);
    }

    private Object invokeService(RpcRequest request)
            throws ReflectiveOperationException {

        // 获取注册的服务对象
        ServiceRegistry.ServiceEntry entry =
                serviceRegistry.getService(
                        request.getServiceName());

        // 今天只演示接收一个 String 参数的方法
        Method method = entry.interfaceClass().getMethod(
                request.getMethodName(),
                request.getParameterTypes()
        );

        Object result ;

        try {
            result = method.invoke(
                    entry.implementation(),
                    request.getParameters()
            );

        } catch (InvocationTargetException e) {
            throw e;
        }

        return result;
    }

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx,
            Throwable cause) {

        System.err.println(
                "RPC 连接异常: " + cause.getMessage());

        ctx.close();
    }
}
