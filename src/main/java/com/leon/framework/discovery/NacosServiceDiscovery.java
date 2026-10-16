package com.leon.framework.discovery;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class NacosServiceDiscovery implements ServiceDiscovery {

    private final NamingService namingService;
    private final LocalServiceCache cache = new LocalServiceCache();
    private final ConcurrentHashMap<String, EventListener> listeners = new ConcurrentHashMap<>();

    public NacosServiceDiscovery(String address) {
        try {
            this.namingService = NamingFactory.createNamingService(address);
        } catch (NacosException e) {
            throw new IllegalStateException("连接nacos服务器失败：" + e.getMessage());
        }
    }

    public List<ServiceAddress> toAddresses(List<Instance> instances) {
        return instances.stream()
                .filter(Instance::isHealthy)
                .filter(Instance::isEnabled)
                .filter(instance -> instance.getWeight() > 0)
                .map(
                        instance -> new ServiceAddress(
                                instance.getIp(),
                                instance.getPort()
                        )
                ).toList();
    }

    public List<ServiceAddress> discover(
            String serviceName
    ) {
        ServiceSnapshot snapshot =
                cache.getSnapshot(serviceName);

        if (snapshot == null) {
            throw new IllegalStateException(
                    "服务缓存尚未初始化：" + serviceName
            );
        }

        if (snapshot.serviceAddresses().isEmpty()) {
            throw new IllegalStateException(
                    "没有可用服务实例：" + serviceName
            );
        }

        return snapshot.serviceAddresses();
    }

    public void initialize(String serviceName)
            throws NacosException {

        if (listeners.containsKey(serviceName)) {
            return;
        }

        EventListener listener =
                createListener(serviceName);

        namingService.subscribe(
                serviceName,
                listener
        );

        try {
            List<Instance> instances =
                    namingService.selectInstances(
                            serviceName,
                            true
                    );

            cache.putIfAbsent(
                    serviceName,
                    toAddresses(instances)
            );

            listeners.put(serviceName, listener);

        } catch (NacosException e) {

            namingService.unsubscribe(
                    serviceName,
                    listener
            );

            throw e;
        }
    }

    public void subscribeService(String serviceName) {
        try {
            namingService.subscribe(serviceName, new EventListener() {
                @Override
                public void onEvent(Event event) {
                    if (event instanceof NamingEvent namingEvent) {
                        List<ServiceAddress> serviceAddresses = toAddresses(namingEvent.getInstances());
                        cache.put(serviceName, serviceAddresses);
                        System.out.println(
                                "缓存更新：" + serviceName
                                        + " -> " + serviceAddresses
                        );
                    }
                }
            });
        } catch (NacosException e) {
            throw new IllegalStateException(
                    "订阅Nacos事件失败：" + e.getMessage()
            );
        }
    }

    public EventListener createListener(String serviceName){
        return new EventListener() {
            @Override
            public void onEvent(Event event) {
                if (event instanceof NamingEvent namingEvent){
                    List<ServiceAddress> addresses = toAddresses(namingEvent.getInstances());
                    cache.put(serviceName, addresses);
                    System.out.println(
                            "服务更新" +
                                    namingEvent.getServiceName() +
                            "实例列表" +
                                    addresses);
                }
            }
        };
    }

    public void close() throws NacosException {

        for (var entry : listeners.entrySet()) {

            namingService.unsubscribe(
                    entry.getKey(),
                    entry.getValue()
            );
        }

        listeners.clear();

        namingService.shutDown();
    }

    public NamingService getNamingService() {
        return namingService;
    }
}
