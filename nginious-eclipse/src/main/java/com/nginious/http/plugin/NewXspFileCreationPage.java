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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public class NewXspFileCreationPage extends WizardNewFileCreationPage {
	
	public NewXspFileCreationPage(IStructuredSelection selection) {
		super(Messages.NewXspFileCreationPage_page_title, selection);
		
		setTitle(Messages.NewXspFileCreationPage_page_title);
		setDescription(Messages.NewXspFileCreationPage_page_description);
		setFileExtension("xsp");
	}
	
	protected InputStream getInitialContents() {
		String template = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n";
		template += "<html>\n";
		template += "	<head>\n";
		template += "		<xsp:meta name=\"package\" content=\"se.netdigital.http.xsp\" />\n";
	    template += "		<xsp:meta name=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n";

		template += "		<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n";
		template += "		<title>Insert title here</title>\n";
		template += "	</head>\n";
		template += "	<body>\n";
		template += "		\n";
		template += "	</body>\n";
		template += "</html>\n";
		
		return new ByteArrayInputStream(template.getBytes());
	}
}
