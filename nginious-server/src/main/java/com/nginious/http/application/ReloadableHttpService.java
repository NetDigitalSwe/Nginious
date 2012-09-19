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
import com.nginious.http.HttpService;
import com.nginious.http.HttpServiceResult;
import com.nginious.http.HttpStatus;

class ReloadableHttpService extends HttpService {
	
	private HttpService service;
	
	private ClassLoader classLoader;
	
	private String className;
	
	private File classFile;
	
	private long lastModified;
	
	private HttpException exception;
	
	ReloadableHttpService(HttpService service, ClassLoader classLoader, String className, File classFile) {
		this.service = service;
		this.classLoader = classLoader;
		this.className = className;
		this.classFile = classFile;
		this.lastModified = classFile.lastModified();
	}
	
	public Service getService() {
		return service.getService();
	}
	
	public HttpServiceResult invoke(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		if(!classFile.exists()) {
			this.service = null;
			throw new HttpServiceRemovedException(HttpStatus.NOT_FOUND, request.getPath());
		}
		
		if(classFile.lastModified() > lastModified || this.service == null) {
			loadServiceClass();
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
	
	private void loadServiceClass() throws HttpException {
		synchronized(this) {
			if(this.exception != null) {
				throw exception;
			}
			
			try {
				this.service = null;
				Class<?> clazz = classLoader.loadClass(className);
				this.service = (HttpService)clazz.newInstance();
			} catch(ClassNotFoundException e) {
				this.exception = new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to load service class");
				throw this.exception;
			} catch(IllegalAccessException e) {
				this.exception = new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to load service class");
				throw this.exception;
			} catch(InstantiationException e) {
				this.exception = new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to load service class");
				throw this.exception;
			}
		}
	}
}
