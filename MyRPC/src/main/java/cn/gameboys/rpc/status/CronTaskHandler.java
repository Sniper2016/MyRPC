package cn.gameboys.rpc.status;

import cn.gameboys.cron.CronTask;

public class CronTaskHandler {
	
	// 20s一次
	@CronTask(cronExpression = "0/20 * * * * ?")
	public void cronWriteLog() {
		System.out.println("@@@@@@@@@@@@20s一次         rpc各接口调用次数： "+StatusManager.getInstance().getRpcReqTotalTimesStatus());
	}
	
	
	//10s一次
	@CronTask(cronExpression = "0/10 * * * * ?")
	public void cronWriteLog2() {
		System.out.println("@@@@@@@@@@@@10s一次       rpc各接口使用时间： "+StatusManager.getInstance().getRpcUseTimeStatus());
	}
	
	
	//1分钟一次
	@CronTask(cronExpression = "0 0/1 * * * ?")
	public void cronWriteLog3() {
		System.out.println("@@@@@@@@@@@@@60s一次       rpc各接口每分钟调用次数： "+StatusManager.getInstance().getRpcReqMinuteTimesStatus());
		StatusManager.getInstance().clearMinuteApiTimes();
	}
	
}
