package cn.gameboys.rpc.client;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gameboys.rpc.expection.RpcErrorEnum;
import cn.gameboys.rpc.expection.RpcExpection;
import cn.gameboys.rpc.registry.ServerConnectInfo;
import cn.gameboys.rpc.util.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 
 * Description:rpcClient的链接管理器，负责节点的增加和删除，保证线程安全
 * 
 * @author sniper(www.gameboys.cn 1084038709)
 * @date 2019年8月5日
 */
public class ConnectManage {
	private static final Logger logger = LoggerFactory.getLogger(ConnectManage.class);
	private volatile static ConnectManage connectManage;

	private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
	private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536),
			new NamedThreadFactory("RPC-Connect-workPool"));
	// 已连接的rpc服务节点列表
	private Map<ServerConnectInfo, RpcClientNode> clientServerNodes = new ConcurrentHashMap<ServerConnectInfo, RpcClientNode>();

	// map<type,list<ServerConnectInfo>>
	private Map<Integer, List<ServerConnectInfo>> typeServerInfos = new ConcurrentHashMap<Integer, List<ServerConnectInfo>>();

	// 随机数
	private ThreadLocalRandom random = ThreadLocalRandom.current();

	// 为rpcClient初始化设置
	private ReentrantLock lock = new ReentrantLock();
	private Condition connected = lock.newCondition();

	private long connectTimeoutMillis = 10000;
	// 标识在初始化的服务数量
	private volatile int initingServerNum = 0;

	private ConnectManage() {
	}

	public static ConnectManage getInstance() {
		if (connectManage == null) {
			synchronized (ConnectManage.class) {
				if (connectManage == null) {
					connectManage = new ConnectManage();
				}
			}
		}
		return connectManage;
	}

	/**
	 * 获取服务节点 方法使用频繁，效率需要考虑 ,这个控制路由和负载
	 * 
	 * @param type
	 * @param hashCode
	 * @return
	 */
	public RpcClientNode route(int type, int hashCode) {

		if (initingServerNum != 0) {
			lock.lock();
			try {
				// 这里还需要判断一下，加完锁再判断，不然在未加锁期间获得的值不对
				if (initingServerNum != 0) {
					logger.warn("服务还在初始化，先阻塞一下,type:" + type);
					connected.await();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		}
		// 暂时先随机分配吧，如果需要取模的后面再设计
		List<ServerConnectInfo> list = typeServerInfos.get(type);
		if (list == null || list.size() == 0) {
			lock.lock();
			try {
				try {
					boolean byNotify = connected.await(this.connectTimeoutMillis, TimeUnit.MILLISECONDS);
					if (!byNotify) {
						logger.error("该类型的服务未注册成功：" + type);
						throw new RpcExpection(RpcErrorEnum.NO_RPC_SERVER, "type:" + type);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} finally {
				lock.unlock();
			}
		}
		if (list != null && list.size() > 0) {
			int index = random.nextInt(list.size());
			return clientServerNodes.get(list.get(index));
		}
		return null;
	}

	/**
	 * 状态变化
	 * 
	 * @param allServerAddress
	 * @throws RpcExpection
	 */
	public void onServerStatusChanged(List<ServerConnectInfo> allServerConnectInfo) {
		if (allServerConnectInfo == null) {
			throw new RpcExpection(RpcErrorEnum.INIT_ERROR, "");
		}
		if (allServerConnectInfo.size() > 0) {
			List<ServerConnectInfo> needAddList = new ArrayList<ServerConnectInfo>();
			List<ServerConnectInfo> needDelList = new ArrayList<ServerConnectInfo>();
			Set<ServerConnectInfo> oldServerInfoList = clientServerNodes.keySet();
			// 需要删掉的节点
			for (ServerConnectInfo oldServerConnectInfo : oldServerInfoList) {
				if (!allServerConnectInfo.contains(oldServerConnectInfo)) {
					needDelList.add(oldServerConnectInfo);
				}
			}
			// 新增的节点
			for (ServerConnectInfo serverConnectInfo : allServerConnectInfo) {
				if (!oldServerInfoList.contains(serverConnectInfo)) {
					needAddList.add(serverConnectInfo);
				}
			}
			// 处理新增和删除逻辑，先删再添加
			for (ServerConnectInfo serverConnectInfo : needDelList) {
				this.removeClientNode(serverConnectInfo);
			}
			for (ServerConnectInfo serverConnectInfo : needAddList) {
				this.addClientNode(serverConnectInfo);
			}
		} else {
			// No available server node ( All server nodes are down )
			logger.error("No available server node. All server nodes are down !!!");
			Set<ServerConnectInfo> oldServerInfoList = clientServerNodes.keySet();
			for (ServerConnectInfo serverConnectInfo : oldServerInfoList) {
				this.removeClientNode(serverConnectInfo);
			}
		}
	}

	/**
	 * 
	 * @param serverConnectInfo
	 */
	private void addClientNode(final ServerConnectInfo serverConnectInfo) {
		this.checkConnectedFinished(1);
		threadPoolExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Bootstrap b = new Bootstrap();
				RpcClientNode clientNode = new RpcClientNode(serverConnectInfo);
				b.group(eventLoopGroup).channel(NioSocketChannel.class).handler(new RpcClientInitializer(clientNode));
				ChannelFuture channelFuture = b.connect(new InetSocketAddress(serverConnectInfo.getIp(), serverConnectInfo.getPort()));
				channelFuture.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(final ChannelFuture channelFuture) throws Exception {
						try {
							if (channelFuture.isSuccess()) {
								logger.info("Successfully connect to remote server. " + serverConnectInfo);
								RpcClientHandler handler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
								clientServerNodes.put(serverConnectInfo, clientNode);
								// 添加到内存
								List<ServerConnectInfo> list = typeServerInfos.get(serverConnectInfo.getType());
								if (list == null) {
									list = new ArrayList<ServerConnectInfo>();
									typeServerInfos.put(serverConnectInfo.getType(), list);
								}
								list.add(serverConnectInfo);
								clientNode.onRpcServerOpen(serverConnectInfo);
							}
						} finally {
							checkConnectedFinished(-1);
						}
					}
				});
			}
		});
	}

	/**
	 * 
	 * @param serverConnectInfo
	 */
	private void removeClientNode(final ServerConnectInfo serverConnectInfo) {
		// 这里设置-100000的含义是不想和添加节点的标识重合，这里就意味着最大服务不能超过这么多
		this.checkConnectedFinished(-100000);
		threadPoolExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					logger.error("remote server down! " + serverConnectInfo);
					RpcClientNode clientNode = clientServerNodes.remove(serverConnectInfo);
					List<ServerConnectInfo> list = typeServerInfos.get(serverConnectInfo.getType());
					if (list != null) {
						for (ServerConnectInfo info : list) {
							if (info.equals(serverConnectInfo)) {
								list.remove(info);
								break;
							}
						}
					}
					if (clientNode != null) {
						clientNode.onRpcServerDown();
					}
				} finally {
					checkConnectedFinished(100000);
				}
			}
		});
	}

	public void checkConnectedFinished(int addCount) {
		lock.lock();
		try {
			this.initingServerNum += addCount;
			if (this.initingServerNum == 0) {
				this.connected.signalAll();
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 关闭rpcClient服务
	 */
	public void stop() {
		Collection<RpcClientNode> rpcClientNodes = clientServerNodes.values();
		for (RpcClientNode clientNode : rpcClientNodes) {
			clientNode.onRpcServerDown();
		}
		clientServerNodes.clear();
		typeServerInfos.clear();

		threadPoolExecutor.shutdownNow();
		eventLoopGroup.shutdownGracefully();
	}

	public int getType(Long key) {
		return (int) (key >> 32);
	}

	public int getServerID(Long key) {
		return (int) (key << 32 >> 32);
	}

	public Long getKey(int type, int serverID) {
		return (((long) type) << 32) + serverID;
	}

}
