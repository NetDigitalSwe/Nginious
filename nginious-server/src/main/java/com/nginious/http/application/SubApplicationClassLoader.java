package com.nginious.http.application;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * A sub app class loader loads classes from a single jar library or a single classes directory for a
 * web application.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class SubApplicationClassLoader extends URLClassLoader {
	
	private final ApplicationClassLoader applicationClassLoader;

	static {
        ClassLoader.registerAsParallelCapable();
    }

	private File jarOrClassDir;
	
	private boolean isJar;
	
	private long lastModified;
	
	private HashMap<String, Long> classTstmps;
	
	/**
	 * Constructs a new sub app class loader for the specified jar library or classes directory.
	 * 
	 * @param jarOrClassDir the jar library file or classes directory
	 * @param parent the parent class loader
	 * @param applicationClassLoader class loader for application
	 */
	SubApplicationClassLoader(ApplicationClassLoader applicationClassLoader, File jarOrClassDir, ClassLoader parent) {
		super(new URL[0], null);
		this.applicationClassLoader = applicationClassLoader;
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
	File getJarOrClassDir() {
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
	boolean isModified(String name, boolean className) {
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
		return this.applicationClassLoader.getResourceInternal(name);
	}
	
	URL getResourceInternal(String name) {
		return super.getResource(name);
	}
	
	public Enumeration<URL> getResources(String name) throws IOException {
		return this.applicationClassLoader.getResourcesInternal(name);
	}
	
	Enumeration<URL> getResourcesInternal(String name) throws IOException {
		return super.getResources(name);
	}
	

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }
	
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		return this.applicationClassLoader.loadClassInternal(name, resolve);
	}
	
	Class<?> loadClassInternal(String name, boolean resolve) throws ClassNotFoundException {
		synchronized(getClassLoadingLock(name)) {
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