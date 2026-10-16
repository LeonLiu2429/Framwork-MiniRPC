package com.leon.framework.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.leon.framework.exception.RpcSerializationException;
import com.leon.framework.protocol.RpcProtocol;
import org.objenesis.strategy.StdInstantiatorStrategy;

public final class KryoSerializer implements Serializer {

    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL =
            ThreadLocal.withInitial(() -> {
                Kryo kryo = new Kryo();
                kryo.setRegistrationRequired(false);
                kryo.setInstantiatorStrategy(
                        new DefaultInstantiatorStrategy(
                                new StdInstantiatorStrategy()
                        )
                );

                return kryo;
            });

    @Override
    public byte getType() {
        return RpcProtocol.KRYO_SERIALIZER;
    }

    @Override
    public byte[] serialize(Object object) throws RpcSerializationException {
        Kryo kryo = KRYO_THREAD_LOCAL.get();

        try (Output output = new Output(256, RpcProtocol.MAX_BODY_LENGTH)) {
            kryo.writeClassAndObject(output, object);
            return output.toBytes();

        } catch (RuntimeException e) {
            throw new RpcSerializationException("Kryo 序列化失败", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> expectedType) {
        if (bytes == null || bytes.length > RpcProtocol.MAX_BODY_LENGTH) {
            throw new IllegalArgumentException("反序列化数据长度非法");
        }

        Kryo kryo = KRYO_THREAD_LOCAL.get();

        try (Input input = new Input(bytes)) {
            Object result = kryo.readClassAndObject(input);
            return expectedType.cast(result);

        } catch (RuntimeException e) {
            throw new RpcSerializationException("Kryo 反序列化失败", e);
        }
    }
}