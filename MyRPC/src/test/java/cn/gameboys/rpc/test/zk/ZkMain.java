package cn.gameboys.rpc.test.zk;

public class ZkMain {

	
	
	
	/**
	 * 由于这是内嵌的建议zk，所以重启的话数据会被清理掉，所以集群测试不能用这个来测试，需要使用专门的zk服务
	 * @param args
	 */
	public static void main(String[] args) {
		new EmbeddedZooKeeper(2181, false).start();
	}

}
