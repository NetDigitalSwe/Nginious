package com.nginious.http.application;

import java.util.HashMap;

enum ControllerMethodType {
	
	STRING_OBJECT(String.class, "Ljava/lang/String;"),
	
	BOOLEAN_OBJECT(Boolean.class, "Ljava/lang/Boolean;"),
	
	BOOLEAN(boolean.class, "Z"),
	
	BYTE_OBJECT(Byte.class, "Ljava/lang/Byte;"),
	
	BYTE(byte.class, "B"),
	
	SHORT_OBJECT(Short.class, "Ljava/lang/Short;"),
	
	SHORT(short.class, "S"),
	
	INTEGER_OBJECT(Integer.class, "Ljava/lang/Integer;"),
	
	INTEGER(int.class, "I"),
	
	LONG_OBJECT(Long.class, "Ljava/lang/Long;"),
	
	LONG(long.class, "J"),
	
	FLOAT_OBJECT(Float.class, "Ljava/lang/Float;"),
	
	FLOAT(float.class, "F"),
	
	DOUBLE_OBJECT(Double.class, "Ljava/lang/Double;"),
	
	DOUBLE(double.class, "D");
	
	private static HashMap<Class<?>, ControllerMethodType> lookup = new HashMap<Class<?>, ControllerMethodType>();
	
	static {
		lookup.put(String.class, STRING_OBJECT);
		lookup.put(Boolean.class, BOOLEAN_OBJECT);
		lookup.put(boolean.class, BOOLEAN);
		lookup.put(Byte.class, BYTE_OBJECT);
		lookup.put(byte.class, BYTE);
		lookup.put(Short.class, SHORT_OBJECT);
		lookup.put(short.class, SHORT);
		lookup.put(Integer.class, INTEGER_OBJECT);
		lookup.put(int.class, INTEGER);
		lookup.put(Long.class, LONG_OBJECT);
		lookup.put(long.class, LONG);
		lookup.put(Float.class, FLOAT_OBJECT);
		lookup.put(float.class, FLOAT);
		lookup.put(Double.class, DOUBLE_OBJECT);
		lookup.put(double.class, DOUBLE);
	}
	
	private Class<?> typeClass;
	
	private String signature;
	
	private ControllerMethodType(Class<?> typeClass, String signature) {
		this.typeClass = typeClass;
		this.signature = signature;
	}
	
	Class<?> getTypeClass() {
		return this.typeClass;
	}
	
	String getSignature() {
		return this.signature;
	}
	
	static ControllerMethodType getControllerMethodType(Class<?> typeClass) {
		return lookup.get(typeClass);
	}
}
