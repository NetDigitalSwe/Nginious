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

package com.nginious.http.application;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import com.nginious.http.common.IteratorEnumeration;

/**
 * A class loader for loading classes from jar libraries and class directories in a web
 * application.
 * 
 * <p>
 * This class loader is capable of detecting changes in jar files and class directories
 * including added, changed and removed jar libraries and classes. Jar libraries and
 * classes are reloaded as needed on next invocation to this class loader. This provides
 * for fast changes during development since classes can be recompiled and resources
 * updated in place without having to repackage and redeploy the whole web application.
 * </p>
 * 
 * <p>
 * Each jar library and class directory has its own sub class loader. When a change in
 * a jar library or class directory is detected only the corresponding sub class loader
 * is removed and replaced with a new sub class loader. This procedure is necessary
 * since class loaders are immutable.
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class ApplicationClassLoader extends ClassLoader {
	
	private ConcurrentHashMap<File, SubAppClassLoader> subClassLoaders;
	
	private ClassLoader parent;
	
	private File webAppDir;
	
	/**
	 * Constructs a new app class loader which uses the specified parent class loader.
	 * 
	 * @throws IOException if unable to set up class loader
	 */
	public ApplicationClassLoader(ClassLoader parent) {
		this(parent, null);
	}
	
	/**
	 * Constructs a new app class loader which uses the specified parent class
	 * loader and loads classes for the web application in the specified
	 * web application directory.
	 * 
	 * @param parent the parent class loaader
	 * @param webAppDir the web application directory
	 */
	public ApplicationClassLoader(ClassLoader parent, File webAppDir) {
		super(parent);
		this.parent = parent;
		this.subClassLoaders = new ConcurrentHashMap<File, SubAppClassLoader>();
		setWebAppDir(webAppDir);
	}
	
	/**
	 * Returns whether or not this class loader has loaded the specified class or not.
	 * 
	 * @param clazz the class to check
	 * @return <code>true</code> if the class had been loaded by this class loader, <code>false</code> otherwise
	 */
	public boolean hasLoaded(Class<?> clazz) {
		ClassLoader loader = clazz.getClassLoader();
		
		if(loader == null) {
			return false;
		}
		
		if(loader.equals(this)) {
			return true;
		}
		
		for(ClassLoader subClassLoader : subClassLoaders.values()) {			
			if(loader.equals(subClassLoader)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Return whether or not the specified class has been modified since it was loaded by this class loader.
	 * 
	 * @param clazz the class to check for modifications
	 * @return <code>true</code> if the class has been modified, <code>false</code> otherwise
	 */
	public boolean reloadIfModified(Class<?> clazz) {
		if(!hasLoaded(clazz)) {
			return true;
		}
		
		ClassLoader loader = clazz.getClassLoader();
		
		for(SubAppClassLoader subClassLoader : subClassLoaders.values()) {
			if(loader.equals(subClassLoader)) {
				boolean modified = subClassLoader.isModified(clazz.getName(), true);
				
				if(modified) {
					reloadIfModified(subClassLoader, clazz.getName(), true);
					return true;
				}
			}
		}
		
		return false;
	}
	
	void setWebAppDir(File webAppDir) {
		if(this.webAppDir != null) {
			throw new IllegalArgumentException("Web app dir already set");
		}
		
		if(webAppDir != null) {
			this.webAppDir = webAppDir;
			setupSubClassLoaders(this.webAppDir, true);
		}
	}
	
	/**
	 * Finds resource with the specified name.
	 * 
	 * @param name the resource name
	 * @return A URL for reading the resource or <code>null</code> if the resource could not be found
	 */
	public URL getResource(String name) {
		return getResourceInternal(name);
	}
	
	/**
	 * Find resource with the specified name. Checks all sub class loaders for the resource until
	 * one of the sub class loaders returns a matching resource. If the owning sub class loader
	 * detects that the resource has changed since the sub class loader was created this class
	 * loader is instructed to replace the sub class loader to update the changes. This procedure
	 * is necessary since class loaders are immutable.
	 * 
	 * @param name the resource name
	 * @return a URL for reading the resource or <code>null</code> if the resource could not be found
	 */
	private URL getResourceInternal(String name) {
		Collection<SubAppClassLoader> classLoaders = subClassLoaders.values();
		
		for(SubAppClassLoader classLoader : classLoaders) {
			classLoader = reloadIfModified(classLoader, name, false);
			
			if(classLoader != null) {
				URL resource = classLoader.getResourceInternal(name);
				
				if(resource != null) {
					return resource;
				}
			}
		}
		
		if(this.webAppDir != null) {
			classLoaders = checkNewSubClassLoaders(this.webAppDir);
			
			for(SubAppClassLoader classLoader : classLoaders) {
				URL resource = classLoader.getResourceInternal(name);
				
				if(resource != null) {
					return resource;
				}				
			}
		}
		
		return super.getResource(name);
	}
	
	/**
	 * Finds all resources with the specified name.
	 * 
	 * @param name the resource name
	 * @return An enumeration over all URLs for the resource or an empty enumeration if no resources found
	 * @throws IOException if unable to load a resource
	 */
	public Enumeration<URL> getResources(String name) throws IOException {
		return getResourcesInternal(name);
	}
	
	/**
	 * Find all resources with the specified name. Checks all sub class loaders for a resource with
	 * the specified name. If a sub class loader detects that a resource with the specified name
	 * has changed since the sub vlass loader was created this class loader is instructed to replace
	 * the sub class loader to update changes. This procedure is neccesary since class loaders are
	 * immutable.
	 * 
	 * @return An enumeration over all URLs for the resource or an empty enumeration if no resources found
	 * @throws IOException if unable to load a resource
	 */
	private Enumeration<URL> getResourcesInternal(String name) throws IOException {
		Collection<SubAppClassLoader> classLoaders = subClassLoaders.values();
		
		HashSet<URL> outResources = new HashSet<URL>();
		
		for(SubAppClassLoader classLoader : classLoaders) {
			classLoader = reloadIfModified(classLoader, name, false);
			
			if(classLoader != null) {
				Enumeration<URL> resources = classLoader.getResourcesInternal(name);
				
				if(resources != null) {
					while(resources.hasMoreElements()) {
						outResources.add(resources.nextElement());
					}
				}
			}
		}
		
		if(outResources.size() == 0 && this.webAppDir != null) {
			classLoaders = checkNewSubClassLoaders(this.webAppDir);
			
			for(SubAppClassLoader classLoader : classLoaders) {
				Enumeration<URL> resources = classLoader.getResourcesInternal(name);
				
				if(resources != null) {
					while(resources.hasMoreElements()) {
						outResources.add(resources.nextElement());
					}
				}				
			}
		}
		
		if(outResources.isEmpty()) {
			return super.getResources(name);
		}
		
		return new IteratorEnumeration<URL>(outResources.iterator());
	}
	
	/**
	 * Loads the class with the specified binary name.
	 * 
	 * @param name binary name of the class
	 * @return the found class object
	 * @throws ClassNotFoundException if the class could not be found
	 */
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, false);
	}
	
	/**
	 * Loads the class with the specified binary name.
	 * 
	 * @param name binary name of the class
	 * @param resolve whether or not to resolve the class
	 * @return the found class object
	 * @throws ClassNotFoundException if the class could not be found
	 */
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		return loadClassInternal(name, resolve);
	}
	
	/**
	 * Loads the class with the specified binary name. Checks all sub class loaders for the class until
	 * one of the sub class loaders returns the class. If the owning sub class loader
	 * detects that the class has changed since the sub class loader was created this class
	 * loader is instructed to replace the sub class loader to update the changes. This procedure
	 * is necessary since class loaders are immutable.
	 *
	 * @param name binary name of the class
	 * @param resolve whether or not to resolve the class
	 * @return the found class object
	 * @throws ClassNotFoundException if the class could not be found
	 */
	protected Class<?> loadClassInternal(String name, boolean resolve) throws ClassNotFoundException {
		Collection<SubAppClassLoader> classLoaders = subClassLoaders.values();
		
		for(SubAppClassLoader classLoader : classLoaders) {
			classLoader = reloadIfModified(classLoader, name, true);
			
			if(classLoader != null) {
				try {
					return classLoader.loadClassInternal(name, resolve);
				} catch(ClassNotFoundException e) {}
			}
		}
		
		if(this.webAppDir != null) {
			classLoaders = checkNewSubClassLoaders(this.webAppDir);
			
			for(SubAppClassLoader classLoader : classLoaders) {
				try {
					return classLoader.loadClassInternal(name, resolve);
				} catch(ClassNotFoundException e) {}
			}
		}
		
		return super.loadClass(name, resolve);
	}
	
	/**
	 * Replaces the specified sub class loader with a new sub class loader if the resource with the specified
	 * has changed since the sub class loader was created.
	 * 
	 * @param classLoader the sub class loader
	 * @param name the resource name
	 * @param className whether or not the resource name is a binary class name
	 * @return the new sub class loader or <code>null</code> if resource not modified
	 */
	private SubAppClassLoader reloadIfModified(SubAppClassLoader classLoader, String name, boolean className) {
		if(classLoader.isModified(name, className)) {
			File jarOrClassDir = classLoader.getJarOrClassDir();
			
			if(jarOrClassDir.exists()) {
				classLoader = new SubAppClassLoader(jarOrClassDir, this.parent);
				subClassLoaders.put(jarOrClassDir, classLoader);
			} else {
				subClassLoaders.remove(jarOrClassDir);
				classLoader = null;
			}
		}
		
		return classLoader;
	}
	
	/**
	 * Checks web applications WEB-INF/lib directory for new jar libraries that need to be added
	 * as a sub class loader for this class loader. If new jar libraries are found a sub class
	 * loader is created for each jar library.
	 * 
	 * @param webAppDir the web application directory to check
	 * @return all newly created sub class loaders
	 */
	private Collection<SubAppClassLoader> checkNewSubClassLoaders(File webAppDir) {
		File webInfDir = new File(webAppDir, "WEB-INF");
		File libDir = new File(webInfDir, "lib");
		File[] possibleLibs = libDir.listFiles();
		ArrayList<SubAppClassLoader> newClassLoaders = new ArrayList<SubAppClassLoader>();
		
		if(possibleLibs != null) {
			for(File possibleLib : possibleLibs) {
				if(!subClassLoaders.containsKey(possibleLib)) {
					SubAppClassLoader classLoader = new SubAppClassLoader(possibleLib, this.parent);
					subClassLoaders.put(possibleLib, classLoader);
					newClassLoaders.add(classLoader);
				}
			}
		}
		
		return newClassLoaders;
	}
	
	/**
	 * Sets up sub class loaders for all jar libraries and classes directories found within the web application
	 * for which this class loader is created.
	 * 
	 * <p>
	 * <ul>
	 * <li>jar libraries are found in the <code>WEB-INF/lib</code> subdirectory of the web application.</li>
	 * <li>classes are found in the <code>WEB-INF/classes<code> subdirectory of the web application.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param webAppDir the web application directory
	 * @param initial whether or not the call to this method is the initial setup of this class loader
	 */
    private void setupSubClassLoaders(File webAppDir, boolean initial) {
		File webInfDir = new File(webAppDir, "WEB-INF");
		
		if(initial) {
			File classesDir = new File(webInfDir, "classes");
			SubAppClassLoader classLoader = new SubAppClassLoader(classesDir, this.parent);
			subClassLoaders.put(classesDir, classLoader);
		}

		File libDir = new File(webInfDir, "lib");
		File[] possibleLibs = libDir.listFiles();

		if(possibleLibs != null) {
			for(File possibleLib : possibleLibs) {
				String name = possibleLib.getName();
				
				if(possibleLib.isFile() && name.endsWith(".jar") && !subClassLoaders.containsKey(possibleLib)) {
					SubAppClassLoader classLoader = new SubAppClassLoader(possibleLib, this.parent);
					subClassLoaders.put(possibleLib, classLoader);
				}
			}
		}    	
    }
    
    /**
     * A sub app class loader loads classes from a single jar library or a single classes directory for a
     * web application.
     * 
     * @author Bojan Pisler, NetDigital Sweden AB
     *
     */
    private class SubAppClassLoader extends URLClassLoader {
		
		private File jarOrClassDir;
		
		private boolean isJar;
		
		private long lastModified;
		
		private HashMap<String, Long> classTstmps;
		
		/**
		 * Constructs a new sub app class loader for the specified jar library or classes directory.
		 * 
		 * @param jarOrClassDir the jar library file or classes directory
		 * @param parent the parent class loader
		 */
		private SubAppClassLoader(File jarOrClassDir, ClassLoader parent) {
			super(new URL[0], null);
			this.jarOrClassDir = jarOrClassDir;
			this.isJar = jarOrClassDir.isFile();
			this.lastModified = jarOrClassDir.lastModified();
			this.classTstmps = new HashMap<String, Long>();
			
			try {
				URL url = jarOrClassDir.toURI().toURL();
				
				// Prevents Java from caching Jars which could lead to stale resources being returned in case
				// Jar file is updated in file system.
				try { url.openConnection().setDefaultUseCaches(false); } catch(IOException e) {}
				
				
				addURL(url);
			} catch(MalformedURLException e) {
				throw new RuntimeException("Creating class loader failed", e);
			}
		}
		
		/**
		 * Returns the jar library file or classes directory from which this class loader loads classes
		 * and resources.
		 * 
		 * @return the jar library file or classes directory
		 */
		private File getJarOrClassDir() {
			return this.jarOrClassDir;
		}
		
		/**
		 * Returns whether or not any of the classes or resources that this sub class loader
		 * has loaded are changed.
		 * 
		 * <p>
		 * The following defines the rules.
		 * 
		 * <ul>
		 * <li>If this sub class loader loads classes and resources from a jar file a check is made to
		 *  verify if the jar library file has changed since this sub class loader was created.</li>
		 * <li>If this sub class loader loads classes and resources from a classes directory a check
		 * 	is made if the resource or class with the specified name has been changed since this sub
		 * 	class loader was created.</li>
		 * </ul>
		 * </p>
		 * 
		 * @param name the resource or binary class name
		 * @param className whether or not the name is a binary class name
		 * @return <code>true</code> if modified, <code>false</code> otherwise
		 */
		private boolean isModified(String name, boolean className) {
			if(this.isJar) {
				if(!jarOrClassDir.exists()) {
					return true;
				}
				
				return jarOrClassDir.lastModified() > this.lastModified;
			}
			
			File resourceFile = null;
			
			if(className) {
				String subPath = name.replaceAll("\\.", File.separator) + ".class";
				resourceFile = new File(this.jarOrClassDir, subPath);
				
				if(!resourceFile.exists()) {
					if(classTstmps.containsKey(name)) {
						return true;
					}
					
					return false;
				}
			} else {
				resourceFile = new File(this.jarOrClassDir, name);
				
				if(!resourceFile.exists()) {
					return false;
				}
			}
			
			long lastModified = 0L;
			
			if(classTstmps.containsKey(name)) {
				lastModified = classTstmps.get(name);
			}
			
			if(resourceFile.exists() && lastModified > 0L && resourceFile.lastModified() > lastModified) {
				return true;
			}
			
			return false;
		}
		
		private long getLastModified(String name) {
			if(this.isJar) {
				return jarOrClassDir.lastModified();
			}
			
			String subPath = name.replaceAll("\\.", File.separator) + ".class";
			File resourceFile = new File(this.jarOrClassDir, subPath);
			return resourceFile.lastModified();
		}
		
		public URL getResource(String name) {
			return ApplicationClassLoader.this.getResourceInternal(name);
		}
		
		private URL getResourceInternal(String name) {
			return super.getResource(name);
		}
		
		public Enumeration<URL> getResources(String name) throws IOException {
			return ApplicationClassLoader.this.getResourcesInternal(name);
		}
		
		private Enumeration<URL> getResourcesInternal(String name) throws IOException {
			return super.getResources(name);
		}
		

	    public Class<?> loadClass(String name) throws ClassNotFoundException {
	        return loadClass(name, false);
	    }
		
	    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			return ApplicationClassLoader.this.loadClassInternal(name, resolve);
		}
		
		private Class<?> loadClassInternal(String name, boolean resolve) throws ClassNotFoundException {
			Class<?> clazz = findLoadedClass(name);
			
			if(clazz == null) {
				clazz = findClass(name);
				
				if(resolve) {
					resolveClass(clazz);
				}
			}
			
			if(clazz != null) {
				long lastModified = getLastModified(name);
				classTstmps.put(name, lastModified);
			}
			
			return clazz;
		}
	}
}
