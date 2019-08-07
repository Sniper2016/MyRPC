# 一、背景
那年，我9岁，她11岁，我告诉我将来要娶她，她也很调皮的回答我：“好”！ 那一年，我12岁她14岁，我说我喜欢她，她没说话，便转过头去。 那一年，我18岁，她20岁，我已在外工作一年，她还上重点大学，我再也说不出口我喜欢她，我觉得自己根本配 不上她，我把想对她说的话，留给了自己，等我配上她的时候，再告诉她。 后来我再没有机会见过她，听说她已工作，我关心的她是否已为人妻为人母我的事业走向正轨再也不为钱所窘迫，再次见到她时，是在东莞，昏黄的房间，只有我们两个人，沉默了良久，她开口了：“八百，你是熟人，就收你五百”。我尽量让自己不再颤抖，我说：“跟我走吧”，她目光呆滞了一下，随即闪烁了一下，说：“我配不上你，我只是一位小姐，我还有客人，没什么事 ，我先走了”。她就匆忙的离去了。 后来，我在电视上看到了她，她被两名民警押着，一脸惊恐与不堪，电视甚至没有给她遮住脸，任由她在我面前晃着，任由她在我心中撞着、撞着、直至粉碎！ 最后一次见她，是她找我去的，当时她在楼顶 ，我在楼下抬头望她，许久，他走了，没留下一句话。. 再有她的消息是一年后，是她妈妈给我打的电话，说她病了，没法治疗，说想最后见我一面。到了医院，在她的病床前，看着她苍白的脸，比以前消瘦了许多。我对她笑了笑，她也对我笑，像七岁那年。她抬起了手，我赶紧过去握住她的手，泪水终忍不住往下掉，她用低沉的浯气说: ”我....我看到过一个很牛的rpc框架-MyRPC，你能帮忙点个赞吗？[坏笑]"
# 二、简介
MyRPC是一个可用于生产环境的轻量级，高可用，高性能，高易用分布式远程调用框架，参考dubbo的设计，是一个五脏俱全的简易版dubbo。
## 1.架构
![架构图](https://gameboys.oss-cn-shenzhen.aliyuncs.com/oneblog/20190805215811114.png)
## 2.特性：
### 连通性
注册中心负责服务地址的注册与查找，相当于目录服务，服务提供者和消费者只在启动时与注册中心交互，注册中心不转发请求，压力较小，服务提供者向注册中心注册其提供的服务，服务消费者向注册中心获取服务提供者地址列表，并根据负载算法直接调用提供者
注册中心通过长连接感知服务提供者的存在，服务提供者宕机，注册中心将立即推送事件通知消费者
### 健壮性
注册中心对等集群，任意一台宕掉后，将自动切换到另一台,注册中心全部宕掉后，服务提供者和服务消费者仍能通过本地缓存通讯
服务提供者无状态，任意一台宕掉后，不影响使用，服务提供者全部宕掉后，服务消费者应用将无法使用，并无限次重连等待服务提供者恢复
### 伸缩性
服务提供者无状态，可动态增加机器部署实例，注册中心将推送新的服务提供者信息给消费者
### 兼容性
Rpc接口使用 Protostuff  序列化框架，可以支持向后兼容
## 3.使用技术
1. 使用成熟的Netty网络框架
2. 使用Protostuff作为高性能的序列化框架
3. 使用高可用的注册中心Zookeeper
4. 使用注解自动注入RPC服务
5. 使用注解实现异步回调，方便易用
# 三、使用
[参考博客](https://www.gameboys.cn/article/32)
# 四、测试用例
## 1.测试zk集群的高可用
1. 服务器上面部署一个3个节点的zk服务，端口分别为192.168.1.107:2181,192.168.1.107:3181,192.168.1.107:4181
2. 启动Type1Server1.java Type1Server2.java Type2Server1.java Type2Server2.java
3. 启动SyncClientTest.java
4. 分别关掉zk1，zk2，开启zk1，开启zk2
5. 观察服务是否ok
6. 结论：基于zk的完美高可用--》zk挂掉了2/3的服务节点的时候服务器会报错，但是已注册的服务还是ok的，挂掉的服务不超过2/3服务器是没有影响的
## 2.测试服务切换
1. 服务器上面部署一个3个节点的zk服务，端口分别为192.168.1.107:2181,192.168.1.107:3181,192.168.1.107:4181，也可以使用ZkMain.java启动内嵌zk服务(注：内嵌zk服不能用来测试高可用，因为他重启是没有保存数据的)
2. 启动Type1Server1.java Type2Server1.java 
3. 启动SyncClientTest.java
4. 启动Type1Server2.java Type2Server2.java，关掉Type1Server1.java Type2Server1.java 
5. 观察服务是否ok
6. 结论：当某个服务挂掉了，已发送的请求会失败，剩余的请求会被路由到其他可用服务，未做dubbo的集群容错机制，后期可以考虑，但已经实现服务挂掉自动感知和自动去除无效节点
## 3.测试同步、异步请求
1. 同步请求 启动Type1Server1.java Type2Server1.java 启动SyncClientTest.java
2. 异步请求 启动Type1Server1.java Type2Server1.java 启动ASyncClientTest.java
3. 结论：同步异步无异常
## 4.测试编解码性能,编解码向后兼容
1. 启动ProtostuffTest.java
 结论：
1. 100000次序列化，使用了700ms，0.007ms每次序列化，速度飞起
2. 完美向后兼容，bean新增字段测试ok
3. map，list等特殊bean编解码测试ok
## 5.测试多个服务和客户端同时启动，看是否能正常正确提供服务
1. 启动SyncClientTest.java 启动ASyncClientTest.java
2. 过30s之后 启动Type1Server1.java Type2Server1.java 
3. 结论：rpcClient在服务未初始化完毕的时候会阻塞10s，直到rpcServer链接成功，解决dubbo启动依赖检查
## 6.测试rpcClient同时连接多类型服务
结论：操作在用例1已经使用到了，多类型服务无问题
