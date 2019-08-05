package cn.gameboys.rpc.server;

/**
 * Description:
 * 
 * @author sniper(www.gameboys.cn 1084038709)
 * @date 2019年8月1日
 */
public class RpcServerConfig {

	/**
	 * 提供服务的基本包地址，如：com.nettyrpc
	 */
	private String basePackage;
	/**
	 * 服务类型【1：a类型，2：b类型】
	 */
	private int type;
	/**
	 * 服务id
	 */
	private int serverID;
	/**
	 * 注册中心地址 如：192.168.1.107:2181
	 */
	private String registryAddress;

	public String getBasePackage() {
		return basePackage;
	}

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getServerID() {
		return serverID;
	}

	public void setServerID(int serverID) {
		this.serverID = serverID;
	}

	public String getRegistryAddress() {
		return registryAddress;
	}

	public void setRegistryAddress(String registryAddress) {
		this.registryAddress = registryAddress;
	}

	@Override
	public String toString() {
		return "RpcServerConfig [basePackage=" + basePackage + ", type=" + type + ", serverID=" + serverID
				+ ", registryAddress=" + registryAddress + "]";
	}

}
