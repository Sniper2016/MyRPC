package cn.gameboys.rpc.test.util;

import java.util.concurrent.ThreadLocalRandom;

import cn.gameboys.util.NetWorkUtil;

/**
 * Description:
 * 
 * @author sniper(www.gameboys.cn 1084038709)
 * @date 2019年8月1日
 */
public class Test {

	public static void main(String[] args) throws Exception {

		System.out.println(NetWorkUtil.getIntranetIp());
		 System.out.println(NetWorkUtil.getOuterNetIp());

		int type = 200;
		int serverID = 3300;
		long key = Test.getKey(type, serverID);
		System.out.println(key);
		System.out.println(Test.getType(key));
		System.out.println(Test.getServerID(key));

		
		
	

	}
	
	public static void testRandom() {

		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int i = 0; i < 100; ++i) {
			System.out.println(random.nextInt(5));
		}
	}
	
	

	public static int getType(Long key) {
		return (int) (key >> 32);
	}

	public static int getServerID(Long key) {
		return (int) (key << 32 >> 32);
	}

	public static Long getKey(int type, int serverID) {
		return (((long) type) << 32) + serverID;
	}

}
