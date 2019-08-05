package cn.gameboys.rpc.registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gameboys.rpc.client.ConnectManage;
import cn.gameboys.rpc.expection.RpcExpection;

/**
 * 
* Description: 服务发现
* @author sniper(www.gameboys.cn 1084038709) 
* @date 2019年8月5日
 */
public class ServiceDiscovery {

	private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);
	private CountDownLatch latch = new CountDownLatch(1);
	private volatile List<ServerConnectInfo> dataList = new ArrayList<ServerConnectInfo>();
	
	private String registryAddress;
	private ZooKeeper zookeeper;

	public ServiceDiscovery(String registryAddress) {
		this.registryAddress = registryAddress;
		zookeeper = connectServer();
		if (zookeeper != null) {
			watchNode(zookeeper);
		}
	}
	
	private ZooKeeper connectServer() {
		ZooKeeper zk = null;
		try {
			zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					if (event.getState() == Event.KeeperState.SyncConnected) {
						latch.countDown();
					}
				}
			});
			latch.await();
		} catch (IOException | InterruptedException e) {
			logger.error("", e);
		}
		return zk;
	}

	private void watchNode(final ZooKeeper zk) {
		try {
			List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					if (event.getType() == Event.EventType.NodeChildrenChanged) {
						watchNode(zk);
					}
				}
			});
			List<ServerConnectInfo> dataList = new ArrayList<>();
			for (String node : nodeList) {
				byte[] bytes = zk.getData(Constant.ZK_REGISTRY_PATH + "/" + node, false, null);
				String strNode = new String(bytes);
				//【typeID|serverID|serverIP|serverPort】
				String[] arr = strNode.split("\\|");
				dataList.add(new ServerConnectInfo(Integer.valueOf(arr[0]),Integer.valueOf(arr[1]),arr[2],Integer.valueOf(arr[3])));
			}
			logger.debug("node data: {}", dataList);
			this.dataList = dataList;
			logger.debug("Service discovery triggered updating connected server node.");
			onServerStatusChanged();
		} catch (KeeperException | InterruptedException e) {
			logger.error("", e);
		}
	}

	/**
	 * 连接状态改变
	 */
	private void onServerStatusChanged() {
		try {
			ConnectManage.getInstance().onServerStatusChanged(this.dataList);
		} catch (RpcExpection e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		if (zookeeper != null) {
			try {
				zookeeper.close();
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
}
