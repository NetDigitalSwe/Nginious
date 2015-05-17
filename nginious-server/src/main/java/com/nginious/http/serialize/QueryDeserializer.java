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

package com.nginious.http.serialize;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.nginious.http.HttpRequest;

/**
 * Base class for all deserializers that deserialize beans from URI query parameter format. Used as base class
 * by {@link QueryDeserializerCreator} when creating deserializers runtime.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 * @param <E> the type of bean that is deserialized by this deserializer
 */
public abstract class QueryDeserializer<E> implements Deserializer<E> {
	
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	/**
	 * Constructs a new query deserializer
	 */
	public QueryDeserializer() {
		super();
	}
	
	/**
	 * Returns the application/-www-form-urlencoded mime type for this deserializer.
	 * 
	 * @return the mime type for this deserializer
	 */
	public String getMimeType() {
		return "application/x-www-form-urlencoded";
	}

	/**
	 * Deserializes a bean from URI query parameters in the specified HTTP request.
	 * 
	 * @param the message to deserialize
	 * @return the deserialized bean
	 * @throws SerializerException if unable to deserialize bean
	 */
	public abstract E deserialize(HttpRequest request) throws SerializerException;
	
	/**
	 * Deserializes a bean from URI query parameters in the specified message.
	 * 
	 * @param message the message to deserialize
	 * @return the deserialized bean
	 * @throws SerializerException if unable to deserialize bean 
	 */
	public E deserialize(String message) throws SerializerException {
		throw new SerializerException("Content type 'application/x-www-form-urlencoded' not supported for message deserialization");
	}
	
	/**
	 * Deserializes parameters with the specified name from the specified HTTP request into a boolean array.
	 * 
	 * @param request the HTTP request
	 * @param name the URI query parameter name
	 * @return the deserialized array or <code>null</code> if parameter doesn't exist
	 * @throws SerializerException if unable to deserialize value
	 */
	protected boolean[] deserializeBooleanArray(HttpRequest request, String name) throws SerializerException {
		try {
			String[] values = request.getParameterValues(name);
			
			if(values != null) {
				boolean[] outArray = new boolean[values.length];
				
				for(int i = 0; i < values.length; i++) {
					outArray[i] = Boolean.parseBoolean(values[i]);
				}
				
				return outArray;
			}
			
			return null;
		} catch(NumberFormatException e) {
			throw new SerializerException("Can't deserialize boolean array property " + name, e);			
		}
	}
	
	/**
	 * Deserializes URI query parameter with the specified name from the specified HTTP request.
	 * 
	 * @param request the HTTP request
	 * @param name the given URI query parameter name
	 * @return <code>true</code> if parameter contains value "true" or "1", <code>false</code> otherwise
	 * @throws SerializerException if unable to deserialize boolean property
	 */
	protected boolean deserializeBoolean(HttpRequest request, String name) throws SerializerException {
		String value = request.getParameter(name);
		return value != null && (value.equals("true") || value.equals("1"));
	}
	
	/**
	 * Deserializes URI query parameter with the specified name from the specified HTTP request to a
	 * calendar object. The query parameter must have a date string in format 'yyyy-MM-dd'T'HH:mm:ssZ'.
	 * 
	 * @param request the HTTP request
	 * @param name the URI query parameter name
	 * @return the deserialized calendar object
	 * @throws SerializerException if unable to deserialize calendar property
	 */
	protected Calendar deserializeCalendar(HttpRequest request, String name) throws SerializerException {
		String value = request.getParameter(name);
		
		if(value == null) {
			return null;
		}
		
		if(value.matches(".*[+-][0-9]{2}:[0-9]{2}$")) {
			int lastIndex = value.lastIndexOf(':');
			value = value.substring(0, lastIndex) + value.substring(lastIndex + 1);
		}
		
		Date date = parseDate(value);
		String tz = value.substring(value.length() - 5, value.length() - 2);
		TimeZone zone = TimeZone.getTimeZone("GMT" + tz);
		Calendar cal = Calendar.getInstance(zone);
		cal.setTime(date);
		return cal;
	}
	
	/**
	 * Deserializes URI query parameter with the specified name from the specified HTTP request to a
	 * date. The query parameter must have a date string in format 'yyyy-MM-dd'T'HH:mm:ssZ'.
	 * 
	 * @param request the HTTP request
	 * @param name the URI query parameter name
	 * @return the deserialized date
	 * @throws SerializerException if unable to deserialize date property
	 */
	protected Date deserializeDate(HttpRequest request, String name) throws SerializerException {
		String value = request.getParameter(name);
		
		if(value == null) {
			return null;
		}
		
		if(value.matches(".*[+-][0-9]{2}:[0-9]{2}$")) {
			int lastIndex = value.lastIndexOf(':');
			value = value.substring(0, lastIndex) + value.substring(lastIndex + 1);
		}
		
		return parseDate(value);
	}
	
