

package dyq.test;

import dyq.rpc.client.Client;

import org.apache.log4j.BasicConfigurator;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ClientTest {
    @Test
    public void testNormal() throws Exception {

        BasicConfigurator.configure();
        Client client = new Client("127.0.0.1:8989");
        HelloService hello = client.service(HelloService.class);
        System.out.println(hello.hello("alan"));
        AddService s = client.service(AddService.class);
        System.out.println(s.add(12, 13));
        System.out.println(s.add(12, 13));
    }

    @Test
    public void testAsync() throws Exception {

        BasicConfigurator.configure();

        Client client = new Client("127.0.0.1:8888");
        for (int i = 0; i < 508; i++) {
            int finalI = i;
            CompletableFuture.runAsync(new Runnable() {
                @Override
                public void run() {
                    long cur = System.currentTimeMillis();
                    HelloService hello = client.service(HelloService.class);
                    System.out.println(hello.hello("alan"));
                    System.out.println("index if hello " + finalI + " cost time " + (System.currentTimeMillis() - cur));
                    AddService s = client.service(AddService.class);
                    System.out.println(s.add(12, 13));
                }
            });
        }
        Thread.sleep(1000 * 60 * 60);
    }

    @Test
    public void testBatch() throws Exception {

        BasicConfigurator.configure();

        Client client = new Client("127.0.0.1:8888");
        for (int i = 0; i < 508; i++) {
            long cur = System.currentTimeMillis();
            HelloService hello = client.service(HelloService.class);
            System.out.println(hello.hello("alan"));
            System.out.println("index if hello " + i + " cost time " + (System.currentTimeMillis() - cur));
            AddService s = client.service(AddService.class);
            System.out.println(s.add(12, 13));
        }
    }

    @Test
    public void test11() {
        System.out.println(UUID.fromString("127.0.0.1"));
        System.out.println(UUID.fromString("127.0.0.1"));
        System.out.println(UUID.fromString("127.0.0.1"));
    }
}
