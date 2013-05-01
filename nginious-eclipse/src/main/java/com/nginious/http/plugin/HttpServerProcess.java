package com.nginious.http.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

class HttpServerProcess {
	
	private Process process;
	
	private String projectName;
	
	private int listenPort;
	
	private int minMemory;
	
	private int maxMemory;
	
	private String adminPassword;
	
	private File webappPath;
	
	private String accessLogPath;
	
	private String serverLogPath;
	
	private Logger logger;
	
	HttpServerProcess(String projectName, int listenPort, String adminPassword, 
			int minMemory, int maxMemory, File webappPath, Logger logger) {
		super();
		this.projectName = projectName;
		this.listenPort = listenPort;
		this.minMemory = minMemory;
		this.maxMemory = maxMemory;
		this.adminPassword = adminPassword;
		this.webappPath = webappPath;
		this.accessLogPath = buildAccessLogPath();
		this.serverLogPath = buildServerLogPath();
		this.logger = logger;
	}
	
	String getAccessLogPath() {
		return this.accessLogPath;
	}
	
	String getServerLogPath() {
		return this.serverLogPath;
	}
	
	void start() throws IOException {
		String classPath = buildClasspath();
		String javaRuntime = buildJavaRuntime();
		String accessLog = this.accessLogPath;
		String serverLog = this.serverLogPath;
		String minMemoryArg = "-Xms" + this.minMemory + "m";
		String maxMemoryArg = "-Xmx" + this.maxMemory + "m";
		String[] cmd = { javaRuntime, 
				minMemoryArg, 
				maxMemoryArg, 
				"-cp", classPath,
				"com.nginious.http.server.Main",
				"-p", Integer.toString(this.listenPort), 
				"-a", this.adminPassword,
				"-w", this.webappPath.getAbsolutePath(),
				"-S", serverLog, "-A", accessLog };
		Object[] params = { javaRuntime, classPath, this.webappPath.getAbsolutePath(), accessLog, serverLog };
		logger.log("HttpServerProcess.start javaRuntime={0}, classpath={1}, webappPath={2}, accessLog={3}, serverLog={4}", params);
		this.process = Runtime.getRuntime().exec(cmd);
	}
	
	void stop() {
		logger.log("HttpServerProcess.stop");
		
		if(this.process != null) {
			process.destroy();
			
			try {
				process.waitFor();
			} catch(InterruptedException e) {}
		}
		
		this.process = null;
	}
	
	private String buildAccessLogPath() {
		return buildLogPath("access");
	}
	
	private String buildServerLogPath() {
		return buildLogPath("server");
	}
	
	private String buildLogPath(String type) {
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		StringBuffer path = new StringBuffer(tmpDirPath);
		path.append(File.separator);
		path.append(this.projectName);
		path.append("_");
		path.append(type);
		path.append(".log");
		return path.toString();		
	}
	
	private String buildJavaRuntime() {
		String javaHome = System.getProperty("java.home");
		File javaRuntime = new File(javaHome, "bin/java");
		return javaRuntime.getAbsolutePath();
	}
		
	private String buildClasspath() throws IOException {
		StringBuffer classPath = new StringBuffer();
		
		URL serverURL = NginiousPlugin.getJar("nginious-server.jar");
		String serverElement = createClasspathElement(serverURL);
		classPath.append(serverElement);
		
		URL apiURL = NginiousPlugin.getJar("nginious-api.jar");
		String apiElement = createClasspathElement(apiURL);
		classPath.append(File.pathSeparator);
		classPath.append(apiElement);
		
		URL jsonURL = NginiousPlugin.getJar("json.jar");
		String jsonElement = createClasspathElement(jsonURL);
		classPath.append(File.pathSeparator);
		classPath.append(jsonElement);
		
		URL asmURL = NginiousPlugin.getJar("asm-3.3.1.jar");
		String asmElement = createClasspathElement(asmURL);
		classPath.append(File.pathSeparator);
		classPath.append(asmElement);
		
		URL log4jURL = NginiousPlugin.getJar("log4j-1.2.17.jar");
		String log4jElement = createClasspathElement(log4jURL);
		classPath.append(File.pathSeparator);
		classPath.append(log4jElement);
		
		return classPath.toString();
	}
	
	private String createClasspathElement(URL jarElement) {
		try {
		  return new File(jarElement.toURI()).getAbsolutePath();
		} catch(URISyntaxException e) {
		  return new File(jarElement.getPath()).getAbsolutePath();
		}
		
	}
	
}
