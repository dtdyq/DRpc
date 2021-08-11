### DRpc
DRpc is a distributed、high-performance rpc framework based on netty and zookeeper.



**features**

- simple and easy-use api
sever end:
```java
Server server = new Server("netty addr(127.0.0.1:9898)", "(zk addr)127.0.0.1:8989");
server.addService(AddService.class, new AddServiceImpl());
server.addService(HelloService.class, new HelloServiceImpl()).start();
```
​                 client end:
```java
Client client = new Client("(netty addr)127.0.0.1:8989");
HelloService hello = client.service(HelloService.class);
System.out.println(hello.hello("alan"));
AddService s = client.service(AddService.class);
System.out.println(s.add(12, 13));
```
​                  output:
```java
hello alan
[12,13]
[12,13]
```
- service register and discover on zookeeper
- tag support,same service with different version can be distinguished by tag