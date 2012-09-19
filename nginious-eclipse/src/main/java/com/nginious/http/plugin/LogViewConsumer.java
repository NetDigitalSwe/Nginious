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
import java.util.LinkedList;

import org.eclipse.jface.text.Document;
import org.eclipse.swt.widgets.Display;

import com.nginious.http.server.LogOutputConsumer;

class LogViewConsumer implements LogOutputConsumer {
	
	private Document document;
	
	private LinkedList<String> lines;
	
	private int lineCount;
	
	LogViewConsumer(Document document) {
		this.document = document;
		this.lines = new LinkedList<String>();
		this.lineCount = 0;
	}
	
	public void start() throws IOException {
		return;
	}

	public void stop() throws IOException {
		return;
	}
	
	public void consume(byte[] logLineBytes) {
		String logLine = new String(logLineBytes);
		lines.addLast(logLine);
		this.lineCount++;
		
		Display display = Display.getDefault();
		
		if(this.lineCount == 1100) {
			for(int i = 0; i < 100; i++) {
				lines.removeFirst();
			}
			
			this.lineCount = 1000;
			StringBuffer newText = new StringBuffer();
			
			for(String line : this.lines) {
				newText.append(line);
			}
			
			final String curText = newText.toString();
			
			display.syncExec(
					new Runnable() {
						public void run() {
							try {
								document.set(curText);
							} catch(Throwable t) {
								t.printStackTrace();
							}
						}
					});
		} else {
			final String curText = document.get() + logLine;
			
			display.syncExec(
					new Runnable() {
						public void run() {
							try {
								document.set(curText);
							} catch(Throwable t) {
								t.printStackTrace();
							}
						}
					});
		}		
	}

	Document getDocument() {
		return this.document;
	}
}
