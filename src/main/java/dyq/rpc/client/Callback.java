package dyq.rpc.client;

public interface Callback<T> {
    void process(T t, Throwable throwable);
}
