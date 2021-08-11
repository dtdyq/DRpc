package dyq.rpc.zk;

import static org.apache.curator.framework.CuratorFrameworkFactory.newClient;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import dyq.rpc.common.Config;
import dyq.rpc.exception.RpcException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CuratorAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(CuratorAgent.class);
    private CuratorFramework curatorFramework;

    public CuratorAgent(String addr) throws Exception {
        LOGGER.info("begin to connect zk server:{}", addr);
        curatorFramework = newClient(addr, new ExponentialBackoffRetry(1000, 3));
        curatorFramework.start();
        curatorFramework.usingNamespace(Config.ZK_NAMESPACE);
        if (curatorFramework.checkExists().forPath(Config.ZK_ROOT_PATH) == null) {
            curatorFramework.create().forPath(Config.ZK_ROOT_PATH);
        }
        LOGGER.info("connect zk server success");
    }

    public Map<String, Set<String>> getAllService() throws Exception {
        Map<String, Set<String>> ret = new HashMap<>();
        List<String> children = curatorFramework.getChildren().forPath(Config.ZK_ROOT_PATH);
        LOGGER.info("current all zk:{}", children);
        for (String addr : children) {
            byte[] data = curatorFramework.getData().forPath(getPath(addr));
            ret.put(addr,
                JSON.parseObject(new String(data, Charset.forName("UTF-8")), new TypeReference<Set<String>>() {}));
        }
        return ret;
    }

    public void pubService(String addr, Set<String> services) throws Exception {
        LOGGER.info("publish zk for:{} {}", addr, services);
        byte[] data = JSON.toJSONString(services).getBytes(Charset.forName("UTF-8"));
        if (curatorFramework.checkExists().forPath(getPath(addr)) != null) {
            throw new RpcException("path exist error:" + addr);
        }
        curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(getPath(addr), data);
        LOGGER.info("publish zk success");
    }

    private String getPath(String addr) {
        return Config.ZK_ROOT_PATH + "/" + addr;
    }

    public void onServiceChanged(PathChildrenCacheListener listener) throws Exception {
        PathChildrenCache watcher = new PathChildrenCache(curatorFramework, Config.ZK_ROOT_PATH, true);
        watcher.getListenable().addListener(listener);
        watcher.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
    }

    public void clear(String pubAddr) throws Exception {
        LOGGER.info("clear service for ide:{}", pubAddr);
        curatorFramework.delete().forPath(getPath(pubAddr));
        curatorFramework.close();
    }
}
