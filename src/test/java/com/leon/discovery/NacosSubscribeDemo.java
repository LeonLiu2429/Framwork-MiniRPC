package com.leon.discovery;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

public class NacosSubscribeDemo {

    private static final String SERVICE_NAME =
            "com.leon.api.UserService";

    public static void main(String[] args)
            throws Exception {

        NamingService namingService =
                NacosFactory.createNamingService(
                        "127.0.0.1:8848"
                );

        EventListener listener = event -> {

            if (event instanceof NamingEvent namingEvent) {

                List<Instance> instances =
                        namingEvent.getInstances();

                System.out.println("实例列表发生变化：");

                for (Instance instance : instances) {
                    if (instance.isHealthy() && instance.isEnabled()){
                        System.out.println(
                                instance.getIp() + ":"
                                        + instance.getPort()
                                        + " healthy="
                                        + instance.isHealthy()
                        );
                    }
                }
            }
        };

        try {
            namingService.subscribe(
                    SERVICE_NAME, listener
            );

            System.out.println("监听已启动，按回车退出");
            System.in.read();

        } finally {
            try {
                namingService.unsubscribe(
                        SERVICE_NAME, listener
                );
            } finally {
                namingService.shutDown();
            }
        }
    }
}
