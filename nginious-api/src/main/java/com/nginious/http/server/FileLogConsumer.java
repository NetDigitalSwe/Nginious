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

package com.nginious.http.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A log consumer which writes log entries to a file. Log entries are queued when added. A separate thread 
 * takes log entries from the queue and writes them to the log file.
 * 
 * <p>
 * The maximum queue size is 1000 entries. When this limit is reached callers block until a slot in the 
 * queue is available.
 * </p>
 * 
 * <p>
 * The log file is rotated at 12:00 am where the old log file is renamed to "[prefix]-yyyy-MM-dd.log" and
 * a new file with the name "[prefix].log" is created for writing. A history of 5 log files is kept.
 * [prefix] is replaced with the log name.
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class FileLogConsumer {
	
	private ArrayBlockingQueue<byte[]> queue;
	
	private Thread thread;
	
	private Writer writer;
	
	private String fileNamePrefix;
	
	/**
	 * Constructs a new file log consumer which writes log entries to a log file with the
	 * specified file name prefix.
	 * 
	 * @param fileNamePrefix the file name prefix
	 */
	public FileLogConsumer(String fileNamePrefix) {
		super();
		this.queue = new ArrayBlockingQueue<byte[]>(1000);
		this.fileNamePrefix = fileNamePrefix;
	}
	
	/**
	 * Starts this file log consumer.
	 * 
	 * @throws IOException if unable to open log file
	 */
	public void start() throws IOException {
		this.writer = new Writer(this.fileNamePrefix);
		this.thread = new Thread(this.writer);
		thread.start();		
	}
	
	/**
	 * Stops this file log consumer.
	 * 
	 * @throws IOException if unable to close log files
	 */
	public void stop() throws IOException {
		if(this.writer != null) {
			writer.stop();
			thread.interrupt();
			
			try {
				thread.join();
			} catch(InterruptedException e) {}
			
			queue.clear();
		}		
	}
	
	/**
	 * Queues the specified log line for writing to the log file.
	 */
	public void consume(byte[] logLine) {
		try {
			queue.put(logLine);
		} catch(InterruptedException e) {}
	}

	/**
	 * Implements writer for writing entries from queue to access log file. Log file is rotated once
	 * every 24 hours at midnight. A history of 5 days is kept.
	 * 
	 * @author Bojan Pisler, NetDigital Sweden AB
	 *
	 */
	private class Writer implements Runnable {
		
		private static final int DEFAULT_BACKLOG = 5;
		
		private FileOutputStream out;
		
		private String fileNamePrefix;
		
		private boolean stopped;
		
		private long nextRotationMillis;
		
		/**
		 * Constructs a new writer
		 * 
		 * @throws IOException if unable to open access log file
		 */
		Writer(String fileNamePrefix) throws IOException {
			super();
			this.fileNamePrefix = fileNamePrefix;
			this.nextRotationMillis = calculateNextRotationTime();
			createNew();
		}
		
		/**
		 * Raises stop flag for this writer
		 */
		void stop() {
			this.stopped = true;
		}
		
		/**
		 * Reads log entries from queue and writes them to the access log file.
		 */
		public void run() {
			try {
				while(!this.stopped) {
					try {
						byte[] b = null;
						long maxWaitTime = this.nextRotationMillis - System.currentTimeMillis();
						
						if(maxWaitTime > 0L) {
							b = queue.poll(maxWaitTime, TimeUnit.MILLISECONDS);
						}
						
						if(System.currentTimeMillis() > this.nextRotationMillis) {
							rotate();
						}
						
						if(b != null) {
							out.write(b);
						}
					} catch(IOException e) {
						this.stopped = true;
						return;
					} catch(InterruptedException e) {}
				}
				
				if(this.stopped) {
					writeRemaining();
				}
			} finally {
				try {
					out.flush();
					out.close();
				} catch(IOException e) {}
			}
		}
		
		/**
		 * Writes any remaining log entries from queue to access log file when stop flag has been
		 * raised and access log is preparing to stop.
		 */
		private void writeRemaining() {
			try {
				byte[] b = null;
				
				while((b = queue.poll()) != null) {
					out.write(b);
				}
			} catch(IOException e) {}
		}
		
		/**
		 * Rotates access log file. The current log file is renamed to "access-yyyy-MM-dd.log" where
		 * "yyyy-MM-dd" is replaced with the current date. A new access log file is created. A maximum
		 * of 5 rotated log files is kept. 
		 * 
		 * @throws IOException if unable to create new access log file.
		 */
		private void rotate() throws IOException {
			out.close();
			moveCurrent();
			this.nextRotationMillis = calculateNextRotationTime();
			createNew();
			deleteOld();
		}
		
		/**
		 * Creates and opens a new access log file.
		 * 
		 * @throws IOException if unable to create access log file
		 */
		private void createNew() throws IOException {
			File outFile = new File(this.fileNamePrefix);
			this.out = new FileOutputStream(outFile, true);
		}
		
		/**
		 * Renames current access log file to "access_yyyy-MM-dd.log" where
		 * "yyyy-MM-dd" is replaced with the current date.
		 */
		private void moveCurrent() {
			StringBuffer fileName = new StringBuffer(this.fileNamePrefix);
			fileName.append(".");
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			fileName.append(format.format(this.nextRotationMillis - 1000L));
			File curFile = new File(this.fileNamePrefix);
			File rotatedFile = new File(fileName.toString());
			curFile.renameTo(rotatedFile);
		}
		
		/**
		 * Deletes all rotated log files with dates older than 5 days.
		 */
		private void deleteOld() {
			for(int i = DEFAULT_BACKLOG; i <= 10; i++) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				cal.add(Calendar.DAY_OF_MONTH, -i);
				StringBuffer fileName = new StringBuffer(this.fileNamePrefix);
				fileName.append(".");
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				fileName.append(format.format(this.nextRotationMillis - 1000L));
				File oldFile = new File(fileName.toString());
				oldFile.delete();
			}
		}
		
		/**
		 * Calculate next log rotation time in milliseconds which is the next
		 * midnight.
		 * 
		 * @return the next log rotation time in milliseconds.
		 */
		private long calculateNextRotationTime() {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			cal.add(Calendar.DAY_OF_MONTH, 1);
			return cal.getTimeInMillis();
		}
	}
}
