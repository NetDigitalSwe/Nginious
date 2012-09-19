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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

public class NewXspFileWizard extends Wizard implements INewWizard {
	
	private IWorkbench workbench;
	
	private IStructuredSelection selection;
	
	private NewXspFileCreationPage pageOne;
	
	public NewXspFileWizard() {
		setWindowTitle(Messages.NewXspFileWizard_title);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
	}
	
	public void addPages() {
		super.addPages();
		
		this.pageOne = new NewXspFileCreationPage(this.selection);
		addPage(this.pageOne);
	}
	
	protected InputStream getInitialContents() {
		String xmlTemplate = "<hc-schema>\n"
			+ "  <tables></tables>\n"
			+ "  <filters></filters>\n"
			+ "  <views></views>\n"
			+ "</hc-schema>\n";
		return new ByteArrayInputStream(xmlTemplate.getBytes());
	}
	
	public boolean performFinish() {
		boolean result = false;
		
		IFile file = pageOne.createNewFile();
		result = file != null;
		
		if (result) {
			try {
				IDE.openEditor(workbench.getActiveWorkbenchWindow().getActivePage(), file);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		} // else no file created...result == false
		
		return result;
	}
}
