
package com.leon.framework.client;

import com.leon.framework.exception.RpcMessageException;
import com.leon.framework.exception.RpcProtocolException;
import com.leon.framework.exception.RpcTransportException;
import com.leon.framework.protocol.RpcMessage;
import com.leon.framework.protocol.RpcProtocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;

public class RpcClientHandler
        extends SimpleChannelInboundHandler<RpcMessage> {

    private final PendingRequests pendingRequests;

    public RpcClientHandler(PendingRequests pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    @Override
    protected void channelRead0(
            ChannelHandlerContext ctx,
            RpcMessage message) {

        long requestId = message.getRequestId();

        // 1. 检查消息是否为响应
        if (message.getMessageType()
                != RpcProtocol.RESPONSE) {

            pendingRequests.fail(
                    requestId,
                    new RpcMessageException("客户端收到非响应消息")
            );

            return ;
        }

        // 3. 通知等待方：响应已到达
        pendingRequests.complete(requestId, message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        pendingRequests.failAll(
                new RpcTransportException("与provider的连接已关闭")
        );
    }

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx,
            Throwable cause
    ) {
        pendingRequests.failAll(cause);
        ctx.close();
    }

}
