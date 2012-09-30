package com.nginious.http.server;

import java.io.IOException;

public class ShutdownHook implements Runnable {

	private HttpServerImpl server;
	
	ShutdownHook(HttpServerImpl server) {
		this.server = server;
	}
	
	public void run() {
		try {
			server.stop();
		} catch(IOException e) {
			e.printStackTrace(System.err);
		}
	}
}
