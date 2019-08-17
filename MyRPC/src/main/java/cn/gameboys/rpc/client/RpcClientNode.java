package cn.gameboys.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gameboys.rpc.protocol.RpcRequest;
import cn.gameboys.rpc.protocol.RpcResponse;
import cn.gameboys.rpc.registry.ServerConnectInfo;
import cn.gameboys.rpc.status.StatusManager;
import cn.gameboys.util.NamedThreadFactory;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * Description: 一个client包含多个clientnode，每个clientNode和rpcServer建立连接
 * 
 * @author sniper(www.gameboys.cn 1084038709)
 * @date 2019年8月1日
 */
public class RpcClientNode implements InvocationHandler, RpcClientRspExcuteable {

	private static final Logger logger = LoggerFactory.getLogger(RpcClientNode.class);

	private BlockingQueue<Runnable> requestQueue = new ArrayBlockingQueue<Runnable>(1024 * 10);;
	private ThreadPoolExecutor workPool = new ThreadPoolExecutor(16, 16, 600L, TimeUnit.SECONDS, requestQueue, new NamedThreadFactory("RPC-Client-workPool"));

	private ServerConnectInfo serverInfo;
	// 是否初始化完毕
	private volatile boolean inited = false;

	public RpcClientNode(ServerConnectInfo serverInfo) {
		this.serverInfo = serverInfo;
	}

	private ConcurrentHashMap<String, RPCFuture> pendingRPC = new ConcurrentHashMap<>();
	private volatile Channel channel;

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return this.invoke(proxy, method, args, false);
	}

	/**
	 * 
	 * @param proxy
	 * @param method
	 * @param args
	 * @param asyncReq  是否是异步请求
	 * @param oneWayReq 是否是单向请求
	 * @return
	 * @throws Throwable
	 */
	public Object invoke(Object proxy, Method method, Object[] args, boolean asyncReq) throws Throwable {
		if (!this.inited) {
			return null;
		}
		long startTime = System.currentTimeMillis();

		if (Object.class == method.getDeclaringClass()) {
			String name = method.getName();
			if ("equals".equals(name)) {
				return proxy == args[0];
			} else if ("hashCode".equals(name)) {
				return System.identityHashCode(proxy);
			} else if ("toString".equals(name)) {
				return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy)) + ", with InvocationHandler " + this;
			} else {
				throw new IllegalStateException(String.valueOf(method));
			}
		}
		// 检查是否需要返回值,如果没有返回，不需要填装future
		boolean isOneWay = method.getReturnType().equals(void.class);
		RpcRequest request = new RpcRequest();
		request.setRequestId(UUID.randomUUID().toString());
		request.setClassName(method.getDeclaringClass().getName());
		request.setMethodName(method.getName());
		request.setParameterTypes(method.getParameterTypes());
		request.setParameters(args);

		if (isOneWay) {
			// 单向请求
			this.sendRequest(request);
			long endTime = System.currentTimeMillis();
			StatusManager.getInstance().addReqTimes(request.getClassName() + "." + request.getMethodName(), (int)(endTime - startTime));
			return null;
		} else if (asyncReq) {
			// 异步请求
			RPCFuture rpcFuture = new RPCFuture(request, this, true);
			pendingRPC.put(request.getRequestId(), rpcFuture);
			// 因为多线程，为了防止还没有把reqID注册到pendingRPC里面，rpcServer就已经响应回来了找不到pendingRPC
			this.sendRequest(request);
			return null;
		} else {
			// 同步请求
			RPCFuture rpcFuture = new RPCFuture(request, this, false);
			pendingRPC.put(request.getRequestId(), rpcFuture);
			this.sendRequest(request);
			// 最多3s不响应的话直接返回
			Object obj = rpcFuture.get(5000L, TimeUnit.MILLISECONDS);
			long endTime = System.currentTimeMillis();
			StatusManager.getInstance().addReqTimes(request.getClassName() + "." + request.getMethodName(), (int)(endTime - startTime));
			return obj;
		}
	}

	/**
	 * 纯发送消息
	 * 
	 * @param request
	 */
	private void sendRequest(RpcRequest request) {
		final CountDownLatch latch = new CountDownLatch(1);
		channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) {
				latch.countDown();
			}
		});
		try {
			latch.await();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		}
	}

	public void onChannelRegistered(Channel channel) {
		this.channel = channel;
	}

	public void onReceivedRsp(RpcResponse response) {
		String requestId = response.getRequestId();
		RPCFuture rpcFuture = pendingRPC.get(requestId);
		if (rpcFuture != null) {
			pendingRPC.remove(requestId);
			rpcFuture.done(response);
		}
	}

	@Override
	public void execute(Runnable task) {
		workPool.execute(task);
	}

	public void onRpcServerOpen(ServerConnectInfo serverInfo) {
		logger.error("rpcClientNode open:" + serverInfo);
		this.inited = true;

	}

	public void onRpcServerDown() {
		logger.error("rpcClientNode down,current requestQueue size:" + requestQueue.size() + " cennectInfo:" + serverInfo);
		this.inited = false;
		pendingRPC.clear();
		workPool.shutdown();
		channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);

	}

}
