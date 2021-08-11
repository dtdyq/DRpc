package dyq.rpc.module;

import java.util.StringJoiner;

public class RpcResponse extends RpcProtocol {
    private Object result;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RpcResponse.class.getSimpleName() + "[", "]").add("result=" + result).toString();
    }
}
