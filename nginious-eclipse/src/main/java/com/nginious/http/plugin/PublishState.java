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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class PublishState extends AbstractSourceProvider implements ISelectionListener {
	
	public final static String STATE = "com.nginious.http.plugin.publishState";
	
	public final static String ACTIVE = "ACTIVE";
	
	public final static String INACTIVE = "INACTIVE";
	
	public final static String NONE = "NONE";
	
	private static PublishState state = null;
	
	private static Object lock = new Object();
	
	private String curState;
	
	public PublishState() {
		synchronized(lock) {
			if(state != null) {
				throw new RuntimeException("Only one publish state allowed");
			}
			
			state = this;
		}
		
		this.curState = NONE;
	}
	
	static PublishState getInstance() {
		return state;
	}
	
	void initSelectionListener() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		ISelectionService service = windows[0].getSelectionService();
		service.addPostSelectionListener(this);		
	}
	
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IProject project = NginiousUtils.extractProject(selection);
		
		if(project != null) {
			fireSourceChanged(ISources.WORKBENCH, STATE, ACTIVE);
		} else {
			fireSourceChanged(ISources.WORKBENCH, STATE, INACTIVE);
		}
	}
	
	public void dispose() {
		return;
	}

    public String[] getProvidedSourceNames() {
        return new String[] { STATE };
    }
    
    public Map<?, ?> getCurrentState() {
        Map<String, String> map = new HashMap<String, String>(1);
        map.put(STATE, curState);
        return map;
    }    
}
