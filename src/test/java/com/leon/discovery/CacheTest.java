package com.leon.discovery;

import com.leon.framework.discovery.LocalServiceCache;
import com.leon.framework.discovery.ServiceAddress;
import com.leon.framework.discovery.ServiceSnapshot;

import java.util.List;

public class CacheTest {

    public static void main(String[] args)
            throws InterruptedException {

        LocalServiceCache cache =
                new LocalServiceCache();

        String serviceName = "UserService";

        cache.put(
                serviceName,
                List.of(
                        new ServiceAddress(
                                "127.0.0.1", 9000
                        )
                )
        );

        Thread reader = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                ServiceSnapshot snapshot =
                        cache.getSnapshot(serviceName);

                System.out.println(
                        "读取：" + snapshot.serviceAddresses()
                );

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });

        reader.start();

        Thread.sleep(350);

        cache.put(
                serviceName,
                List.of(
                        new ServiceAddress(
                                "127.0.0.1", 9001
                        )
                )
        );

        reader.join();
    }
}