package cn.gameboys.rpc.expection;
/**
 * 
* Description: 
* @author sniper(www.gameboys.cn 1084038709) 
* @date 2019年8月5日
 */
public enum RpcErrorEnum {

	CONNECT_TIME_OUT(-1000, "连接超时"), 
	INIT_ERROR(-1001, "初始化失败"),
	NO_TYPE_SERVER(-1002, "没有指定类型的rpc服务"),
	NO_RPC_SERVER(-1003, "无rpc服务"),
	SYNC_TIME_OUT(-1004, "同步请求超时"),
	EN_DE_CODE_ERROR(-1005, "编解码异常"),
	NO_METHOD_SERVICE(-1006, "该方法在rpc服务没有对应的方法"),
	REQ_ERROR(-1007, "rpc请求错误")
	
	;

	private int code;
	private String msg;

	RpcErrorEnum(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}

	
	
}
