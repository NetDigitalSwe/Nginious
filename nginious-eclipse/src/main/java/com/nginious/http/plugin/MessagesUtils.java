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

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class MessagesUtils {

	public static void handle(InvocationTargetException e, Shell parent, String title, String message) {
		perform(e, parent, title, message);
	}

	protected static void perform(CoreException e, Shell shell, String title, String message) {
		IStatus status= e.getStatus();
		
		if(status != null) {
			ErrorDialog.openError(shell, title, message, status);
		} else {
			displayMessageDialog(e.getMessage(), shell, title, message);
		}
	}
	
	protected static void perform(InvocationTargetException e, Shell shell, String title, String message) {
		Throwable target = e.getTargetException();
		
		if(target instanceof CoreException) {
			perform((CoreException)target, shell, title, message);
		} else {
			if(e.getMessage() != null && e.getMessage().length() > 0) {
				displayMessageDialog(e.getMessage(), shell, title, message);
			} else {
				displayMessageDialog(target.getMessage(), shell, title, message);
			}
		}
	}
	
	static void displayMessageDialog(String exceptionMessage, final Shell shell, final String title, String message) {
		final StringWriter msg = new StringWriter();
		
		if(message != null) {
			msg.write(message);
			msg.write("\n\n");
		}
		
		if(exceptionMessage == null || exceptionMessage.length() == 0) {
			msg.write("See error log message");
		} else {
			msg.write(exceptionMessage);
		}
		
		Display display = Display.getDefault();
		
		display.syncExec(
				new Runnable() {
					public void run() {
						try {
							MessageDialog.openError(shell, title, msg.toString());
						} catch(Throwable t) {
							t.printStackTrace();
						}
					}
				});
	}
}
