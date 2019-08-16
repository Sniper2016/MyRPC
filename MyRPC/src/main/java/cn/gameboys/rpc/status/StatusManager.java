package cn.gameboys.rpc.status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:统计接口调用次数，接口调用时间
 * 
 * @author sniper(www.gameboys.cn 1084038709)
 * @date 2019年8月15日
 */
public class StatusManager {
	private static final Logger logger = LoggerFactory.getLogger(StatusManager.class);
	private volatile static StatusManager statusManager;

	private StatusManager() {
	}

	public static StatusManager getInstance() {
		if (statusManager == null) {
			synchronized (StatusManager.class) {
				if (statusManager == null) {
					statusManager = new StatusManager();
				}
			}
		}
		return statusManager;
	}
	
	
	

	/**
	 * 
	 * @param apiName  接口名字
	 * @param useMsTime  单位毫秒
	 */
	public void addReqTimes(String apiName, int useMsTime) {

	}

	
	
	
	
}
