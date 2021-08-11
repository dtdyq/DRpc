package dyq.rpc.client;

import dyq.rpc.client.agent.ProxyAgent;
import dyq.rpc.client.route.RouteEngine;
import dyq.rpc.common.Config;
import dyq.rpc.zk.CuratorAgent;

public class Client {
    private ProxyAgent agent;

    public Client(String zkAddr) throws Exception {
        CuratorAgent curatorAgent = new CuratorAgent(zkAddr);
        RouteEngine engine = RouteEngine.getInstance(curatorAgent);
        this.agent = new ProxyAgent(engine);
    }

    public <T> T service(final Class<T> clz) {
        return service(clz, Config.DEFAULT_TAG);
    }

    public <T> T service(final Class<T> clz, final String tag) {
        return agent.proxy(clz, tag);
    }

    public <T> void service(Class<T> clz, Callback<T> callback) {
        agent.proxy(clz,Config.DEFAULT_TAG, callback);
    }

    public <T> void service(Class<T> clz,String tag, Callback<T> callback) {
        agent.proxy(clz,tag, callback);
    }
}
