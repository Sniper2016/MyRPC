package cn.gameboys.rpc.status;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gameboys.cron.CronManager;

/**
 * Description:统计接口调用次数，接口调用时间
 * 
 * @author sniper(www.gameboys.cn 1084038709)
 * @date 2019年8月15日
 */
public class StatusManager {
	private static final Logger logger = LoggerFactory.getLogger(StatusManager.class);
	private volatile static StatusManager statusManager = new StatusManager();
	// 每日的总请求信息
	private Map<String, Long> totalApiTimes = new ConcurrentHashMap<>();
	// 每分钟请求信息
	private Map<String, Long> minuteApiTimes = new ConcurrentHashMap<>();
	// 使用时间数组，userTimeArr[100]表示花费时间超过100ms的请求数量
	private volatile int[] useTimeArr = new int[101];

	private StatusManager() {
	}

	public static StatusManager getInstance() {
		return statusManager;
	}

	public void init() {
		CronManager.getInstance().init("cn.gameboys.rpc.status");
	}

	/**
	 * 
	 * @param apiName   接口名字
	 * @param useMsTime 单位毫秒
	 */
	public void addReqTimes(String apiName, int useMsTime) {
		Long totalTimes = totalApiTimes.get(apiName);
		if (totalTimes == null) {
			totalApiTimes.put(apiName, 1L);
		} else {
			totalApiTimes.put(apiName, totalTimes + 1);
		}
		Long minuteTimes = minuteApiTimes.get(apiName);
		if (minuteTimes == null) {
			minuteApiTimes.put(apiName, 1L);
		} else {
			minuteApiTimes.put(apiName, minuteTimes + 1);
		}
		int index = useMsTime >= 100 ? 100 : useMsTime;
		useTimeArr[index]++;
	}

	public String getRpcUseTimeStatus() {
		return Arrays.toString(this.useTimeArr);
	}

	
	public String getRpcReqTotalTimesStatus() {
		return this.totalApiTimes.toString();
	}

	public String getRpcReqMinuteTimesStatus() {
		return this.minuteApiTimes.toString();
	}

	public void clearMinuteApiTimes() {
		this.minuteApiTimes.clear();
	}

	public static void main(String[] args) {
		// System.out.println(DateUtil.getMinute(new Date()));

	}

}
