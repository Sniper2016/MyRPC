package cn.gameboys.cron;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

/**
 * Description:
 * 
 * @author sniper(www.gameboys.cn 1084038709)
 * @date 2019年8月16日
 */
public class ScheduleUtils {
	public final static String JOB_PARAM_KEY = "jobParamKey";

	/**
	 * 获取表达式触发器
	 */
	public static CronTrigger getCronTrigger(Scheduler scheduler, String taskName) {
		try {
			return (CronTrigger) scheduler.getTrigger(TriggerKey.triggerKey(taskName));
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 创建定时任务
	 */
	public static void createScheduleJob(Scheduler scheduler, CronTaskInfo taskInfo) {
		try {
			// 构建job信息
			JobDetail jobDetail = JobBuilder.newJob(ScheduleJob.class).withIdentity(JobKey.jobKey(taskInfo.getTaskName())).build();
			// 表达式调度构建器
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(taskInfo.getCronExpression()).withMisfireHandlingInstructionDoNothing();

			// 按新的cronExpression表达式构建一个新的trigger
			CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(TriggerKey.triggerKey(taskInfo.getTaskName())).withSchedule(scheduleBuilder).build();

			// 放入参数，运行时的方法可以获取
			jobDetail.getJobDataMap().put(JOB_PARAM_KEY, taskInfo);

			scheduler.scheduleJob(jobDetail, trigger);

			// 暂停任务
			if (taskInfo.isStop()) {
				pauseJob(scheduler, taskInfo.getTaskName());
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新定时任务
	 */
	public static void updateScheduleJob(Scheduler scheduler, CronTaskInfo taskInfo) {
		try {
			TriggerKey triggerKey = TriggerKey.triggerKey(taskInfo.getTaskName());
			// 表达式调度构建器
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(taskInfo.getCronExpression()).withMisfireHandlingInstructionDoNothing();

			CronTrigger trigger = getCronTrigger(scheduler, taskInfo.getTaskName());

			// 按新的cronExpression表达式重新构建trigger
			trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

			// 参数
			trigger.getJobDataMap().put(JOB_PARAM_KEY, taskInfo);

			scheduler.rescheduleJob(triggerKey, trigger);

			// 暂停任务
			if (taskInfo.isStop()) {
				pauseJob(scheduler, taskInfo.getTaskName());
			}

		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 立即执行任务
	 */
	public static void run(Scheduler scheduler, CronTaskInfo taskInfo) {
		try {
			// 参数
			JobDataMap dataMap = new JobDataMap();
			dataMap.put(JOB_PARAM_KEY, taskInfo);
			scheduler.triggerJob(JobKey.jobKey(taskInfo.getTaskName()), dataMap);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 暂停任务
	 */
	public static void pauseJob(Scheduler scheduler, String taskName) {
		try {
			scheduler.pauseJob(JobKey.jobKey(taskName));
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 恢复任务
	 */
	public static void resumeJob(Scheduler scheduler, String taskName) {
		try {
			scheduler.resumeJob(JobKey.jobKey(taskName));
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除定时任务
	 */
	public static void deleteScheduleJob(Scheduler scheduler, String taskName) {
		try {
			scheduler.deleteJob(JobKey.jobKey(taskName));
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
}
