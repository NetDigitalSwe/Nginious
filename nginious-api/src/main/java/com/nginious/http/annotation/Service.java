package com.nginious.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A class marked with this annotation is run as a background service in a web application. Below is an example
 * service which also implements the java.lang.Runnable interface.
 * 
 * <pre>
 * &#64;Service(name = "counter")
 * public class CounterService implements Runnable {
 *   
 *   private AtomicInteger counter;
 *   
 *   public TestService() {
 *     this.counter = new AtomicInteger(0);
 *   }
 *   
 *   public int getCounter() {
 *     return value.get();
 *   }
 *   
 *   public void run() {
 *		Thread serviceThread = Thread.currentThread();
 *		boolean stopped = false;
 *		
 *		while(!stopped) {
 *			counter.incrementAndGet();
 *			stopped = serviceThread.isInterrupted();
 *			
 *			if(!stopped) {
 *				try {
 *					Thread.sleep(1000L);
 *				} catch(InterruptedException e) {
 *					stopped = true;
 *				}
 *			}
 *		}
 *   }
 * }
 * </pre>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
	
	/**
	 * Service name. 
	 * 
	 * @return the service name
	 */
	String name() default "";
}
