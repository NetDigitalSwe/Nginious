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
 * Manages all applications for a HTTP server including creation, publishing, unpublishing,
 * version management and deletion of applications. 
 * 
 * <p>
 * An application can be published for the first time or updated by calling the {@link #publish(String, File)}
 * method. If the application is packaged in a web application archive any previously published versions of an
 * are backed up. Up to 10 versions of an application are backed up. By calling the {@link #rollback(String)}
 * the current version of an application is unpublished and the latest backed up version is published.
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden
 *
 */
public interface ApplicationManager {
	
	/**
	 * Returns the application with the specified name.
	 * 
	 * @param name application name
	 * @return the application or <code>null</code> if no application with the given name exists
	 */
	public Application getApplication(String name);
	
	/**
	 * Creates an application with the specified name and binds it to this application manager.
	 * The application is not published until the {@link #publish(Application)} method is called.
	 * 
	 * @param name the application name
	 * @return the created application
	 * @throws ApplicationException if an application with the given name already exists
	 */
	public Application createApplication(String name) throws ApplicationException;
	
	/**
	 * Creates an application with the specified name and base directory for static content. The
	 * application is bound to this application manager. The application is not published until the
	 * {@link #publish(Application)} method is called.
	 * 
	 * @param name the application name
	 * @param baseDir the static content base directory
	 * @return the created application
	 * @throws ApplicationException if unable to create application
	 */
	public Application createApplication(String name, File baseDir) throws ApplicationException;
	
	/**
	 * Returns all applications bound to this applications manager.
	 * 
	 * @return list of all applications
	 */
	public List<Application> getApplications();
	
	/**
	 * Publishes the specified application. The application must have been created using on of the
	 * {@link #createApplication(String)} or {@link #createApplication(String, File)} methods.
	 * 
	 * @param application the application to publish
	 * @return the published application
	 * @throws ApplicationException if unable to publish application
	 */
	public Application publish(Application application) throws ApplicationException;
	
	/**
	 * Publishes the application packaged in the specified directory or web application
	 * archive. The directory or web application archive is inspected to find HTTP and REST
	 * services.
	 * 
	 * <p>
	 * If the application is packaged in a web application archive the previous version is
	 * moved to the backup directory. A later call to {@link #rollback(String)} restores
	 * the backed up version.
	 * </p>
	 * 
	 * @param name the application name
	 * @param dirOrWarFile the web application archive or directory
	 * @return the created application
	 * @throws ApplicationException if unable to publish application
	 */
	public Application publish(String name, File dirOrWarFile) throws ApplicationException;
	
	/**
	 * Unpublishes the application with the specified name and publishes the previous version
	 * 
	 * @param name the application name
	 * @return the published application
	 * @throws ApplicationException if unable to rollback application
	 */
	public Application rollback(String name) throws ApplicationException;
	
	/**
	 * Unpublishes the specified application which makes it unavailable for access over
	 * HTTP. Any web application directory or archive associated with the application is
	 * kept in the web applications directory.
	 * 
	 * @param application the application to unpublish
	 * @throws ApplicationException if unable to unpublish application
	 */
	public void unpublish(Application application) throws ApplicationException;
	
	/**
	 * Unpublishes the application with the specified name which makes it unavailble for
	 * access over HTTP. Any web application directory or archive associated with the application
	 * is kept in the web application directory.
	 * 
	 * @param name the application name
	 * @throws ApplicationException if unable to unpublish application
	 */
	public void unpublish(String name) throws ApplicationException;
	
	/**
	 * Deletes the specified application which makes it unavailable for access over HTTP. Any
	 * web application directory or archive associated with the application is deleted.
	 * 
	 * @param application the application to delete
	 * @throws ApplicationException if unable to delete application
	 */
	public void delete(Application application) throws ApplicationException;
	
	/**
	 * Deletes the application with the specified name which makes it unavailable for access
	 * over HTTP. Any web application directory or archive associated with the application is
	 * deleted.
	 * 
	 * @param name the application name
	 * @throws ApplicationException if unable to delete application
	 */
	public void delete(String name) throws ApplicationException;
}
