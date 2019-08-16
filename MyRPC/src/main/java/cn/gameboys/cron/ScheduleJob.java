package cn.gameboys.cron;

import java.lang.reflect.InvocationTargetException;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Description:
 * 
 * @author sniper(www.gameboys.cn 1084038709)
 * @date 2019年8月16日
 */
public class ScheduleJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			JobDetail jobDetail = context.getJobDetail();
			CronTaskInfo info = (CronTaskInfo) jobDetail.getJobDataMap().get(ScheduleUtils.JOB_PARAM_KEY);
			info.getMethod().invoke(info.getObj());
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
