package com.leon.framework.transport;

import com.leon.framework.protocol.RpcMessage;
import com.leon.framework.protocol.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class clientChannelHandler extends SimpleChannelInboundHandler<RpcMessage>{

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {

    }
}