	/**
	 * Parses the specified value into a date. The date value must be in format 'yyyy-MM-dd'T'HH:mm:ssZ'.
	 * 
	 * @param value the date string
	 * @return the parsed date
	 * @throws SerializerException if unable to parse date
	 */
	private Date parseDate(String value) throws SerializerException {
		try {
			return format.parse(value);
		} catch(ParseException e) {
			throw new SerializerException("Can't deserialize date " + value, e);
		}
	}
	
	/**
	 * Deserializes parameters with the specified name from the specified HTTP request into a double array.
	 * 
	 * @param request the HTTP request
	 * @param name the URI query parameter name
	 * @return the deserialized array or <code>null</code> if parameter doesn't exist
	 * @throws SerializerException if unable to deserialize value
	 */
	protected double[] deserializeDoubleArray(HttpRequest request, String name) throws SerializerException {
		try {
			String[] values = request.getParameterValues(name);
			
			if(values != null) {
				double[] outArray = new double[values.length];
				
				for(int i = 0; i < values.length; i++) {
					outArray[i] = Double.parseDouble(values[i]);
				}
				
				return outArray;
			}
			
			return null;
		} catch(NumberFormatException e) {
			throw new SerializerException("Can't deserialize double array property " + name, e);			
		}
	}
	
	/**
	 * Deserializes URI query parameter with the specified name from the specified HTTP request into a double.
	 * 
	 * @param request the HTTP request
	 * @param name the URI query parameter name
	 * @return the deserialized double
	 * @throws SerializerException if unable to deserialize double property
	 */
	protected double deserializeDouble(HttpRequest request, String name) throws SerializerException {
		String value = request.getParameter(name);
		
		if(value != null) {
			try {
				return Double.parseDouble(value);
			} catch(NumberFormatException e) {
				throw new SerializerException("Can't deserialize double property " + name + "(" + value + ")", e);
			}
		}
		
		return 0.0d;
	}
	
	/**
	 * Deserializes parameters with the specified name from the specified HTTP request into a float array.
	 * 
	 * @param request the HTTP request
	 * @param name the URI query parameter name
	 * @return the deserialized array or <code>null</code> if parameter doesn't exist
	 * @throws SerializerException if unable to deserialize value
	 */
	protected float[] deserializeFloatArray(HttpRequest request, String name) throws SerializerException {
		try {
			String[] values = request.getParameterValues(name);
			
			if(values != null) {
				float[] outArray = new float[values.length];
				
				for(int i = 0; i < values.length; i++) {
					outArray[i] = Float.parseFloat(values[i]);
				}
				
				return outArray;
			}
			
			return null;
		} catch(NumberFormatException e) {
			throw new SerializerException("Can't deserialize float array property " + name, e);			
		}
	}
	
	/**
	 * Deserializes URI query parameter with the specified name from the specified HTTP request into a float.
	 * 
	 * @param request the HTTP request
	 * @param name the URI query parameter name
	 * @return the deserialized float
	 * @throws SerializerException if unable to deserialize float property
	 */
	protected float deserializeFloat(HttpRequest request, String name) throws SerializerException {
		String value = request.getParameter(name);
		
		if(value != null) {
			try {
				return Float.parseFloat(value);
			} catch(NumberFormatException e) {
				throw new SerializerException("Can't deserialize float property " + name + "(" + value + ")", e);
			}
		}
		
		return 0.0f;
	}
	
	/**
	 * Deserializes parameters with the specified name from the specified HTTP request into a integer array.
	 * 
	 * @param request the HTTP request
	 * @param name the URI query parameter name
	 * @return the deserialized array or <code>null</code> if parameter doesn't exist
	 * @throws SerializerException if unable to deserialize value
	 */
	protected int[] deserializeIntArray(HttpRequest request, String name) throws SerializerException {
		try {
			String[] values = request.getParameterValues(name);
			
			if(values != null) {
				int[] outArray = new int[values.length];
				
				for(int i = 0; i < values.length; i++) {
					outArray[i] = Integer.parseInt(values[i]);
				}
				
				return outArray;
			}
			
			return null;
		} catch(NumberFormatException e) {
			throw new SerializerException("Can't deserialize integer array property " + name, e);			
		}
	}
	
