package com.leon.framework.discovery;

import com.alibaba.nacos.api.exception.NacosException;

import java.util.List;

public interface ServiceDiscovery extends AutoCloseable {
    List<ServiceAddress> discover(String serviceName) throws NacosException;

    void subscribeService(String serviceName);
}
