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

package com.nginious.http.ant;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.nginious.http.client.HttpClientException;
import com.nginious.http.upload.ApplicationUploader;

/**
 * An ant task for deploying a web application archive to a running ProjectX server. The example below
 * deploys a web application archive located in file 'test/webapps/test.war' to the local ProjectX
 * server with name 'test'. Once deployed the web application is accessible under context
 * <code>http://127.0.0.1/test</code>. If a web application with the same name already exists it is
 * replaced with the deployed web application.
 * 
 * <pre>
 * &lt;deploy file="test/webapps/root.war" 
 *	 url="http://127.0.0.1/admin/app/test"
 *	 username="admin"
 *	 password="admin" /&gt;
 * </pre>
 * 
 * The deploy task accepts the following attributes.
 * 
 * <ul>
 * 	<li>file - name of web application archive to deploy.</li>
 * 	<li>url - location of the deploy service on the web server. The last path segment of the URI specified the name under
 * 	which the web application should be deployed.</li>
 * 	<li>username - username for accessing the admin services on the ProjectX server.</li>
 * 	<li>password - password for accessing the admin services on the ProjectX server.</li>
 * </ul>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class DeployTask extends Task {
	
	private URL url;
	
	private File file;
	
	private String username;
	
	private String password;
	
	/**
	 * Constructs a new empty deploy task.
	 */
    public DeployTask() {
		super();
	}
    
    /**
     * Sets the URL location of the deploy service to the specified URL.
     * 
     * @param url the URL.
     */
    public void setUrl(URL url) {
    	this.url = url;
    }
    
    /**
     * Returns the URL location of the deploy service.
     * 
     * @return the URL
     */
    public URL getUrl() {
    	return this.url;
    }
    
    /**
     * Sets the web application archive file to deploy to the specified file.
     * 
     * @param file the web application archive file
     */
    public void setFile(File file) {
    	this.file = file;
    }
    
    /**
     * Returns the web application archive file to deploy.
     * 
     * @return the web application archive file
     */
    public File getFile() {
    	return this.file;
    }
    
    /**
     * Sets the username for accessing the ProjectX admin services to the specified username.
     * 
     * @param username the username
     */
    public void setUsername(String username) {
    	this.username = username;
    }
    
    /**
     * Returns the username for accessing the ProjectX admin services.
     * 
     * @return the username
     */
    public String getUsername() {
    	return this.username;
    }
    
    /**
     * Sets the password for accessing the ProjectX admin services.
     * 
     * @param password the password
     */
    public void setPassword(String password) {
    	this.password = password;
    }
    
    /**
     * Returns the password for accessing the ProjectX admin services.
     * 
     * @return the password
     */
    public String getPassword() {
    	return this.password;
    }
    
    /**
     * Executes this task by checking that all necessary attributes are set, then deploys the
     * web application archive to the ProjectX server.
     * 
     * @throws BuildException if any of the attributes is invalid or if thee deploy fails
     */
    public void execute() throws BuildException {
    	checkParameters();
    	deployFile();
    }
    
    /**
     * Deploys the web application archive file to the ProjectX server.
     * 
     * @throws BuildException if unable to deploy web application archive file
     */
    private void deployFile() throws BuildException {
    	try {
    		ApplicationUploader uploader = new ApplicationUploader(null, this.url, this.file, this.username, this.password);
    		uploader.upload();
    	} catch(HttpClientException e) {
    		throw new BuildException("failed sending request to server: " + e.getMessage(), e, getLocation());
    	} catch(IOException e) {
    		throw new BuildException("failed sending request to server: " + e.getMessage(), e, getLocation());    		
    	}
    	
    }
    
    /**
     * Checks all attributes for validity.
     * 
     * @throws BuildException if any of the attributes is invalid
     */
    private void checkParameters() throws BuildException {
    	if(this.url == null) {
    		throw new BuildException("url attribute must be set!", getLocation());
    	}
    	
    	if(this.file == null) {
    		throw new BuildException("file attribute must be set!", getLocation());
    	}
    	
    	if(this.username == null) {
    		throw new BuildException("username attribute must be set!", getLocation());
    	}
    	
    	if(this.password == null) {
    		throw new BuildException("password attribute must be set!", getLocation());
    	}
    	
    	if(!file.exists()) {
    		throw new BuildException("web application file \""
    				+ this.file
    				+ "\" does not exist", getLocation());
    	}
    	
    	if(!file.isFile()) {
    		throw new BuildException("web application file \""
    				+ this.file
    				+ "\" is not a file", getLocation());    		
    	}
    }
}
