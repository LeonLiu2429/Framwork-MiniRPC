package com.leon.framework.discovery;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LocalServiceCache {

    private final ConcurrentHashMap<String, ServiceSnapshot> cache = new ConcurrentHashMap<>();

    public ServiceSnapshot getSnapshot(String serviceName) {
        return cache.get(serviceName);
    }

    public void put(String serviceName, List<ServiceAddress> instances) {
        cache.put(serviceName, new ServiceSnapshot(instances, System.currentTimeMillis()));
    }

    public void putIfAbsent(String serviceName, List<ServiceAddress> instances) {
        cache.putIfAbsent(serviceName, new ServiceSnapshot(instances, System.currentTimeMillis()));
    }

    public boolean containsService(String serviceName) {
        return cache.containsKey(serviceName);
    }
}
