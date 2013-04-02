package com.nginious.http.service;

import java.util.concurrent.atomic.AtomicInteger;

import com.nginious.http.annotation.Service;

@Service(name = "test")
public class TestService implements Runnable {
	
	private AtomicInteger value;
	
	public TestService() {
		super();
		this.value = new AtomicInteger(0);
	}
	
	public int getValue() {
		return value.get();
	}
	
	public void run() {
		Thread serviceThread = Thread.currentThread();
		boolean stopped = false;
		
		while(!stopped) {
			value.incrementAndGet();
			stopped = serviceThread.isInterrupted();
			
			if(!stopped) {
				try {
					Thread.sleep(1000L);
				} catch(InterruptedException e) {
					stopped = true;
				}
			}
		}
	}
}
