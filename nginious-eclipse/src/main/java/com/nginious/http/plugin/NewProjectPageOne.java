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
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class NewProjectPageOne extends NewJavaProjectWizardPageOne implements ListenPortValidator, Listener {

	private PropertiesBlock block;
	
	private Text nameText;
	
	public NewProjectPageOne() {
		super();
		setTitle(Messages.NewProjectPageOne_page_title);
		setDescription(Messages.NewProjectPageOne_page_description);
	}

	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(initGridLayout(new GridLayout(1, false), true));
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		// create UI elements
		Control nameControl= createNameControl(composite);
		nameControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Composite nameComposite = (Composite)nameControl;
		this.nameText = (Text)nameComposite.getChildren()[1];
		nameText.addListener(SWT.KeyUp, this);
		
		Control locationControl= createLocationControl(composite);
		locationControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Control jreControl= createJRESelectionControl(composite);
		jreControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		int horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		int verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		int marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		int marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		this.block = new PropertiesBlock(horizontalSpacing, verticalSpacing, marginWidth, marginHeight);
		block.setListenPortValidator(this);
		block.createControls(composite);
		
		Control infoControl= createInfoControl(composite);
		infoControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		setControl(composite);
	}
	
	public void handleEvent(Event e) {
		block.setPublishUrlName(nameText.getText());
	}
	
	public boolean validate(int listenPort) {
		ServerManager manager = ServerManager.getInstance();
		IProject project = manager.checkListenPortUsage(listenPort);
		
		if(project != null) {
			setErrorMessage(Messages.NewProjectPageOne_Listen_port_error + " " + project.getName());
			return false;
		} else {
			setErrorMessage(null);
			return true;
		}
	}

	int getListenPort() {
		return block.getListenPort();
	}
	
	String getPublishUrl() {
		return block.getPublishUrl();
	}
	
	String getPublishUsername() {
		return block.getPublishUsername();
	}
	
	String getPublishPassword() {
		return block.getPublishPassword();
	}
	
	int getMinMemory() {
		return block.getMinMemory();
	}
	
	int getMaxMemory() {
		return block.getMaxMemory();
	}
	
	private GridLayout initGridLayout(GridLayout layout, boolean margins) {
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		
		if(margins) {
			layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
			layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		} else {
			layout.marginWidth = 0;
			layout.marginHeight = 0;
		}
		return layout;
	}	
}
