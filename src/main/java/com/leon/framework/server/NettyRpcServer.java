
package com.leon.framework.server;

import com.leon.framework.codec.RpcMessageDecoder;
import com.leon.framework.codec.RpcMessageEncoder;
import com.leon.framework.protocol.RpcProtocol;
import com.leon.framework.registry.ServiceRegistry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class NettyRpcServer {

    private final int port;
    private Channel serverChannel;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final ServiceRegistry serviceRegistry;

    public NettyRpcServer(
            int port,
            ServiceRegistry serviceRegistry,
            NioEventLoopGroup bossGroup,
            NioEventLoopGroup workerGroup) {
        this.port = port;
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.serviceRegistry = serviceRegistry;
    }

    public Channel start() throws InterruptedException {

        try {
            ServerBootstrap bootstrap =
                    new ServerBootstrap();

            bootstrap.group(bossGroup, workerGroup)

                    // 使用基于 Java NIO 的服务端 Channel
                    .channel(NioServerSocketChannel.class)

                    // 等待接受的连接队列参数
                    .option(ChannelOption.SO_BACKLOG, 128)

                    // 为子连接启用 TCP keepalive
                    .childOption(
                            ChannelOption.SO_KEEPALIVE, true)

                    // 每个新连接初始化自己的 Pipeline
                    .childHandler(
                            new ChannelInitializer<SocketChannel>() {

                                @Override
                                protected void initChannel(
                                        SocketChannel ch) {

                                    ch.pipeline().addLast(
                                            "marginHandler",
                                            new LengthFieldBasedFrameDecoder(
                                                    RpcProtocol.MAX_BODY_LENGTH + RpcProtocol.HEADER_LENGTH,
                                                    15,
                                                    4
                                            ));

                                    ch.pipeline().addLast(
                                            "decoder",
                                            new RpcMessageDecoder()
                                    );

                                    ch.pipeline().addLast(
                                            "encoder",
                                            new RpcMessageEncoder()
                                    );

                                    ch.pipeline().addLast(
                                            "serverHandler",
                                            new RpcServerHandler(
                                                    serviceRegistry)
                                    );
                                }
                            });

            // 仅监听本机回环地址，供教学实验使用
            serverChannel = bootstrap
                    .bind("127.0.0.1", port)
                    .sync().channel();

            System.out.println(
                    "Netty RPC Server 启动成功，端口: "
                            + port);

            return serverChannel;
        } catch (InterruptedException e) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
            workerGroup.shutdownGracefully().syncUninterruptibly();
            return null;
        }
    }

    public void awaitClose() throws InterruptedException{
        serverChannel.closeFuture().sync();
    }

    public void close() {
        if (serverChannel != null){
            serverChannel.close().syncUninterruptibly();
        }

        if (bossGroup != null){
            bossGroup.shutdownGracefully().syncUninterruptibly();
        }

        if (workerGroup != null){
            workerGroup.shutdownGracefully().syncUninterruptibly();
        }
    }
}
