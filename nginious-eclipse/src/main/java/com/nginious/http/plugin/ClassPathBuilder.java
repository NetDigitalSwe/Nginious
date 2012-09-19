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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.PreferenceConstants;

class ClassPathBuilder implements IResourceProxyVisitor {
	
	private IClasspathEntry[] classpathEntries;
	
	private IProject project;
	
	private IProgressMonitor monitor;
	
	private HashSet<IPath> sources;
	
	private HashSet<IResource> classes;
	
	private HashSet<IPath> jars;
	
	private IPath output;
	
	ClassPathBuilder(IProject project, IProgressMonitor monitor) {
		this.project = project;
		this.monitor = monitor;
		this.sources = new HashSet<IPath>();
		this.classes = new HashSet<IResource>();
		this.jars = new HashSet<IPath>();
	}
	
	public boolean visit(IResourceProxy proxy) {
		if(monitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		if(proxy.getType() == IResource.FILE) {
			String name = proxy.getName();
			
			if(name.endsWith(".java")) {
				IResource sourceFile = proxy.requestResource();
				findSourceRoot((IFile)sourceFile);
			} else if(name.endsWith(".class")) {
				classes.add(proxy.requestResource());
			} else if (name.endsWith(".jar")) {
				jars.add(proxy.requestFullPath());
			}
			
			return false;
		}
		
		return true;
	}
	
	private void findSourceRoot(IFile sourceFile) {
		InputStream in = null;
		BufferedReader reader = null;
		
		try {
			in = sourceFile.getContents();
			reader = new BufferedReader(new InputStreamReader(in));
			String line = null;
			String packageName = null;
			
			while(packageName == null && (line = reader.readLine()) != null) {
				line = line.trim();
				
				if(line.startsWith("package")) {
					packageName = line.substring(7).trim();
				}
			}
			
			IPath packagePath = sourceFile.getParent().getFullPath();		
			
			if (packageName == null) {
				sources.add(packagePath);
			} else {
				IPath relativePath = new Path(packageName.replace('.', '/'));
				int segmentDiff = packagePath.segmentCount() - relativePath.segmentCount();
				
				if(segmentDiff >= 0) {
					IPath commonPath = packagePath.removeFirstSegments(segmentDiff);
					
					if(commonPath.equals(relativePath)) {
						sources.add(packagePath.uptoSegment(segmentDiff));
					}
				}
			}
		} catch(CoreException e) {
			// Skip file
		} catch(IOException e) {
			// Skip file
		} finally {
			if(in != null) {
				try { in.close(); } catch(IOException e) {}
			}
		}
	}
	
	void build(IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(Messages.ClassPathBuilder_operation_description, 4);
			project.accept(this, IResource.NONE);
			monitor.worked(1);
			ArrayList<IClasspathEntry> entries= new ArrayList<IClasspathEntry>();
			findSourceFolders(entries);
			
			if(monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			
			monitor.worked(1);

			if(monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			
			monitor.worked(1);

			IPath outputLocation = findOutputLocation();
			
			if(monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			
			monitor.worked(1);
			
			findLibraries(entries);
			
			if(monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			
			monitor.worked(1);

			findApi(entries);
			
			if(monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			
			monitor.worked(1);

			IClasspathEntry[] jreEntries= PreferenceConstants.getDefaultJRELibrary();
			
			for (int i = 0; i < jreEntries.length; i++) {
				entries.add(jreEntries[i]);
			}
			
			IClasspathEntry[] outEntries = entries.toArray(new IClasspathEntry[entries.size()]);
			
			this.classpathEntries = outEntries;
			this.output = outputLocation;
		} finally {
			monitor.done();
		}
	}
	
	private IPath findOutputLocation() {
		IPath projectPath = project.getFullPath();
		IPath outputPath = projectPath.append("web/WEB-INF/classes");
		return outputPath;
	}
	
	private void findSourceFolders(ArrayList<IClasspathEntry> entries) {
		for(IPath source : this.sources) {
			IClasspathEntry entry = JavaCore.newSourceEntry(source);
			entries.add(entry);
		}
	}
	
	private void findApi(ArrayList<IClasspathEntry> entries) {
		try {
			URL apiJar = NginiousPlugin.getApiJar();
			
			if(apiJar != null) {
				String filePath = apiJar.toString();
				filePath = filePath.substring(5);
				Path path = new Path(filePath);
				IClasspathEntry entry = JavaCore.newLibraryEntry(path, null, null);
				entries.add(entry);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void findLibraries(ArrayList<IClasspathEntry> entries) {
		IPath projectPath = project.getFullPath();
		IPath libPath = projectPath.append("WebContent/WEB-INF/lib");
		
		for(IPath path : this.jars) {
			if(libPath.isPrefixOf(path)) {
				IClasspathEntry entry = JavaCore.newLibraryEntry(path, null, null);
				entries.add(entry);
			}
		}
	}
	
	IClasspathEntry[] getClassPath() {
		return this.classpathEntries;
	}
	
	IPath getOutputLocation() {
		return this.output;
	}
}
