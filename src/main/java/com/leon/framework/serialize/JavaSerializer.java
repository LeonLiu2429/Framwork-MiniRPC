
package com.leon.framework.serialize;

import com.leon.framework.protocol.RpcProtocol;
import com.leon.framework.protocol.RpcRequest;
import com.leon.framework.protocol.RpcResponse;

import java.io.*;

public final class JavaSerializer implements Serializer{

    public JavaSerializer() {
    }

    @Override
    public byte getType() {
        return RpcProtocol.JAVA_SERIALIZER;
    }

    public  byte[] serialize(Object object) {
        try (
                ByteArrayOutputStream bos =
                        new ByteArrayOutputStream();
                ObjectOutputStream oos =
                        new ObjectOutputStream(bos)
        ) {
            oos.writeObject(object);
            oos.flush();
            return bos.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException(
                    "RPC 序列化失败", e);
        }
    }

    public <T> T deserialize(
            byte[] bytes, Class<T> expectedType) {

        if (bytes == null
                || bytes.length > 1024 * 1024) {
            throw new IllegalArgumentException(
                    "反序列化数据长度非法");
        }

        try (
                ByteArrayInputStream bis =
                        new ByteArrayInputStream(bytes);
                ObjectInputStream ois =
                        new ObjectInputStream(bis)
        ) {
            ois.setObjectInputFilter(info -> {

                if (info.depth() > 20
                        || info.references() > 1000
                        || info.streamBytes() > 1024 * 1024
                        || info.arrayLength() > 1000) {
                    return ObjectInputFilter.Status.REJECTED;
                }

                Class<?> type = info.serialClass();

                if (type == null) {
                    return ObjectInputFilter.Status.UNDECIDED;
                }

                // 允许数组，但继续检查数组元素类型
                while (type.isArray()) {
                    type = type.getComponentType();
                }

                if (type.isPrimitive()
                        || type == Object.class
                        || type == Class.class
                        || type == String.class
                        || type == Integer.class
                        || type == Long.class
                        || type == Boolean.class
                        || type == Number.class
                        || type == RpcRequest.class
                        || type == RpcResponse.class) {

                    return ObjectInputFilter.Status.ALLOWED;
                }

                return ObjectInputFilter.Status.REJECTED;
            });

            Object result = ois.readObject();

            return expectedType.cast(result);

        } catch (IOException
                 | ClassNotFoundException
                 | ClassCastException e) {

            throw new IllegalStateException(
                    "RPC 反序列化失败", e);
        }
    }
}
