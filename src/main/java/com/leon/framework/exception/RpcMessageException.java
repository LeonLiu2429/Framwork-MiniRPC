package com.leon.framework.exception;

public class RpcMessageException extends RpcException{
    public RpcMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcMessageException(String message) {
        super(message);
    }
}
