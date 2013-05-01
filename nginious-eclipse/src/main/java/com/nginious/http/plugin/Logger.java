package com.nginious.http.plugin;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class Logger {
	
	private PrintWriter writer;
	
	private SimpleDateFormat format;
	
	Logger(String fileName) {
		try {
			FileOutputStream out = new FileOutputStream(fileName);
			this.writer = new PrintWriter(out);
			this.format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		} catch(IOException e) {}
	}
	
	void log(String message, Throwable t) {
		String threadName = Thread.currentThread().getName();
		String log = format.format(new Date()) + " [" + threadName + "] " + message;
		writer.println(log);
		t.printStackTrace(this.writer);
		writer.flush();
	}
	
	void log(String message) {
		String threadName = Thread.currentThread().getName();
		String log = format.format(new Date()) + " [" + threadName + "] " + message;
		writer.println(log);
		writer.flush();
	}
	
	void log(String message, Object param1) {
		Object[] params = { param1 };
		log(message, params);
	}
	
	void log(String message, Object[] params) {
		String threadName = Thread.currentThread().getName();
		String log = MessageFormat.format(format.format(new Date()) + " [" + threadName + "] " + message, params);
		writer.println(log);
		writer.flush();
	}
}
