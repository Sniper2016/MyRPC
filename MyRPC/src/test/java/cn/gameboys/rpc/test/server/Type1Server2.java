package cn.gameboys.rpc.test.server;

import cn.gameboys.rpc.server.RpcServer;
import cn.gameboys.rpc.server.RpcServerConfig;

public class Type1Server2 {

	public static void main(String[] args) {
		RpcServerConfig cfg = new RpcServerConfig();
		cfg.setBasePackage("cn.gameboys.rpc.test.server.type1");
		cfg.setRegistryAddress("192.168.1.107:2181,192.168.1.107:3181,192.168.1.107:4181");
		// cfg.setRegistryAddress("127.0.0.1:2181");
		cfg.setServerID(2);
		cfg.setType(1);
		RpcServer rpcServer = new RpcServer(cfg);
		try {
			rpcServer.start();
		} catch (Exception ex) {
		}
	}

}
