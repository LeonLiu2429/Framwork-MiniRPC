package com.leon.consumer;

import com.leon.api.UserService;
import com.leon.framework.client.NettyRpcClient;
import com.leon.framework.client.RpcClient;
import com.leon.framework.discovery.LocalServiceCache;
import com.leon.framework.discovery.NacosServiceDiscovery;
import com.leon.framework.discovery.ServiceDiscovery;
import com.leon.framework.discovery.ServiceAddress;
import com.leon.framework.exception.RpcServiceNotFoundException;
import com.leon.framework.proxy.RpcProxyFactory;

import java.util.List;

public class ConsumerApplication {

    public static void main(String[] args)
            throws Exception {

        try (ServiceDiscovery discovery = new NacosServiceDiscovery("127.0.0.1:8848")) {

            discovery.subscribeService(UserService.class.getName());

            System.out.println("输入回车结束");
            System.in.read();
        }
}


}
