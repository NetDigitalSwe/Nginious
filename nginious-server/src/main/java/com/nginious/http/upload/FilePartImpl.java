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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

import com.nginious.http.common.FixedBuffer;

/**
 * A file part from multipart content has a file name and content stored either in a buffer or
 * in a temporary file. File parts up to 8192 bytes are stored in buffers. Larger file parts
 * are stored in temporary files.
 * 
 * <p>
 * A file part is expected to have a <code>Content-Type</code> header defining the mime type
 * of the file and a <code>Content-Disposition</code> header defining the original filename.
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class FilePartImpl extends PartImpl implements FilePart {
	
	private static AtomicLong sequence = new AtomicLong(1);
	
	private String fileName;
	
	private FixedBuffer buffer;
	
	private File tmpFile;
	
	private FileOutputStream out;
	
	/**
	 * Constructs a new file part with the specified name.
	 * @param name
	 */
	FilePartImpl(String name) {
		super(name);
		this.buffer = new FixedBuffer(8192);		
	}
	
	/**
	 * Returns this file parts content type.
	 * 
	 * @return the content type.
	 */
	public String getContentType() {
		return getHeader("Content-Type");
	}
	
	/**
	 * Returns a file where this file parts content is stored temporarily. If this
	 * file part is stored in a buffer it is first written to a temporary file.
	 * 
	 * @return the part content file
	 * @throws IOException if an I/O error occurs
	 */
	public File getFile() throws IOException {
		try {
			if(this.tmpFile == null) {
				moveBufferToFile();
			}
			
			return this.tmpFile;
		} finally {
			if(this.out != null) {
				try { out.close(); } catch(IOException e) {}
			}
		}
	}
	
	/**
	 * Returns an input stream for reading this file parts content.
	 * 
	 * @return the input stream
	 * @throws IOException if an I/O error occurs while opening input stream
	 */
	public InputStream getInputStream() throws IOException {
		if(this.buffer != null) {
			return new ByteArrayInputStream(buffer.toByteArray());
		}
		
		return new FileInputStream(this.tmpFile);
	}
	
	/**
	 * Returns the size of this file parts content.
	 * 
	 * @return the content size
	 */
	public int getSize() {
		if(this.buffer != null) {
			return buffer.size() - buffer.remaining();
		}
		
		return (int)tmpFile.length();
	}
	
	/**
	 * Sets this file parts original filename to the specified filename..
	 * 
	 * @param fileName the original filename.
	 */
	void setFilename(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * Returns this file parts original filename.
	 * 
	 * @return the filename
	 */
	public String getFilename() {
		return this.fileName;
	}
	
	/**
	 * Adds content to this file part from the specified content starting at the
	 * specified offset and of specified length.
	 * 
	 * @param content the content to add from
	 * @param off the offset in content to start adding from
	 * @param len the number of bytes to add
	 * @throws IOException if an I/O exception occurs while adding content
	 */
	void content(byte[] content, int off, int len) throws IOException {
		if(this.buffer == null) {
			out.write(content, off, len);
		} else if(buffer.remaining() < len) {
			moveBufferToFile();
			out.write(content, off, len);
		} else {
			buffer.put(content, off, len);
		}
	}
	
	/**
	 * Ends adding of content by closing the temporary file.
	 */
	void end() {
		if(this.out != null) {
			try { out.close(); } catch(IOException e) {}
		}
	}
	
	/**
	 * Deletes this file parts temporary content file.
	 */
	public void delete() {
		if(this.tmpFile != null) {
			tmpFile.delete();
		}
	}
	
	/**
	 * Moves this file parts buffer to a temporary file.
	 * 
	 * @throws IOException if unable to write buffer to temporary file
	 */
	private void moveBufferToFile() throws IOException {
		this.tmpFile = createTempFile();
		this.out = new FileOutputStream(this.tmpFile);
		out.write(buffer.toByteArray());
		this.buffer = null;		
	}
	
	/**
	 * Creates temporary file for this file part.
	 * 
	 * @return the temporary file
	 */
	private File createTempFile() {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		StringBuffer fileName = new StringBuffer("Nginious_filePart_");
		fileName.append(sequence.getAndIncrement());
		fileName.append(".tmp");
		
		File file = new File(tmpDir, fileName.toString());
		return file;
	}
}
