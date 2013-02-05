package com.nginious.http.plugin;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;

class Logger {
	
	private PrintWriter writer;
	
	Logger(String fileName) {
		try {
			FileOutputStream out = new FileOutputStream(fileName);
			this.writer = new PrintWriter(out);
		} catch(IOException e) {}
	}
	
	void log(String message, Throwable t) {
		String threadName = Thread.currentThread().getName();
		String log = "[" + threadName + "] " + message;
		writer.println(log);
		t.printStackTrace(this.writer);
		writer.flush();
	}
	
	void log(String message) {
		String threadName = Thread.currentThread().getName();
		String log = "[" + threadName + "] " + message;
		writer.println(log);
		writer.flush();
	}
	
	void log(String message, Object param1) {
		Object[] params = { param1 };
		log(message, params);
	}
	
	void log(String message, Object[] params) {
		String threadName = Thread.currentThread().getName();
		String log = MessageFormat.format("[" + threadName + "] " + message, params);
		writer.println(log);
		writer.flush();
	}
}
