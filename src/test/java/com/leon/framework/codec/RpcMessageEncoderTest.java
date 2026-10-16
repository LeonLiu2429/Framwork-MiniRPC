
package com.leon.framework.codec;

import com.leon.framework.protocol.RpcMessage;
import com.leon.framework.protocol.RpcProtocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class RpcMessageEncoderTest {

    @Test
    void testEncode() {

        // 1. 创建待编码的 RPC 消息
        byte[] body = "Hello RPC".getBytes(
                StandardCharsets.UTF_8);

        RpcMessage message = new RpcMessage(
                RpcProtocol.REQUEST,
                RpcProtocol.JAVA_SERIALIZER,
                1001L,
                body
        );

        // 2. 创建模拟 Channel
        EmbeddedChannel channel = new EmbeddedChannel(
                new RpcMessageEncoder()
        );

        try {
            // 3. 将 Java 对象写入出站方向
            assertTrue(channel.writeOutbound(message));

            // 4. 获取编码后的 ByteBuf
            ByteBuf result = channel.readOutbound();

            assertNotNull(result);

            try {
                // 5. 检查总长度
                assertEquals(
                        RpcProtocol.HEADER_LENGTH + body.length,
                        result.readableBytes()
                );

                // 6. 依次检查协议字段
                assertEquals(
                        RpcProtocol.MAGIC,
                        result.readInt()
                );

                assertEquals(
                        RpcProtocol.VERSION,
                        result.readByte()
                );

                assertEquals(
                        RpcProtocol.REQUEST,
                        result.readByte()
                );

                assertEquals(
                        RpcProtocol.JAVA_SERIALIZER,
                        result.readByte()
                );

                assertEquals(1001L, result.readLong());

                assertEquals(body.length, result.readInt());

                // 7. 读取消息体
                byte[] decodedBody = new byte[body.length];
                result.readBytes(decodedBody);

                assertArrayEquals(body, decodedBody);
                assertEquals(0, result.readableBytes());

            } finally {
                // 从 Channel 取出后，由测试代码负责释放
                result.release();
            }

        } finally {
            channel.finishAndReleaseAll();
        }
    }
}
