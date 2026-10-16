
package com.leon.framework.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistry {

    private final Map<String, ServiceEntry> services =
            new ConcurrentHashMap<>();

    public void register(
            Class<?> interfaceClass,
            Object implementation) {

        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException(
                    "必须注册接口类型");
        }

        if (!interfaceClass.isInstance(implementation)) {
            throw new IllegalArgumentException(
                    "实现对象未实现该接口");
        }

        String serviceName = interfaceClass.getName();

        services.put(
                serviceName,
                new ServiceEntry(interfaceClass, implementation)
        );

        System.out.println(
                "注册 RPC 服务: " + serviceName);
    }

    public ServiceEntry getService(String serviceName) {
        ServiceEntry entry = services.get(serviceName);

        if (entry == null) {
            throw new IllegalArgumentException(
                    "找不到 RPC 服务: " + serviceName);
        }

        return entry;
    }

    public record ServiceEntry(
            Class<?> interfaceClass,
            Object implementation) {
    }
}
