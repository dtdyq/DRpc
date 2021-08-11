package dyq.rpc.exception;

public class RpcException extends Exception {
    public RpcException(String s) {
        super(s);
    }

    public RpcException(Throwable e) {
        super(e);
    }
}
