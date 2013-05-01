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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class NewProjectWizard extends Wizard implements INewWizard {

	private IWorkbench workbench;
	
	private IStructuredSelection selection;
	
	private NewProjectPageOne pageOne;
	
	private IConfigurationElement configurationElement;
	
	public NewProjectWizard() {
		super();
		setWindowTitle(Messages.NewProjectWizard_title);
		setNeedsProgressMonitor(true);
	}
	
	public void addPages() {
		if(this.pageOne == null) {
			pageOne= new NewProjectPageOne();
		}
		
		addPage(this.pageOne);
		pageOne.init(getSelection(), getActivePart());
	}
	
	protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
		return;
	}
	
	private IWorkbenchPart getActivePart() {
		IWorkbenchWindow activeWindow = getWorkbench().getActiveWorkbenchWindow();
		
		if(activeWindow != null) {
			IWorkbenchPage activePage= activeWindow.getActivePage();
			
			if(activePage != null) {
				return activePage.getActivePart();
			}
		}
		
		return null;
	}
	
	protected void handleFinishException(Shell shell, InvocationTargetException e) {
		String title = Messages.NewProjectWizard_op_error_title;
		String message= Messages.NewProjectWizard_op_error_create_message;
		MessagesUtils.handle(e, getShell(), title, message);
	}
	
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) {
		this.configurationElement = config;
	}
	
	public boolean performCancel() {
		return super.performCancel();
	}
	
	public boolean performFinish() {
	    String name = pageOne.getProjectName();
        IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
        
        if(newProject.exists()) {
        	throw new RuntimeException("Project exists!");
        }
        
        try {        	
    	    int listenPort = pageOne.getListenPort();
    	    
    	    if(!pageOne.validate(listenPort)) {
    	    	return false;
    	    }
    	    
    	    IProgressMonitor progressMonitor = new NullProgressMonitor();
        	
        	// Create project
        	newProject.create(progressMonitor);
            newProject.open(progressMonitor);        	

        	// Create folder structure
            String[] paths = { "src", "WebContent", "WebContent/WEB-INF", 
            		"WebContent/WEB-INF/classes", "WebContent/WEB-INF/lib", "WebContent/WEB-INF/xsp" };
            addToProjectStructure(newProject, paths);
            
        	ClassPathBuilder builder = new ClassPathBuilder(newProject, progressMonitor);
        	builder.build(progressMonitor);
        	
        	// Set project nature
        	IProjectDescription description = newProject.getDescription();
        	description.setNatureIds(new String[] { JavaCore.NATURE_ID, NginiousPlugin.NATURE_ID });
        	newProject.setDescription(description, null);
        	
            // Create java project
            IJavaProject javaProject = JavaCore.create(newProject);
            
            // Set classes output folder
        	IFolder classesFolder = newProject.getFolder("WebContent/WEB-INF/classes");
        	javaProject.setOutputLocation(classesFolder.getFullPath(), null);
        	
        	// Set classpath
        	IClasspathEntry[] entries = builder.getClassPath();
        	javaProject.setRawClasspath(entries, progressMonitor);
        	
        	// Set source folder
        	IFolder sourceFolder = newProject.getFolder("src");
        	IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(sourceFolder);
        	IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        	IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
        	System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
        	newEntries[oldEntries.length] = JavaCore.newSourceEntry(root.getPath());
        	javaProject.setRawClasspath(newEntries, null);
    	    BasicNewProjectResourceWizard.updatePerspective(this.configurationElement);    
    	    
    	    newProject.setPersistentProperty(NginiousPlugin.LISTEN_PORT_PROP_KEY, Integer.toString(listenPort));
    	    newProject.setPersistentProperty(NginiousPlugin.PUBLISH_URL_PROP_KEY, pageOne.getPublishUrl());
    	    newProject.setPersistentProperty(NginiousPlugin.PUBLISH_USERNAME_PROP_KEY, pageOne.getPublishUsername());
    	    newProject.setPersistentProperty(NginiousPlugin.PUBLISH_PASSWORD_PROP_KEY, pageOne.getPublishPassword());   
    	    newProject.setPersistentProperty(NginiousPlugin.MIN_MEMORY_PROP_KEY, Integer.toString(pageOne.getMinMemory()));
    	    newProject.setPersistentProperty(NginiousPlugin.MAX_MEMORY_PROP_KEY, Integer.toString(pageOne.getMaxMemory()));
        } catch(Exception e) {
        	throw new RuntimeException(e.getMessage(), e);
        }

	    return true;
	}
	
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		this.workbench= workbench;
		this.selection= currentSelection;
	}

	public IStructuredSelection getSelection() {
		return this.selection;
	}

	public IWorkbench getWorkbench() {
		return this.workbench;
	}
	
	protected void selectAndReveal(IResource newResource) {
		BasicNewResourceWizard.selectAndReveal(newResource, workbench.getActiveWorkbenchWindow());
	}
	
	protected ISchedulingRule getSchedulingRule() {
		return ResourcesPlugin.getWorkspace().getRoot(); // look all by default
	}
	
	private void createFolder(IFolder folder) throws CoreException {
        IContainer parent = folder.getParent();
        
        if(parent instanceof IFolder) {
            createFolder((IFolder)parent);
        }
        
        if(!folder.exists()) {
            folder.create(false, true, null);
        }
    }

    private void addToProjectStructure(IProject newProject, String[] paths) throws CoreException {
        for(String path : paths) {
            IFolder etcFolders = newProject.getFolder(path);
            createFolder(etcFolders);
        }
    }	
}