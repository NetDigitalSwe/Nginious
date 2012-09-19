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

package com.nginious.http.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;

import com.nginious.http.xsp.XspCompiler;
import com.nginious.http.xsp.XspException;

/**
 * An ant task for precompiling XSP files into Java classes. Below is an example that compiles
 * all XSP files found in directory 'webapps/test/WEB-INF/xsp' to class directory
 * 'webapps/test/WEB-INF/classes'. XSP files are compiled if no previous compiled class exists
 * or if the modification time of the XSP file is newer than the existing classes modification
 * time.
 * 
 * <pre>
 * <xspc srcdir="webapps/test/WEB-INF/xsp" destdir="webapps/test/WEB-INF/classes" />
 * </pre>
 * 
 * The xspc task accepts the following attributes.
 * 
 * <ul>
 * 	<li>srcdir - source directory for XSP files.</li>
 * 	<li>destdir - base destination directory for classes compiled from the XSP files.</li>
 * </ul>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class XspcTask extends MatchingTask {
	
	private File basedir;
	
	private Path src;
	
	private File destDir;
	
    protected File[] compileList = new File[0];
    
    /**
     * Constructs a new XSPC task.
     */
    public XspcTask() {
		super();
	}
    
    /**
     * Sets base directory to the specified base directory. The base directory defines the working directory
     * where this task is executed.
     * 
     * @param basedir the base working directory
     */
    public void setBasedir(File basedir) {
    	this.basedir = basedir;
    }
    
    /**
     * Returns the base working directory for this XSPC task.
     * 
     * @return the base working directory
     */
    public File getBasedir() {
    	return this.basedir;
    }
	
    /**
     * Sets the XSP file source directory to the specified directory for this XSPC task.
     * 
     * @param srcDir the XSP file source directory
     */
    public void setSrcdir(Path srcDir) {
    	if(this.src == null) {
    		this.src = srcDir;
        } else {
            src.append(srcDir);
        }
    }
	
    /**
     * Returns the XSP file source directory for this XSPC task.
     * 
     * @return the XSP file source directory
     */
    public Path getSrcdir() {
        return this.src;
    }
    
    /**
     * Sets the destination directory for compiled XSP classes to the specified directory.
     * 
     * @param destDir the XSP classes destination directory
     */
    public void setDestdir(File destDir) {
    	this.destDir = destDir;
    }
    
    /**
     * Returns the XSP classes destination directory for this XSPC task.
     *  
     * @return the XSP classes destination directory
     */
    public File getDestdir() {
        return destDir;
    }
    
    /**
     * Executes this XSPC task by compiling all XSP files found in the source directory and stores
     * the compiled XSP classes under the base destination directory. Any XSP files that have already
     * been compiled are not recompiled unless the modification time of the XSP file is newer than the
     * existing compiled XSP class.
     * 
     * @throws BuildException if the XSP file is invalid
     */
    public void execute() throws BuildException {
    	checkParameters();
    	resetFileLists();
    	
    	String[] list = src.list();
    	
    	for(int i = 0; i < list.length; i++) {
    		File srcDir = getProject().resolveFile(list[i]);
    		
    		if(!srcDir.exists()) {
    			throw new BuildException("srcdir \"" + srcDir.getPath() + "\" does not exist!", getLocation());
            }
    		
            DirectoryScanner ds = this.getDirectoryScanner(srcDir);
            String[] files = ds.getIncludedFiles();
            this.compileList = createFiles(srcDir, files);
        }

        compile();
    }

    protected void resetFileLists() {
        compileList = new File[0];
    }
    
    private File[] createFiles(File srcDir, String[] files) {
    	File[] outFiles = new File[files.length];
    	int pos = 0;
    	
    	for(String file : files) {
    		outFiles[pos++] = new File(srcDir, file);
    	}
    	
    	return outFiles;
    }
    
    protected void compile() {
    	XspCompiler compiler = new XspCompiler();
    	boolean noneCompiled = true;
		
		for(File compileFile : this.compileList) {
			try {
				boolean compiled = compiler.compileClass(basedir.getAbsolutePath(), compileFile.getAbsolutePath(), getDestdir().getPath());
				
				if(compiled) {
					System.out.println("Compiled " + compileFile);
					noneCompiled = false;
				}
			} catch(XspException e) {
				noneCompiled = false;
				log("Compile failed " + e.getMessage(), e, Project.MSG_WARN);
			}
    	}
		
		if(noneCompiled) {
			System.out.println("No new or modified files");
		}
    }
    
    protected void checkParameters() throws BuildException {
    	if(src == null) {
    		throw new BuildException("srcdir attribute must be set!", getLocation());
    	}
    	
    	if(src.size() == 0) {
    		throw new BuildException("srcdir attribute must be set!", getLocation());
    	}
    	
    	if(destDir != null && !destDir.isDirectory()) {
    		throw new BuildException("destination directory \""
    				+ destDir
    				+ "\" does not exist "
    				+ "or is not a directory", getLocation());
    	}
    }
}
