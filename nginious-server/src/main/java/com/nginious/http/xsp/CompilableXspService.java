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

package com.nginious.http.xsp;

import java.io.File;
import java.io.IOException;

import com.nginious.http.HttpException;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpService;
import com.nginious.http.HttpServiceResult;

/**
 * A XSP service that contains another XSP service and the original XSP file that the service was compiled
 * from. If the original XSP file is modified the contained XSP service is recompiled and replaced.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class CompilableXspService extends HttpService {
	
	private ClassLoader classLoader;
	
	private XspService service;
	
	private File baseDir;
	
	private File srcFile;
	
	private File classFile;
	
	/**
	 * Constructs a new compilable XSP service from the specified XSP service and XSP file located in the
	 * specified base dir and source file.
	 * 
	 * @param classLoader the class loader to use for loading recompiled XSP service classes
	 * @param service the XSP service to encapsulate
	 * @param baseDir the XSP page file base directory
	 * @param srcFile the XSP page source file
	 * @param classFile the XSP service class file
	 */
	public CompilableXspService(ClassLoader classLoader, XspService service, File baseDir, File srcFile, File classFile) {
		this.classLoader = classLoader;
		this.service = service;
		this.baseDir = baseDir;
		this.srcFile = srcFile;
		this.classFile = classFile;
	}

	/**
	 * Called by the server to execute a HTTP GET for the contained XSP service using the specified HTTP request and response.
	 * If the XSP page has been modified since the last execution it is recompiled before execution.
	 *
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @return a result indicating if execution is done or is asynchronous
	 * @throws HttpException if the HTTP request is invalid or if the service is unable to process the request
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executeGet(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		compileIfModified();
		return service.executeGet(request, response);
	}

	/**
	 * Called by the server to execute a HTTP POST for the contained XSP page using the specified HTTP request and response.
	 * If the XSP page has been modified since the last execution it is recompiled before execution.
	 *
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @return a result indicating if execution is done or is asynchronous
	 * @throws HttpException if the HTTP request is invalid or if the service is unable to process the request
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executePost(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		compileIfModified();
		return service.executePost(request, response);
	}

	/**
	 * Called by the server to execute a HTTP PUT for the contained XSP page using the specified HTTP request and response.
	 * If the XSP page has been modified since the last execution it is recompiled before execution.
	 *
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @return a result indicating if execution is done or is asynchronous
	 * @throws HttpException if the HTTP request is invalid or if the service is unable to process the request
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executePut(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		compileIfModified();
		return service.executePut(request, response);
	}

	/**
	 * Called by the server to execute a HTTP DELETE for the contained XSP page using the specified HTTP request and response.
	 * If the XSP page has been modified since the last execution it is recompiled before execution.
	 *
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @return a result indicating if execution is done or is asynchronous
	 * @throws HttpException if the HTTP request is invalid or if the service is unable to process the request
	 * @throws IOException if an I/O error occurs
	 */
	public HttpServiceResult executeDelete(HttpRequest request, HttpResponse response) throws HttpException, IOException {
		compileIfModified();
		return service.executeDelete(request, response);
	}
	
	/**
	 * Recompiles the contained XSP service.
	 * 
	 * @throws XspException if unable to recompile
	 */
	private void compileIfModified() throws XspException {
		if(!classFile.exists() || srcFile.lastModified() > classFile.lastModified()) {
			File destDir = new File(this.baseDir, "classes");
			XspCompiler compiler = new XspCompiler(this.classLoader);
			this.service = compiler.compileService(baseDir.getAbsolutePath(), srcFile.getAbsolutePath(), destDir.getAbsolutePath());
		}
	}
}
