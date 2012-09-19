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
import java.text.MessageFormat;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.nginious.http.HttpMethod;
import com.nginious.http.HttpStatus;
import com.nginious.http.client.HttpClient;
import com.nginious.http.client.HttpClientException;
import com.nginious.http.client.HttpClientRequest;
import com.nginious.http.client.HttpClientResponse;
import com.nginious.http.server.Digest;

public class RollbackHandler extends AbstractHandler {
	
	public RollbackHandler() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		Shell shell = window.getShell();
		ISelectionService service = window.getSelectionService();
		ISelection selection = service.getSelection();
		IProject project = extractProject(selection);
		
		if(project != null) {
			rollbackProject(project, shell);
		}
		
		return null;
	}

	private void rollbackProject(IProject project, Shell shell) {
		HttpClient client = null;
		
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
			HttpClientRequest request = new HttpClientRequest();
			request.setMethod(HttpMethod.POST);
			request.setPath(url.getPath());
			request.setHeader("Host", url.getHost());
			request.setHeader("Content-Type", "text/xml; charset=utf-8");
			request.setHeader("Connection", "close");
			request.setHeader("Content-Length", "0");
			String authorization = createAuthorization(HttpMethod.POST, publishUsername, publishPassword);
			request.setHeader("Authorization", authorization);
			
			client = new HttpClient(url.getHost(), url.getPort());
			HttpClientResponse response = client.request(request, "".getBytes());
			
			if(response.getStatus() != HttpStatus.OK) {
				throw new HttpClientException("failed rollback " + response.getStatusMessage());
			}
			
			RollbackState state = RollbackState.getInstance();
			state.setState(project, response);
		} catch(CoreException e) {
			String title = Messages.RollbackHandler_error_title;
			Object[] args = { project.getName() };
			String message = MessageFormat.format(Messages.RollbackHandler_error_message, args);
			MessagesUtils.perform(e, shell, title, message);
		} catch(HttpClientException e) {
			String title = Messages.RollbackHandler_error_title;
			Object[] args = { project.getName() };
			String message = MessageFormat.format(Messages.RollbackHandler_error_message, args);
			MessagesUtils.displayMessageDialog(e.getMessage(), null, title, message);
		} catch(IOException e) {
			String title = Messages.RollbackHandler_error_title;
			Object[] args = { project.getName() };
			String message = MessageFormat.format(Messages.RollbackHandler_error_message, args);
			MessagesUtils.displayMessageDialog(e.getMessage(), null, title, message);
		} finally {
			if(client != null) {
				try { client.close(); } catch(IOException e) {}
			}
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
