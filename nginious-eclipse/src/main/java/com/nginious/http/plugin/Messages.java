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

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

	private static final String BUNDLE_NAME= "com.nginious.http.plugin.Messages";//$NON-NLS-1$

	private Messages() {
		super();
	}

	public static String NewProjectWizard_title;
	public static String NewProjectWizard_op_error_title;
	public static String NewProjectWizard_op_error_create_message;
	
	public static String NewProjectPageOne_Memory;
	public static String NewProjectPageOne_Min_memory;
	public static String NewProjectPageOne_Max_memory;
	
	public static String NewProjectPageOne_Server;
	public static String NewProjectPageOne_Listen_port;
	public static String NewProjectPageOne_Publish;
	public static String NewProjectPageOne_Publish_url;
	public static String NewProjectPageOne_page_title;
	public static String NewProjectPageOne_page_description;
	public static String NewProjectPageOne_Listen_port_error;
	
	public static String ClassPathBuilder_operation_description;
	
	public static String NewXspFileCreationPage_page_title;
	public static String NewXspFileCreationPage_page_description;
	
	public static String NewXspFileWizard_title;
	
	public static String ProjectPropertyPage_listen_port_error_title;
	public static String ProjectPropertyPage_listen_port_error_message;
	public static String ProjectPropertyPage_save_error_title;
	public static String ProjectPropertyPage_save_error_message;
	
	public static String PropertiesBlock_listen_port_error_title;
	public static String PropertiesBlock_listen_port_error_message;
	public static String PropertiesBlock_properties_error_title;
	public static String PropertiesBlock_properties_error_message;

	public static String Startup_op_error_title;
	public static String Startup_op_error_message;
			 
	public static String ServerManager_listen_port_error_title;
	public static String ServerManager_listen_port_error_message;
	public static String ServerManager_properties_error_title;
	public static String ServerManager_properties_error_message;
	public static String ServerManager_server_error_title;
	public static String ServerManager_server_error_message;
	
	public static String PublishHandler_begin_message;
	public static String PublishHandler_creating_war_message;
	public static String PublishHandler_upload_message;
	public static String PublishHandler_error_title;
	public static String PublishHandler_error_message;

	public static String RollbackHandler_error_title;
	public static String RollbackHandler_error_message;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
