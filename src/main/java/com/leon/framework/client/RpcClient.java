package com.leon.framework.client;

import com.leon.framework.protocol.RpcRequest;
import com.leon.framework.protocol.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface RpcClient {

    CompletableFuture<RpcResponse> sendRequestAsync(RpcRequest request);

    default RpcResponse sendRequest(RpcRequest request) {
        try {
            return sendRequestAsync(request).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("等待 RPC 响应时被中断", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("RPC 异步调用失败", e.getCause());
        } catch (TimeoutException e) {
            throw new IllegalStateException("RPC 调用超时", e);
        }
    }

    void close();
}