package cn.gameboys.rpc.expection;

/**
 * 
 * Description:
 * 
 * @author sniper(www.gameboys.cn 1084038709)
 * @date 2019年8月5日
 */
public class RpcExpection extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5181877010639469882L;

	RpcErrorEnum errorInfo;
	Object extInfo;

	public RpcExpection(RpcErrorEnum errorInfo, Object extInfo) {
		super("ErrorCode：" + errorInfo.getCode() + " msg:" + errorInfo.getMsg() + " extInfo:" + extInfo);
		this.errorInfo = errorInfo;
		this.extInfo = extInfo;
	}

	
	
	
	
	@Override
	public String toString() {
		return "ErrorCode：" + errorInfo.getCode() + " msg:" + errorInfo.getMsg() + " extInfo:" + extInfo;
	}





	public RpcExpection(Exception e) {
		super(e);
	}

	@Override
	public Throwable fillInStackTrace() {
		return this;
	}

}
