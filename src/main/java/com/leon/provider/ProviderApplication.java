package com.leon.provider;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.leon.api.UserService;
import com.leon.framework.registry.ServiceRegistry;
import com.leon.framework.server.NettyRpcServer;
import com.leon.provider.service.impl.UserServiceImpl;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;


public class ProviderApplication {

    public static void main(String[] args)
            throws Exception {

        int port = Integer.parseInt(args[0]);

        // 1. 创建服务注册表
        ServiceRegistry registry =
                new ServiceRegistry();

        // 2. 注册业务服务
        registry.register(
                UserService.class,
                new UserServiceImpl()
        );

        // 3. 创建 Netty RPC 服务端
        NettyRpcServer server =
                new NettyRpcServer(
                        port,
                        registry,
                        new NioEventLoopGroup(1),
                        new NioEventLoopGroup(4)
                );

        Channel serverChannel = server.start();

        // 4. 注册nacos

        boolean registered = false;
        NamingService namingService = NamingFactory.createNamingService("127.0.0.1:8848");

        try {

//            namingService.registerInstance(UserService.class.getName(), "127.0.0.1", 9000);
            namingService.registerInstance(UserService.class.getName(), "127.0.0.1", port);
            registered = true;

            //5.等待channel关闭

            server.awaitClose();

        } catch (NacosException e) {
            System.err.println("nacos服务注册失败：" + e.getErrMsg());
        } finally {
            try {
                if (registered) {
//                    namingService.deregisterInstance(UserService.class.getName(), "127.0.0.1", 9000);
                    namingService.deregisterInstance(UserService.class.getName(), "127.0.0.1", port);

                }
            } finally {
                try {
                    namingService.shutDown();
                } finally {
                    server.close();
                }
            }
        }
    }
}
