package com.leon.framework.serialize;

import com.leon.framework.protocol.RpcRequest;

public interface Serializer {

    byte getType();

    byte[] serialize(Object object);

    <T>T deserialize(byte[] bytes, Class<T> expectedType);
}
