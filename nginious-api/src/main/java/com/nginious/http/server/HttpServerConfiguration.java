/**
 * Copyright 2012 NetDigital Sweden AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.nginious.http.server;

import com.nginious.http.annotation.CommandLine;

/**
 * A HTTP server configuration contains information to configure a HTTP server with the following
 * 
 * <ul>
 * <li>Interfaces - Listen network interfaces for HTTP server. Default is "all".</li>
 * <li>Port - Listen port for HTTP server. Default is "80"</li>
 * <li>Webappsdir - Directory where web applications are deployed. Default is "webapps"</li>
 * <li>AdminPwd - Administration password for application management. Default is "admin"</li>
 * </ul>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class HttpServerConfiguration {
	
	private String interfaces;
	
	private int port;
	
	private String webappsDir;
	
	private String webappDirOrFile;
	
	private String adminPwd;
	
	private String session;
	
	private String accessLogPath;
	
	private String serverLogPath;
	
	/**
	 * Constructs a new HTTP server configuration.
	 */
	public HttpServerConfiguration() {
		super();
		this.interfaces = "all";
		this.port = 80;
		this.webappsDir = "webapps";
		this.webappDirOrFile = null;
		this.adminPwd = "admin";
		this.session = "memory";
		this.accessLogPath = "logs/access.log";
		this.serverLogPath = "logs/server.log";
	}
	
	/**
	 * Returns list of network interfaces that HTTP server should listen to.
	 * 
	 * @return list of interfaces
	 */
	public String getInterfaces() {
		return this.interfaces;
	}
	
	/**
	 * Sets list of network interfaces to the specified list of network interfaces.
	 * 
	 * @param interfaces list of network interfaces
	 */
	@CommandLine(shortName="-i", 
			longName="--interfaces", 
			mandatory=false, 
			description="Comma separated list of network interfaces that server listens to.")
	public void setInterfaces(String interfaces) {
		this.interfaces = interfaces;
	}
	
	/**
	 * Returns listen port.
	 * 
	 * @return the listen port.
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Sets listen port to the specified port.
	 * 
	 * @param port the listen port
	 */
	@CommandLine(shortName="-p", 
			longName="--port", 
			mandatory=false, 
			description="Port that server listents to.")
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Returns webapps dir.
	 * 
	 * @return the webapps dir
	 */
	public String getWebappsDir() {
		return this.webappsDir;
	}
	
	/**
	 * Sets webapps dir to the specified dir.
	 * 
	 * @param webappsDir the webapps dir
	 */
	@CommandLine(shortName="-d", 
			longName="--webappsDir", 
			mandatory=false, 
			description="Directory for web applications.")
	public void setWebappsDir(String webappsDir) {
		this.webappsDir = webappsDir;
	}
	
	/**
	 * Returns webapp dir or file.
	 * 
	 * @return the webapp dir or file.
	 */
	public String getWebappDirOrFile() {
		return this.webappDirOrFile;
	}
	
	/**
	 * Sets webapp dir or file to the specified value.
	 * 
	 * @param webappDirOrFile the webapp dir or file
	 */
	@CommandLine(shortName="-w", 
			longName="--webapp", 
			mandatory=false, 
			description="File or directory for web application.")
	public void setWebappDirOrFile(String webappDirOrFile) {
		this.webappDirOrFile = webappDirOrFile;
	}
	
	/**
	 * Returns admin password.
	 * 
	 * @return the admin password
	 */
	public String getAdminPwd() {
		return this.adminPwd;
	}
	
	/**
	 * Sets admin password to the specified password.
	 * 
	 * @param adminPwd the admin password
	 */
	@CommandLine(shortName="-a", 
			longName="--adminPassword", 
			mandatory=false, 
			description="Administration password.")
	public void setAdminPwd(String adminPwd) {
		this.adminPwd = adminPwd;
	}
	
	/**
	 * Returns session type.
	 * 
	 * @return the session type
	 */
	public String getSession() {
		return this.session;
	}
	
	/**
	 * Sets session type to the specified type.
	 * 
	 * @param session the session type
	 */
	@CommandLine(shortName="-s",
			longName="--session",
			mandatory=false,
			description="Session type (memory|cookie)")
	public void setSession(String session) {
		this.session = session;
	}
	
	/**
	 * Returns the access log path.
	 * 
	 * @return the access log path
	 */
	public String getAccessLogPath() {
		return this.accessLogPath;
	}
	
	/**
	 * Sets the access log path to the specified path.
	 * 
	 * @param accessLogPath the access log path
	 */
	@CommandLine(shortName="-A",
			longName="--accessLog",
			mandatory=false,
			description="Path to access log")
	public void setAccessLogPath(String accessLogPath) {
		this.accessLogPath = accessLogPath;
	}
	
	/**
	 * Returns the server log path
	 * 
	 * @return the server log path
	 */
	public String getServerLogPath() {
		return this.serverLogPath;
	}
	
	/**
	 * Sets the server log path to the specified path.
	 * 
	 * @param serverLogPath the server log path
	 */
	@CommandLine(shortName="-S",
			longName="--serverLog",
			mandatory=false,
			description="Path to server log")
	public void setServerLogPath(String serverLogPath) {
		this.serverLogPath = serverLogPath;
	}
}
