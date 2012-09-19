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

package com.nginious.http.application;

import java.io.File;
import java.util.List;

import com.nginious.http.HttpService;

/**
 * An application groups a set of resources and makes them available for access over HTTP. An application is 
 * published under a base path where its resources can be accessed over HTTP. An application supports the
 * following types of resources.
 * 
 * <ul>
 * <li>HTTP services - subclasses of {@link com.nginious.http.HttpService} for dynamic content generation.</li>
 * <li>REST services - subclasses of {@link com.nginious.http.rest.RestService} for dynamic content generation with
 * REST service semantics.</li>
 * <li>XSP pages - script pages compiled into {@link com.nginious.http.HttpService} subclasses for dynamic content generation.</li>
 * <li>Static content - served from files on disk from the applications base directory.</li>
 * </ul>
 * 
 * <p>
 * An application is published in three possible ways.
 * 
 * <ul>
 * <li>War file - by packaging necessary service classes, XSP files and static content into a war file which is placed in the
 * webapps directory of the server. The application name is taken from the war file name.</li>
 * <li>Directory - by placing necessary service classes, XSP files and static content in a subdirectory in the webapps directory
 * of the server. The application name is taken from the directory name.</li>
 * <li>Programatically - by using the methods in this class and in {@link ApplicationManager} to create an application.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * A web application archive has the following structure
 * 
 * <ul>
 * <li>WEB-INF/classes - individual classes are placed in this directory.
 * <li>WEB-INF/lib - jar archives are placed in this directory.
 * <li>WEB-INF/xsp - xsp script files.
 * <li>Any other files or subdirectories with files are regarded as static content.</li>
 * </ul>
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public interface Application {
	
	/**
	 * Returns the name of this application.
	 * 
	 * @return the name of this application
	 */
	public String getName();
	
	/**
	 * Returns the base directory where all files for this application are located.
	 * 
	 * @return the base directory
	 */
	public File getBaseDir();
	
	/**
	 * Sets the base directory where all files for this application are located.
	 * @param baseDir
	 */
	public void setBaseDir(File baseDir);
	
	/**
	 * Adds the specified HTTP service to this application. The HTTP service is bound to the
	 * path specified in the HTTP services {@link Service} annotation.
	 * 
	 * @param service the HTTP service to add
	 * @throws ApplicationException if unable to add HTTP service
	 */
	public void addHttpService(HttpService service) throws ApplicationException;
	
	/**
	 * Adds the specified HTTP service to this application and binds it to the specified
	 * path.
	 * 
	 * @param path the HTTP service path
	 * @param service the HTTP service
	 * @throws ApplicationException if unable to add HTTP service
	 */
	public void addHttpService(String path, HttpService service) throws ApplicationException;
	
	/**
	 * Removes the specified HTTP service from this application.
	 * 
	 * @param service the HTTP service to remove
	 * @return the removed HTTP service or <code>null</code> if HTTP service was not part of this
	 * application
	 */
	public HttpService removeHttpService(HttpService service);
	
	/**
	 * Removes the HTTP service bound to the specified path from this application.
	 * 
	 * @param path the HTTP service path
	 * @return the removed HTTP service or <code>null</code> if no HTTP service is bound to the
	 * path
	 */
	public HttpService removeHttpService(String path);
	
	/**
	 * Returns all HTTP services for this application.
	 * 
	 * @return list of all HTTP services
	 */
	public List<HttpService> getHttpServices();
}
