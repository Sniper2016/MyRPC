package cn.gameboys.rpc.registry;
/**
 * 
* Description: 服务节点信息
* @author sniper(www.gameboys.cn 1084038709) 
* @date 2019年8月5日
 */
public class ServerConnectInfo {
	private int type;
	private int serverID;
	private String ip;
	private int port;

	public ServerConnectInfo() {
		super();
	}



	public ServerConnectInfo(int type, int serverID, String ip, int port) {
		super();
		this.type = type;
		this.serverID = serverID;
		this.ip = ip;
		this.port = port;
	}



	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
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

	
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + port;
		result = prime * result + serverID;
		result = prime * result + type;
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServerConnectInfo other = (ServerConnectInfo) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (port != other.port)
			return false;
		if (serverID != other.serverID)
			return false;
		if (type != other.type)
			return false;
		return true;
	}



	@Override
	public String toString() {
		return "{ip=" + ip + ", port=" + port + ", type=" + type + ", serverID=" + serverID + "},";
	}

}
