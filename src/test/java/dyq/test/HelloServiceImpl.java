

package dyq.test;

import dyq.rpc.server.annotation.Rpc;

@Rpc
public class HelloServiceImpl implements HelloService {
    public String hello(String name) {
        return "hello " + name;
    }
}
