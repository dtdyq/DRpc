package dyq.rpc.codec;

import com.alibaba.fastjson.JSON;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class RpcEncoder extends MessageToByteEncoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcEncoder.class);
    private Class<?> target;

    public RpcEncoder(Class<?> clz) {
        this.target = clz;
    }

    protected void encode(ChannelHandlerContext channelHandlerContext, Object obj, ByteBuf byteBuf) {
        LOGGER.info("new encode req input");
        byte[] toWrite = JSON.toJSONString(obj).getBytes(StandardCharsets.UTF_8);
        byteBuf.writeInt(toWrite.length);
        byteBuf.writeBytes(toWrite);
        LOGGER.info("encode end");
    }
}
