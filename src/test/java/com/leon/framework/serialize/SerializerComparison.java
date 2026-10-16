package com.leon.framework.serialize;

import com.leon.framework.protocol.RpcRequest;
import java.util.Arrays;

/** Educational micro-experiment, not a JMH benchmark. */
public final class SerializerComparison {
    private static final int WARMUP_ITERATIONS = 2_000;
    private static final int MEASURE_ITERATIONS = 10_000;
    private static final int ROUNDS = 5;
    private static volatile Object blackhole;

    private SerializerComparison() { }

    public static void main(String[] args) {
        RpcRequest request = new RpcRequest(
                "com.leon.api.UserService",
                "getUserById",
                new Class<?>[]{Long.class},
                new Object[]{1001L}
        );
        compare("JDK", SerializerFactory.getSerializer("java"), request);
        compare("Kryo", SerializerFactory.getSerializer("kryo"), request);
    }

    private static void compare(String name, Serializer serializer, RpcRequest request) {
        byte[] bytes = serializer.serialize(request);
        long serializeNanos = medianSerializeNanos(serializer, request);
        long deserializeNanos = medianDeserializeNanos(serializer, bytes);
        System.out.printf(
                "%s: body=%d bytes, serialize median=%d ns/op, deserialize median=%d ns/op%n",
                name, bytes.length, serializeNanos, deserializeNanos
        );
    }

    private static long medianSerializeNanos(Serializer serializer, RpcRequest request) {
        long[] samples = new long[ROUNDS];
        for (int round = 0; round < ROUNDS; round++) {
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                blackhole = serializer.serialize(request);
            }
            byte[] last = null;
            long start = System.nanoTime();
            for (int i = 0; i < MEASURE_ITERATIONS; i++) {
                last = serializer.serialize(request);
            }
            samples[round] = (System.nanoTime() - start) / MEASURE_ITERATIONS;
            blackhole = last;
        }
        Arrays.sort(samples);
        return samples[ROUNDS / 2];
    }

    private static long medianDeserializeNanos(Serializer serializer, byte[] bytes) {
        long[] samples = new long[ROUNDS];
        for (int round = 0; round < ROUNDS; round++) {
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                blackhole = serializer.deserialize(bytes, RpcRequest.class);
            }
            RpcRequest last = null;
            long start = System.nanoTime();
            for (int i = 0; i < MEASURE_ITERATIONS; i++) {
                last = serializer.deserialize(bytes, RpcRequest.class);
            }
            samples[round] = (System.nanoTime() - start) / MEASURE_ITERATIONS;
            blackhole = last;
        }
        Arrays.sort(samples);
        return samples[ROUNDS / 2];
    }
}
