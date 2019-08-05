package cn.gameboys.rpc.client;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gameboys.rpc.client.async.MethodObjectInfo;
import cn.gameboys.rpc.expection.RpcErrorEnum;
import cn.gameboys.rpc.expection.RpcExpection;
import cn.gameboys.rpc.protocol.RpcRequest;
import cn.gameboys.rpc.protocol.RpcResponse;

/**
 * 
 * Description:rpc请求回调信息
 * 
 * @author sniper(www.gameboys.cn 1084038709)
 * @date 2019年8月5日
 */
public class RPCFuture implements Future<Object> {
	private static final Logger logger = LoggerFactory.getLogger(RPCFuture.class);

	private Sync sync;
	private RpcRequest request;
	private RpcResponse response;
	private long startTime;
	private long responseTimeThreshold = 5000;
	private RpcClientRspExcuteable excuter;
	// 十分是异步请求
	private boolean asyncReq;

//	private List<AsyncRPCCallback> pendingCallbacks = new ArrayList<AsyncRPCCallback>();
//	private ReentrantLock lock = new ReentrantLock();

	public RPCFuture(RpcRequest request, RpcClientRspExcuteable excuter, boolean asyncReq) {
		this.sync = new Sync();
		this.request = request;
		this.startTime = System.currentTimeMillis();
		this.excuter = excuter;
		this.asyncReq = asyncReq;
	}

	@Override
	public boolean isDone() {
		return sync.isDone();
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		sync.acquire(-1);
		if (this.response != null) {
			return this.response.getResult();
		} else {
			return null;
		}
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
		if (success) {
			if (this.response != null) {

				if (this.response.isError()) {
					throw new RpcExpection(RpcErrorEnum.REQ_ERROR, this.response.getResult());
				}
				return this.response.getResult();

			} else {
				return null;
			}
		} else {
			String info = "【className:" + this.request.getClassName() + " method:" + this.request.getMethodName() + " args:" + Arrays.toString(this.request.getParameters()) + "】";
			throw new RpcExpection(RpcErrorEnum.SYNC_TIME_OUT, info);
		}
	}

	@Override
	public boolean isCancelled() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		throw new UnsupportedOperationException();
	}

	public void done(RpcResponse reponse) {
		this.response = reponse;
		sync.release(1);
		// 异步请求
		if (this.asyncReq) {
			excuter.execute(new Runnable() {
				@Override
				public void run() {
					List<MethodObjectInfo> list = RpcClient.getMethodObjectInfo(request.getMethodName());
					if (list != null) {
						try {
							for (MethodObjectInfo moInfo : list) {
								moInfo.getMethod().invoke(moInfo.getObj(), reponse.isError(), request.getParameters(), response.getResult());
							}
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
		// invokeCallbacks();
		// Threshold
		long responseTime = System.currentTimeMillis() - startTime;
		if (responseTime > this.responseTimeThreshold) {
			logger.warn("Service response time is too slow. Request id = " + reponse.getRequestId() + ". Response Time = " + responseTime + "ms");
		}
	}

	static class Sync extends AbstractQueuedSynchronizer {
		private static final long serialVersionUID = 1L;
		// future status
		private final int done = 1;// 完成
		private final int pending = 0;// 待定

		@Override
		protected boolean tryAcquire(int arg) {
			return getState() == done;
		}

		@Override
		protected boolean tryRelease(int arg) {
			if (getState() == pending) {
				if (compareAndSetState(pending, done)) {
					return true;
				} else {
					return false;
				}
			} else {
				return true;
			}
		}

		public boolean isDone() {
			getState();
			return getState() == done;
		}
	}
}
