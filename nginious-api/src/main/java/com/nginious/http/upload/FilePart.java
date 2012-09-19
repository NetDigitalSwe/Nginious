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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A file multipart content part.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public interface FilePart extends Part {
	
	/**
	 * Returns this file parts content type.
	 * 
	 * @return the content type.
	 */
	public String getContentType();
	
	/**
	 * Returns a file where this file parts content is stored temporarily. If this
	 * file part is stored in a buffer it is first written to a temporary file.
	 * 
	 * @return the part content file
	 * @throws IOException if an I/O error occurs
	 */
	public File getFile() throws IOException;
	
	/**
	 * Returns an input stream for reading this file parts content.
	 * 
	 * @return the input stream
	 * @throws IOException if an I/O error occurs while opening input stream
	 */
	public InputStream getInputStream() throws IOException;
	
	/**
	 * Returns the size of this file parts content.
	 * 
	 * @return the content size
	 */
	public int getSize();
	
	/**
	 * Returns this file parts original filename.
	 * 
	 * @return the filename
	 */
	public String getFilename();
	
	/**
	 * Deletes this file parts temporary content file.
	 */
	public void delete();
}
