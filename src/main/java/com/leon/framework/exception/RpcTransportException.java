package com.leon.framework.exception;

public class RpcTransportException extends RpcException{

    public RpcTransportException(String message){
        super(message);
    }

    public RpcTransportException(String message, Throwable cause) {
        super(message, cause);
    }
}
