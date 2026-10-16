package com.leon.framework.protocol;

import java.io.Serializable;

public class RpcResponse implements Serializable {

    private static final long serialVersionUID = 2L;

    private final boolean success;
    private final Object result;
    private final String errorMessage;

    private RpcResponse(
            boolean success,
            Object result,
            String errorMessage) {

        this.success = success;
        this.result = result;
        this.errorMessage = errorMessage;
    }

    public static RpcResponse success(Object result) {
        return new RpcResponse(true, result, null);
    }

    public static RpcResponse failure(String errorMessage) {
        return new RpcResponse(false, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getResult() {
        return result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
