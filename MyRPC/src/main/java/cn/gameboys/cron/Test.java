package cn.gameboys.cron;

/**
 * Description:
 * 
 * @author sniper(www.gameboys.cn 1084038709)
 * @date 2019年8月16日
 */
public class Test {
	public static void main(String[] args) {
		CronManager.getInstance().init("cn.gameboys.cron.task");
	}
}
