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

package com.nginious.http.upload;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.nginious.http.HttpMethod;
import com.nginious.http.HttpStatus;
import com.nginious.http.client.HttpClient;
import com.nginious.http.client.HttpClientException;
import com.nginious.http.client.HttpClientRequest;
import com.nginious.http.client.HttpClientResponse;
import com.nginious.http.server.Digest;

/**
 * Publishes a web application to a server URL.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class ApplicationUploader {
	
	private ProgressListener listener;
	
	private URL url;
	
	private File file;
	
	private String username;
	
	private String password;
	
	/**
	 * Constructs a new application uploader with the specified server URL, file to publish, username
	 * and password.
	 * 
	 * @param url the server publish url
	 * @param file the application file to upload and publish
	 * @param username the server username
	 * @param password the server password
	 */
	public ApplicationUploader(ProgressListener listener, URL url, File file, String username, String password) {
		super();
		this.listener = listener;
		this.url = url;
		this.file = file;
		this.username = username;
		this.password = password;
	}
	
	/**
	 * Uploads the application file.
	 * 
	 * @throws HttpClientException if a server error occurs
	 * @throws IOException if an I/O error occurs while uploading file
	 * @return response from server
	 */
    public HttpClientResponse upload() throws HttpClientException, IOException {
    	HttpClient client = new HttpClient(url.getHost(), url.getPort());
    	boolean exists = findDeployedApp(client);
    	return uploadFile(client, exists);
    }
    
    /**
     * Uploads the web application archive file to the ProjectX server. The upload is done as multipart
     * content in a HTTP POST or HTTP PUT depending on if a web application with the same name already
     * exists.
     * 
     * @param client the HTTP client for uploading the web application archive file
     * @param exists whether or not a web application with the same name already exists
     * @throws HttpClientException if a HTTP protocol error occurs while uploading the file
     * @throws IOException if an I/O error occurs while uploading the file
     * @return response from server
     */
    private HttpClientResponse uploadFile(HttpClient client, boolean exists) throws HttpClientException, IOException {
    	MultipartInputStream in = null;
    	
    	try {
    		in = new MultipartInputStream("AaB03x", this.file);
    		
    		String path = url.getPath();
    		String appName = path.substring(path.lastIndexOf('/'), path.length());
    		StringBuffer contentDisposition = new StringBuffer("form-data; name=\"");
    		contentDisposition.append(appName);
    		contentDisposition.append("\"; filename=\"");
    		contentDisposition.append(file.getName());
    		contentDisposition.append("\"");
    		in.setHeader("Content-Disposition", contentDisposition.toString());
    		in.setHeader("Content-Type", "application/octet-stream");
    		long length = in.length();
    		
    		HttpClientRequest request = new HttpClientRequest();
    		request.setMethod(exists ? HttpMethod.POST : HttpMethod.PUT);
    		request.setPath(url.getPath());
    		request.addHeader("Host", url.getHost());
    		request.addHeader("Connection", "close");
    		request.addHeader("Content-Type", "multipart/form-data; boundary=AaB03x");
    		request.addHeader("Content-Length", Long.toString(length));
    		String authorization = createAuthorization(request.getMethod());
    		request.addHeader("Authorization", authorization);
    		
    		ProgressInputStream progressIn = new ProgressInputStream(in, this.listener);
    		HttpClientResponse response = client.request(request, progressIn);
    		
    		if(response.getStatus() != HttpStatus.OK) {
    			throw new HttpClientException("failed deploy " + response.getStatusMessage());
    		}
    		
    		return response;
    	} finally {
    		if(in != null) {
    			try { in.close(); } catch(IOException e) {}
    		}
    	}
    	
    }
    
    /**
     * Checks whether or not a web application with the same name as the one being deployed already exists
     * on the ProjectX server.
     * 
     * @param client the HTTP client to use for communicating with the ProjectX server
     * @return <code>true</code> if a web application with the same name already exists, <code>false</code> otherwise
     * @throws HttpClientException if a HTTP protocol error occurs while checking
     * @throws IOException if an I/O error occurs while checking
     */
    private boolean findDeployedApp(HttpClient client) throws HttpClientException, IOException {
    	HttpClientRequest request = new HttpClientRequest();
    	request.setMethod(HttpMethod.GET);
		request.setPath(url.getPath());
		request.addHeader("Host", url.getHost());
		request.addHeader("Connection", "keep-alive");
		String authorization = createAuthorization(HttpMethod.GET);
		request.addHeader("Authorization", authorization);
		
		HttpClientResponse response = client.request(request);
		
		if(response.getStatus() != HttpStatus.OK && response.getStatus() != HttpStatus.NOT_FOUND) {
			HttpStatus status = response.getStatus();
			String msg = response.getStatusMessage();
			throw new HttpClientException("got error response code from server " + status.getStatusCode() + " " + msg);
		}
		
    	return response.getStatus() == HttpStatus.OK;
    }
    
    /**
     * Creates a digest authorization for authenticating with the ProjectX server. The authorization is created
     * from the username and password supplied in the attributes.
     * 
     * @param method the HTTP method
     * @return the created digest authorization
     */
    private String createAuthorization(HttpMethod method) {
    	Digest digest = new Digest();
    	digest.setCnonce("0a4f113b");
    	digest.setMethod(method.toString());
    	digest.setNc("0000001");
    	digest.setNonce("dcd98b7102dd2f0e8b11d0f600bfb0c093");
    	digest.setQop("auth");
    	digest.setRealm("admin");
    	digest.setUri("/admin");
    	digest.setUsername(this.username);
    	String response = digest.createResponse(this.password);
    	
    	StringBuffer authorization = new StringBuffer();
    	authorization.append("username=\"");
    	authorization.append(this.username);
    	authorization.append("\", realm=\"admin\", ");
    	authorization.append("nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", ");
    	authorization.append("uri=\"/admin\", ");
    	authorization.append("qop=auth, ");
    	authorization.append("nc=0000001, ");
    	authorization.append("cnonce=\"0a4f113b\", ");
    	authorization.append("response=\"");
    	authorization.append(response);
    	authorization.append("\", ");
    	authorization.append("opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"");
    	return authorization.toString();
    }    
}
