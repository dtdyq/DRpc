package dyq.rpc.client.route;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import dyq.rpc.client.agent.ConnectionAgent;
import dyq.rpc.exception.RpcException;
import dyq.rpc.handler.RpcClientHandler;
import dyq.rpc.zk.CuratorAgent;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class RouteEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(RouteEngine.class);
    private ConnectionAgent connectionAgent;
    private CuratorAgent curatorAgent;
    private ConcurrentHashMap<String, Policy> policyMap = new ConcurrentHashMap<>();

    private RouteEngine(CuratorAgent curatorAgent) throws Exception {
        this.curatorAgent = curatorAgent;
        this.connectionAgent = new ConnectionAgent();
        init();
        register();
    }

    public static RouteEngine getInstance(CuratorAgent curatorAgent) throws Exception {
        return new RouteEngine(curatorAgent);
    }

    private void init() throws Exception {
        Map<String, Set<String>> datas = curatorAgent.getAllService();
        LOGGER.info("all zk data:{}", datas);
        datas.forEach((k, v) -> {
            attachPolicy(v);
            try {
                connectionAgent.newConnectionSync(k, v);
            } catch (InterruptedException e) {
                LOGGER.info("init exception:", e);
            }
        });
    }

    public synchronized RpcClientHandler route(String identifier) throws RpcException {
        List<RpcClientHandler> conns = connectionAgent.getConnections(identifier);
        LOGGER.info("clients for service:{} is:{}", identifier, conns);
        if (conns != null && !conns.isEmpty()) {
            return policyMap.getOrDefault(identifier, new RoudPolicy()).peek(conns);
        } else {
            throw new RpcException("can not find zk for " + identifier);
        }
    }

    private void register() throws Exception {
        curatorAgent.onServiceChanged((curatorFramework, pathChildrenCacheEvent) -> {
            if (pathChildrenCacheEvent.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED) {
                LOGGER.info("process zk add event:{}", pathChildrenCacheEvent);
                ChildData data = pathChildrenCacheEvent.getData();
                String path = data.getPath();
                String addr = path.substring(11);
                Set<String> services = JSON.parseObject(new String(data.getData(), Charset.forName("UTF-8")),
                    new TypeReference<Set<String>>() {});
                attachPolicy(services);
                connectionAgent.newConnection(addr, services);
            }
            if (pathChildrenCacheEvent.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
                LOGGER.info("process zk remove event:{}", pathChildrenCacheEvent);
                ChildData data = pathChildrenCacheEvent.getData();
                String path = data.getPath();
                String addr = path.substring(11);
                connectionAgent.removeConnection(addr);
            }
        });
    }

    private void attachPolicy(Set<String> services) {
        services.forEach(s -> policyMap.put(s, new RoudPolicy()));
    }

    public static class RoudPolicy implements Policy {
        private AtomicInteger index = new AtomicInteger(0);

        @Override
        public RpcClientHandler peek(List<RpcClientHandler> clientHandlers) {

            if (clientHandlers == null || clientHandlers.size() == 0) {
                return null;
            }
            int size = clientHandlers.size();
            LOGGER.info("dtdyq:{}", index.get());
            return clientHandlers.get((index.getAndAdd(1) + size) % size);
        }
    }

    public class RandPolicy implements Policy {
        private Random random = new Random();

        @Override
        public RpcClientHandler peek(List<RpcClientHandler> clientHandlers) {
            if (clientHandlers == null || clientHandlers.size() == 0) {
                return null;
            }
            int size = clientHandlers.size();
            return clientHandlers.get(random.nextInt(size));
        }
    }
}
