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
	
	//1s调用一次
	@CronTask(cronExpression = "0/1 * * * * ?")
	public void cronSayHi() {
		logger.info(Thread.currentThread().getName() + "@@@@@@@@@@@@@@@@@@@@   hi");
	}

	//10s调用一次
	@CronTask(cronExpression = "0/10 * * * * ?")
	public void cronSayBoy() {
		logger.info(Thread.currentThread().getName() + "####################   boy");
	}
	
	
	
	
//	Cron Expressions示例
//	CronTrigger示例1 - 创建一个触发器的表达式，每5分钟就会触发一次
//	“0 0/5 * * *？”
//	CronTrigger示例2 - 创建触发器的表达式，每5分钟触发一次，分钟后10秒（即上午10时10分，上午10:05:10等）。
//
//	“10 0/5 * * *？”
//	CronTrigger示例3 - 在每个星期三和星期五的10:30，11:30，12:30和13:30创建触发器的表达式。
//
//	“0 30 10-13？* WED，FRI“
//	CronTrigger示例4 - 创建触发器的表达式，每个月5日和20日上午8点至10点之间每半小时触发一次。请注意，触发器将不会在上午10点开始，仅在8:00，8:30，9:00和9:30
//
//	“0 0/30 8-9 5,20 *？”
//	请注意，一些调度要求太复杂，无法用单一触发表示 - 例如“每上午9:00至10:00之间每5分钟，下午1:00至晚上10点之间每20分钟”一次。在这种情况下的解决方案是简单地创建两个触发器，并注册它们来运行相同的作业。
//	
	
	
	
	
	
	
	
	
	
	
	
}
