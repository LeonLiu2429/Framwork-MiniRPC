package com.leon.framework.discovery;

import java.util.List;

public class DiscoveryFallback {

    private static final long MAX_STALE_MS = 30_000;

    private final LocalServiceCache cache;

    public DiscoveryFallback(LocalServiceCache cache) {
        this.cache = cache;
    }

    public List<ServiceAddress> getStaleIfAllowed(
            String serviceName
    ) {

        ServiceSnapshot snapshot =
                cache.getSnapshot(serviceName);

        if (snapshot == null) {
            throw new IllegalStateException(
                    "服务没有本地缓存：" + serviceName
            );
        }

        long age = System.currentTimeMillis()
                - snapshot.updatedAt();

        if (age < 0 || age > MAX_STALE_MS) {
            throw new IllegalStateException(
                    "本地缓存已过期：" + serviceName
            );
        }

        if (snapshot.serviceAddresses().isEmpty()) {
            throw new IllegalStateException(
                    "本地缓存没有可用实例：" + serviceName
            );
        }

        return snapshot.serviceAddresses();
    }
}