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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IStartup;

public class Startup implements IStartup {

	public void earlyStartup() {
		try {
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			ServerManager manager = ServerManager.getInstance();
			
			for(IProject project : projects) {
				if(project.isOpen() && project.hasNature(NginiousPlugin.NATURE_ID)) {
					manager.startServer(project);
				}
			}
			
			RollbackState.getInstance().initSelectionListener();
			PublishState.getInstance().initSelectionListener();
		} catch(CoreException e) {
			String title = Messages.Startup_op_error_title;
			String message = Messages.Startup_op_error_message;
			MessagesUtils.perform(e, null, title, message);
		}
	}
}
