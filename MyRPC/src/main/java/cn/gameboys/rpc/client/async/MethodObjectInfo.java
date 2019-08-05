package cn.gameboys.rpc.client.async;

import java.lang.reflect.Method;

/**
 * 
* Description: 回调方法bean
* @author sniper(www.gameboys.cn 1084038709) 
* @date 2019年8月5日
 */
public class MethodObjectInfo {
	public MethodObjectInfo(Object obj, Method method) {
		this.obj = obj;
		this.method = method;
	}

	private Object obj;
	private Method method;

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

}
