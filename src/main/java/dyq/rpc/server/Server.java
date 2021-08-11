package dyq.rpc.server;

import dyq.rpc.common.CommonUtil;
import dyq.rpc.common.Config;
import dyq.rpc.handler.RpcServerInitializer;
import dyq.rpc.module.ServiceCache;
import dyq.rpc.server.annotation.Rpc;
import dyq.rpc.zk.CuratorAgent;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private ServiceCache serviceCache = new ServiceCache();
    private String pubAddr;
    private String zkAddr;
    private CuratorAgent agent;
    private ChannelFuture channelFuture;

    public Server(String pubAddr, String zkAddr) {
        this.pubAddr = pubAddr;
        this.zkAddr = zkAddr;
    }

    public <T> Server addService(Class<T> clz, T instance) {
        addService(clz, instance, Config.DEFAULT_TAG);
        return this;
    }

    public <T> Server addService(Class<T> clz, T instance, String tag) {
        String ide = CommonUtil.genServiceIde(clz, tag);
        serviceCache.put(ide, instance);
        return this;
    }

    public Server addForPackage(String pkgName) {
        AnnotationConfigApplicationContext ret = new AnnotationConfigApplicationContext();
        ret.scan(pkgName);
        ret.refresh();
        Map<String, Object> clzs = ret.getBeansWithAnnotation(Rpc.class);
        LOGGER.info("all annotated clzs:{}", clzs);
        clzs.values().forEach(o -> {
            Rpc rpc = o.getClass().getAnnotation(Rpc.class);
            String ide = CommonUtil.genServiceIde(o.getClass(), rpc.tag());
            LOGGER.info("add service:{}", o.getClass().getCanonicalName());
            serviceCache.put(ide, o);
        });
        return this;
    }

    public void start() {
        CompletableFuture.runAsync(() -> {
            try {
                agent = new CuratorAgent(zkAddr);
            } catch (Exception e) {
                LOGGER.error("connect zk center error:", e);
                System.exit(-1);
            }
        }).thenCombineAsync(CompletableFuture.runAsync(() -> {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new RpcServerInitializer(serviceCache));
            try {
                channelFuture =
                    serverBootstrap.bind(pubAddr.split(":")[0], Integer.parseInt(pubAddr.split(":")[1])).sync();
                channelFuture.addListener(future1 -> {
                    LOGGER.info("establish server end");
                });
            } catch (InterruptedException e) {
                LOGGER.error("server error:", e);
            }
        }), (unused1, unused2) -> {
            if (agent == null) {
                LOGGER.error("obtain curator agent failed");
                System.exit(-1);
            }
            try {
                agent.pubService(pubAddr, serviceCache.keySet());
                channelFuture.channel().closeFuture().sync();
            } catch (Exception e) {
                LOGGER.error("pub service error:", e);
                System.exit(-1);
            }
            return null;
        }).join();
    }

    public void stop() throws Exception {
        if (agent != null) {
            agent.clear(pubAddr);
        }
        if (channelFuture != null) {
            channelFuture.channel().close();
        }
    }
}
