package cn.gameboys.rpc.client;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gameboys.rpc.client.async.AsyncRPCCallback;
import cn.gameboys.rpc.client.async.MethodObjectInfo;
import cn.gameboys.rpc.expection.RpcErrorEnum;
import cn.gameboys.rpc.expection.RpcExpection;
import cn.gameboys.rpc.registry.ServiceDiscovery;
import cn.gameboys.rpc.util.ClassUtils;

/**
 * 
* Description: 
* @author sniper(www.gameboys.cn 1084038709) 
* @date 2019年8月5日
 */
public class RpcClient {
	private static final Logger logger = LoggerFactory.getLogger(ConnectManage.class);
	/**
	 * 服务更新监听器
	 */
	private ServiceDiscovery serviceDiscovery;

	/**
	 * 回调信息
	 */
	private static Map<String, List<MethodObjectInfo>> callbackInfoMap = new ConcurrentHashMap<String, List<MethodObjectInfo>>();

	public RpcClient(ServiceDiscovery serviceDiscovery, String basePackage) throws RpcExpection {
		this.serviceDiscovery = serviceDiscovery;
		// 初始化
		this.autoRegistAsyncRpcInfo(basePackage);
	}

	/**
	 * 获取回调的对象方法信息
	 * 
	 * @param interfaceName
	 * @return
	 */
	public static List<MethodObjectInfo> getMethodObjectInfo(String interfaceName) {
		return callbackInfoMap.get(interfaceName);
	}

	@SuppressWarnings("unchecked")
	public static <T> T create(int type, int hashCode, Class<T> interfaceClass) {
		RpcClientNode clientNode = ConnectManage.getInstance().route(type, hashCode);
		if (clientNode == null) {
			throw new RpcExpection(RpcErrorEnum.NO_TYPE_SERVER, "type:" + type);
			// return null;
		}
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] { interfaceClass }, clientNode);
	}

	@SuppressWarnings("unchecked")
	public static <T> T createAsync(int type, int hashCode, Class<T> interfaceClass) {
		RpcClientNode clientNode = ConnectManage.getInstance().route(type, hashCode);
		if (clientNode == null) {
			throw new RpcExpection(RpcErrorEnum.NO_TYPE_SERVER, "type:" + type);
		}
		RpcClientNodeProxy nodeProxy = new RpcClientNodeProxy(clientNode);
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] { interfaceClass }, nodeProxy);
	}

	public void stop() {
		serviceDiscovery.stop();
		ConnectManage.getInstance().stop();
	}

	/**
	 * 自动加载异步回调
	 * 
	 * @param basePackage
	 * @throws RpcExpection
	 */
	private void autoRegistAsyncRpcInfo(String basePackage) {
		Set<Class<?>> classes = ClassUtils.getClasses(basePackage);
		for (Class<?> clazz : classes) {
			Method[] methods = clazz.getMethods();
			for (Method m : methods) {
				AsyncRPCCallback rpcRequest = m.getAnnotation(AsyncRPCCallback.class);
				if (rpcRequest != null) {
					List<MethodObjectInfo> list = callbackInfoMap.get(rpcRequest.value());
					if (list == null) {
						list = new ArrayList<MethodObjectInfo>();
						callbackInfoMap.put(rpcRequest.value(), list);
					}
					try {
						list.add(new MethodObjectInfo(clazz.newInstance(), m));
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
		logger.info("初始化异步回调完毕, 注册数量：" + callbackInfoMap.size());
	}
}
