package com.leon.framework.protocol;

public final class RpcProtocol {

    private RpcProtocol() {
    }

    // "LRPC" 的 ASCII 字节
    public static final int MAGIC = 0x4C525043;

    public static final byte VERSION = 1;

    // 消息类型
    public static final byte REQUEST = 1;
    public static final byte RESPONSE = 2;

    // 序列化类型
    public static final byte JAVA_SERIALIZER = 1;
    public static final byte KRYO_SERIALIZER = 2;

    // 固定头长度：4 + 1 + 1 + 1 + 8 + 4
    public static final int HEADER_LENGTH = 19;

    // 消息体最大 1 MB
    public static final int MAX_BODY_LENGTH =
            1024 * 1024;
}
