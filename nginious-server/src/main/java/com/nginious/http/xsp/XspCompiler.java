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

package com.nginious.http.xsp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * <p>
 * Creates XSP service classes from XSP files. The XSP service classes are created runtime by building
 * necessary bytecode for the class. The created class is a subclass of {@link XspService} and overrides the
 * method {@link XspService#executeXsp(com.nginious.http.HttpRequest, com.nginious.http.HttpResponse)}.
 * </p>
 * 
 * <p>
 * The following outlines the steps used for creating a XSP service class
 * <ul>
 * <li>The XSP file is parsed into a document tree node structure. See {@link DocumentPart}.</li>
 * <li>Bytecode for a subclass of {@link XspService} is created.</li>
 * <li>The name of the subclass is created from the meta tag in the XSP file (if available) and the XSP filename.</li>
 * <li>The document tree node structure is traversed and converted into bytecode for the subclass.</li>
 * </ul>
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class XspCompiler {
	
	private ClassLoader classLoader;
	
	/**
	 * Constructs a new XSP compiler which uses the calling threads class loader to load XSP service classes 
	 * constructed from XSP pages at runtime.
	 */
    public XspCompiler() {
    	this(Thread.currentThread().getContextClassLoader());
    }
    
    /**
     * Constructs a new XSP compiler which uses the specified class loader to load XSP service classes
     * constructed from XSP pages at runtime.
     * 
     * @param classLoader the class loader
     */
    public XspCompiler(ClassLoader classLoader) {
        super();
        this.classLoader = classLoader;
    }
    
    /**
     * Compiles the XSP page at the specified source file path and saves the class file for the
     * generated XSP service in the specified destination directory.
     * 
     * @param baseDir the web application base directory
     * @param srcFilePath XSP page source file path relative to the base directory
     * @param destDirPath the directory relative to the base directory where generated XSP service class should be saved
     * @return <code>true</code> if XSP service class has been compiled, <code>false</code> otherwise. A XSP page is
     *  compiled into a XSP service class if the XSP service class does not exist or is modified.
     * @throws XspException if XSP page is invalid
     */
    public boolean compileClass(String baseDir, String srcFilePath, String destDirPath) throws XspException {
    	return compile(baseDir, srcFilePath, destDirPath) != null;
    }
    
    /**
     * Compiles the XSP page at the specified source file path and saves the class file for the
     * generated XSP service in the specified destination directory. The XSP service class is loaded and instantiated.
     * 
     * @param baseDir the web application base directory
     * @param srcFilePath XSP page source file path relative to the base directory
     * @param destDirPath the directory relative to the base directory where generated XSP service class should be saved
     * @return instance of compiled XSP service or <code>null</code> if XSP service has already been compiled and isn't
     *  modified since last compilation.
     * @throws XspException if XSP page is invalid
     */
    public XspService compileService(String baseDir, String srcFilePath, String destDirPath) throws XspException {
    	ClassDescriptor descriptor = compile(baseDir, srcFilePath, destDirPath);
    	
    	if(descriptor != null) {
    		try {
    			Class<?> clazz = classLoader.loadClass(descriptor.getClassName());
    			return (XspService)clazz.newInstance();
    		} catch(Exception e) {
    			throw new XspException("Unable to compile " + srcFilePath, e);
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Compiles the XSP page at the specified source file path and saves the class file for the generated
     * XSP service in the specified destination directory.
     * 
     * @param baseDir the web application base directory
     * @param srcFilePath XSP page source file path relative to the base directory
     * @param destDirPath the directory relative to the base directory where generated XSP service class should be saved
     * @return a class descriptor for the generated XSP service
     * @throws XspException if XSP page is invalid
     */
    private ClassDescriptor compile(String baseDir, String srcFilePath, String destDirPath) throws XspException {
    	checkJavaVersion();
    	byte[] fileData = readFile(srcFilePath);
    	XspParser parser = new XspParser();
    	DocumentPart document = parser.parse(srcFilePath, fileData);
    	
    	if(isNewOrModified(document, srcFilePath, destDirPath)) {
        	String relSrcPath = createRelativePath(baseDir, srcFilePath);
    		ClassDescriptor descriptor = compileService(document, relSrcPath);
    		saveService(document, srcFilePath, destDirPath, descriptor.getByteCode());
    		return descriptor;
    	}
    	
    	return null;
    	
    }
    
    /**
     * Compiles the XSP page at the specified source file path.
     * 
     * @param srcFilePath the XSP page source file path
     * @return a subclass of {@link XspService} compiled from the XSP page
     * @throws XspException if unable to compile XSP page
     */
    public XspService compileService(String srcFilePath) throws XspException {
        try {
        	checkJavaVersion();
        	byte[] fileData = readFile(srcFilePath);
            XspParser parser = new XspParser();
            DocumentPart document = parser.parse(srcFilePath, fileData);
            ClassDescriptor descriptor = compileService(document, srcFilePath);
            Class<?> clazz = loadClass(descriptor);
            return (XspService)clazz.newInstance();
        } catch(Exception e) {
            throw new XspException("Unable to compile " + srcFilePath, e);
        }
   	
    }
    
    /**
     * Creates a relative path from the specified absolute source file path using the specified base
     * directory as base.
     * 
     * @param baseDir the base directory
     * @param srcFilePath the absolute source file path
     * @return a relative source file path
     * @throws XspException if source file path is not a subdirectory of base dir
     */
    private String createRelativePath(String baseDir, String srcFilePath) throws XspException {
    	if(baseDir.endsWith("/")) {
    		baseDir = baseDir.substring(0, baseDir.length() - 1);
    	}
    	
    	if(!srcFilePath.startsWith(baseDir)) {
    		throw new XspException("Src file path " + srcFilePath + " is not in a subdirectory of base dir " + baseDir);
    	}
    	
    	return srcFilePath.substring(baseDir.length());
    }
    
    /**
     * Checks that runtime Java version is 1.6 or later.
     * 
     * @throws XspException if Java version is earlier than 1.6
     */
    private void checkJavaVersion() throws XspException {
    	String javaVersion = System.getProperty("java.version");
    	
    	try {
    		double majorMinor = Double.parseDouble(javaVersion.substring(0, 3));
    		
    		if(majorMinor < 1.6d) {
    			throw new XspException("Incompatible Java version " + javaVersion + ", must be 1.6 or higher");
    		}
    	} catch(NumberFormatException e) {
    		throw new RuntimeException("Unable to determine Java version", e);
    	}
    }
    
    /**
     * Saves the specified class bytes into a class file in the specified destination directory path.
     * 
     * @param document the tree node document
     * @param srcFilePath the source file path
     * @param destDirPath the destination directory path
     * @param classBytes the class bytes
     * @throws XspException if unable to save class
     */
    private void saveService(DocumentPart document, String srcFilePath, String destDirPath, byte[] classBytes) throws XspException {
    	File destFile = createDestFile(document, srcFilePath, destDirPath, true);
    	FileOutputStream out = null;
    	
    	try {
    		out = new FileOutputStream(destFile);
    		out.write(classBytes);
    		out.flush();
    	} catch(IOException e) {
    		throw new XspException("Can't save compiled service class " + destFile, e);
    	} finally {
    		if(out != null) {
    			try { out.close(); } catch(IOException e) {}
    		}
    	}
    }
    
    /**
     * Checks whether or not a class file for the XSP file specified by the document tree node exists
     * or is modified compared to the original XSP file.
     * 
     * @param document the document tree node
     * @param srcFilePath the XSP source file path
     * @param destDirPath the class destination directory
     * @return <code>true</code> if the class file does not exists or XSP file is newer, <code>false</code> otherwise
     */
    private boolean isNewOrModified(DocumentPart document, String srcFilePath, String destDirPath) {
    	String packageName = document.getMetaContent("package");
    	File destDir = new File(destDirPath);
    	
    	if(!destDir.exists()) {
    		return true;
    	}
    	
    	if(packageName != null) {
    		String[] packageParts = packageName.split("\\.");
    		
    		for(String packagePart : packageParts) {
    			destDir = new File(destDir, packagePart);
    			
    			if(!destDir.exists()) {
    				return true;
    			}
    		}
    	}
    	
        File destFile = createFile(srcFilePath, destDir);
        File srcFile = new File(srcFilePath);
        boolean modified = srcFile.lastModified() > destFile.lastModified();
        return modified;
    }
    
    /**
     * Creates destination file based on the specified tree node document, source file path and destination
     * directory path.
     * 
     * @param document the tree node document
     * @param srcFilePath the source file path
     * @param destDirPath the destination directory path
     * @param createSubDirs whether or not to create sub directories 
     * @return the creates destination file
     * @throws XspException if unable to create destination file
     */
    private File createDestFile(DocumentPart document, String srcFilePath, String destDirPath, boolean createSubDirs) throws XspException {
    	String packageName = document.getMetaContent("package");
    	File destDir = new File(destDirPath);
    	
    	if(createSubDirs && !destDir.exists()) {
    		throw new XspException("Destination directory " + destDir.getAbsolutePath() + " for service classes does not exist");
    	}
    	
    	if(packageName != null) {
    		String[] packageParts = packageName.split("\\.");
    		
    		for(String packagePart : packageParts) {
    			destDir = new File(destDir, packagePart);
    			
    			if(createSubDirs && !destDir.exists()) {
    				if(!destDir.mkdir()) {
    					throw new XspException("Unable to create destination directory " + destDir.getAbsolutePath() + " for compiled service classes");
    				}
    			}
    		}
    	}
    	
    	return createFile(srcFilePath, destDir);
    }
    
    /**
     * Creates file for XSP service class from the specified XSP file source path and
     * destination directory
     * 
     * @param srcFilePath the XSP file source path
     * @param destDir the destination directory
     * @return the created file
     */
    private File createFile(String srcFilePath, File destDir) {
    	StringBuffer classFileName = new StringBuffer();
        String[] fileParts = srcFilePath.split(File.separator);
        String fileName = fileParts[fileParts.length - 1];
        
        if(fileName.endsWith(".xsp")) {
            fileName = fileName.substring(0, fileName.length() - 4);
        }

        classFileName.append(fileName);
        classFileName.append("Service");
        classFileName.append(".class");
        return new File(destDir, classFileName.toString());
    }
    
    /**
     * Creates subclass of {@link XspService} for the XSP file represented by the specified document
     * tree node structure.
     * 
     * @param document the document tree node structure
     * @param srcFilePath the XSP file source path
     * @return a descriptor for the generated subclass
     * @throws XspException if unable to create subclass
     */
    private ClassDescriptor compileService(DocumentPart document, String srcFilePath) throws XspException {
        ClassWriter writer = new ClassWriter(0);

        // Create class
        String packageName = document.getMetaContent("package");
        String intServiceClazzName = createIntServiceClassName(packageName, srcFilePath);
        String serviceClazzName = createServiceClassName(packageName, srcFilePath);
        writer.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, intServiceClazzName, "Lcom/nginious/http/xsp/XspService;", "com/nginious/http/xsp/XspService", null);
        
        // Create constructor
        createConstructor(writer, "com/nginious/http/xsp/XspService");

        // Create xsp service method
        MethodVisitor visitor = createXspMethod(writer);
        
		Label tryLabel = new Label();
		Label startCatchLabel = new Label();
		Label endCatchLabel = new Label();
		
		// Start try block
		visitor.visitTryCatchBlock(tryLabel, startCatchLabel, endCatchLabel, "java/lang/Throwable");
		
		visitor.visitLabel(tryLabel);
		
		visitor.visitTypeInsn(Opcodes.NEW, "com/nginious/http/xsp/expr/HttpRequestVariables");
		visitor.visitInsn(Opcodes.DUP);
		visitor.visitVarInsn(Opcodes.ALOAD, 1);
		visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/nginious/http/xsp/expr/HttpRequestVariables", "<init>", "(Lcom/nginious/http/HttpRequest;)V");
		visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "com/nginious/http/xsp/expr/Expression", "setVariables", "(Lcom/nginious/http/xsp/expr/Variables;)V");
		
        document.compile(intServiceClazzName, writer, visitor);
        
        visitor.visitLabel(startCatchLabel);
        visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "com/nginious/http/xsp/expr/Expression", "removeVariables", "()V");
        
        visitor.visitLdcInsn(true);
        visitor.visitInsn(Opcodes.IRETURN);
        
        // Start finally block
        visitor.visitLabel(endCatchLabel);
        visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "com/nginious/http/xsp/expr/Expression", "removeVariables", "()V");
        visitor.visitInsn(Opcodes.ATHROW);
        
        visitor.visitMaxs(12, 12);
        visitor.visitEnd();
        
        document.compileMethod(intServiceClazzName, writer);

        writer.visitEnd();
        byte[] clazzBytes = writer.toByteArray();
        return new ClassDescriptor(serviceClazzName, clazzBytes);
    }
    
    /**
     * Creates a constructor with no arguments for the XSP service class being created with the
     * specified class writer. Bytecode is created for calling the superclass constructor in the
     * superclass with the specified supperclass name.
     * 
     * @param writer the class writer
     * @param superclassName the superclass name
     * @see XspService
     */
    void createConstructor(ClassWriter writer, String superclassName) {
        MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        visitor.visitCode();
        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, superclassName, "<init>", "()V");
        visitor.visitInsn(Opcodes.RETURN);
        visitor.visitMaxs(1, 1);
        visitor.visitEnd();
    }
    
    /**
     * Creates bytecode that overrides the {@link XspService#executeXsp(com.nginious.http.HttpRequest, com.nginious.http.HttpResponse)}
     * method for the XSP service class being created with the specified class writer.
     * 
     * @param writer the class writer
     * @return a method writer for adding bytecode to the created method
     */
    private MethodVisitor createXspMethod(ClassWriter writer) {
    	// 0 = this, 1 = HttpRequest, 2 = HttpResponse
    	String[] exceptions = { "java/io/IOException", "com/nginious/http/HttpException" };			
        MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PROTECTED, "executeXsp", "(Lcom/nginious/http/HttpRequest;Lcom/nginious/http/HttpResponse;)Z", null, exceptions);
        visitor.visitCode();
        
        return visitor;
    }
    
    /**
     * Creates service class name for a XSP service based on the specified package name and XSP file source
     * file path.
     * 
     * <p>
     * The package name <code>com.nginious.http.xsp</code> and XSP file name <code>Test.xsp</code>
     * would creates the binary class name <code>com.nginious.http.xsp.TestService.class</code>.
     * </p>
     * 
     * @param packageName the package name derived from the package meta tag or <code>null</code>
     * @param srcFilePath the source file path of the XSP file
     * @return the class name
     */
    String createServiceClassName(String packageName, String srcFilePath) {
        if(srcFilePath.endsWith(".xsp")) {
            srcFilePath = srcFilePath.substring(0, srcFilePath.length() - 4);
        }
        
        StringBuffer className = new StringBuffer();
        
        if(packageName != null) {
        	className.append(packageName);
            className.append(".");
        }
        
        String[] fileParts = srcFilePath.split(File.separator);
        className.append(fileParts[fileParts.length - 1]);
        className.append("Service");
        return className.toString();    	
    }
    
    /**
     * Creates binary class name for a XSP service based on the specified package name and XSP file source 
     * file path. 
     * 
     * <p>
     * The package name <code>com.nginious.http.xsp</code> and XSP file name <code>Test.xsp</code>
     * would creates the binary class name <code>com/nginious/http/xsp/TestService.class</code>.
     * </p>
     * 
     * @param packageName the package name derived from the package meta tag or <code>null</code>
     * @param srcFilePath the source file path of the XSP file
     * @return the binary class name
     */
    String createIntServiceClassName(String packageName, String srcFilePath) {
        if(srcFilePath.endsWith(".xsp")) {
            srcFilePath = srcFilePath.substring(0, srcFilePath.length() - 4);
        }
        
        StringBuffer className = new StringBuffer();
        
        if(packageName != null) {
        	className.append(packageName.replace(".", "/"));
            className.append("/");
        }
        
        String[] fileParts = srcFilePath.split(File.separator);
        className.append(fileParts[fileParts.length - 1]);
        className.append("Service");
        return className.toString();
    }
    
    /**
     * Reads the file with the specified file name into a byte array.
     * 
     * @param fileName the name of the file
     * @return the file contents as a byte array
     * @throws XspException if unable to read file
     */
    private byte[] readFile(String fileName) throws XspException {
        FileInputStream in = null;
        File file = new File(fileName);
        
        try {
            if(!file.exists()) {
            	throw new XspException("Service file " + file.getAbsolutePath() + " not found");
            }

            long length = file.length();

            if(length > 2097152) {
            	throw new XspException("Service file " + file.getAbsolutePath() + " is too large, size > 2097152");
            }

            byte[] buf = new byte[(int)length];
            in = new FileInputStream(fileName);

            if(in.read(buf) != length) {
                throw new XspException("Unable to read " + file.getAbsolutePath() + " from disk, invalid length");
            }

            return buf;
        } catch(IOException e) {
        	throw new XspException("Unable to read " + file.getAbsolutePath() + " from disk", e);
        } finally {
            if(in != null) {
                try { in.close(); } catch(IOException e) {}
            }
        }
    }
    
    /**
     * Loads the class defined by the specified class descriptor.
     * 
     * @param descriptor the class descriptor
     * @return the loaded class
     * @throws Exception if unable to load class
     */
    private Class<?> loadClass (ClassDescriptor descriptor) throws Exception {
    	//override classDefine (as it is protected) and defines the class.
    	Class<?> clazz = null;

    	Class<?> cls = Class.forName("java.lang.ClassLoader");
    	java.lang.reflect.Method method = cls.getDeclaredMethod("defineClass", new Class[] { String.class, byte[].class, int.class, int.class });
    	
    	// protected method invocaton
    	method.setAccessible(true);
    	
    	try {
    		String className = descriptor.getClassName();
    		byte[] byteCode = descriptor.getByteCode();
    		Object[] args = new Object[] { className, byteCode, new Integer(0), new Integer(byteCode.length)};
    		clazz = (Class<?>)method.invoke(this.classLoader, args);
    	} finally {
    		method.setAccessible(false);
    	}
    	
        return clazz;
    }
    
    class ClassDescriptor {
    	
    	private String className;
    	
    	private byte[] byteCode;
    	
    	ClassDescriptor(String className, byte[] byteCode) {
    		this.className = className;
    		this.byteCode = byteCode;
    	}
    	
    	String getClassName() {
    		return this.className;
    	}
    	
    	byte[] getByteCode() {
    		return this.byteCode;
    	}
    }
    
    /**
     * Main for executing XSP compiler from command line with the specified arguments.
     * 
     * @param argv the command line arguments
     */
    public static void main(String[] argv) {
        try {
            XspCompiler compiler = new XspCompiler(ClassLoader.getSystemClassLoader());
            compiler.compileService(argv[0], argv[1], argv[2]);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
