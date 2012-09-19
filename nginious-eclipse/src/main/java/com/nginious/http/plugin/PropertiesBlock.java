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

import java.util.Observable;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

class PropertiesBlock {
	
	private IProject project;
	
	private ListenPortValidator validator;
	
	private PublishGroup publishGroup;
	
	private ServerGroup serverGroup;
	
	private int horizontalSpacing;
	
	private int verticalSpacing;
	
	private int marginWidth;
	
	private int marginHeight;
	
	private int initialListenPort;
	
	private String initialPublishUrl;
	
	private String initialPublishUsername;
	
	private String initialPublishPassword;
	
	PropertiesBlock(int horizontalSpacing, int verticalSpacing, int marginWidth, int marginHeight) {
		super();
		this.publishGroup = new PublishGroup();
		this.serverGroup = new ServerGroup();
		this.horizontalSpacing = horizontalSpacing;
		this.verticalSpacing = verticalSpacing;
		this.marginWidth = marginWidth;
		this.marginHeight = marginHeight;
	}
	
	PropertiesBlock(IProject project, int horizontalSpacing, int verticalSpacing, int marginWidth, int marginHeight) {
		this(horizontalSpacing, verticalSpacing, marginWidth, marginHeight);
		this.project = project;
	}
	
	void setListenPortValidator(ListenPortValidator validator) {
		this.validator = validator;
	}
	
	boolean hasChanged() {
		if(this.initialListenPort != serverGroup.getListenPort()) {
			return true;
		}
		
		if(!initialPublishUrl.equals(publishGroup.getUrl())) {
			return true;
		}
		
		if(!initialPublishUsername.equals(publishGroup.getUsername())) {
			return true;
		}
		
		if(!initialPublishPassword.equals(publishGroup.getPassword())) {
			return true;
		}
		
		return false;
	}
	
	int getInitialListenPort() {
		return this.initialListenPort;
	}
	
	int getListenPort() {
		return serverGroup.getListenPort();
	}
	
	String getInitialPublishUrl() {
		return this.initialPublishUrl;
	}
	
	String getPublishUrl() {
		return publishGroup.getUrl();
	}
	
	void setPublishUrlName(String name) {
		publishGroup.setUrlName(name);
	}
	
	String getInitialPublishUsername() {
		return this.initialPublishUsername;
	}
	
	String getPublishUsername() {
		return publishGroup.getUsername();
	}
	
	String getInitialPublishPassword() {
		return this.initialPublishPassword;
	}
	
	String getPublishPassword() {
		return publishGroup.getPassword();
	}
	
