package com.leon.framework.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageCodec;

public class nettyClient {

//    public void start(String host, int port){
//
//        EventLoopGroup group = new NioEventLoopGroup(1);
//
//        Bootstrap bootstrap = new Bootstrap();
//        bootstrap.group(group)
//                .channel(NioSocketChannel.class)
//                .handler(new ChannelInitializer<SocketChannel>() {
//                    @Override
//                    protected void initChannel(SocketChannel ch) throws Exception {
//
//                        ChannelPipeline pipeline = ch.pipeline();
//
//                        pipeline.addLast(new LengthFieldBasedFrameDecoder(
//                                1024 * 1024, 15, 4
//                                ));
//
//                        pipeline.addLast(new MessageToMessageCodec<>() {
//                        })
//                    }
//                })
//
//    }
}
