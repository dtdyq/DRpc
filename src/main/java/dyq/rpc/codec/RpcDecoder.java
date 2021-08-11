package dyq.rpc.codec;

import com.alibaba.fastjson.JSON;

import dyq.rpc.common.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RpcDecoder extends ByteToMessageDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcDecoder.class);

    private Class<?> target;
    public RpcDecoder(Class<?> clz) {
        this.target = clz;
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        byteBuf.markReaderIndex();
        int len = byteBuf.readInt();
        if (len > byteBuf.readableBytes()) {
            byteBuf.resetReaderIndex();
            return;
        }
        LOGGER.info("new decode req input");
        String str = CommonUtil.getUTF8(byteBuf);
        Object o = JSON.parseObject(str, target);
        list.add(o);
        LOGGER.info("decode end:{}",o);
    }
}
