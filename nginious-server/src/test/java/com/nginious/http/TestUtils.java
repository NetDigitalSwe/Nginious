package com.nginious.http;

import java.io.File;

public class TestUtils {
	
	public static File findFile(String baseDirPath, String namePart) {
		File baseDir = new File(baseDirPath);
		
		if(!baseDir.exists()) {
			return null;
		}
		
		File[] files = baseDir.listFiles();
		
		for(File file : files) {
			if(file.getName().indexOf(namePart) > -1) {
				return file;
			}
		}
		
		return null;
	}
}
