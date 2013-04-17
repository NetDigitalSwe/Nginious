package com.nginious.http.application;

import java.io.File;

import org.apache.log4j.Logger;

import com.nginious.http.HttpException;
import com.nginious.http.HttpStatus;
import com.nginious.http.annotation.Service;

class ServiceRunner {
	
	private static Logger logger = Logger.getLogger(ServiceRunner.class);
	
	private ApplicationClassLoader classLoader;
	
	private File classFile;
	
	private String className;
	
	private long lastModified;
	
	private Object service;
	
	private Thread serviceThread;
	
	private boolean runnable;
	
	private HttpException exception;
	
	ServiceRunner(ApplicationClassLoader classLoader, File classFile, Object service) {
		this.classLoader = classLoader;
		this.classFile = classFile;
		
		if(classFile != null) {
			this.lastModified = classFile.lastModified();
		}
		
		this.service = service;
		Class<?> clazz = service.getClass();
		this.className = clazz.getName();
		this.runnable = Runnable.class.isAssignableFrom(clazz);
	}
	
	Object getService() {
		if(classFile != null && !classFile.exists()) {
			stop();
			return null;
		}
		
		if(!classLoader.hasLoaded(service.getClass()) || (classFile != null && classFile.lastModified() > this.lastModified)) {
			loadServiceClass();
		}
		
		return this.service;
	}
	
	void start() {
		if(this.runnable) {
			Service mapping = service.getClass().getAnnotation(Service.class);
			Runnable runnable = (Runnable)service;
			ServiceRunnable serviceRunnable = new ServiceRunnable(runnable);
			this.serviceThread = new Thread(serviceRunnable);
			serviceThread.setName("runner-" + mapping.name());
			serviceThread.start();
		}
	}
	
	Object stop() {
		if(this.runnable && this.serviceThread != null) {
			serviceThread.interrupt();
			
			try {
				serviceThread.join(5000L);
			} catch(InterruptedException e) {}
			
			this.serviceThread = null;
		}
		
		return this.service;
	}
	
	private void loadServiceClass() {
		synchronized(this) {
			if(this.exception != null) {
				return;
			}
			
			try {
				stop();
				this.service = null;
				Class<?> clazz = classLoader.loadClass(this.className);
				this.service = clazz.newInstance();
				this.lastModified = classFile.lastModified();
				start();
			} catch(ClassNotFoundException e) {
				this.exception = new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to load controller class");
				this.service = null;
			} catch(IllegalAccessException e) {
				this.exception = new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to load controller class");
				this.service = null;
			} catch(InstantiationException e) {
				this.exception = new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to load controller class");
				this.service = null;
			}
		}
	}
	
	private class ServiceRunnable implements Runnable {
		
		private Runnable runnable;
		
		private ServiceRunnable(Runnable runnable) {
			this.runnable = runnable;
		}
		
		public void run() {
			try {
				runnable.run();
			} catch(Exception e) {
				logger.warn("Failed to run service", e);
			}
		}
	}
}
