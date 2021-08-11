package dyq.rpc.handler;

import dyq.rpc.module.RpcRequest;
import dyq.rpc.module.RpcResponse;
import dyq.rpc.module.ServiceCache;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerHandler.class);
    private static Executor executor = Executors.newCachedThreadPool();
    private ServiceCache instances;

    public RpcServerHandler(ServiceCache instances) {
        this.instances = instances;
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        CompletableFuture.runAsync(() -> {
            try {
                LOGGER.info("begin to process req:{}", rpcRequest);
                System.out.println(rpcRequest);
                String name = rpcRequest.getIdentifier();
                Object o = instances.get(name);
                Class<?>[] classes = new Class<?>[rpcRequest.getParamTypes().size()];
                Method me = o.getClass()
                    .getDeclaredMethod(rpcRequest.getMethod(), rpcRequest.getParamTypes().toArray(classes));
                Object ret = me.invoke(o, rpcRequest.getParams().toArray());
                RpcResponse response = new RpcResponse();
                response.setUuid(rpcRequest.getUuid());
                response.setResult(ret);
                LOGGER.info("process req end:ret:{}", response);
                channelHandlerContext.writeAndFlush(response);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

        },executor);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
