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
import java.io.IOException;
import java.util.Collection;

import com.nginious.http.HttpException;
import com.nginious.http.HttpServiceResult;
import com.nginious.http.HttpStatus;
import com.nginious.http.common.PathParameters;
import com.nginious.http.rest.RestRequest;
import com.nginious.http.rest.RestResponse;
import com.nginious.http.rest.RestService;
import com.nginious.http.upload.FilePart;

/**
 * A REST service for deploying, redeploying, removing and getting information about a web application.
 * The following methods are supported.
 * 
 * <p>
 * <ul>
 * <li>GET http://[host]/app/[appname] - returns information about a deployed web application an all its versions.
 * 	A serialized {@link ApplicationInfo} bean is returned in JSON or XML format depending on the <code>Accept</code> header 
 *  in the request to this method.</li>
 *  
 * <li>POST http://[host]/app/[appname] - redeploys an existing web application or rolls back a web application to
 * 	its previous version. If redeploying a new version the request must be a multipart request with one file containing 
 * 	the web application archive. A serialized {@link ApplicationInfo} bean is returned in JSON or XML format with information 
 * 	about the redeployed web application. The format depends on the <code>Accept</code> header in the request to this method.</li>
 * 
 * <li>PUT http://[host]/app/[appname] - deploys a web application for the first time. The request must be a 
 * 	multipart request with one file containing the web application archive. A serialized {@link ApplicationInfo}
 * 	bean is returned in JSON or XML format with information about the deployed web application. The format depends
 * on the <code>Accept</code> header in the request to this method.</li>
 * 
 * <li>DELETE http://[host]/app/[appname] - removes a web application and all its versions.</li>
 * </ul>
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB.
 *
 */
class ApplicationService extends RestService<Void, ApplicationInfo> {
	
	private ApplicationManagerImpl manager;
	
	ApplicationService(ApplicationManagerImpl manager) {
		this.manager = manager;
	}
	
	/**
	 * Returns information about a web application and all its versions. The information is returned in the specified
	 * response with the specified type {@link ApplicationInfo}.
	 * 
	 * @param request the REST request
	 * @param response the REST response
	 * @return result of service execution
	 * @throws HttpException if any of the request data is invalid causing a HTTP error to the client
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executeGet(RestRequest<Void> request, RestResponse<ApplicationInfo> response) throws HttpException, IOException {
		PathParameters params = new PathParameters(request);
		String name = params.get(2);
		
		if(name == null) {
			throw new HttpException(HttpStatus.BAD_REQUEST, "application name is missing in uri path '" + request.getPath() + "'");
		}
		
		ApplicationImpl application = manager.getApplicationImpl(name);
		
		if(application == null) {
			throw new HttpException(HttpStatus.NOT_FOUND, "app '" + name + "' not found");
		}
		
		ApplicationInfo info = manager.createApplicationInfo(application);
		response.setBean(info);
		return HttpServiceResult.DONE;
	}
	
	/**
	 * Deploys a new version of a already deployed web application. The specified request must be a multipart request which
	 * contains one web application archive (war) file for deployment. Information about the web application is returned in
	 * the specified response with the specified type {@link ApplicationInfo}.
	 * 
	 * @param request the REST request
	 * @param response the REST response
	 * @return result of service execution
	 * @throws HttpException if any of the request data is invalid causing a HTTP error to the client
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executePost(RestRequest<Void> request, RestResponse<ApplicationInfo> response) throws HttpException, IOException {
		try {
			PathParameters params = new PathParameters(request);
			String name = params.get(2);

			if(name == null) {
				throw new HttpException(HttpStatus.BAD_REQUEST, "application name is missing in uri path '" + request.getPath() + "'");
			}
			
			ApplicationImpl application = manager.getApplicationImpl(name);
			
			if(application == null) {
				throw new HttpException(HttpStatus.NOT_FOUND, "application '" + name + "' not found");
			}
			
			Collection<FilePart> parts = request.getFiles();
			
			if(parts.size() == 0) {
				manager.rollback(name);
			} else {
				if(parts.size() > 1) {
					throw new HttpException(HttpStatus.BAD_REQUEST, "expected one war archive to deploy");
				}
				
				FilePart part = parts.iterator().next();
				File warFile = part.getFile();
				application = (ApplicationImpl)manager.publish(name, warFile);
			}
			
			ApplicationInfo info = manager.createApplicationInfo(application);
			response.setBean(info);
			return HttpServiceResult.DONE;
		} catch(ApplicationException e) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "unable to deploy app");
		}
	}
	
	/**
	 * Deploys a new web application. The specified request must be a multipart request which contains one web application 
	 * archive (war) file for deployment. Information about the web application is returned in the specified response with the 
	 * specified type {@link ApplicationInfo}.
	 * 
	 * @param request the REST request
	 * @param response the REST response
	 * @return result of service execution
	 * @throws HttpException if any of the request data is invalid causing a HTTP error to the client
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executePut(RestRequest<Void> request, RestResponse<ApplicationInfo> response) throws HttpException, IOException {
		try {
			PathParameters params = new PathParameters(request);
			String name = params.get(2);

			if(name == null) {
				throw new HttpException(HttpStatus.BAD_REQUEST, "application name is missing in uri path '" + request.getPath() + "'");
			}
			
			ApplicationImpl application = manager.getApplicationImpl(name);
			
			if(application != null) {
				throw new HttpException(HttpStatus.BAD_REQUEST, "application '" + name + "' is already deployed");
			}
			
			Collection<FilePart> parts = request.getFiles();
			
			if(parts.size() == 0) {
				throw new HttpException(HttpStatus.BAD_REQUEST, "no war archive found to deploy");
			}
			
			if(parts.size() > 1) {
				throw new HttpException(HttpStatus.BAD_REQUEST, "expected one war archive to deploy");
			}
			
			FilePart part = parts.iterator().next();
			File warFile = part.getFile();
			application = (ApplicationImpl)manager.publish(name, warFile);
			ApplicationInfo info = manager.createApplicationInfo(application);
			response.setBean(info);
			return HttpServiceResult.DONE;
		} catch(ApplicationException e) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "unable to deploy app");
		}
	}
	
	/**
	 * Deletes a web application and all its backed up versions.  Information about the deleted web application is returned in the 
	 * specified response with the specified type {@link ApplicationInfo}.
	 * 
	 * @param request the REST request
	 * @param response the REST response
	 * @return result of service execution
	 * @throws HttpException if any of the request data is invalid causing a HTTP error to the client
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executeDelete(RestRequest<Void> request, RestResponse<ApplicationInfo> response) throws HttpException, IOException {
		try {
			PathParameters params = new PathParameters(request);
			String name = params.get(2);
			
			if(name == null) {
				throw new HttpException(HttpStatus.BAD_REQUEST, "application name is missing in uri path '" + request.getPath() + "'");
			}
			
			ApplicationImpl application = manager.getApplicationImpl(name);
			
			if(application == null) {
				throw new HttpException(HttpStatus.NOT_FOUND, "application '" + name + "' is not deployed");
			}
			
			if(application.isDirectory()) {
				throw new HttpException(HttpStatus.BAD_REQUEST, "application '" + name + "' is directory");
			}
			
			manager.delete(name);
			response.setBean(null);
			return HttpServiceResult.DONE;
		} catch(ApplicationException e) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "unable to delete app");
		}
	}	
}
