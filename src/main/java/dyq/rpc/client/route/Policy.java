package dyq.rpc.client.route;

import dyq.rpc.handler.RpcClientHandler;

import java.util.List;

public interface Policy {
    RpcClientHandler peek(List<RpcClientHandler> clientHandlers);
}
