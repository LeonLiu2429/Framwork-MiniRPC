package com.leon.framework.serialize;

import com.leon.framework.spi.SpiLoader;

import java.util.HashMap;
import java.util.Map;

public final class SerializerFactory {

    private static final SpiLoader<Serializer> LOADER = SpiLoader.getLoader(Serializer.class);

    private static final Map<Byte, Serializer> BY_TYPE = loadByType();

    private static final Map<String, Serializer> BY_NAME = loadByName();

    private SerializerFactory() {
    }

    private static Map<Byte, Serializer> loadByType() {

        Map<Byte, Serializer> serializers = new HashMap<>();

        for (String serializerName : LOADER.getSupportedNames()) {
            Serializer serializer = LOADER.getExtension(serializerName);

            Serializer previous = serializers.putIfAbsent(
                    serializer.getType(),
                    serializer
            );

            if (previous != null) {
                throw new IllegalStateException(
                        "序列化类型标识重复：" + serializer.getType()
                );
            }
        }

        return Map.copyOf(serializers);
    }

    private static Map<String, Serializer> loadByName() {

        Map<String, Serializer> serializers = new HashMap<>();

        for (String serializerName : LOADER.getSupportedNames()) {
            Serializer serializer = LOADER.getExtension(serializerName);

            Serializer previous = serializers.putIfAbsent(
                    serializerName,
                    serializer
            );

            if (previous != null) {
                throw new IllegalStateException(
                        "序列化类型标识重复：" + serializer.getType()
                );
            }
        }

        return Map.copyOf(serializers);
    }

    public static Serializer getSerializer(byte type) {
        Serializer serializer = BY_TYPE.get(type);

        if (serializer == null) {
            throw new IllegalArgumentException(
                    "未找到序列化器，类型标识: " + type
            );
        }

        return serializer;
    }

    public static Serializer getSerializer(String name) {
        Serializer serializer = BY_NAME.get(name);

        if (serializer == null) {
            throw new IllegalArgumentException(
                    "未找到序列化器，名称: " + name
            );
        }

        return serializer;
    }
}