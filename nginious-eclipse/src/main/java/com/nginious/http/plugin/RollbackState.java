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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nginious.http.HttpMethod;
import com.nginious.http.HttpStatus;
import com.nginious.http.client.HttpClient;
import com.nginious.http.client.HttpClientException;
import com.nginious.http.client.HttpClientRequest;
import com.nginious.http.client.HttpClientResponse;
import com.nginious.http.server.Digest;

public class RollbackState extends AbstractSourceProvider implements ISelectionListener {
	
	public final static String STATE = "com.nginious.http.plugin.rollbackState";
	
	public final static String ACTIVE = "ACTIVE";
	
	public final static String INACTIVE = "INACTIVE";
	
	public final static String NONE = "NONE";
	
	private static RollbackState state = null;
	
	private static Object lock = new Object();
	
	private ConcurrentHashMap<String, String> projectStates;
	
	private IProject curProject;
	
	public RollbackState() {
		synchronized(lock) {
			if(state != null) {
				throw new RuntimeException("Only one rollback state allowed");
			}
			
			state = this;
		}
		
		this.projectStates = new ConcurrentHashMap<String, String>();
	}
	
	static RollbackState getInstance() {
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
			String state = projectStates.get(project.getName());
			
			if(state == null) {
				checkState(project);
			} else {
				fireSourceChanged(ISources.WORKBENCH, STATE, state);
			}
		} else {
			this.curProject = null;
			fireSourceChanged(ISources.WORKBENCH, STATE, NONE);
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
        
        if(this.curProject == null) {
        	map.put(STATE, NONE);
        } else {
        	String name = curProject.getName();
        	String state = projectStates.get(name);
        	
        	if(state != null) {
        		map.put(STATE, state);
        	} else {
        		map.put(STATE, NONE);
        	}
        }
        
        return map;
    }
    
    void checkState(IProject project) {
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
    		
    		URL url = new URL(publishUrl);
    		int port = url.getPort();
    		
    		if(port == -1) {
    			port = url.getDefaultPort();
    		}
    		
    		HttpClientRequest request = new HttpClientRequest();
    		request.setMethod(HttpMethod.GET);
    		request.setPath(url.getPath());
    		request.setHeader("Host", url.getHost());
    		request.setHeader("Content-Type", "text/xml; charset=utf-8");
    		request.setHeader("Connection", "close");
    		request.setHeader("Content-Length", "0");
    		String authorization = createAuthorization(HttpMethod.GET, publishUsername, publishPassword);
    		request.setHeader("Authorization", authorization);
    		
    		HttpClient client = new HttpClient(url.getHost(), port);
    		HttpClientResponse response = client.request(request, "".getBytes());
    		
    		if(response.getStatus() == HttpStatus.OK) {
    			setState(project, response);
    		}
    	} catch(HttpClientException e) {
    		e.printStackTrace();
    	} catch(IOException e) {
    		e.printStackTrace();
    	} catch(CoreException e) {
    		e.printStackTrace();
    	}
    }
    
    void setState(IProject project, HttpClientResponse response) {
    	try {
    		byte[] content = response.getContent();
    		JSONObject json = new JSONObject(new String(content));
    		JSONObject applicationInfo = json.getJSONObject("applicationInfo");
    		JSONArray versions = applicationInfo.getJSONArray("versions");
    		String newState = null;
    		
    		if(versions.length() > 1) {
    			newState = ACTIVE;
    		} else {
    			newState = INACTIVE;
    		}
    		
    		notifyState(project, newState);
    	} catch(JSONException e) {
    		return;
    	}
    }
    
    void notifyState(IProject project, String state) {
    	String previousState = projectStates.put(project.getName(), state);
    	String curName = this.curProject != null ? curProject.getName() : null;
    	
    	if(!state.equals(previousState) && (project.getName().equals(curName) || this.curProject == null)) {
        	fireSourceChanged(ISources.WORKBENCH, STATE, state);
    	}
    }
    
    private String createAuthorization(HttpMethod method, String username, String password) {
    	Digest digest = new Digest();
    	digest.setCnonce("0a4f113b");
    	digest.setMethod(method.toString());
    	digest.setNc("0000001");
    	digest.setNonce("dcd98b7102dd2f0e8b11d0f600bfb0c093");
    	digest.setQop("auth");
    	digest.setRealm("admin");
    	digest.setUri("/admin");
    	digest.setUsername(username);
    	String response = digest.createResponse(password);
    	
    	StringBuffer authorization = new StringBuffer();
    	authorization.append("username=\"");
    	authorization.append(username);
    	authorization.append("\", realm=\"admin\", ");
    	authorization.append("nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", ");
    	authorization.append("uri=\"/admin\", ");
    	authorization.append("qop=auth, ");
    	authorization.append("nc=0000001, ");
    	authorization.append("cnonce=\"0a4f113b\", ");
    	authorization.append("response=\"");
    	authorization.append(response);
    	authorization.append("\", ");
    	authorization.append("opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"");
    	return authorization.toString();
    }    
}