	/**
	 * Deserializes URI query parameter with the specified name from the specified HTTP request into a integer.
	 * 
	 * @param request the HTTP request
	 * @param name the URI query parameter name
	 * @return the deserialized integer
	 * @throws SerializerException if unable to deserialize integer property
	 */
	protected int deserializeInt(HttpRequest request, String name) throws SerializerException {
		String value = request.getParameter(name);
		
		if(value != null) {
			try {
				return Integer.parseInt(value);
			} catch(NumberFormatException e) {
				throw new SerializerException("Can't deserialize int property " + name + "(" + value + ")", e);
			}
		}
		
		return 0;
	}
	
	/**
	 * Deserializes parameters with the specified name from the specified HTTP request into a long array.
	 * 
	 * @param request the HTTP request
	 * @param name the URI query parameter name
	 * @return the deserialized array or <code>null</code> if parameter doesn't exist
	 * @throws SerializerException if unable to deserialize value
	 */
	protected long[] deserializeLongArray(HttpRequest request, String name) throws SerializerException {
		try {
			String[] values = request.getParameterValues(name);
			
			if(values != null) {
				long[] outArray = new long[values.length];
				
				for(int i = 0; i < values.length; i++) {
					outArray[i] = Long.parseLong(values[i]);
				}
				
				return outArray;
			}
			
			return null;
		} catch(NumberFormatException e) {
			throw new SerializerException("Can't deserialize long array property " + name, e);			
		}
	}
	
	/**
	 * Deserializes URI query parameter with the specified name from the specified HTTP request into a long.
	 * 
	 * @param request the HTTP request
	 * @param name the URI query parameter name
	 * @return the deserialized long
	 * @throws SerializerException if unable to deserialize long property
	 */
	protected long deserializeLong(HttpRequest request, String name) throws SerializerException {
		String value = request.getParameter(name);
		
		if(value != null) {
			try {
				return Long.parseLong(value);
			} catch(NumberFormatException e) {
				throw new SerializerException("Can't deserialize long property " + name + "(" + value + ")", e);
			}
		}
		
		return 0;		
	}
	
	/**
	 * Deserializes parameters with the specified name from the specified HTTP request into a short array.
	 * 
	 * @param request the HTTP request
	 * @param name the URI query parameter name
	 * @return the deserialized array or <code>null</code> if parameter doesn't exist
	 * @throws SerializerException if unable to deserialize value
	 */
	protected short[] deserializeShortArray(HttpRequest request, String name) throws SerializerException {
		try {
			String[] values = request.getParameterValues(name);
			
			if(values != null) {
				short[] outArray = new short[values.length];
				
				for(int i = 0; i < values.length; i++) {
					outArray[i] = Short.parseShort(values[i]);
				}
				
				return outArray;
			}
			
			return null;
		} catch(NumberFormatException e) {
			throw new SerializerException("Can't deserialize short array property " + name, e);			
		}
	}
	
	/**
	 * Deserializes URI query parameter with the specified name from the specified HTTP request into a short.
	 * 
	 * @param request the HTTP request
	 * @param name the URI query parameter name
	 * @return the deserialized short
	 * @throws SerializerException if unable to deserialize short property
	 */
	protected short deserializeShort(HttpRequest request, String name) throws SerializerException {
		String value = request.getParameter(name);
		
		if(value != null) {
			try {
				return Short.parseShort(value);
			} catch(NumberFormatException e) {
				throw new SerializerException("Can't deserialize short property " + name + "(" + value + ")", e);
			}
		}
		
		return 0;		
	}
	
	/**
	 * Deserializes URI query parameters with the specified name from the specified HTTP request into a string array.
	 * 
	 * @param request the HTTP request
	 * @param name the URI query parameter name
	 * @return the deserialized array or <code>null</code> if property doesn't exist
	 * @throws SerializerException if unable to deserialize value
	 */
	protected String[] deserializeStringArray(HttpRequest request, String name) throws SerializerException {
		return request.getParameterValues(name);
	}
	
	/**
	 * Deserializes URI query parameter with the specified name from the specified HTTP request into a string.
	 * 
	 * @param request the HTTP request
	 * @param name the URI query parameter name
	 * @return the deserialized string
	 * @throws SerializerException if unable to deserialize string property
	 */
	protected String deserializeString(HttpRequest request, String name) throws SerializerException {
		return request.getParameter(name);
	}
}
