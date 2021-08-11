package dyq.rpc.client.agent;

import dyq.rpc.codec.RpcDecoder;
import dyq.rpc.codec.RpcEncoder;
import dyq.rpc.common.CommonUtil;
import dyq.rpc.handler.RpcClientHandler;
import dyq.rpc.module.RpcRequest;
import dyq.rpc.module.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

public class ConnectionAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionAgent.class);

    private ConcurrentHashMap<String, List<RpcClientHandler>> service2Handler = new ConcurrentHashMap<>();

    private ExecutorService executorService = CommonUtil.getSuperExecutor("client-agent");

    public synchronized void newConnectionSync(String addr, Set<String> data) throws InterruptedException {
        LOGGER.info("now begin to add new connect:{} for zk:{}", addr, data);
        RpcClientHandler client = service2Handler.values()
            .stream()
            .flatMap((Function<List<RpcClientHandler>, Stream<RpcClientHandler>>) Collection::stream)
            .distinct()
            .filter(rpcClientHandler -> rpcClientHandler.getUuid() == addr.hashCode())
            .findAny()
            .orElse(connect(addr));
        data.forEach(s -> service2Handler.compute(s, (s1, handlers) -> {
            if (handlers == null) {
                List<RpcClientHandler> tmp = new ArrayList<>();
                tmp.add(client);
                return tmp;
            } else {
                if (!handlers.contains(client)) {
                    handlers.add(client);
                }
                return handlers;
            }
        }));
    }

    public synchronized void newConnection(String addr, Set<String> data) {
        CompletableFuture.runAsync(() -> {
            try {
                newConnectionSync(addr, data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, executorService)
            .whenComplete((a, throwable) -> LOGGER.info("add net connection end:{},throwable:{}", a, throwable));
    }

    public List<RpcClientHandler> getConnections(String ide) {
        return service2Handler.get(ide);
    }

    private RpcClientHandler connect(String addr) throws InterruptedException {
        LOGGER.info("begin to connect:{}", addr);
        final RpcClientHandler clientHandler = new RpcClientHandler(executorService, addr);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline pipe = ch.pipeline();
                    pipe.addLast(new IdleStateHandler(0, 0, 5, TimeUnit.SECONDS));
                    pipe.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
                    pipe.addLast(new RpcDecoder(RpcResponse.class));
                    pipe.addLast(new RpcEncoder(RpcRequest.class));
                    pipe.addLast(clientHandler);
                }
            });
        int times = 3;
        while (times > 0) {
            try {
                ChannelFuture future =
                    bootstrap.connect(addr.split(":")[0], Integer.parseInt(addr.split(":")[1])).sync();
                if (future.isSuccess()) {
                    future.channel().closeFuture();
                    break;
                }
            } catch (Throwable e) {
                LOGGER.info("connection failed:", e);
            }
            Thread.sleep(1000 * 5);
            times--;
        }
        return clientHandler;
    }

    public void removeConnection(String addr) {
        CompletableFuture.runAsync(() -> {
            service2Handler.values().forEach(clientHandlers -> clientHandlers.removeIf(rpcClientHandler -> {
                rpcClientHandler.close();
                return rpcClientHandler.getUuid() == addr.hashCode();
            }));
        }, executorService).whenComplete(CommonUtil.completableCompleteConsume("remove connection complete"));
    }
}
