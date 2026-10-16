package com.leon.framework.client;

import com.leon.framework.protocol.RpcMessage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class PendingRequests {

    private final ConcurrentMap<Long, CompletableFuture<RpcMessage>> pending =
            new ConcurrentHashMap<>();

    public CompletableFuture<RpcMessage> register(long requestId) {
        CompletableFuture<RpcMessage> future =
                new CompletableFuture<>();

        CompletableFuture<RpcMessage> previous =
                pending.putIfAbsent(requestId, future);

        if (previous != null) {
            throw new IllegalStateException(
                    "重复的 RPC RequestId: " + requestId
            );
        }

        return future;
    }

    public boolean complete(long requestId, RpcMessage response) {
        CompletableFuture<RpcMessage> future =
                pending.remove(requestId);

        return future != null && future.complete(response);
    }

    public boolean fail(long requestId, Throwable cause) {
        CompletableFuture<RpcMessage> future =
                pending.remove(requestId);

        return future != null && future.completeExceptionally(cause);
    }

    public void failAll(Throwable cause){
        for (Long id : pending.keySet()){
            fail(id, cause);
        }
    }

    public boolean remove(
            long requestId,
            CompletableFuture<RpcMessage> expectedFuture
    ) {
        return pending.remove(requestId, expectedFuture);
    }

    public boolean containsId(long requestId){
        return pending.containsKey(requestId);
    }

    public int size() {
        return pending.size();
    }
}