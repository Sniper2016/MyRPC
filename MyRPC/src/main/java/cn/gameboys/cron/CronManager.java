package cn.gameboys.cron;

import java.lang.reflect.Method;
import java.util.Set;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.gameboys.rpc.util.ClassUtils;

/**
 * Description:定时任务管理器，单机版本
 * 
 * @author sniper(www.gameboys.cn 1084038709)
 * @date 2019年8月16日
 */
public class CronManager {

	private static final Logger logger = LoggerFactory.getLogger(CronManager.class);
	private volatile static CronManager cronManager = new CronManager();

	private CronManager() {
	}

	public static CronManager getInstance() {
		return cronManager;
	}

	public void init(String basePackage) {
		try {
			this.autoRegistCronTask(basePackage);
		} catch (InstantiationException | IllegalAccessException | SchedulerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 自动注册实现类
	 * 
	 * @param basePackage
	 * @throws SchedulerException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private void autoRegistCronTask(String basePackage) throws SchedulerException, InstantiationException, IllegalAccessException {
		Set<Class<?>> classes = ClassUtils.getClasses(basePackage);
		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
		for (Class<?> clazz : classes) {
			Method[] methods = clazz.getMethods();
			for (Method m : methods) {
				CronTask cronTask = m.getAnnotation(CronTask.class);
				if (cronTask != null) {
					CronTaskInfo taskInfo = new CronTaskInfo();
					taskInfo.setStop(false);
					taskInfo.setTaskName(clazz.getSimpleName() +"."+ m.getName());
					taskInfo.setCronExpression(cronTask.cronExpression());
					taskInfo.setMethod(m);
					taskInfo.setObj(clazz.newInstance());
					ScheduleUtils.createScheduleJob(scheduler, taskInfo);
				}
			}
		}
		scheduler.start();
	}

}
