package cn.gameboys.rpc.server;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gameboys.rpc.expection.RpcErrorEnum;
import cn.gameboys.rpc.expection.RpcExpection;
import cn.gameboys.rpc.protocol.RpcDecoder;
import cn.gameboys.rpc.protocol.RpcEncoder;
import cn.gameboys.rpc.protocol.RpcRequest;
import cn.gameboys.rpc.protocol.RpcResponse;
import cn.gameboys.rpc.registry.ServiceRegistry;
import cn.gameboys.util.ClassUtils;
import cn.gameboys.util.NamedThreadFactory;
import cn.gameboys.util.NetWorkUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import net.sf.cglib.reflect.FastClass;

/**
 * 
 * Description:
 * 
 * @author sniper(www.gameboys.cn 1084038709)
 * @date 2019年8月5日
 */
public class RpcServer {

	private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

	private String serverAddress;
	private RpcServerConfig cfg;

	private ServiceRegistry serviceRegistry;

	private Map<String, Object> handlerMap = new HashMap<>();
	private BlockingQueue<Runnable> requestQueue = new ArrayBlockingQueue<Runnable>(1024 * 10);;
	private ThreadPoolExecutor workPool = new ThreadPoolExecutor(100, 110, 20, TimeUnit.SECONDS, requestQueue, new NamedThreadFactory("RPC-Server-workPool"));
	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;

	public RpcServer(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public RpcServer(RpcServerConfig cfg) {
		// 应该先检查cfg，后面做
		this.cfg = cfg;
		String ip = NetWorkUtil.getIntranetIp();
		// 11001表示type为1，serverid=1
		// ，21002表示：type==11，serverID=2，22520表示type==12，serverID=520
		String serverAddress = ip + ":" + (10000 + (cfg.getType() * 1000) + cfg.getServerID());
		logger.info(serverAddress);
		ServiceRegistry serviceRegistry = new ServiceRegistry(cfg.getRegistryAddress());
		this.serverAddress = serverAddress;
		this.serviceRegistry = serviceRegistry;
		this.autoRegistService(cfg.getBasePackage());
	}

	public void stop() {
		if (bossGroup != null) {
			bossGroup.shutdownGracefully();
		}
		if (workerGroup != null) {
			workerGroup.shutdownGracefully();
		}
	}

	public void submit(Runnable task) {
		workPool.execute(task);
	}

	public RpcServer addService(String interfaceName, Object serviceBean) {
		if (!handlerMap.containsKey(interfaceName)) {
			logger.info("Loading service: {}", interfaceName);
			handlerMap.put(interfaceName, serviceBean);
		}
		return this;
	}

	public void start() throws Exception {
		if (bossGroup == null && workerGroup == null) {
			bossGroup = new NioEventLoopGroup();
			workerGroup = new NioEventLoopGroup();
			ServerBootstrap bootstrap = new ServerBootstrap();
			RpcServer rpcServer = this;
			bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel channel) throws Exception {
					channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0)).addLast(new RpcDecoder(RpcRequest.class)).addLast(new RpcEncoder(RpcResponse.class))
							.addLast(new RpcHandler(rpcServer));
				}
			}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);
			String[] array = serverAddress.split(":");
			String host = array[0];
			int port = Integer.parseInt(array[1]);
			ChannelFuture future = bootstrap.bind(host, port).sync();
			logger.info("Server started on port {}", port);
			if (serviceRegistry != null) {
				serviceRegistry.register(cfg.getType() + "|" + cfg.getServerID() + "|" + host + "|" + port);
			}
			future.channel().closeFuture().sync();
		}
	}

	/**
	 * 自动注册实现类
	 * 
	 * @param basePackage
	 */
	private void autoRegistService(String basePackage) {
		Set<Class<?>> classes = ClassUtils.getClasses(basePackage);
		for (Class<?> class1 : classes) {
			RpcService rpcService = class1.getAnnotation(RpcService.class);
			if (rpcService != null) {
				Class<?> interfaceName = rpcService.value();
				try {
					this.addService(interfaceName.getName(), class1.newInstance());
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		logger.error("自动注入服务完毕：" + handlerMap.size());
	}

	public void onReceivedMsg(RpcRequest request, ChannelHandlerContext ctx) {
		workPool.execute(new Runnable() {
			@Override
			public void run() {
				logger.debug("Receive request " + request.getRequestId());
				RpcResponse response = new RpcResponse();
				response.setRequestId(request.getRequestId());
				try {
					Object result = handle(request);
					response.setResult(result);
				} catch (RpcExpection expection) {	
					response.setError(expection.toString());
					response.setResult(expection);
					// logger.error("RPC Server handle request error", t);
				}
				ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture channelFuture) throws Exception {
						logger.debug("Send response for request " + request.getRequestId());
					}
				});
			}
		});

	}

	private Object handle(RpcRequest request) throws RpcExpection {
		String className = request.getClassName();
		Object serviceBean = handlerMap.get(className);
		if (serviceBean == null) {
			String info = "【currentServerInfo:" + this.cfg + " className:" + className + "】";
			throw new RpcExpection(RpcErrorEnum.NO_METHOD_SERVICE, info);
		}

		Class<?> serviceClass = serviceBean.getClass();
		String methodName = request.getMethodName();
		Class<?>[] parameterTypes = request.getParameterTypes();
		Object[] parameters = request.getParameters();
		logger.debug(serviceClass.getName());
		logger.debug(methodName);
		for (int i = 0; i < parameterTypes.length; ++i) {
			logger.debug(parameterTypes[i].getName());
		}
		for (int i = 0; i < parameters.length; ++i) {
			logger.debug(parameters[i].toString());
		}
		// JDK reflect
		/*
		 * Method method = serviceClass.getMethod(methodName, parameterTypes);
		 * method.setAccessible(true); return method.invoke(serviceBean, parameters);
		 */

		// Cglib reflect
		FastClass serviceFastClass = FastClass.create(serviceClass);
//        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
//        return serviceFastMethod.invoke(serviceBean, parameters);
		// for higher-performance
		int methodIndex = serviceFastClass.getIndex(methodName, parameterTypes);
		try {
			return serviceFastClass.invoke(methodIndex, serviceBean, parameters);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

}
