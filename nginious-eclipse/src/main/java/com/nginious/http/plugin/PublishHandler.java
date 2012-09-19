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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.ui.jarpackager.IJarBuilder;
import org.eclipse.jdt.ui.jarpackager.JarPackageData;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.nginious.http.client.HttpClientException;
import com.nginious.http.client.HttpClientResponse;
import com.nginious.http.upload.ApplicationUploader;
import com.nginious.http.upload.ProgressListener;

public class PublishHandler extends AbstractHandler {
	
	public PublishHandler() {
		super();
	}
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		Shell shell = window.getShell();
		ISelectionService service = window.getSelectionService();
		ISelection selection = service.getSelection();
		IProject project = extractProject(selection);
		
		if(project != null) {
			publishProject(shell, project);
		}
		
		return null;
	}	
	
	private void publishProject(Shell shell, IProject project) {
		IRunnableContext context = new ProgressMonitorDialog(shell);
		
		try {
			Publisher publisher = new Publisher(shell, project);
			context.run(true, false, publisher);
		} catch(InterruptedException e) {
		} catch(InvocationTargetException e) {
			String title = Messages.PublishHandler_error_title;
			Object[] args = { project.getName() };
			String message = MessageFormat.format(Messages.PublishHandler_error_message, args);
			MessagesUtils.displayMessageDialog(e.getMessage(), null, title, message);
		}
	}
	
	private IProject extractProject(ISelection selection) {
		if(!(selection instanceof IStructuredSelection)) {
			return null;
		}
		
		IStructuredSelection structuredSelection = (IStructuredSelection)selection;
		Object element = structuredSelection.getFirstElement();
		
		if(element instanceof IProject) {
			return (IProject)element;
		}
		
		if(element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable)element;
			Object adapter = adaptable.getAdapter(IResource.class);
			
			if(adapter instanceof IProject) {
				return (IProject)adapter;
			}
			
		}
		
		return null;
	}
	
	private class Publisher implements IRunnableWithProgress, ProgressListener {
		
		private Shell shell;
		
		private IProject project;
		
		private IProgressMonitor monitor;
		
		private Publisher(Shell shell, IProject project) {
			this.shell = shell;
			this.project = project;
		}
		
		public void run(IProgressMonitor monitor) {
			this.monitor = monitor;
			File warFile = null;
			monitor.beginTask(Messages.PublishHandler_begin_message, 110);
			
			try {
				String publishUrl = project.getPersistentProperty(NginiousPlugin.PUBLISH_URL_PROP_KEY);
				
				if(publishUrl == null) {
					publishUrl = NginiousPlugin.DEFAULT_PUBLISH_URL;
				}
				
				String publishUsername = project.getPersistentProperty(NginiousPlugin.PUBLISH_USERNAME_PROP_KEY);
				
				if(publishUsername == null) {
					publishUsername = NginiousPlugin.DEFAULT_PUBLISH_USERNAME;
				}
				
				String publishPassword = project.getPersistentProperty(NginiousPlugin.PUBLISH_PASSWORD_PROP_KEY);
				
				if(publishPassword == null) {
					publishPassword = NginiousPlugin.DEFAULT_PUBLISH_PASSWORD;
				}
				
				monitor.setTaskName(Messages.PublishHandler_creating_war_message);
				warFile = createWebApplicationArchive(this.shell, monitor, this.project);
				
				if(warFile != null) {
					monitor.setTaskName(Messages.PublishHandler_upload_message);
					monitor.worked(10);
					URL url = new URL(publishUrl);
					ApplicationUploader applicationPublisher = new ApplicationUploader(this, url, warFile, publishUsername, publishPassword);
					HttpClientResponse response = applicationPublisher.upload();
					
					RollbackState state = RollbackState.getInstance();
					state.setState(this.project, response);
					
					monitor.worked(110);
					monitor.done();
				} else {
					monitor.setCanceled(true);
				}
			} catch(CoreException e) {
				monitor.setCanceled(true);
				String title = Messages.PublishHandler_error_title;
				Object[] args = { project.getName() };
				String message = MessageFormat.format(Messages.PublishHandler_error_message, args);
				MessagesUtils.perform(e, shell, title, message);
			} catch(HttpClientException e) {
				monitor.setCanceled(true);
				String title = Messages.PublishHandler_error_title;
				Object[] args = { project.getName() };
				String message = MessageFormat.format(Messages.PublishHandler_error_message, args);
				MessagesUtils.displayMessageDialog(e.getMessage(), null, title, message);
			} catch(IOException e) {
				monitor.setCanceled(true);
				String title = Messages.PublishHandler_error_title;
				Object[] args = { project.getName() };
				String message = MessageFormat.format(Messages.PublishHandler_error_message, args);
				MessagesUtils.displayMessageDialog(e.getMessage(), null, title, message);
			} finally {
				if(warFile != null && warFile.exists()) {
					warFile.delete();
				}
			}			
		}

		public void progress(int progress) {
			monitor.worked(10 + progress);
		}
	}
	
	private File createWebApplicationArchive(Shell shell, IProgressMonitor monitor, IProject project) throws CoreException, IOException {
		IJarBuilder builder = null;
		String tmpFile = null;
		boolean done = false;
		
		try {
			IFolder folder = project.getFolder("WebContent");
			ArrayList<IFile> fileList = new ArrayList<IFile>();
			findWebApplicationFiles(folder, fileList);
			IFile[] files = fileList.toArray(new IFile[fileList.size()]);
			
			tmpFile = createTempWebApplicationFile();
			JarPackageData description= new JarPackageData();
			IPath location = new Path(tmpFile);
			description.setJarLocation(location);
			description.setSaveManifest(false);
			description.setElements(files);
			
			builder = description.createFatJarBuilder();
			builder.open(description, shell, null);
			int index = 0;
			int items = files.length / 10;
			
			if(items == 0) {
				items = 1;
			}
			
			for(IFile file : files) {
				IPath path = file.getFullPath();
				path = path.removeFirstSegments(2);
				builder.writeFile(file, path);
				
				index++;
				int worked = index / items;
				monitor.worked(worked);
			}
			
			done = true;
			return new File(tmpFile);
		} finally {
			if(builder != null) {
				builder.close();
			}
			
			if(!done) {
				File tmp = new File(tmpFile);
				
				if(tmp.exists()) {
					tmp.delete();
				}
			}
		}
	}
	
	private String createTempWebApplicationFile() throws IOException {
		File tmpFile = File.createTempFile("nginious", ".war");
		tmpFile.delete();
		return tmpFile.toString();
	}
	
	private void findWebApplicationFiles(IFolder folder, List<IFile> files) throws CoreException {
		IResource[] resources = folder.members(IResource.FILE | IResource.FOLDER);
		
		for(IResource resource : resources) {
			if(resource.getType() == IResource.FOLDER) {
				findWebApplicationFiles((IFolder)resource, files);
			} else if(resource.getType() == IResource.FILE) {
				files.add((IFile)resource);
			}
		}
	}
}
