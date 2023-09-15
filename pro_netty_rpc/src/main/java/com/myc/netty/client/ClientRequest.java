package com.myc.netty.client;

import java.util.concurrent.atomic.AtomicLong;

public class ClientRequest {

	private final long id;
	private Object content;
	private String command;//某个类的某个方法
	private final AtomicLong aid = new AtomicLong(1);
	
	public ClientRequest() {
		id = aid.incrementAndGet();
	}
	
	
	public String getCommand() {
		return command;
	}


	public void setCommand(String command) {
		this.command = command;
	}


	public long getId() {
		return id;
	}


	public Object getContent() {
		return content;
	}
	public void setContent(Object content) {
		this.content = content;
	}


	
}
