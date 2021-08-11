package dyq.rpc.module;

import java.util.List;
import java.util.StringJoiner;

public class RpcRequest extends RpcProtocol {
    private String identifier;
    private String method;
    private List<Class<?>> paramTypes;
    private List<Object> params;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RpcRequest.class.getSimpleName() + "[", "]")
            .add("identifier='" + identifier + "'")
            .add("method='" + method + "'")
            .add("paramTypes=" + paramTypes)
            .add("params=" + params)
            .toString();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }

    public List<Class<?>> getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(List<Class<?>> paramTypes) {
        this.paramTypes = paramTypes;
    }
}
