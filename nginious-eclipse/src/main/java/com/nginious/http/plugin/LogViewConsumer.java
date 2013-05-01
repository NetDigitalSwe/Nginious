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
import java.io.RandomAccessFile;
import java.util.LinkedList;

import org.eclipse.jface.text.Document;
import org.eclipse.swt.widgets.Display;

class LogViewConsumer {
	
	private Document document;
	
	private String logPath;
	
	private LinkedList<String> lines;
	
	private int lineCount;
	
	private String lineSeparator;
	
	private RandomAccessFile file;
	
	private Logger logger;
	
	LogViewConsumer(Logger logger, String logPath) throws IOException {
		this.document = new Document();
		this.logger = logger;
		this.logPath = logPath;
		this.lines = new LinkedList<String>();
		this.lineCount = 0;
		this.lineSeparator = System.getProperty("line.separator");
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
		final String text = readLastLines();
		Display display = Display.getDefault();

		display.syncExec(
				new Runnable() {
					public void run() {
						try {
							document.set(text);
						} catch(Throwable t) {
							t.printStackTrace();
						}
					}
				});
		
		return this.document;
	}
	
	private String readLastLines() {
		logger.log("ENTER LogViewConsumer.readLastLines");
		
		if(this.lineCount == 1000) {
			lines.removeFirst();
		}
		
		try {
			if(this.file == null) {
				this.file = new RandomAccessFile(this.logPath, "r");
			}
			
			boolean done = false;
			
			while(!done && this.lineCount < 1000) {
				String line = file.readLine();
				
				if(line != null) {
					lines.addLast(line);
					this.lineCount++;
				} else {
					done = true;
				}			
			}
			
			StringBuffer text = new StringBuffer();
			
			for(String line : this.lines) {
				text.append(line);
				text.append(this.lineSeparator);
			}
			
			logger.log("EXIT LogViewConsumer.readLastLines");			
			return text.toString();
		} catch(IOException e) {
			logger.log("EXIT LogViewConsumer.readLastLines exception");			
			return "Failed! " + e.getMessage();
		}
	}
}
