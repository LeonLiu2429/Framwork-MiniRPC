package com.leon.discovery;

import com.leon.framework.discovery.DiscoveryFallback;
import com.leon.framework.discovery.LocalServiceCache;
import com.leon.framework.discovery.ServiceAddress;

import java.util.List;

public class FallbackTest {

    public static void main(String[] args) {

        LocalServiceCache cache =
                new LocalServiceCache();

        cache.put(
                "UserService",
                List.of()
        );

        DiscoveryFallback fallback =
                new DiscoveryFallback(cache);

        List<ServiceAddress> addresses =
                fallback.getStaleIfAllowed(
                        "UserService"
                );

        System.out.println(addresses);
    }
}