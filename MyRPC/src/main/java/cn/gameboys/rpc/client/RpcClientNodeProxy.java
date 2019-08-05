package cn.gameboys.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Description: 这个类的作用是向invoke传递一个是否异步的参数
 * 
 * @author sniper(www.gameboys.cn 1084038709)
 * @date 2019年8月2日
 */
public class RpcClientNodeProxy implements InvocationHandler {

	RpcClientNode clientNode;

	public RpcClientNodeProxy(RpcClientNode clientNode) {
		this.clientNode = clientNode;
	}

	@Override
	public Object invoke(Object obj, Method method, Object[] aobj) throws Throwable {
		return clientNode.invoke(obj, method, aobj, true);
	}

}
