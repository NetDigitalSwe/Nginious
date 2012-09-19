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

package com.nginious.http.upload;

import java.io.IOException;
import java.io.InputStream;

import com.nginious.http.upload.MultipartInputStream;

public class ProgressInputStream extends InputStream {
	
	private MultipartInputStream in;
	
	private ProgressListener listener;
	
	private long length;
	
	private long pos;
	
	private int progress;
	
	public ProgressInputStream(MultipartInputStream in, ProgressListener listener) throws IOException {
		this.in = in;
		this.listener = listener;
		this.length = in.length();
	}

	public int available() throws IOException {
		return in.available();
	}

	public void close() throws IOException {
		in.close();
	}

	public synchronized void mark(int mark) {
		in.mark(mark);
	}

	public boolean markSupported() {
		return in.markSupported();
	}

	public int read() throws IOException {
		int b = in.read();
		this.pos++;
		progress();
		return b;
	}

	public int read(byte[] b, int start, int len) throws IOException {
		int readLen = in.read(b, start, len);
		this.pos += readLen;
		progress();
		return readLen;
	}

	public int read(byte[] b) throws IOException {
		int readLen = in.read(b);
		this.pos += readLen;
		progress();
		return readLen;
	}

	public synchronized void reset() throws IOException {
		in.reset();
	}

	public long skip(long len) throws IOException {
		return in.skip(len);
	}
	
	private void progress() {
		if(this.listener == null) {
			return;
		}
		
		int newProgress = (int)((this.pos / this.length) * 100);
		
		if(newProgress != this.progress) {
			this.progress = newProgress;
			listener.progress(this.progress);
		}
	}
}