	void createControls(Composite composite) {
		int listenPort = NginiousPlugin.DEFAULT_LISTEN_PORT;
		String publishUrl = NginiousPlugin.DEFAULT_PUBLISH_URL;
		String publishUsername = NginiousPlugin.DEFAULT_PUBLISH_USERNAME;
		String publishPassword = NginiousPlugin.DEFAULT_PUBLISH_PASSWORD;
		
		if(this.project != null) {
			try {
				String listenPortStr = project.getPersistentProperty(NginiousPlugin.LISTE_PORT_PROP_KEY);
				
				if(listenPortStr != null) {
					listenPort = Integer.parseInt(listenPortStr);
				}
				
				publishUrl = project.getPersistentProperty(NginiousPlugin.PUBLISH_URL_PROP_KEY);
				
				if(publishUrl == null) {
					publishUrl = NginiousPlugin.DEFAULT_PUBLISH_URL;
				}
				
				publishUsername = project.getPersistentProperty(NginiousPlugin.PUBLISH_USERNAME_PROP_KEY);
				
				if(publishUsername == null) {
					publishUsername = NginiousPlugin.DEFAULT_PUBLISH_USERNAME;
				}
				
				publishPassword = project.getPersistentProperty(NginiousPlugin.PUBLISH_PASSWORD_PROP_KEY);
				
				if(publishPassword == null) {
					publishPassword = NginiousPlugin.DEFAULT_PUBLISH_PASSWORD;
				}
				
				this.initialListenPort = listenPort;
				this.initialPublishUrl = publishUrl;
				this.initialPublishUsername = publishUsername;
				this.initialPublishPassword = publishPassword;				
			} catch(CoreException e) {
				String title = Messages.PropertiesBlock_properties_error_title;
				String message = Messages.PropertiesBlock_properties_error_message + " " + project.getName();
				MessagesUtils.perform(e, null, title, message);
			} catch(NumberFormatException e) {
				String title = Messages.PropertiesBlock_listen_port_error_title;
				String message = Messages.PropertiesBlock_listen_port_error_message + " " + project.getName();
				MessagesUtils.displayMessageDialog(e.getMessage(), null, title, message);
			}
		}
		
		Control serverControl = createServerLayoutControl(composite, listenPort);
		serverControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Control publishControl = createPublishLayoutControl(composite, publishUrl, publishUsername, publishPassword);
		publishControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
	
	private Control createServerLayoutControl(Composite composite, int listenPort) {
		return serverGroup.createContent(composite, listenPort);
	}
	
	private Control createPublishLayoutControl(Composite composite, String publishUrl, String publishUsername, String publishPassword) {
		return publishGroup.createContent(composite, publishUrl, publishUsername, publishPassword);
	}
		
	private GridLayout initGridLayout(GridLayout layout, boolean margins) {
		layout.horizontalSpacing = this.horizontalSpacing;
		layout.verticalSpacing = this.verticalSpacing;
		
		if(margins) {
			layout.marginWidth = this.marginWidth;
			layout.marginHeight = this.marginHeight;
		} else {
			layout.marginWidth = 0;
			layout.marginHeight = 0;
		}
		
		return layout;
	}
	
	private final class PublishGroup extends Observable {
		
		private Group group;
		
		private Label urlLabel;
		
		private Text urlText;
		
		private Label usernameLabel;
		
		private Text usernameText;
		
		private Label passwordLabel;
		
		private Text passwordText;
		
		private PublishGroup() {
			super();
		}
		
		private String getUrl() {
			return urlText.getText();
		}
		
		private void setUrlName(String name) {
			String url = urlText.getText();
			int idx = url.lastIndexOf("application/");
			
			if(idx > -1) {
				url = url.substring(0, idx + "application/".length());
				url += name;
				urlText.setText(url);
			}
		}
		
		private String getUsername() {
			return usernameText.getText();
		}
		
		private String getPassword() {
			return passwordText.getText();
		}
		
		private Control createContent(Composite composite, String publishUrl, String publishUsername, String publishPassword) {
			this.group = new Group(composite, SWT.NONE);
			group.setFont(composite.getFont());
			group.setLayout(initGridLayout(new GridLayout(6, true), true));
			group.setText(Messages.NewProjectPageOne_Publish);

			this.urlLabel= new Label(this.group, SWT.LEFT | SWT.WRAP);
			urlLabel.setFont(composite.getFont());
			urlLabel.setEnabled(true);
			urlLabel.setText(Messages.NewProjectPageOne_Publish_url);
			GridData gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalSpan = 1;
			urlLabel.setLayoutData(gd);

			this.urlText = new Text(this.group, SWT.SINGLE | SWT.BORDER);
			urlText.setText(publishUrl);
			gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalSpan = 5;
			urlText.setLayoutData(gd);
			
			this.usernameLabel= new Label(this.group, SWT.LEFT | SWT.WRAP);
			usernameLabel.setFont(composite.getFont());
			usernameLabel.setEnabled(true);
			usernameLabel.setText("Username:");
			gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalSpan = 1;
			usernameLabel.setLayoutData(gd);
			
			this.usernameText = new Text(this.group, SWT.SINGLE | SWT.BORDER);
			usernameText.setText(publishUsername);
			gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalSpan = 2;
			usernameText.setLayoutData(gd);
			
			this.passwordLabel= new Label(this.group, SWT.LEFT | SWT.WRAP);
			passwordLabel.setFont(composite.getFont());
			passwordLabel.setEnabled(true);
			passwordLabel.setText("Password:");
			gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalSpan = 1;
			passwordLabel.setLayoutData(gd);
			
			this.passwordText = new Text(this.group, SWT.PASSWORD | SWT.SINGLE | SWT.BORDER);
			passwordText.setText(publishPassword);
			gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalSpan = 2;
			passwordText.setLayoutData(gd);
			
			return this.group;
		}
	}
	
	private final class ServerGroup extends Observable {
		
		private Label label;
		
		private Text text;
		
		private Group group;

		private ServerGroup() {
			super();
		}
		
		int getListenPort() {
			return Integer.parseInt(text.getText());
		}
		
		private Control createContent(Composite composite, int listenPort) {
			this.group = new Group(composite, SWT.NONE);
			group.setFont(composite.getFont());
			group.setLayout(initGridLayout(new GridLayout(3, false), true));
			group.setText(Messages.NewProjectPageOne_Server);
			
			this.label= new Label(this.group, SWT.LEFT | SWT.WRAP);
			label.setFont(composite.getFont());
			label.setEnabled(true);
			label.setText(Messages.NewProjectPageOne_Listen_port);
			
			this.text = new Text(this.group, SWT.SINGLE | SWT.BORDER);
			text.setTextLimit(5);
			text.setText(Integer.toString(listenPort));
			
			text.addListener(SWT.KeyUp, new Listener() {
				public void handleEvent(Event e) {
					if(PropertiesBlock.this.validator != null) {
						PropertiesBlock.this.validator.validate(Integer.parseInt(text.getText()));
					}
					
				}
			});
			
			text.addListener(SWT.Verify, new Listener() {
				public void handleEvent (Event e) {
					String text = e.text;
					
					for(int i = 0; i < text.length(); i++) {
						if(text.charAt(i) < '0' || text.charAt(i) > '9') {
							e.doit = false;
							return;
						}
					}
				}
			});
			
			GC gc = new GC(this.text);
			FontMetrics fm = gc.getFontMetrics();
			int width = 6 * fm.getAverageCharWidth();
			int height = fm.getHeight();
			gc.dispose();
			Point point = text.computeSize(width, height);
			
			GridData gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = false;
			gd.horizontalSpan = 2;
			gd.widthHint = point.x;
			
			text.setLayoutData(gd);
			
			return this.group;
		}
	}		
}
