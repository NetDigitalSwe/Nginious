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

import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import com.nginious.http.application.ApplicationManagerImpl;

public class HttpServerFactoryImpl extends HttpServerFactory {

	/**
	 * Creates a new HTTP server from the specified HTTP server configuration.
	 * 
	 * @param configuration the HTTP server configuration.
	 * @return the created HTTP server
	 */
	public HttpServer create(HttpServerConfiguration configuration) {
		Properties log4jProperties = new Properties();
		log4jProperties.put("log4j.appender.server", "org.apache.log4j.DailyRollingFileAppender");
		log4jProperties.put("log4j.appender.server.DatePattern", "yyyy-MM-dd");
		log4jProperties.put("log4j.appender.server.File", configuration.getServerLogPath());
		log4jProperties.put("log4j.appender.server.append", "true");
		log4jProperties.put("log4j.appender.server.threshold", "INFO");
		log4jProperties.put("log4j.appender.server.layout", "org.apache.log4j.PatternLayout");
		log4jProperties.put("log4j.appender.server.layout.ConversionPattern", "%d %-5p [%t] %c{1} - %m%n");
		log4jProperties.put("log4j.logger.com.nginious", "INFO, server");
		PropertyConfigurator.configure(log4jProperties);
		
		
		HttpServerImpl server = new HttpServerImpl(configuration);
		ApplicationManagerImpl manager = new ApplicationManagerImpl(configuration);
		server.setApplicationManager(manager);
		return server;
	}	
}
