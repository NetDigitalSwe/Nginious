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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class ProjectPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
	
	private PropertiesBlock block;
	
	private IProject project;
	
	public ProjectPropertyPage() {
		super();
	}
	
	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);
		
		IAdaptable element = this.getElement();
		
		if(element instanceof IJavaProject) {
			IJavaProject javaProject = (IJavaProject)element;
			this.project = javaProject.getProject();
		} else {
			this.project = (IProject)element;
		}
		
		final Composite composite= new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(initGridLayout(new GridLayout(1, false), true));
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		
		int horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		int verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		int marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		int marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		this.block = new PropertiesBlock(this.project, horizontalSpacing, verticalSpacing, marginWidth, marginHeight);
		block.createControls(composite);
		
		return composite;
	}
	
	protected void performApply() {
		super.performApply();
		
		int listenPort = block.getListenPort();
		
		if(!checkListenPort(listenPort)) {
			return;
		}
		
		String publishUrl = block.getPublishUrl();
		String publishUsername = block.getPublishUsername();
		String publishPassword = block.getPublishPassword();
		int minMemory = block.getMinMemory();
		int maxMemory = block.getMaxMemory();
		setProjectProperties(listenPort, publishUrl, publishUsername, publishPassword, minMemory, maxMemory);
	}
	
	
	
	public boolean performCancel() {
		boolean result = super.performCancel();
		
		if(result) {
			int listenPort = block.getInitialListenPort();
			String publishUrl = block.getInitialPublishUrl();
			String publishUsername = block.getInitialPublishUsername();
			String publishPassword = block.getInitialPublishPassword();	
			int minMemory = block.getInitialMinMemory();
			int maxMemory = block.getInitialMaxMemory();
			setProjectProperties(listenPort, publishUrl, publishUsername, publishPassword, minMemory, maxMemory);
		}
		
		return result;
	}

	public boolean performOk() {
		boolean result = super.performOk();
		
		if(result) {
			int listenPort = block.getListenPort();
			
			if(!checkListenPort(listenPort)) {
				return false;
			}
			
			String publishUrl = block.getPublishUrl();
			String publishUsername = block.getPublishUsername();
			String publishPassword = block.getPublishPassword();
			int minMemory = block.getMinMemory();
			int maxMemory = block.getMaxMemory();
			setProjectProperties(listenPort, publishUrl, publishUsername, publishPassword, minMemory, maxMemory);
		}
		
		return result;
	}
	
	private boolean checkListenPort(int listenPort) {
		ServerManager manager = ServerManager.getInstance();
		IProject project = manager.checkListenPortUsage(listenPort);
		
		if(project != null && !project.equals(this.project)) {
			String title = Messages.ProjectPropertyPage_listen_port_error_title;
			String message = Messages.ProjectPropertyPage_listen_port_error_message + " " + project.getName();
			MessagesUtils.displayMessageDialog("", getShell(), title, message);
			return false;
		}
		
		return true;
	}
	
	private boolean setProjectProperties(int listenPort, String publishUrl, String publishUsername, 
			String publishPassword, int minMemory, int maxMemory) {
		IProject project = null;
		IAdaptable element = getElement();
		
		if(element instanceof IJavaProject) {
			IJavaProject javaProject = (IJavaProject)element;
			project = javaProject.getProject();
		} else {
			project = (IProject)element;
		}
		
		try {
			project.setPersistentProperty(NginiousPlugin.LISTEN_PORT_PROP_KEY, Integer.toString(listenPort));
			project.setPersistentProperty(NginiousPlugin.PUBLISH_URL_PROP_KEY, publishUrl);
			project.setPersistentProperty(NginiousPlugin.PUBLISH_USERNAME_PROP_KEY, publishUsername);
			project.setPersistentProperty(NginiousPlugin.PUBLISH_PASSWORD_PROP_KEY, publishPassword);
			project.setPersistentProperty(NginiousPlugin.MIN_MEMORY_PROP_KEY, Integer.toString(minMemory));
			project.setPersistentProperty(NginiousPlugin.MAX_MEMORY_PROP_KEY, Integer.toString(maxMemory));
			
			if(block.hasChanged()) {
				ServerManager manager = ServerManager.getInstance();
				manager.restartServer(project);
			}
			
			return true;
		} catch(CoreException e) {
			String title = Messages.ProjectPropertyPage_save_error_title;
			String message = Messages.ProjectPropertyPage_save_error_message;
			MessagesUtils.perform(e, getShell(), title, message);
			return false;
		}		
	}

	private GridLayout initGridLayout(GridLayout layout, boolean margins) {
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		
		if(margins) {
			layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
			layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		} else {
			layout.marginWidth = 0;
			layout.marginHeight = 0;
		}
		return layout;
	}	
}
