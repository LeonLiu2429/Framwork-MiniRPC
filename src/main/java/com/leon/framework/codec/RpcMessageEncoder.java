package com.leon.framework.codec;

import com.leon.framework.exception.RpcMessageException;
import com.leon.framework.protocol.RpcMessage;
import com.leon.framework.protocol.RpcProtocol;
import com.leon.framework.serialize.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
        //获取消息体
        byte[] body = msg.getBody();

        //验证消息体长度
        if (body.length > RpcProtocol.MAX_BODY_LENGTH) {
            throw new RpcMessageException("消息体长度超过限制：" + body.length);
        }

        //写入数据
        out.writeInt(RpcProtocol.MAGIC);
        out.writeByte(RpcProtocol.VERSION);
        out.writeByte(msg.getMessageType());
        out.writeByte(msg.getSerializerType());
        out.writeLong(msg.getRequestId());
        out.writeInt(body.length);
        out.writeBytes(body);

    }
}
