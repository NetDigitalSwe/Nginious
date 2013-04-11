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

package com.nginious.http.plugin;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;

class ServerManager implements IResourceChangeListener {
	
	private static ServerManager manager = null;
	
	private static Object lock = new Object();
	
	private ConcurrentHashMap<String, HttpServerEnvironment> servers;
	
	private Logger logger;
	
	private ServerManager() {
		super();
		this.servers = new ConcurrentHashMap<String, HttpServerEnvironment>();
		int eventMask = IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE;
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, eventMask);
		this.logger = new Logger("/tmp/nginious.out");
	}
	
	static ServerManager getInstance() {
		synchronized(lock) {
			if(manager == null) {
				manager = new ServerManager();
			}
			
			return manager;
		}
		
	}
	
	Logger getLogger() {
		return this.logger;
	}
	
	public void resourceChanged(IResourceChangeEvent event) {
		logger.log("ENTER ServerManager.resourceChanged event={0}", event);
		
		int type = event.getType();
		
		switch(type) {
		case IResourceChangeEvent.PRE_CLOSE:
		case IResourceChangeEvent.PRE_DELETE:
			IProject project = (IProject)event.getResource();
			stopServer(project);
			break;
			
		case IResourceChangeEvent.POST_CHANGE:
			IResourceDelta delta = event.getDelta();
			IResourceDelta[] children = delta.getAffectedChildren();
			
			if(children != null && children.length > 0) {
				for(IResourceDelta child : children) {
					IResource resource = child.getResource();
					
					if(resource instanceof IProject) {
						IProject changeProject = (IProject)resource;
						
						if(projectHasChanged(changeProject)) {
							stopServer(changeProject);
							startServer(changeProject);
						}
					}
				}
			}
			break;
		}
		
		logger.log("EXIT ServerManager.resourceChanged");
	}
	
	private boolean projectHasChanged(IProject project) {
		logger.log("ENTER ServerManager.projectHasChanged project={0}", project);
		HttpServerEnvironment env = servers.get(project.getName());
		
		if(env != null) {
			boolean changed = env.hasChanged();
			logger.log("EXIT ServerManager.projectHasChanged changed={0}", changed);
			return changed;
		}
		
		// Verify that project has been fully created
		IFolder folder = project.getFolder("WebContent");
		
		if(!folder.exists()) {
			logger.log("EXIT ServerManager.projectHasChanged changed=false");
			return false;
		}
		
		boolean open = project.isOpen();
		logger.log("EXIT ServerManager.projectHAsChanged open={0}", open);
		return open;
	}
	
	void stopAllServers() {
		logger.log("ENTER ServerManager.stopAllServers");
		
		for(HttpServerEnvironment env : servers.values()) {
			HttpServerProcess serverProcess = env.getServerProcess();
			serverProcess.stop();
		}
		
		logger.log("EXIT ServerManager.stopAllServers");
	}
	
	void stopServer(IProject project) {
		logger.log("ENTER ServerManager.stopServer project={0}", project);
		
		HttpServerEnvironment env = servers.remove(project.getName());
		
		if(env != null) {
			HttpServerProcess serverProcess = env.getServerProcess();
			serverProcess.stop();
		}
		
		servers.clear();
		logger.log("EXIT SeverManager.stopServer");
	}
	
	void restartServer(IProject project) {
		logger.log("ENTER ServerManager.restartServer project={0}", project);
		stopServer(project);
		startServer(project);
		logger.log("EXIT ServerManager.restartServer");
	}
	
	boolean updateProjectWithPluginVersion(IProject project) {
		logger.log("ENTER ServerManager.updateProjectWithPluginVersion project={0}", project);
		
		try {
			URL apiJar = NginiousPlugin.getApiJar();
			String filePath = apiJar.toString();
			filePath = filePath.substring(5);
			Path apiJarPath = new Path(filePath);
			
			IJavaProject javaProject = JavaCore.create(project);
			IClasspathEntry[] entries = javaProject.getRawClasspath();
			ArrayList<IClasspathEntry> newEntries = new ArrayList<IClasspathEntry>();
			boolean changed = false;
			// javaProject.save
			
			for(IClasspathEntry entry : entries) {
				if(entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					IPath path = entry.getPath();
					
					if(path.lastSegment() != null && path.lastSegment().endsWith("nginious-api.jar")) {
						changed = true;
						entry = JavaCore.newLibraryEntry(apiJarPath, null, null);						
					}
				}
				
				newEntries.add(entry);
			}
			
			if(changed) {
				IProgressMonitor progress = new NullProgressMonitor();
				entries = newEntries.toArray(new IClasspathEntry[newEntries.size()]);
				javaProject.setRawClasspath(entries, progress);
				javaProject.save(progress, true);
			}
			
			logger.log("EXIT ServerManager.updateProjectWithPluginVersion changed={0}", changed);
			return changed;
		} catch(JavaModelException e) {
			logger.log("ServerManager.updateProkectWithPluginVersion exception", e);
			return false;
		} catch(IOException e) {
			logger.log("ServerManager.updateProkectWithPluginVersion exception", e);
			return false;
		}
	}
	
	void startServer(IProject project) {
		logger.log("ENTER ServerManager.startServer project={0}", project);
		int listenPort = NginiousPlugin.DEFAULT_LISTEN_PORT;
		String publishUrl = NginiousPlugin.DEFAULT_PUBLISH_URL;
		String publishUsername = NginiousPlugin.DEFAULT_PUBLISH_USERNAME;
		String publishPassword = NginiousPlugin.DEFAULT_PUBLISH_PASSWORD;
		
		try {
			String listenPortStr = project.getPersistentProperty(NginiousPlugin.LISTE_PORT_PROP_KEY);
			
			if(listenPortStr != null) {
				listenPort = Integer.parseInt(listenPortStr);
			}
			
			publishUrl = project.getPersistentProperty(NginiousPlugin.PUBLISH_URL_PROP_KEY);
			publishUsername = project.getPersistentProperty(NginiousPlugin.PUBLISH_USERNAME_PROP_KEY);
			publishPassword = project.getPersistentProperty(NginiousPlugin.PUBLISH_PASSWORD_PROP_KEY);
		} catch(NumberFormatException e) {
			String title = Messages.ServerManager_listen_port_error_title;
			String message = Messages.ServerManager_listen_port_error_message + " " + project.getName();
			MessagesUtils.displayMessageDialog(e.getMessage(), null, title, message);
			logger.log("ServerManager.startServer exception", e);
		} catch(CoreException e) {
			String title = Messages.ServerManager_properties_error_title;
			String message = Messages.ServerManager_listen_port_error_message + " " + project.getName();
			MessagesUtils.perform(e, null, title, message);
			logger.log("ServerManager.startServer exception", e);
		} catch(Throwable t) {
			logger.log("ServerManager.startServer exception", t);
		}
		
		try {
			IPath projectPath = project.getLocation();
			IPath webappsPath = projectPath.append("WebContent");
			HttpServerProcess serverProcess = new HttpServerProcess(project.getName(), listenPort, publishPassword, webappsPath.toFile(), this.logger);
			serverProcess.start();
			
			LogViewConsumer accessLogConsumer = new LogViewConsumer(serverProcess.getAccessLogPath());
			LogViewConsumer messageLogConsumer = new LogViewConsumer(serverProcess.getServerLogPath());
			
			HttpServerEnvironment env = new HttpServerEnvironment(project, serverProcess, accessLogConsumer, messageLogConsumer);
			env.setPort(listenPort);
			env.setPublishUrl(publishUrl);
			env.setPublishUsername(publishUsername);
			env.setPublishPassword(publishPassword);
			servers.put(project.getName(), env);

			logger.log("EXIT ServerManager.startServer");
		} catch(IOException e) {
			String title = Messages.ServerManager_server_error_title;
			String message = Messages.ServerManager_server_error_message + " " + project.getName();
			MessagesUtils.displayMessageDialog(e.getMessage(), null, title, message);
			logger.log("ServerManager.startServer exception", e);
		} catch(Throwable t) {
			logger.log("ServerManager.startServer exception", t);
		}
	}
	
	IProject checkListenPortUsage(int listenPort) {
		logger.log("ENTER ServerManager.checkListenPortUsage listenPort={0}", listenPort);
		Collection<HttpServerEnvironment> envs = servers.values();
		
		for(HttpServerEnvironment env : envs) {
			if(env.getPort() == listenPort) {
				IProject project = env.getProject();
				logger.log("EXIT ServerMamager.checkListenPortUsage project={0}", project);
			}
		}
		
		logger.log("EXIT ServerManager.checkListenPortUsage project=null");
		return null;
	}
	
	Document getMessageLogDocument(IProject project) {
		logger.log("ENTER ServerManager.getMessageLogDocument project=", project);
		HttpServerEnvironment env = servers.get(project.getName());
		
		if(env != null) {
			LogViewConsumer consumer = env.getMessageLogConsumer();
			
			if(consumer != null) {
				Document doc =  consumer.getDocument();
				logger.log("EXIT ServerManager.getMessageLogDocument document={0}", doc);
				return doc;
			}
		}
		
		logger.log("EXIT ServerManager.getMessageLogDocument document=null");
		return null;
	}
	
	Document getAccessLogDocument(IProject project) {
		logger.log("ENTER ServerManager.getAccessLogDocument project={0}", project);
		HttpServerEnvironment env = servers.get(project.getName());
		
		if(env != null) {
			LogViewConsumer consumer = env.getAccessLogConsumer();
			
			if(consumer != null) {
				Document doc = consumer.getDocument();
				logger.log("EXIT ServerManager.getAccessLogDocument document={0}", doc);
				return doc;
			}
		}
		
		logger.log("EXIT ServerManager.getAccessLogDocument document=null");
		return null;
	}
	
	private class HttpServerEnvironment {
		
		private IProject project;
		
		private int port;
		
		@SuppressWarnings("unused")
		private String publishUrl;
		
		@SuppressWarnings("unused")
		private String publishUsername;
		
		private String publishPassword;
		
		private HttpServerProcess serverProcess;
		
		private LogViewConsumer accessLogConsumer;
		
		private LogViewConsumer messageLogConsumer;
		
		private HttpServerEnvironment(IProject project, HttpServerProcess serverProcess, LogViewConsumer accessLogConsumer, LogViewConsumer messageLogConsumer) {
			super();
			this.project = project;
			this.serverProcess = serverProcess;
			this.accessLogConsumer = accessLogConsumer;
			this.messageLogConsumer = messageLogConsumer;
		}
		
		private IProject getProject() {
			return this.project;
		}
		
		private void setPort(int port) {
			this.port = port;
		}
		
		private int getPort() {
			return this.port;
		}
		
		private void setPublishUrl(String publishUrl) {
			this.publishUrl = publishUrl;
		}

		private void setPublishUsername(String publishUsername) {
			this.publishUsername = publishUsername;
		}

		private void setPublishPassword(String publishPassword) {
			this.publishPassword = publishPassword;
		}
		
		private HttpServerProcess getServerProcess() {
			return this.serverProcess;
		}
		
		private LogViewConsumer getMessageLogConsumer() {
			return this.messageLogConsumer;
		}
		
		private LogViewConsumer getAccessLogConsumer() {
			return this.accessLogConsumer;
		}
		
		private boolean hasChanged() {
			try {
				String newPort = project.getPersistentProperty(NginiousPlugin.LISTE_PORT_PROP_KEY);
				String newPublishPassword = project.getPersistentProperty(NginiousPlugin.PUBLISH_PASSWORD_PROP_KEY);
				
				if(!Integer.toString(this.port).equals(newPort)) {
					return true;
				}
				
				if(!publishPassword.equals(newPublishPassword)) {
					return true;
				}
				
				return false;
			} catch(CoreException e) {
				return false;
			}
		}
	}	
}
