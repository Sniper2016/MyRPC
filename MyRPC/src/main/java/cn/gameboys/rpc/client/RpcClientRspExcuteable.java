package cn.gameboys.rpc.client;

/**
 * 
 * Description: rpcServer响应之后从io线程间任务抛给逻辑线程的处理器
 * 
 * @author sniper(www.gameboys.cn 1084038709)
 * @date 2019年8月5日
 */
public interface RpcClientRspExcuteable {

	void execute(Runnable task);

}
