package dyq.rpc.client.agent;

import dyq.rpc.client.Callback;
import dyq.rpc.client.route.RouteEngine;
import dyq.rpc.common.CommonUtil;
import dyq.rpc.exception.RpcException;
import dyq.rpc.handler.RpcClientHandler;
import dyq.rpc.module.RpcRequest;
import dyq.rpc.module.RpcResponse;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.*;

public class ProxyAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyAgent.class);

    private static ExecutorService executor = CommonUtil.getNormalExecutor("client-proxy");
    private ConcurrentHashMap<String, Object> cachedProxy = new ConcurrentHashMap<>();
    private RouteEngine engine;

    public ProxyAgent(RouteEngine e) {
        this.engine = e;
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T proxy(Class<T> clz, String tag) {
        String ide = CommonUtil.genServiceIde(clz, tag);
        if (cachedProxy.containsKey(ide)) {
            return (T) cachedProxy.get(ide);
        }
        Object ret = clz.isInterface() ? usingGDK(clz, ide) : usingCGLIB(clz, ide);
        cachedProxy.put(ide, ret);
        return (T) ret;
    }

    private <T> Object usingCGLIB(Class<T> clz, String ide) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clz);
        enhancer
            .setCallback((MethodInterceptor) (o, method, objects, methodProxy) -> generateProxy(ide, method, objects));
        return enhancer.create();
    }

    private Object usingGDK(Class<?> clz, String ide) {
        return Proxy.newProxyInstance(clz.getClassLoader(), new Class[] {clz},
            (proxy1, method, args) -> generateProxy(ide, method, args));
    }

    public <T> void proxy(Class<T> clz, String tag, Callback<T> callback) {
        CompletableFuture.supplyAsync(() -> proxy(clz, tag), executor).whenComplete(callback::process);
    }

    private Object generateProxy(String ide, Method method, Object[] args)
        throws RpcException, InterruptedException, TimeoutException, ExecutionException {
        LOGGER.info("start to invoke remote service for {}#{}", ide, method.getName());
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setIdentifier(ide);
        rpcRequest.setMethod(method.getName());
        rpcRequest.setParamTypes(Arrays.asList(method.getParameterTypes()));
        rpcRequest.setParams(Arrays.asList(args));
        rpcRequest.setUuid(UUID.randomUUID().toString());
        RpcClientHandler client = engine.route(rpcRequest.getIdentifier());
        CompletableFuture<RpcResponse> future = client.sendRequest(rpcRequest);
        LOGGER.info("invoke remote start,waiting for ret");
        return future.get(5, TimeUnit.SECONDS).getResult();
    }
}
