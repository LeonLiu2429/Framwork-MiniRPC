package com.leon.framework.codec;

import com.leon.framework.protocol.RpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class RpcMessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //读取消息
        int magic = in.readInt();
        byte version = in.readByte();
        byte messageType = in.readByte();
        byte serializerType = in.readByte();
        long requestId = in.readLong();
        int bodyLength = in.readInt();
        byte[] body = new byte[bodyLength];
        in.readBytes(body);

        //构建消息对象
        RpcMessage msg = new RpcMessage(
                messageType,
                serializerType,
                requestId,
                body);

        //传递消息对象
        out.add(msg);
    }
}
