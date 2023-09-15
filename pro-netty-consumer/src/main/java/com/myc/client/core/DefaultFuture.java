package com.myc.client.core;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.myc.client.param.ClientRequest;
import com.myc.client.param.Response;

public class DefaultFuture {

	public static ConcurrentHashMap<Long, DefaultFuture> allDefaultFuture = new ConcurrentHashMap<>();
	
	final Lock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();
	private Response response;
	private long timeout = 2*60*1000;//请求时间
	private long startTime = System.currentTimeMillis();//开始时间
	
	
	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public long getStartTime() {
		return startTime;
	}

	public DefaultFuture(ClientRequest request) {
		allDefaultFuture.put(request.getId(),this);
	}

	//主线程获取数据，首先等待结果
	public Response get() {
		lock.lock();
		try {
			while(!done()) {
				condition.await();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			lock.unlock();
		}
		
		return this.response;
	}
	
	public Response get(long time) {
		lock.lock();
		try {
			while(!done()) {
				condition.await(time,TimeUnit.SECONDS);
				//若等待时间大约这个时间则代表超时
				if((System.currentTimeMillis()-startTime) > time) {
					System.out.println("请求超时！");
					break;
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			lock.unlock();
		}
		
		return this.response;
	}
	
	public static void recive(Response response) {
		DefaultFuture df = allDefaultFuture.get(response.getId());
		if(df != null) {
			Lock lock = df.lock;
			lock.lock();
			try {
				df.setResponse(response);
				df.condition.signal();
				allDefaultFuture.remove(df);
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				lock.unlock();
			}
			
		}
	}

		public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	
	private boolean done() {
		// TODO Auto-generated method stub
		if(this.response != null) {
			return true;
		}
		return false;
	}

	//遍历allDefaultFuture需要重开一个线程
	static class FutureThread extends Thread{

		@Override
		public void run() {
			Set<Long> ids = allDefaultFuture.keySet();
			for(Long id : ids) {
				DefaultFuture df = allDefaultFuture.get(id);
				if(df == null) {
					allDefaultFuture.remove(df);
				}else {
					//加入链路超时
					if(df.getTimeout() < (System.currentTimeMillis() - df.getStartTime())) {
						Response resp = new Response();
					    resp.setId(id);
					    resp.setCode("333333");
					    resp.setMsg("链路请求超时");
					    recive(resp);
					}
				}
			}
		}
		
		static {
			FutureThread futureThread = new FutureThread();
			//设置为后台守护线程
			futureThread.setDaemon(true);
			futureThread.start();
		}
		
	}

}
