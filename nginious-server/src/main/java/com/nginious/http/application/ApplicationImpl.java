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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.nginious.http.HttpException;
import com.nginious.http.HttpMethod;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpService;
import com.nginious.http.HttpServiceResult;
import com.nginious.http.HttpStatus;
import com.nginious.http.application.Application;
import com.nginious.http.application.ApplicationException;
import com.nginious.http.application.Service;
import com.nginious.http.rest.RestService;
import com.nginious.http.server.HttpServiceChain;
import com.nginious.http.xsp.CompilableXspService;
import com.nginious.http.xsp.XspCompiler;
import com.nginious.http.xsp.XspException;
import com.nginious.http.xsp.XspService;

/*
 * A concrete application implementation.
 */
class ApplicationImpl implements Application {
	
	private static HashSet<HttpMethod> methods;
	
	static {
		methods = new HashSet<HttpMethod>();
		methods.add(HttpMethod.HEAD);
		methods.add(HttpMethod.GET);
		methods.add(HttpMethod.POST);
		methods.add(HttpMethod.PUT);
		methods.add(HttpMethod.DELETE);
	}
	
	private String name;
	
	private Date publishTime;
	
	private boolean directory;
	
	private boolean war;
	
	private boolean memory;
	
	private File baseDir;
	
	private ConcurrentHashMap<String, HttpService> executableServices;
	
	private HashSet<HttpService> addedServices;
	
	private TreeSet<HttpServiceFilter> filterServices = new TreeSet<HttpServiceFilter>();
	
	private HashMap<String, String> allowedServiceMethods = new HashMap<String, String>();
	
	private ApplicationClassLoader classLoader;
	
	/*
	 * Constructs a new application with the specified name.
	 */
	ApplicationImpl(String name) {
		this.name = name;
		this.publishTime = new Date();
		this.executableServices = new ConcurrentHashMap<String, HttpService>();
		this.addedServices = new HashSet<HttpService>();
		this.allowedServiceMethods = new HashMap<String, String>();
		this.filterServices = new TreeSet<HttpServiceFilter>();
	}
	
	public String getName() {
		return this.name;
	}
	
	void setName(String name) {
		this.name = name;
	}
	
