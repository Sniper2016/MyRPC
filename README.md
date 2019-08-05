[作者博客](https://www.gameboys.cn/article/32)
# 一、背景
## 1.念头
最近在看dubbo高可用视频，感触很深，于是就想到手写一个dubbo证明一下自己dubbo玩得溜，后面经过多方考虑，打算写一个非spring环境下的高可用rpc框架来练练手，一方面是既然dubbo已经这么成熟了，再写也没必要了，另外一方面是dubbo里面的东西确实多，凭一人之力恐怕写到天昏地暗。
## 2.名字由来
在写完框架的当晚，我翻来覆去睡不着，失眠了，既然模仿dubbo，那么叫OneRPC？ThreeRPC？太张扬，正好最近自己在学习MySQL，于是将计就计，就叫MyRPC吧，希望他能像MySQL一样在开源的世界一飞冲天。
## 3.目标
易用、实用、高可用！
# 二、简介
MyRPC是一个可用于生产环境的轻量级，高可用，高性能，高易用分布式远程调用框架，参考dubbo的设计，是一个非spring环境下的dubbo框架二号。
## 1.架构
![架构图](https://gameboys.oss-cn-shenzhen.aliyuncs.com/oneblog/20190805215811114.png)
## 2.特性：
### 连通性
注册中心负责服务地址的注册与查找，相当于目录服务，服务提供者和消费者只在启动时与注册中心交互，注册中心不转发请求，压力较小
服务提供者向注册中心注册其提供的服务
服务消费者向注册中心获取服务提供者地址列表，并根据负载算法直接调用提供者
注册中心，服务提供者，服务消费者三者之间均为长连接，
注册中心通过长连接感知服务提供者的存在，服务提供者宕机，注册中心将立即推送事件通知消费者
### 健壮性
数据库宕掉后，注册中心仍能通过缓存提供服务列表查询，但不能注册新服务
注册中心对等集群，任意一台宕掉后，将自动切换到另一台
注册中心全部宕掉后，服务提供者和服务消费者仍能通过本地缓存通讯
服务提供者无状态，任意一台宕掉后，不影响使用
服务提供者全部宕掉后，服务消费者应用将无法使用，并无限次重连等待服务提供者恢复
### 伸缩性
服务提供者无状态，可动态增加机器部署实例，注册中心将推送新的服务提供者信息给消费者
### 兼容性
Rpc接口使用pb序列化，可以支持向后兼容
## 3.使用技术
使用成熟的Netty网络框架
使用Protostuff作为高性能的序列化框架
使用高可用的注册中心Zookeeper
使用注解自动注入RPC服务
使用注解实现异步回调，方便易用
# 三、使用
参考[博客](https://www.gameboys.cn/article/32)
# 四、测试用例
## 1.测试zk集群的高可用
a.服务器上面部署一个3个节点的zk服务，端口分别为192.168.1.107:2181,192.168.1.107:3181,192.168.1.107:4181
b.启动Type1Server1.java Type1Server2.java Type2Server1.java Type2Server2.java
c.启动SyncClientTest.java
d.分别关掉zk1，zk2，开启zk1，开启zk2
e.观察服务是否ok
结论：基于zk的完美高可用--》zk挂掉了2/3的服务节点的时候服务器会报错，但是已注册的服务还是ok的，挂掉的服务不超过2/3服务器是没有影响的
## 2.测试服务切换
a.服务器上面部署一个3个节点的zk服务，端口分别为192.168.1.107:2181,192.168.1.107:3181,192.168.1.107:4181，也可以使用ZkMain.java启动内嵌zk服务(注：内嵌zk服不能用来测试高可用，因为他重启是没有保存数据的)
b.启动Type1Server1.java Type2Server1.java 
c.启动SyncClientTest.java
d.启动Type1Server2.java Type2Server2.java，关掉Type1Server1.java Type2Server1.java 
e.观察服务是否ok
结论：当某个服务挂掉了，已发送的请求会失败，剩余的请求会被路由到其他可用服务，未做dubbo的集群容错机制，后期可以考虑，但已经实现服务挂掉自动感知和自动去除无效节点
## 3.测试同步、异步请求
a.同步请求 启动Type1Server1.java Type2Server1.java 启动SyncClientTest.java
b.异步请求 启动Type1Server1.java Type2Server1.java 启动ASyncClientTest.java
结论：同步异步无异常
## 4.测试编解码性能,编解码向后兼容
ProtostuffTest.java
结论：
a.100000次序列化，使用了700ms，0.007ms每次序列化，速度飞起
b.完美向后兼容，bean新增字段测试ok
c.map，list等特殊bean编解码测试ok
## 5.测试多个服务和客户端同时启动，看是否能正常正确提供服务
a.启动SyncClientTest.java 启动ASyncClientTest.java
b.过30s之后 启动Type1Server1.java Type2Server1.java 
结论：rpcClient在服务未初始化完毕的时候会阻塞10s，直到rpcServer链接成功，解决dubbo启动依赖检查
## 6.测试rpcClient同时连接多类型服务
结论：操作在用例1已经使用到了，多类型服务无问题
