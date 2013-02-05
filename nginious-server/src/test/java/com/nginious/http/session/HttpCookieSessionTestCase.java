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

package com.nginious.http.session;

import java.util.Random;

import com.nginious.http.HttpSession;
import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationManager;
import com.nginious.http.server.FileLogConsumer;
import com.nginious.http.server.HttpServer;
import com.nginious.http.server.HttpServerConfiguration;
import com.nginious.http.server.HttpServerFactory;
import com.nginious.http.server.HttpTestConnection;
import com.nginious.http.server.HttpTestRequest;
import com.nginious.http.service.TestSessionController;
import com.nginious.http.session.HttpCookieSessionDeserializer;
import com.nginious.http.session.HttpCookieSessionManager;
import com.nginious.http.session.HttpCookieSessionSerializer;
import com.nginious.http.session.HttpSessionConstants;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class HttpCookieSessionTestCase extends TestCase {
	
    private static final String chars = "abcdefghijklmnopqrstuvxyzABCDEFGHIJKLMNOPQRSTUVXYZ0123456789";
    
    private Random rnd;
    
	private HttpServer server;
	
	public HttpCookieSessionTestCase(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        this.rnd = new Random();
		HttpServerConfiguration config = new HttpServerConfiguration();
		config.setWebappsDir(null);
		config.setSession("cookie");
		config.setPort(9000);
		HttpServerFactory factory = HttpServerFactory.getInstance();
		this.server = factory.create(config);
		server.setAccessLogConsumer(new FileLogConsumer("build/test-access"));
		server.setMessageLogConsumer(new FileLogConsumer("build/test-server"));
		ApplicationManager manager = server.getApplicationManager();
		Application application = manager.createApplication("test");
		application.addController(new TestSessionController());
		manager.publish(application);
		server.start();
   }

    public void tearDown() throws Exception {
    	super.tearDown();
    	
		if(this.server != null) {
			server.stop();
		}
    }
    
    private String nextString(int length) {
    	StringBuffer str = new StringBuffer();

        for (int i = 0; i < length; i++) {
            int pos = rnd.nextInt(chars.length());
            str.append(chars.charAt(pos));
        }

        return str.toString();
    }
    
    public void testHttpSession1() throws Exception {
    	HttpCookieSessionManager manager = new HttpCookieSessionManager();
    	HttpTestRequest request = new HttpTestRequest();
    	
    	for(int i = 0; i < 20000; i += 500) {
    		String value = nextString(i);
    		HttpSession session = manager.getSession(request, true);
    		session.setAttribute("Test", value);
    		String encoded = HttpCookieSessionSerializer.serialize(session);
    		
    		HttpSession session2 = HttpCookieSessionDeserializer.deserialize(encoded);
    		assertNotNull(session2);
    		
    		String value2 = (String)session.getAttribute("Test");
    		assertEquals(value, value2);
    		assertEquals(session.getCreationTime(), session2.getCreationTime());
    		// First session has later or equal last access time since attribute Test is accessed
    		assertTrue(session.getLastAccessedTime() >= session2.getLastAccessedTime());
    	}
    }
    
	public void testHttpSession2() throws Exception {
		HttpTestConnection conn = null;
		
		try {
			for(int i = 1; i < 20000; i += 500) {
				String value = nextString(i);
				int len = value.length() + 5;
				String request = "POST /test/session HTTP/1.1\015\012" + 
					"Host: localhost\015\012" +
					"Content-Type: application/x-www-form-urlencoded; charset=iso-8859-1\015\012" +
					"Content-Length: " + len + "\015\012" + 
					"Connection: close\015\012\015\012" +
					"data=" + value;
				
				conn = new HttpTestConnection();
				conn.write(request);
				
				String response = conn.readString();
				String encoded = getEncodedSession(response);
				
				HttpSession session = HttpCookieSessionDeserializer.deserialize(encoded);
				assertEquals(value, session.getAttribute("data"));
				
				conn.close();
				conn = null;
			}
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public void testHttpSession3() throws Exception {
		HttpTestConnection conn = null;
    	HttpCookieSessionManager manager = new HttpCookieSessionManager();
    	HttpTestRequest httpRequest = new HttpTestRequest();
		
		try {
			for(int i = 1; i < 7600; i += 500) {
				String value = nextString(i);
				String request = "GET /test/session HTTP/1.1\015\012" + 
					"Host: localhost\015\012" +
					"<cookie>" +
					"Content-Type: application/x-www-form-urlencoded; charset=iso-8859-1\015\012" +
					"Content-Length: 0\015\012" + 
					"Connection: close\015\012\015\012";
				
				HttpSession session = manager.getSession(httpRequest, true);
				session.setAttribute("data", value);
				String encoded = HttpCookieSessionSerializer.serialize(session);
				
				int postfix = 1;
				StringBuffer cookies = new StringBuffer();
				
				for(int j = 0; j < encoded.length(); j += 2048) {
					int length = encoded.length() - j > 2048 ? 2048 : encoded.length() - j;
					String data = encoded.substring(j, j + length).replace('=', '%');
					cookies.append("Cookie: ");
					cookies.append(HttpSessionConstants.COOKIE_PREFIX);
					cookies.append(postfix);
					cookies.append("=\"");
					cookies.append(data);
					cookies.append("\"\015\012");
					postfix++;
				}		
				
				request = request.replaceAll("<cookie>", cookies.toString());
				
				conn = new HttpTestConnection();
				conn.write(request);
				
				String response = conn.readBodyString("iso-8859-1");
				assertEquals(value, response);
				
				conn.close();
				conn = null;
			}
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	private String getEncodedSession(String response) {
		StringBuffer encoded = new StringBuffer();
		int idx = 1;
		
		while(true) {
			String name = "http_session_" + idx;
			int start = response.indexOf(name);
			
			if(start > -1) {
				start += name.length() + 2;
				int end = response.indexOf('"', start);
				encoded.append(response.substring(start, end));
				idx++;
			} else {
				break;
			}
		}
		
		return encoded.toString().replace('%', '=');
	}
	
    public static Test suite() {
        return new TestSuite(HttpCookieSessionTestCase.class);
    }    
}
