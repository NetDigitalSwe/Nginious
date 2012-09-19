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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.ui.JavaUI;

public class StatusInfo implements IStatus {

	public static final IStatus OK_STATUS= new StatusInfo();

	private String statusMessage;
	
	private int severity;

	public StatusInfo() {
		this(OK, null);
	}

	public StatusInfo(int severity, String message) {
		this.statusMessage = message;
		this.severity = severity;
	}

	public boolean isOK() {
		return this.severity == IStatus.OK;
	}

	public boolean isWarning() {
		return this.severity == IStatus.WARNING;
	}

	public boolean isInfo() {
		return this.severity == IStatus.INFO;
	}

	public boolean isError() {
		return this.severity == IStatus.ERROR;
	}

	public String getMessage() {
		return this.statusMessage;
	}

	public void setError(String errorMessage) {
		Assert.isNotNull(errorMessage);
		this.statusMessage= errorMessage;
		this.severity= IStatus.ERROR;
	}

	public void setWarning(String warningMessage) {
		Assert.isNotNull(warningMessage);
		this.statusMessage= warningMessage;
		this.severity= IStatus.WARNING;
	}

	public void setInfo(String infoMessage) {
		Assert.isNotNull(infoMessage);
		this.statusMessage = infoMessage;
		this.severity = IStatus.INFO;
	}

	public void setOK() {
		this.statusMessage = null;
		this.severity = IStatus.OK;
	}

	public boolean matches(int severityMask) {
		return(this.severity & severityMask) != 0;
	}

	public boolean isMultiStatus() {
		return false;
	}

	public int getSeverity() {
		return this.severity;
	}

	public String getPlugin() {
		return JavaUI.ID_PLUGIN;
	}

	public Throwable getException() {
		return null;
	}

	public int getCode() {
		return this.severity;
	}

	public IStatus[] getChildren() {
		return new IStatus[0];
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("StatusInfo "); //$NON-NLS-1$
		
		if(this.severity == OK) {
			buf.append("OK"); //$NON-NLS-1$
		} else if(this.severity == ERROR) {
			buf.append("ERROR"); //$NON-NLS-1$
		} else if(this.severity == WARNING) {
			buf.append("WARNING"); //$NON-NLS-1$
		} else if(this.severity == INFO) {
			buf.append("INFO"); //$NON-NLS-1$
		} else {
			buf.append("severity="); //$NON-NLS-1$
			buf.append(this.severity);
		}
		
		buf.append(": "); //$NON-NLS-1$
		buf.append(this.statusMessage);
		return buf.toString();
	}
}
