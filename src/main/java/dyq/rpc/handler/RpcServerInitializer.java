package dyq.rpc.handler;

import dyq.rpc.codec.RpcDecoder;
import dyq.rpc.codec.RpcEncoder;
import dyq.rpc.module.RpcRequest;
import dyq.rpc.module.RpcResponse;
import dyq.rpc.module.ServiceCache;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class RpcServerInitializer extends ChannelInitializer<SocketChannel> {
    private ServiceCache serviceCache;
    public RpcServerInitializer(ServiceCache cache) {
        this.serviceCache = cache;
    }
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline channelPipeline = socketChannel.pipeline();
        channelPipeline.addLast(new IdleStateHandler(0, 0, 90, TimeUnit.SECONDS));
        channelPipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        channelPipeline.addLast(new RpcDecoder(RpcRequest.class));
        channelPipeline.addLast(new RpcEncoder(RpcResponse.class));
        channelPipeline.addLast(new RpcServerHandler(serviceCache));
    }
}
