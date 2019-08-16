package cn.gameboys.cron.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gameboys.cron.CronManager;
import cn.gameboys.cron.CronTask;

/**
 * Description:
 * 
 * @author sniper(www.gameboys.cn 1084038709)
 * @date 2019年8月16日
 */
public class Task {
	private static final Logger logger = LoggerFactory.getLogger(Task.class);
	@CronTask(cronExpression = "0/1 * * * * ?")
	public void cronSayHi() {
		logger.info(Thread.currentThread().getName() + "@@@@@@@@@@@@@@@@@@@@   hi");
	}

	
	@CronTask(cronExpression = "0/2 * * * * ?")
	public void cronSayBoy() {
		logger.info(Thread.currentThread().getName() + "####################   boy");
	}
}
