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

import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

/**
 * Translates filenames into mime types. A properties file 'mime.types' is loaded using the
 * system class loader. The properties file acts as a lookup table translating file extensions
 * to mime types.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class MimeTypes {
	
	private static final PropertyResourceBundle extensionToMimeType;
	
	static {
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			InputStream in = loader.getResourceAsStream("mime.types");
			extensionToMimeType = new PropertyResourceBundle(in);
			in.close();
		} catch(IOException e) {
			throw new RuntimeException("Can't load mime types");
		}
	}
	
	/**
	 * Looks up the mime type for the specified file name.
	 * 
	 * @param fileName the file name
	 * @return the mime type or <code>null</code> if no mime type found
	 */
	public static String getMimeTypeByExtenstion(String fileName) {
		String type = null;
		int pos = 0;
		
		while(type == null && pos != -1) {
			pos = fileName.indexOf(".", pos + 1);
			
			if(pos > -1) {
				String ext = fileName.substring(pos + 1);
				
				try {
					type = extensionToMimeType.getString(ext);
				} catch(MissingResourceException e) {}
			}
		}
		
		return type;
	}
}
