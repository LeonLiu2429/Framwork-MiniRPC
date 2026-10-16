package com.leon.framework.discovery;

import java.util.List;

public record ServiceSnapshot(
        List<ServiceAddress> serviceAddresses,
        long updatedAt
) {

    public ServiceSnapshot {
        serviceAddresses = List.copyOf(serviceAddresses);
    }
}