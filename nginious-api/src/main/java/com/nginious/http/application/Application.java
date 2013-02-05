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

/**
 * An application groups a set of resources and makes them available for access over HTTP. An application is 
 * published under a base path where its resources can be accessed over HTTP. An application supports the
 * following types of resources.
 * 
 * <ul>
 * <li>Controller - any class marked with the {@link com.nginious.http.annotation.Controller} annotation for dynamic content generation.</li>
 * <li>XSP pages - script pages compiled into services for dynamic content generation.</li>
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
	 * Adds the specified controller to this application. The controller is bound to the path
	 * specified in the controllers {@link com.nginious.http.annotation.Controller} annotation.
	 * 
	 * @param controller the controller to add
	 * @throws ApplicationException if unable to add controller
	 */
	public void addController(Object controller) throws ApplicationException;
	
	/**
	 * Removes the specified controller from this application.
	 * 
	 * @param controller the controller to remove
	 * @return the removed controller or <code>null</code> if controller was not part of this application
	 */
	public Object removeController(Object controller);
	
	/**
	 * Removes the controller bound to the specified path from this application.
	 * 
	 * @param path the controller path
	 * @return the removed controller or <code>null</code> if no controller is bound to the path
	 */
	public Object removeController(String path);
	
	/**
	 * Returns all controllers for this application.
	 * 
	 * @return list of all controllers
	 */
	public List<Object> getControllers();
	
	/**
	 * Adds the specified service to this application. the service is bound to the name
	 * specified in the services {@link com.nginious.http.annotation.Service} annotation.
	 * 
	 * @param service the service to add
	 * @throws ApplicationException if unable to add service
	 */
	public void addService(Object service) throws ApplicationException;
	
	/**
	 * Removes the specified service from this application.
	 * 
	 * @param service the service to remove
	 * @return the remove service or <code>null</code> if service was not part of this application
	 */
	public Object removeService(Object service);
	
	/**
	 * Removes the service bound to the specified name from this application.
	 * 
	 * @param name the service name
	 * @return the removed service or <code>null</code> if no service is bound to the name
	 */
	public Object removeService(String name);
	
	/**
	 * Returns the service with the specified name from this application.
	 * 
	 * @param name the service name
	 * @return the service or <code>null</code> if no service with the provided name exists
	 */
	public Object getService(String name);
}
