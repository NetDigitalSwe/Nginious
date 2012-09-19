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

package com.nginious.http.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * File handling utility methods.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class FileUtils {
	
	/**
	 * Copies all files and subdirectories from the specified source directory to the
	 * specified destination directory.
	 * 
	 * @param src the source directory
	 * @param dest the destination directory
	 * @throws IOException id an I/O error occurs while copying
	 */
	public static void copyDir(String src, String dest) throws IOException {
		copyDir(new File(src), new File(dest));
	}
	
	/**
	 * Copies all files and subdirectories from the specified source directory to the
	 * specified destination directory.
	 * 
	 * @param src the source directory
	 * @param dest the destination directory
	 * @throws IOException id an I/O error occurs while copying
	 */
	public static void copyDir(File src, File dest) throws IOException {
		if(!dest.exists()) {
			dest.mkdir();
		}
		
		File[] files = src.listFiles();
		
		for(File file : files) {
			if(file.isFile()) {
				copyFile(file, new File(dest, file.getName()));
			} else if(file.isDirectory()) {
				copyDir(file, new File(dest, file.getName()));
			}
		}
	}
	
	/**
	 * Deletes the specified directory and all its subdirectories and files.
	 * 
	 * @param dir the directory to delete
	 */
	public static void deleteDir(String dir) {
		deleteDir(new File(dir));
	}
	
	/**
	 * Deletes the specified directory and all its subdirectories and files.
	 * 
	 * @param dir the directory to delete
	 */
	public static void deleteDir(File dir) {
		File[] files = dir.listFiles();
		
		for(File file : files) {
			if(file.isFile()) {
				file.delete();
			} else if(file.isDirectory()) {
				deleteDir(file);
			}
		}
		
		dir.delete();
	}
	
	/**
	 * Copies the specified source file to the specified destination file.
	 * 
	 * @param src the source file to copy
	 * @param dest the destination file
	 * @throws IOException if an I/O error occurs while copying the file
	 */
	public static void copyFile(String src, String dest) throws IOException {
		copyFile(new File(src), new File(dest));
	}
	
	/**
	 * Copies the specified source file to the specified destination file.
	 * 
	 * @param src the source file to copy
	 * @param dest the destination file
	 * @throws IOException if an I/O error occurs while copying the file
	 */
	public static void copyFile(File src, File dest) throws IOException {
		FileInputStream in = null;
		FileOutputStream out = null;
		
		try {
			in = new FileInputStream(src);
			out = new FileOutputStream(dest);
			byte[] b = new byte[4096];
			int len = 0;
			
			while((len = in.read(b)) > 0) {
				out.write(b, 0, len);
			}
			
			out.flush();
		} finally {
			if(in != null) {
				try { in.close(); } catch(IOException e) {}
			}
			
			if(out != null) {
				try { out.close(); } catch(IOException e) {}
			}
		}
	}
}
