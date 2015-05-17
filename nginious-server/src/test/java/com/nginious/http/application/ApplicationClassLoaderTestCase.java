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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.nginious.http.TestUtils;
import com.nginious.http.common.FileUtils;

public class ApplicationClassLoaderTestCase extends TestCase {
	
    public ApplicationClassLoaderTestCase() {
		super();
	}

	public ApplicationClassLoaderTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		File dir = new File("build/test-webapps/classload/WEB-INF/lib");
		dir.mkdirs();
		dir = new File("build/test-webapps/classload/WEB-INF/classes");
		dir.mkdir();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		FileUtils.deleteDir("build/test-webapps");
	}	

	public void testClassLoading() throws Exception {
		File webappDir = new File("build/test-webapps/classload");
		ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
		
		ApplicationClassLoader loader = new ApplicationClassLoader(contextLoader, webappDir);
		
		try {
			loader.loadClass("com.nginious.http.loader.ClassLoadTest1");
			fail("Must not be possible to load class that is not in any jar");
		} catch(ClassNotFoundException e) {}
		
		File destDir = new File("build/test-webapps/classload/WEB-INF/classes");
		FileUtils.copyDir("build/classes/testload1/classes", destDir.getAbsolutePath());
		
		try {
			Class.forName("com.nginious.http.loader.ClassLoadTest1");
			fail("Must not be possible to load class that is not in threads context class loader");
		} catch(ClassNotFoundException e) {}
		
		Class<?> clazz = loader.loadClass("com.nginious.http.loader.ClassLoadTest1");
		Object obj = clazz.newInstance();
		assertEquals("ClassLoader1-ClassLoadTest1", obj.toString());
		
		Thread.sleep(1000L);
		
		FileUtils.copyDir("build/classes/testload2/classes", destDir.getAbsolutePath());
		
		clazz = loader.loadClass("com.nginious.http.loader.ClassLoadTest1");
		obj = clazz.newInstance();
		assertEquals("ClassLoader2-ClassLoadTest1", obj.toString());

		File classFile = new File("build/test-webapps/classload/WEB-INF/classes/com/nginious/http/loader/ClassLoadTest1.class");
		classFile.delete();
		
		try {
			clazz = loader.loadClass("com.nginious.http.loader.ClassLoadTest1");
			fail("Must not be possible to load class from removed class file");
		} catch(ClassNotFoundException e) {}
	}
	
	public void testJarLoading() throws Exception {
		File webappDir = new File("build/test-webapps/classload");
		ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
		
		ApplicationClassLoader loader = new ApplicationClassLoader(contextLoader, webappDir);
		
		try {
			loader.loadClass("com.nginious.http.loader.ClassLoadTest1");
			fail("Must not be possible to load class that is not in any jar");
		} catch(ClassNotFoundException e) {}
		
		File srcFile = TestUtils.findFile("build/libs", "testload1");
		File destFile = new File("build/test-webapps/classload/WEB-INF/lib/nginious-loader.jar");
		FileUtils.copyFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
		
		try {
			Class.forName("com.nginious.http.loader.ClassLoadTest1");
			fail("Must not be possible to load class that is not in threads context class loader");
		} catch(ClassNotFoundException e) {}
		
		Class<?> clazz = loader.loadClass("com.nginious.http.loader.ClassLoadTest1");
		Object obj = clazz.newInstance();
		assertEquals("ClassLoader1-ClassLoadTest1", obj.toString());
		
		Thread.sleep(1000L);
		
		srcFile = TestUtils.findFile("build/libs", "testload2");
		FileUtils.copyFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
		clazz = loader.loadClass("com.nginious.http.loader.ClassLoadTest1");
		obj = clazz.newInstance();
		assertEquals("ClassLoader2-ClassLoadTest1", obj.toString());
		
		destFile.delete();
		
		try {
			clazz = loader.loadClass("com.nginious.http.loader.ClassLoadTest1");
			fail("Must not be possible to load class from removed jar");
		} catch(ClassNotFoundException e) {}
	}
	
	public void testCrossReference() throws Exception {
		File webappDir = new File("build/test-webapps/classload");
		ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
		
		File destFile = new File("build/test-webapps/classload/WEB-INF/lib/nginious-loader.jar");
		File srcFile = TestUtils.findFile("build/libs", "testload1");
		FileUtils.copyFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
		File destDir = new File("build/test-webapps/classload/WEB-INF/classes");
		FileUtils.copyDir("build/classes/testload1/classes", destDir.getAbsolutePath());
		File clazzFile = new File("build/test-webapps/classload/WEB-INF/classes/com/nginious/http/app/ClassLoadTest1.class");
		clazzFile.delete();
		clazzFile = new File("build/test-webapps/classload/WEB-INF/classes/com/nginious/http/app/ClassLoadTest2.class");
		clazzFile.delete();
		File propsFile = new File("build/test-webapps/classload/WEB-INF/classes/test.properties");
		propsFile.delete();
		
		ApplicationClassLoader loader = new ApplicationClassLoader(contextLoader, webappDir);
		
		Class<?> clazz = loader.loadClass("com.nginious.http.loader.ClassLoadTest3");
		Object obj = clazz.newInstance();
		assertEquals("ClassLoader1-ClassLoadTest3", obj.toString());
	}
	
	public void testJarResourceLoading() throws Exception {
		File webappDir = new File("build/test-webapps/classload");
		ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
		
		ApplicationClassLoader loader = new ApplicationClassLoader(contextLoader, webappDir);
		
		URL resourceUrl = loader.getResource("test.properties");
		assertNull(resourceUrl);
		Enumeration<URL> resources = loader.getResources("test.properties");
		assertFalse(resources.hasMoreElements());
		
		File destFile = new File("build/test-webapps/classload/WEB-INF/lib/nginious-loader.jar");
		File srcFile = TestUtils.findFile("build/libs", "testload2");
		FileUtils.copyFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
		
		resourceUrl = loader.getResource("test.properties");
		assertNotNull(resourceUrl);
		resources = loader.getResources("test.properties");
		assertTrue(resources.hasMoreElements());
		URL resourcesUrl = resources.nextElement();
		assertEquals(resourceUrl, resourcesUrl);
		assertFalse(resources.hasMoreElements());
		InputStream in = loader.getResourceAsStream("test.properties");
		Properties props = new Properties();
		props.load(in);
		in.close();
		assertEquals("Classloader2", props.getProperty("test"));
		
		Thread.sleep(1000L);
		
		srcFile = TestUtils.findFile("build/libs", "testload1");
		FileUtils.copyFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
		
		resourceUrl = loader.getResource("test.properties");
		assertNotNull(resourceUrl);
		
		resources = loader.getResources("test.properties");
		assertTrue(resources.hasMoreElements());
		resourcesUrl = resources.nextElement();
		assertEquals(resourceUrl, resourcesUrl);
		assertFalse(resources.hasMoreElements());
		in = loader.getResourceAsStream("test.properties");
		props = new Properties();
		props.load(in);
		in.close();
		assertEquals("Classloader1", props.getProperty("test"));

		destFile.delete();
		
		resourceUrl = loader.getResource("test.properties");
		assertNull(resourceUrl);		
		resources = loader.getResources("test.properties");
		assertFalse(resources.hasMoreElements());
	}
	
	public void testClassResourceLoading() throws Exception {
		File webappDir = new File("build/test-webapps/classload");
		ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
		
		ApplicationClassLoader loader = new ApplicationClassLoader(contextLoader, webappDir);
		
		URL resourceUrl = loader.getResource("test.properties");
		assertNull(resourceUrl);		
		Enumeration<URL> resources = loader.getResources("test.properties");
		assertFalse(resources.hasMoreElements());
		
		File destFile = new File("build/test-webapps/classload/WEB-INF/classes/test.properties");
		FileUtils.copyFile("src/testload1/resources/test.properties", destFile.getAbsolutePath());
		
		resourceUrl = loader.getResource("test.properties");
		assertNotNull(resourceUrl);
		resources = loader.getResources("test.properties");
		assertTrue(resources.hasMoreElements());
		URL resourcesUrl = resources.nextElement();
		assertEquals(resourceUrl, resourcesUrl);
		assertFalse(resources.hasMoreElements());
		InputStream in = loader.getResourceAsStream("test.properties");
		Properties props = new Properties();
		props.load(in);
		in.close();
		assertEquals("Classloader1", props.getProperty("test"));
		
		Thread.sleep(1000L);
		
		FileUtils.copyFile("src/testload2/resources/test.properties", destFile.getAbsolutePath());
		
		resourceUrl = loader.getResource("test.properties");
		assertNotNull(resourceUrl);
		resources = loader.getResources("test.properties");
		assertTrue(resources.hasMoreElements());
		resourcesUrl = resources.nextElement();
		assertEquals(resourceUrl, resourcesUrl);
		assertFalse(resources.hasMoreElements());
		in = loader.getResourceAsStream("test.properties");
		props = new Properties();
		props.load(in);
		in.close();
		assertEquals("Classloader2", props.getProperty("test"));

		destFile.delete();
		resourceUrl = loader.getResource("test.properties");
		assertNull(resourceUrl);		
		resources = loader.getResources("test.properties");
		assertFalse(resources.hasMoreElements());
	}

	public void testMultithreadClassLoading() throws Exception {
		File srcFile = TestUtils.findFile("build/libs", "testload1");
		File destFile = new File("build/test-webapps/classload/WEB-INF/lib/nginious-loader.jar");
		FileUtils.copyFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath());

		for (int i = 0; i < 1000; i++) {
			List<ApplicationClassLoaderTest> tests = new ArrayList<>();
			CountDownLatch startLatch = new CountDownLatch(1);
			CountDownLatch stopLatch = new CountDownLatch(50);

			File webappDir = new File("build/test-webapps/classload");
			ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
			
			ApplicationClassLoader loader = new ApplicationClassLoader(contextLoader, webappDir);
			
			for (int j = 0; j < 50; j++) {
				String clazzName = j % 2 == 0 ? "com.nginious.http.loader.ClassReferenceTest1" : "com.nginious.http.loader.ClassReferenceTest2";
				String expectedToString = j % 2 == 0 ? "ClassLoader1-ClassReferenceTest1" : "ClassLoader1-ClassReferenceTest2";
				ApplicationClassLoaderTest test = new ApplicationClassLoaderTest(loader,
						startLatch,
						stopLatch,
						clazzName,
						expectedToString);
				tests.add(test);
				Thread testThread = new Thread(test);
				testThread.start();
			}

			startLatch.countDown();
			stopLatch.await();

			for (ApplicationClassLoaderTest test : tests) {
				assertTrue(test.isSuccess());
			}
		}
	}

	public static Test suite() {
		return new TestSuite(ApplicationClassLoaderTestCase.class);
	}

	public static void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}

	private class ApplicationClassLoaderTest implements Runnable {

		private ApplicationClassLoader loader;

		private CountDownLatch startLatch;

		private CountDownLatch stopLatch;

		private String clazzName;

		private String expectedToString;

		private boolean success;

		private ApplicationClassLoaderTest(ApplicationClassLoader loader,
				CountDownLatch startLatch,
				CountDownLatch stopLatch,
				String clazzName,
				String expectedToString) {
			super();
			this.loader = loader;
			this.startLatch = startLatch;
			this.stopLatch = stopLatch;
			this.clazzName = clazzName;
			this.expectedToString = expectedToString;
		}

		private boolean isSuccess() {
			return success;
		}

		public void run() {
			try {
				startLatch.await();

				Class<?> clazz = loader.loadClass(clazzName);
				Object obj = clazz.newInstance();
				assertEquals(expectedToString, obj.toString());

				success = true;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				stopLatch.countDown();
			}
		}
	}
}
