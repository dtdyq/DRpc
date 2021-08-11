package dyq.rpc.handler;

import dyq.rpc.module.RpcRequest;
import dyq.rpc.module.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClientHandler.class);
    private final int uuid;
    private LinkedBlockingQueue<RpcRequest> reqQueue = new LinkedBlockingQueue<>(200);
    private LinkedBlockingQueue<RpcResponse> respQueue = new LinkedBlockingQueue<>(200);
    private Channel channel;
    private ConcurrentHashMap<String, CompletableFuture<RpcResponse>> responses = new ConcurrentHashMap<>();

    public RpcClientHandler(ExecutorService executor, String addr) {
        uuid = addr.hashCode();
        executor.execute(() -> {
            LOGGER.info("start new client listener");
            try {
                while (true) {
                    RpcRequest request = reqQueue.take();
                    LOGGER.info("begin to send req:{} using channel :{}", request.getIdentifier(), getUuid());
                    channel.writeAndFlush(request);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
    }

    public CompletableFuture<RpcResponse> sendRequest(RpcRequest request) throws InterruptedException {
        LOGGER.info("new rpc request input:{}", request);
        CompletableFuture<RpcResponse> future = new CompletableFuture<>();
        reqQueue.put(request);
        responses.put(request.getUuid(), future);
        RpcResponse response = respQueue.take();
        responses.get(response.getUuid()).complete(response);
        return future;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) throws Exception {
        LOGGER.info("receive rpc response:{}", response);
        respQueue.put(response);
    }

    public int getUuid() {
        return uuid;
    }

    @Override
    public int hashCode() {
        return getUuid();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof RpcClientHandler)) {
            return false;
        }
        final RpcClientHandler book = (RpcClientHandler) obj;
        if (this == book) {
            return true;
        } else {
            return getUuid() == book.getUuid();
        }

    }

    public void close() {
        channel.close();
    }
}
