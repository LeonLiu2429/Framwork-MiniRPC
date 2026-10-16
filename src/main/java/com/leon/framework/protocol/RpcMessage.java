package com.leon.framework.protocol;

public class RpcMessage {

    private final byte messageType;
    private final byte serializerType;
    private final long requestId;
    private final byte[] body;

    public RpcMessage(
            byte messageType,
            byte serializerType,
            long requestId,
            byte[] body
    ) {
        this.messageType = messageType;
        this.serializerType = serializerType;
        this.requestId = requestId;
        this.body = body.clone();
    }

    public byte getMessageType() {
        return messageType;
    }

    public byte getSerializerType() {
        return serializerType;
    }

    public long getRequestId() {
        return requestId;
    }

    public byte[] getBody() {
        return body.clone();
    }
}