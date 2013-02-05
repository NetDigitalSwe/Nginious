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

import com.nginious.http.HttpException;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpStatus;
import com.nginious.http.annotation.Controller;

/**
 * A reloadable controller service checks a controller class file for modifications prior to invocation of the
 * controllers controller service. If the class file is modified the controller service is removed and a new
 * controller service is created. 
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class ReloadableControllerService extends HttpService {
	
	private Object controller;
	
	private ControllerService service;
	
	private ControllerServiceFactory factory;
	
	private ClassLoader classLoader;
	
	private String className;
	
	private File classFile;
	
	private long lastModified;
	
	private HttpException exception;
	
	/**
	 * Constructs a new reloadable controller service using the specified controller service factory, service, class loader, class name and
	 * class file.
	 * 
	 * @param factory the controller service factory to use for creating controller services for reloaded controllers
	 * @param service the controller service to use for invoking the controller
	 * @param classLoader the class loader to use for loading recreated controller service classes
	 * @param className the controller class name
	 * @param classFile the controller class file to check for modifications
	 */
	ReloadableControllerService(ControllerServiceFactory factory, ControllerService service, ClassLoader classLoader, String className, File classFile) {
		this.factory = factory;
		this.service = service;
		this.controller = service.getController();
		this.classLoader = classLoader;
		this.className = className;
		this.classFile = classFile;
		this.lastModified = classFile.lastModified();
	}
	
	/**
	 * Returns the controller annotation for the controller class.
	 *  
	 * @return the controller annotation
	 */
	Object getController() {
		return controller.getClass().getAnnotation(Controller.class);
	}
	
	/**
	 * Inspects the controllers class file for modifications and invokes the controller service with the specified HTTP request and 
	 * response. If the controller class file is modified the corresponding controller service is regenerated to reflect changes
	 * in the modified controller. 
	 * 
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @return a result indicating if execution is done, should continue or is asynchronous.
	 * @throws HttpException if the HTTP request is invalid or if the service is unable to process the request
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult invoke(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		if(!classFile.exists()) {
			this.controller = null;
			throw new HttpControllerRemovedException(HttpStatus.NOT_FOUND, request.getPath());
		}
		
		if(classFile.lastModified() > lastModified || this.controller == null) {
			loadControllerClass();
		}
		
		return service.invoke(request, response);
	}

	public final HttpServiceResult executeGet(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		return invoke(request, response);
	}

	public final HttpServiceResult executePost(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		return invoke(request, response);
	}

	public final HttpServiceResult executePut(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		return invoke(request, response);
	}

	public final HttpServiceResult executeDelete(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		return invoke(request, response);
	}
	
	private void loadControllerClass() throws HttpException {
		synchronized(this) {
			if(this.exception != null) {
				throw exception;
			}
			
			try {
				factory.destroyControllerService(controller.getClass());
				this.service = null;
				Class<?> clazz = classLoader.loadClass(className);
				this.controller = clazz.newInstance();
				this.service = factory.createControllerService(this.controller);
				this.lastModified = classFile.lastModified();
			} catch(ClassNotFoundException e) {
				this.exception = new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to load controller class");
				throw this.exception;
			} catch(IllegalAccessException e) {
				this.exception = new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to load controller class");
				throw this.exception;
			} catch(InstantiationException e) {
				this.exception = new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to load controller class");
				throw this.exception;
			} catch(ControllerServiceFactoryException e) {
				this.exception = new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to load controller class");
				throw this.exception;
			}
		}
	}
}