	public File getBaseDir() {
		return this.baseDir;
	}

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}
	
	void setClassLoader(ApplicationClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	boolean isDirectory() {
		return this.directory;
	}
	
	void setDirectory(boolean directory) {
		this.directory = directory;
	}
	
	boolean isWar() {
		return this.war;
	}
	
	void setWar(boolean war) {
		this.war = war;
	}
	
	boolean isMemory() {
		return this.memory;
	}
	
	void setMemory(boolean memory) {
		this.memory = memory;
	}
	
	boolean isUnpacked() {
		return !this.war && !this.memory;
	}
	
	Date getPublishTime() {
		return this.publishTime;
	}
	
	void addReloadableHttpService(ReloadableHttpService reloadableService) throws ApplicationException {
		Service mapping = reloadableService.getService();
		
		if(mapping == null) {
			throw new ApplicationException("Http service mapping annotation for class '" + reloadableService.getClass().getName() + "' is missing");			
		}
		
		String path = mapping.path();
		String pattern = mapping.pattern();
		String methods = decodeMethods(reloadableService.getClass().getName(), mapping.methods());
		
		addHttpService(path, reloadableService, methods, pattern, mapping);
		
	}
	
	public void addHttpService(HttpService service) throws ApplicationException {
		Service mapping = service.getService();
		
		if(mapping == null) {
			throw new ApplicationException("Http service mapping annotation for class '" + service.getClass().getName() + "' is missing");			
		}
		
		String path = mapping.path();
		String pattern = mapping.pattern();
		String methods = decodeMethods(service.getClass().getName(), mapping.methods());
		
		addHttpService(path, service, methods, pattern, mapping);
	}
	
	public void addHttpService(String path, HttpService service) throws ApplicationException {
		addHttpService(path, service, "HEAD,GET,POST,PUT,DELETE", null, null);
	}
	
	private void addHttpService(String path, HttpService service, String methods, String pattern, Service mapping) throws ApplicationException {
		if(executableServices.containsKey(path)) {
			throw new ApplicationException("Another HTTP service is already bound to path '" + path + "'");
		}
		
		if(path != null && !path.equals("")) {
			addedServices.add(service);
			executableServices.put(path, service);
			allowedServiceMethods.put(path, methods);
		} else if(pattern != null && !pattern.equals("")) {
			validateFilterMethods(service.getClass().getName(), methods);
			filterServices.add(new HttpServiceFilter(service, mapping));
			addedServices.add(service);			
		}
	}

	public HttpService removeHttpService(HttpService service) {
		Service mapping = service.getClass().getAnnotation(Service.class);
		
		if(mapping != null) {
			String path = mapping.path();
			
			if(path != null && !path.equals("")) {
				return removeHttpService(path);
			} else {
				addedServices.remove(service);
				return service;
			}
		}
		
		return null;
	}
	
	public HttpService removeHttpService(String path) {
		HttpService service = executableServices.remove(path);
		
		if(service != null) {
			allowedServiceMethods.remove(path);
			addedServices.remove(service);
			
			while(service instanceof HttpServiceChain) {
				HttpService[] chainedServices = ((HttpServiceChain)service).getServices();
				service = chainedServices[chainedServices.length - 1];
			}
		}
		
		return service;
	}

	public List<HttpService> getHttpServices() {
		int size = addedServices.size();
		ArrayList<HttpService> outServices = new ArrayList<HttpService>(size);
		outServices.addAll(this.addedServices);
		return outServices;
	}
	
	void publish() {
		applyFilters();
	}
	
	private void applyFilters() {
		for(HttpServiceFilter filter : this.filterServices) {
			Set<String> paths = executableServices.keySet();
			
			for(String path : paths) {
				applyFilter(filter, path);
			}
		}		
	}
	
	private void applyFilters(String path) {
		for(HttpServiceFilter filter : this.filterServices) {
			applyFilter(filter, path);
		}
	}
	
	private void applyFilter(HttpServiceFilter filter, String path) {
		if(path.matches(filter.getMapping().pattern())) {
			HttpService service = executableServices.get(path);
			
			if(service instanceof HttpServiceChain) {
				HttpServiceChain chain = (HttpServiceChain)service;
				chain.addServiceFirst(filter.getService());
			} else {
				HttpServiceChain chain = new HttpServiceChain();
				chain.addServiceLast(filter.getService());
				chain.addServiceLast(service);
				executableServices.put(path, chain);
			}
		}		
	}
	
	void unpublish() {
		if(isWar()) {
			cleanup(this.baseDir);
		}
	}
	
	private void cleanup(File dir) {
		File[] files = dir.listFiles();
		
		for(File file : files) {
			if(file.isDirectory()) {
				cleanup(file);
			} else if(file.isFile()) {
				file.delete();
			}
		}
		
		dir.delete();
		
		if(classLoader != null) {
			classLoader.cleanup();
		}		
	}
	
	HttpServiceResult execute(String localPath, HttpRequest request, HttpResponse response) throws HttpException, IOException {
		HttpService httpService = executableServices.get(localPath);
		HttpMethod method = request.getMethod();
		
		if(httpService != null && !method.equals(HttpMethod.OPTIONS)) {
			try {
				return httpService.invoke(request, response);
			} catch(HttpServiceRemovedException e) {
				executableServices.remove(localPath);
				throw new HttpException(e.getStatus(), e.getMessage(), e.getCause());
			}
		}
		
		if(httpService != null && method.equals(HttpMethod.OPTIONS)) {
			String allowed = allowedServiceMethods.get(localPath);
			response.setStatus(HttpStatus.OK);
			response.setContentLength(0);
			response.addHeader("Allow", allowed);
			return HttpServiceResult.DONE;
		}
		
		if(localPath.endsWith(".xsp") && !isMemory()) {
			return compileAndExecuteHttpService(localPath, request, response);
		}
		
		if(isUnpacked() && !staticContentExists(localPath)) {
			if(findHttpService(localPath)) {
				execute(localPath, request, response);
			} else {
				throw new HttpException(HttpStatus.NOT_FOUND, "/" + this.name + localPath);	
			}
		} else {
			executeStaticContent(request, response, localPath);
		}
		
		return HttpServiceResult.DONE;		
	}
	
	private boolean findHttpService(String localPath) {
		List<String> files = new ArrayList<String>();
		File classesBaseDir = new File(this.baseDir, "WEB-INF/classes");
		createFileList(classesBaseDir, classesBaseDir, files);
		
		for(String file : files) {
			
			if(file.endsWith(".class")) {
				String className = file.substring(1, file.length() - 6).replace('/', '.');
				
				try {
					Class<?> clazz = classLoader.loadClass(className);
					
					if(RestService.class.isAssignableFrom(clazz) || HttpService.class.isAssignableFrom(clazz)) {
						Service mapping = clazz.getAnnotation(Service.class);
						
						if(mapping != null) {
							String path = mapping.path();
							
							if(localPath.equals(path)) {
								HttpService service = (HttpService)clazz.newInstance();
								File classFile = new File(classesBaseDir, file);
								ReloadableHttpService reloadableService = new ReloadableHttpService(service, classLoader, className, classFile);
								addReloadableHttpService(reloadableService);
								applyFilters(path);
								return true;
							}
						}
						
					}
				} catch(ClassNotFoundException e) {
				} catch(Exception e) {
					return false;
				}
			}
		}
		
		return false;
	}
	
	private void createFileList(File baseDir, File dir, List<String> files) {
		if(!dir.exists()) {
			return;
		}
		
		File[] subFiles = dir.listFiles();
		
		for(File subFile : subFiles) {
			String subFileName = subFile.getAbsolutePath();
			String baseDirName = baseDir.getAbsolutePath();
			files.add(subFileName.substring(baseDirName.length()));
			
			if(subFile.isDirectory()) {
				createFileList(baseDir, subFile, files);
			}
		}
	}
	
	private HttpServiceResult compileAndExecuteHttpService(String localPath, HttpRequest request, HttpResponse response) throws IOException, HttpException {
		try {
			XspCompiler compiler = new XspCompiler(this.classLoader);
			File webInfDir = new File(this.baseDir, "WEB-INF");
			File xspFile = new File(webInfDir, localPath);
			
			if(!xspFile.exists()) {
				throw new HttpException(HttpStatus.NOT_FOUND, localPath);
			}
			
			File classesDir = new File(this.baseDir, "WEB-INF/classes");
			
			if(!classesDir.exists()) {
				throw new HttpException(HttpStatus.NOT_FOUND, localPath);			
			}
			
			XspService service = compiler.compileService(webInfDir.getAbsolutePath(), xspFile.getAbsolutePath(), classesDir.getPath());
			
			if(service == null) {
				throw new HttpException(HttpStatus.NOT_FOUND, localPath);
			}
			
			Class<?> serviceClazz = service.getClass();
			Service mapping = serviceClazz.getAnnotation(Service.class);
			String path = mapping.path();
			
			HttpService compilableService = new CompilableXspService(this.classLoader, service, webInfDir, xspFile, classesDir);
			executableServices.put(path, compilableService);
			return service.invoke(request, response);
		} catch(XspException e) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "Compilation failed");
		}
	}
	
	private void executeStaticContent(HttpRequest request, HttpResponse response, String localPath) throws IOException, HttpException {
		File contentFile = new File(this.baseDir, localPath);
		
		if(!contentFile.exists()) {
			throw new HttpException(HttpStatus.NOT_FOUND, "/" + this.name + localPath);			
		}
		
		HttpMethod method = request.getMethod();
		
		if(!method.isSupportedByStaticContent()) {
			throw new HttpException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed " + method);
		}
		
		if(method.equals(HttpMethod.OPTIONS)) {
			executeStaticContentOptions(response);
			return;
		}
		
		StaticContent content = new StaticContent(this.baseDir, localPath);
		content.execute(request, response);
	}
	
	private boolean staticContentExists(String localPath) {
		File contentFile = new File(this.baseDir, localPath);
		return contentFile.exists();
	}
	
	private void executeStaticContentOptions(HttpResponse response) {
		response.setStatus(HttpStatus.OK);
		response.setContentLength(0);
		response.addHeader("Allow", "GET, HEAD, OPTIONS");
	}
	
	private void validateFilterMethods(String serviceName, String methodSpec) throws ApplicationException {
		HttpMethod[] expectedMethods = { HttpMethod.HEAD, HttpMethod.GET, HttpMethod.POST, 
				HttpMethod.PUT, HttpMethod.DELETE };
		
		for(HttpMethod method : expectedMethods) {
			if(methodSpec.indexOf(method.toString()) == -1) {
				throw new ApplicationException("Filters " + serviceName + " does not support method " + 
						method + ", filters must support all methods");
			}
		}
	}
	
	private String decodeMethods(String serviceName, String methodSpec) throws ApplicationException {
		String[] supportedMethods = methodSpec.split(",");
		StringBuffer outMethods = new StringBuffer();
		boolean first = true;
		
		for(int i = 0; i < supportedMethods.length; i++) {
			supportedMethods[i] = supportedMethods[i].trim();
			
			if(!methods.contains(HttpMethod.valueOf(supportedMethods[i]))) {
				throw new ApplicationException("Invalid method " + supportedMethods[i] + 
						" in annotation for service " + serviceName);
			}
			
			if(!first) {
				outMethods.append(", ");
			}
			
			outMethods.append(supportedMethods[i]);
			first = false;
		}
		
		return outMethods.toString();
	}
	
	private class HttpServiceFilter implements Comparable<HttpServiceFilter> {
		
		private HttpService service;
		
		private Service mapping;
		
		private HttpServiceFilter(HttpService service, Service mapping) {
			this.service = service;
			this.mapping = mapping;
		}
		
		HttpService getService() {
			return this.service;
		}
		
		Service getMapping() {
			return this.mapping;
		}
		
		public int hashCode() {
			String id = mapping.pattern() + mapping.index();
			return id.hashCode();
		}
		
		public boolean equals(Object o) {
			if(o instanceof HttpServiceFilter) {
				HttpServiceFilter other = (HttpServiceFilter)o;
				Service otherMapping = other.getMapping();
				return otherMapping.pattern().equals(mapping.pattern()) && otherMapping.index() == mapping.index();
			}
			
			return super.equals(o);
		}
		
		public int compareTo(HttpServiceFilter filter) {
			Service filterMapping = filter.getMapping();
			
			if(mapping.index() == filterMapping.index()) {
				return mapping.pattern().compareTo(filterMapping.pattern());
			}
			
			return mapping.index() - filterMapping.index();
		}
	}	
}
