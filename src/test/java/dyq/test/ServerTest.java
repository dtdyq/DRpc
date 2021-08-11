package dyq.test;

import dyq.rpc.server.Server;

import org.apache.log4j.BasicConfigurator;
import org.junit.jupiter.api.Test;

public class ServerTest {

    @Test
    public void testServer() throws Exception {
        BasicConfigurator.configure();
        Server server = new Server("127.0.0.1:9898", "127.0.0.1:8989");
        server.addService(AddService.class, new AddServiceImpl());
        server.addService(HelloService.class, new HelloServiceImpl()).start();
    }

    @Test
    public void testPackage() throws Exception {
        BasicConfigurator.configure();
        Server server = new Server("127.0.0.1:8989", "127.0.0.1:8888");
        server.addForPackage("dyq.test").start();
    }

    @Test
    public void test0() throws Exception {
        BasicConfigurator.configure();
        Server server = new Server("127.0.0.1:9000", "127.0.0.1:8888");
        server.addService(HelloService.class, new HelloServiceImpl(),"newv").start();
    }

    @Test
    public void test1() throws Exception {
        BasicConfigurator.configure();
        Server server = new Server("127.0.0.1:9001", "127.0.0.1:8888");
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(1000 * 60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    server.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        server.addService(AddService.class, new AddServiceImpl()).start();
    }

    @Test
    public void test2() throws Exception {
        BasicConfigurator.configure();
        Server server = new Server("127.0.0.1:9002", "127.0.0.1:8888");
        server.addService(HelloService.class, new HelloServiceImpl());
        server.addService(AddService.class, new AddServiceImpl()).start();
    }

    @Test
    public void test3() throws Exception {
        BasicConfigurator.configure();
        Server server = new Server("127.0.0.1:9003", "127.0.0.1:8888");
        server.addService(HelloService.class, new HelloServiceImpl());
        server.addService(AddService.class, new AddServiceImpl()).start();
    }

    @Test
    public void test4() throws Exception {
        BasicConfigurator.configure();
        Server server = new Server("127.0.0.1:9004", "127.0.0.1:8888");
        server.addService(HelloService.class, new HelloServiceImpl());
        server.addService(AddService.class, new AddServiceImpl()).start();
    }

    @Test
    public void test5() throws Exception {
        BasicConfigurator.configure();
        Server server = new Server("127.0.0.1:9005", "127.0.0.1:8888");
        server.addService(HelloService.class, new HelloServiceImpl());
        server.addService(AddService.class, new AddServiceImpl()).start();
    }

    @Test
    public void test6() throws Exception {
        BasicConfigurator.configure();
        Server server = new Server("127.0.0.1:9006", "127.0.0.1:8888");
        server.addService(HelloService.class, new HelloServiceImpl());
        server.addService(AddService.class, new AddServiceImpl()).start();
    }

    @Test
    public void test7() throws Exception {
        BasicConfigurator.configure();
        Server server = new Server("127.0.0.1:9007", "127.0.0.1:8888");
        server.addService(HelloService.class, new HelloServiceImpl());
        server.addService(AddService.class, new AddServiceImpl()).start();
    }

    @Test
    public void test8() throws Exception {
        BasicConfigurator.configure();
        Server server = new Server("127.0.0.1:9008", "127.0.0.1:8888");
        server.addService(HelloService.class, new HelloServiceImpl());
        server.addService(AddService.class, new AddServiceImpl()).start();
    }

    @Test
    public void test9() throws Exception {
        BasicConfigurator.configure();
        Server server = new Server("127.0.0.1:9009", "127.0.0.1:8888");
        server.addService(HelloService.class, new HelloServiceImpl());
        server.addService(AddService.class, new AddServiceImpl()).start();
    }

}
