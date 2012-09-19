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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

class NginiousUtils {

	static IProject extractProject(ISelection selection) {
		if(!(selection instanceof IStructuredSelection)) {
			return null;
		}
		
		IProject project = null;
		IStructuredSelection structuredSelection = (IStructuredSelection)selection;
		Object element = structuredSelection.getFirstElement();
		
		if(element instanceof IProject) {
			project = (IProject)element;
		}
		
		if(project == null && (element instanceof IAdaptable)) {
			IAdaptable adaptable = (IAdaptable)element;
			Object adapter = adaptable.getAdapter(IResource.class);
			
			if(adapter instanceof IProject) {
				project = (IProject)adapter;
			}
			
		}
		
		try {
			return project != null && project.hasNature(NginiousPlugin.NATURE_ID) ? project : null;
		} catch(CoreException e) {
			return null;
		}
	}
}
