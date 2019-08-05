package cn.gameboys.rpc.test.client;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gameboys.rpc.client.RpcClient;
import cn.gameboys.rpc.client.async.AsyncRPCCallback;
import cn.gameboys.rpc.registry.ServiceDiscovery;
import cn.gameboys.rpc.test.api.Person;
import cn.gameboys.rpc.test.api.Type1Service;
import cn.gameboys.rpc.test.api.Type2Service;

public class ASyncClientTest {
	
	private static final Logger logger = LoggerFactory.getLogger(ASyncClientTest.class);
	
	public static void main(String[] args) throws Exception {

	
		//ServiceDiscovery serviceDiscovery = new ServiceDiscovery("127.0.0.1:2181");
		
		
		ServiceDiscovery serviceDiscovery = new ServiceDiscovery("192.168.1.107:2181,192.168.1.107:3181,192.168.1.107:4181");
		
		
		
		final RpcClient rpcClient = new RpcClient(serviceDiscovery, "cn.gameboys.rpc.test.client");

		int thread1Num = 1;
		int thread2Num = 1;
		int requestNum = 1000;
		Thread[] threads1 = new Thread[thread1Num];
		Thread[] threads2 = new Thread[thread2Num];

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < thread1Num; ++i) {
			threads1[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					// 执行type==1
					for (int i = 0; i < requestNum; i++) {
						List<Person> list = null;
						try {
							Type1Service client = rpcClient.create(1, 0, Type1Service.class);
							list = client.getTestPerson("sniper", 20);

							Type1Service asyncClient = rpcClient.createAsync(1, 0, Type1Service.class);
							asyncClient.mapTest(list);

						} catch (Exception e) {
							System.out.println(Thread.currentThread().getName() + " " + e);
						}

						try {
							Thread.currentThread().sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
			threads1[i].start();
		}

		for (int i = 0; i < thread2Num; ++i) {
			threads2[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					// 执行type==2
					for (int i = 0; i < requestNum; i++) {
						List<Person> list = null;
						try {
							Type2Service asyncClient = rpcClient.createAsync(2, 0, Type2Service.class);
							asyncClient.getTestPerson("sniper", 20);
						} catch (Exception e) {
							System.out.println(Thread.currentThread().getName() + " " + e);
						}
						try {
							Thread.currentThread().sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
			threads2[i].start();
		}

//		for (int i = 0; i < threads.length; i++) {
//			threads[i].join();
//		}

		long endTime = System.currentTimeMillis();
		//System.out.println(endTime - startTime);

		while (true) {

		}

		// rpcClient.stop();

	}

	@AsyncRPCCallback(value = "mapTest")
	public void hehe(boolean isError, Object[] parameters, Object result) {
//		System.out.println(isError);
//		for (Object object : parameters) {
//			System.out.println(object);
//		}
//		System.out.println(result);
		
		logger.info("@@@@@@@@hehe---" + Thread.currentThread().getName() + " " + result);
	}

	@AsyncRPCCallback(value = "getTestPerson")
	public void haha(boolean isError, Object[] parameters, Object result) {
		logger.info("@@@@@@@@haha---" + Thread.currentThread().getName() + " " + result);

	}

}
