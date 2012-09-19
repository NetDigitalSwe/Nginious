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
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

abstract class LogView extends ViewPart implements ISelectionListener {

	protected TextViewer viewer;
	
	LogView() {
		super();
	}

	public void createPartControl(Composite parent) {
		viewer = new TextViewer(parent, SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setDocument(new Document());
		viewer.setEditable(false);
		viewer.setInput(getViewSite());

		ISelectionService service = getSite().getWorkbenchWindow().getSelectionService();
		service.addPostSelectionListener(this);
	}
	
	public void dispose() {
		ISelectionService service = getSite().getWorkbenchWindow().getSelectionService();
		service.removePostSelectionListener(this);
		super.dispose();
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IProject project = NginiousUtils.extractProject(selection);
		
		if(project != null) {
			Document document = getDocument(project);
			
			if(document != null) {
				viewer.setDocument(document);
			}
		}
	}
	
	protected abstract Document getDocument(IProject project);
}
