package com.leon.framework.client;

import com.leon.framework.codec.RpcMessageDecoder;
import com.leon.framework.codec.RpcMessageEncoder;
import com.leon.framework.exception.RpcSerializationException;
import com.leon.framework.protocol.RpcMessage;
import com.leon.framework.protocol.RpcProtocol;
import com.leon.framework.protocol.RpcRequest;
import com.leon.framework.protocol.RpcResponse;
import com.leon.framework.serialize.Serializer;
import com.leon.framework.serialize.SerializerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.net.ConnectException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class NettyRpcClient implements RpcClient, AutoCloseable {

    private static final AtomicLong REQUEST_ID = new AtomicLong(0);

    private static final String DEFAULT_SERIALIZER_NAME =
            System.getProperty("rpc.serializer", "kryo");
    private final Serializer serializer =
            SerializerFactory.getSerializer(DEFAULT_SERIALIZER_NAME);

    private final String host;
    private final int port;

    private final PendingRequests pendingRequests = new PendingRequests();

    private final EventLoopGroup group = new NioEventLoopGroup();
    private final Bootstrap bootstrap = new Bootstrap();
    private final Object connectLock = new Object();
    private volatile Channel channel;

    public NettyRpcClient(String host, int port){
        this.host = host;
        this.port = port;
    }

    private Channel getOrConnect() throws ConnectException {
        Channel current = channel;

        if (current != null && current.isActive()) {
            return current;
        }

        synchronized (connectLock) {
            current = channel;

            if (current != null && current.isActive()) {
                return current;
            }

            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(
                            ChannelOption.CONNECT_TIMEOUT_MILLIS,
                            3000
                    )
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(
                                            RpcProtocol.MAX_BODY_LENGTH + RpcProtocol.HEADER_LENGTH,
                                            15,
                                            4
                                    )
                            );

                            ch.pipeline().addLast(
                                    "decoder",
                                    new RpcMessageDecoder()
                            );

                            ch.pipeline().addLast(
                                    "encoder",
                                    new RpcMessageEncoder()
                            );

                            ch.pipeline().addLast(
                                    "handler",
                                    new RpcClientHandler(pendingRequests)
                            );
                        }
                    });

            // 在这里使用 bootstrap.connect(host, port) 建立连接，
            try {
                channel = bootstrap.connect(host, port).sync().channel();
            } catch (InterruptedException e) {
                throw new ConnectException("连接provider失败：" + e.getMessage());
            }
            // 并将新 Channel 保存到 channel 字段。
            return channel;
        }
    }

    private RpcResponse decodeResponse(RpcMessage message) {
        if (message.getMessageType() != RpcProtocol.RESPONSE) {
            throw new IllegalStateException("收到的消息不是 RPC 响应");
        }

        if (message.getSerializerType() != serializer.getType()) {
            throw new IllegalStateException("响应序列化类型与请求不一致");
        }

        return serializer.deserialize(message.getBody(), RpcResponse.class);
    }

    @Override
    public CompletableFuture<RpcResponse> sendRequestAsync(RpcRequest request) {
        //获取本次请求的id,并登记
        long requestId = REQUEST_ID.incrementAndGet();
        CompletableFuture<RpcMessage> resFuture = pendingRequests.register(requestId);
        resFuture.orTimeout(5, TimeUnit.SECONDS);
        resFuture.whenComplete((message, error) -> {
            pendingRequests.remove(requestId, resFuture);
        });

        try {

            //获取本次IO的channel
            Channel channel = getOrConnect();

            //序列化请求
            byte[] body = serializer.serialize(request);

            //组装消息
            RpcMessage message = new RpcMessage(
                    RpcProtocol.REQUEST,
                    serializer.getType(),
                    requestId,
                    body
            );

            channel.writeAndFlush(message).addListener(writeFuture -> {
                if (!writeFuture.isSuccess()) {
                    pendingRequests.fail(requestId, writeFuture.cause());
                }
            });

            return resFuture.thenApply(this::decodeResponse);

        } catch (ConnectException | RpcSerializationException e) {
            System.err.println(e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    public void close(){
        if (channel != null){
            channel.close();
        }
        group.shutdownGracefully().syncUninterruptibly();
    }
}
