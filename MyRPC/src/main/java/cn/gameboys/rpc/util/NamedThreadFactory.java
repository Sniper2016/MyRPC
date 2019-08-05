package cn.gameboys.rpc.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description: 线程工厂
 * 
 * @author sniper(www.gameboys.cn 1084038709)
 * @date 2019年8月1日
 */
public class NamedThreadFactory implements ThreadFactory {

	protected static final AtomicInteger POOL_SEQ = new AtomicInteger(1);

	protected final AtomicInteger mThreadNum = new AtomicInteger(1);

	protected final String mPrefix;

	protected final boolean mDaemon;

	protected final ThreadGroup mGroup;

	public NamedThreadFactory() {
		this("pool-" + POOL_SEQ.getAndIncrement(), false);
	}

	public NamedThreadFactory(String prefix) {
		this(prefix, false);
	}

	public NamedThreadFactory(String prefix, boolean daemon) {
		mPrefix = prefix + "-thread-";
		mDaemon = daemon;
		SecurityManager s = System.getSecurityManager();
		mGroup = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
	}

	@Override
	public Thread newThread(Runnable runnable) {
		String name = mPrefix + mThreadNum.getAndIncrement();
		Thread ret = new Thread(mGroup, runnable, name, 0);
		// 默认不要弄成守护线程
		ret.setDaemon(mDaemon);
		return ret;
	}

	public ThreadGroup getThreadGroup() {
		return mGroup;
	}
}
