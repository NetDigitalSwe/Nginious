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

package com.nginious.http.server;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Represents a HTTP header value and its parameters and sub parameters. Parameters are delimited by ','
 * and sub parameters within parameters are delimited by ';'. Parameters and sub parameters may be
 * inside single or double quotes. See section 14.1 Accept in 
 * <a href="http://www.ietf.org/rfc/rfc2616.txt">HTTP/1.1 RFC 2616</a> for an example. 
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 * 
 */
public class Header {
	
	private enum State {
		STATE_START, STATE_PARAMETER_NAME, STATE_PARAMETER_VALUE, STATE_SUB_PARAMETER_NAME, STATE_SUB_PARAMETER_VALUE
	}
	
	private static final ThreadLocal<SimpleDateFormat> rfc1123FormatLocal =
			new ThreadLocal<SimpleDateFormat>() {
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			return format;
		}
	};
	
	private static final ThreadLocal<SimpleDateFormat> rfc850FormatLocal =
			new ThreadLocal<SimpleDateFormat>() {
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat format = new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			return format;
		}
	};
	
	private static final ThreadLocal<SimpleDateFormat> ansiCFormatLocal =
			new ThreadLocal<SimpleDateFormat>() {
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat format = new SimpleDateFormat("EEE MMM d hh:mm:ss yyyy", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			return format;
		}
	};
	
	private String name;
	
	private String value;
	
	private HeaderParameters parameters;
	
	/**
	 * Constructs a new HTTP header with the specified name and value.
	 * 
	 * @param name the name
	 * @param value the value
	 */
    public Header(String name, String value) {
        super();
        this.name = name;
        this.value = value;
    }
    
    /**
     * Returns this HTTP headers name.
     * 
     * @return the name
     */
    public String getName() {
    	return this.name;
    }
    
    /**
     * Returns this HTTP headers value.
     * 
     * @return the value
     */
    public String getValue() {
    	return this.value;
    }
    
    /**
     * Returns all parameters in this HTTP header value
     * 
     * @return all header parameters
     * @throws HeaderException if unable to parse this HTTP headers value into parameters
     */
	public HeaderParameters getParameters() throws HeaderException {
    	if(this.parameters == null) {
    		this.parameters = parseParameters();
    	}
    	
    	return this.parameters;
    }
    
	/**
	 * Creates byte ranges from parameters in this HTTP header. The byte ranges are sorted
	 * in ascending order.
	 * 
	 * @param contentLength the content length
	 * @return all byte ranges sorted in ascending order
	 * @throws HeaderException if unable to parse this HTTP header value
	 */
    public ByteRange[] createByteRanges(int contentLength) throws HeaderException {
    	HeaderParameter[] parameters = getParameters().getSorted();
    	ByteRange[] ranges = new ByteRange[parameters.length];
    	
    	for(int i = 0; i < parameters.length; i++) {
    		ranges[i] = new ByteRange(parameters[i].getName(), contentLength);
    	}
    	
    	return ranges;
    }
    
    /**
     * Returns whether or not any of the parameters in this headers accepts the specified content type.
     *
     * @param contentType the content type to match
     * @return <code>true</code> if match, <code>false</code> otherwise
     * @throws HeaderException if unable to parse header parameters
     */
    public boolean isAcceptable(String contentType) throws HeaderException {
    	if(!name.toLowerCase().equals("accept")) {
    		return false;
    	}
    	
    	HeaderParameter[] parameters = getParameters().getSorted();
    	
		for(HeaderParameter parameter : parameters) {
			if(parameter.accepts(contentType)) {
				return true;
			}
		}
    	
		return false;
    }
    
    /**
     * Parses this HTTP headers value into parameters and subparameters. Parameters are
     * delimited by ',' and sub parameters within parameters are delimited by ';'.
     * 
     * @return the header parameters
     * @throws HeaderException if unable to parse header value
     */
    @SuppressWarnings("incomplete-switch")
	private HeaderParameters parseParameters() throws HeaderException {
    	HeaderParameters parameters = new HeaderParameters();
    	
        int pos = 0;
        int start = 0;
        int length = value.length();
        State state = State.STATE_START;
        
        HeaderParameter parameter = null;
        String parameterName = null;
        String parameterValue = null;
        char quote = 0;
        boolean quoted = false;
        
        while(pos < length) {
        	char c = value.charAt(pos);
        	
        	switch(state) {
        	case STATE_START:
        		switch(c) {
        		case ',':
        		case ';':
        			throw new HeaderException("Unexpected token ';' at " + pos + " in header " + this.value);
        		
        		default:
        			state = State.STATE_PARAMETER_NAME;
        			start = pos;
        			break;
        		}
        		break;
        	
        	case STATE_PARAMETER_NAME:
        		switch(c) {
        		case ',':
        			parameterName = extractToken(this.value, start, pos, false);
        			parameter = new HeaderParameter(parameterName);
        			parameters.addParameter(parameter);
        			parameter = null;
        			state = State.STATE_START;
        			start = pos + 1;
        			parameter = null;
        			break;
        		
        		case ';':
        			parameterName = extractToken(this.value, start, pos, false);
        			parameter = new HeaderParameter(parameterName);
        			parameters.addParameter(parameter);
        			state = State.STATE_SUB_PARAMETER_NAME;
        			start = pos + 1;
        			break;
        		
        		case '=':
        			parameterName = extractToken(this.value, start, pos, false);
        			parameter = new HeaderParameter(parameterName);
        			parameters.addParameter(parameter);
        			state = State.STATE_PARAMETER_VALUE;
        			start = pos + 1;
        			break;
        		
        		case ' ':
        			parameterName = extractToken(this.value, start, pos, false);
        			parameter = new HeaderParameter(parameterName);
        			parameters.addParameter(parameter);
        			state = State.STATE_PARAMETER_NAME;
        			start = pos + 1;
        		}
        		break;
        		
        	case STATE_PARAMETER_VALUE:
        		switch(c) {
        		case ',':
        			if(quote == 0) {
        				if(quoted) {
        					parameterValue = extractToken(this.value, start + 1, pos - 1, false);
        					quoted = false;
        				} else {
        					parameterValue = extractToken(this.value, start, pos, false);        					
        				}
        				
        				parameter.setValue(parameterValue);
        				state = State.STATE_PARAMETER_NAME;
        				start = pos + 1;
        			}
        			break;
        		
        		case ';':
        			if(quote == 0) {
        				if(quoted) {
        					parameterValue = extractToken(this.value, start + 1, pos - 1, false);
        					quoted = false;
        				} else {
        					parameterValue = extractToken(this.value, start, pos, false);        					
        				}
        				
        				parameter.setValue(parameterValue);
        				state = State.STATE_SUB_PARAMETER_NAME;
        				start = pos + 1;
        			}
        			break;
        		
        		case '"':
        		case '\'':
        			if(quote == c) {
        				quote = 0;
        				quoted = true;
        			} else if(quote == 0) {
        				quote = c;
        			}        			
        			break;
        		}
        		break;
        		
        	case STATE_SUB_PARAMETER_NAME:
        		switch(c) {
        		case ',':
        			parameterName = extractToken(this.value, start, pos, false);
        			parameter.addSubParameter(parameterName, null);
        			parameter = null;
        			state = State.STATE_START;
        			start = pos + 1;
        			break;
        		
        		case ';':
        			parameterName = extractToken(this.value, start, pos, false);
        			parameter.addSubParameter(parameterName, null);
        			state = State.STATE_SUB_PARAMETER_NAME;
        			start = pos + 1;
        			break;
        		
        		case '=':
        			parameterName = extractToken(this.value, start, pos, false);
        			state = State.STATE_SUB_PARAMETER_VALUE;
        			start = pos + 1;
        			break;
        		}
        		break;
        	
        	case STATE_SUB_PARAMETER_VALUE:
        		switch(c) {
        		case ',':
        			if(quote == 0) {
        				if(quoted) {
        					parameterValue = extractToken(this.value, start + 1, pos - 1, false);
        					quoted = false;
        				} else {
        					parameterValue = extractToken(this.value, start, pos, false);        					
        				}
        				
        				parameterValue = extractToken(this.value, start, pos, true);
        				parameter.addSubParameter(parameterName, parameterValue);
        				parameter = null;
        				state = State.STATE_START;
        				start = pos + 1;
        			}
        			break;
        		
        		case ';':
        			if(quote == 0) {
        				if(quoted) {
        					parameterValue = extractToken(this.value, start + 1, pos - 1, false);
        					quoted = false;
        				} else {
        					parameterValue = extractToken(this.value, start, pos, false);        					
        				}
        				        				
        				parameterValue = extractToken(this.value, start, pos, true);
        				parameter.addSubParameter(parameterName, parameterValue);
        				state = State.STATE_SUB_PARAMETER_NAME;
        				start = pos + 1;
        			}
        			break;
        		
        		case '"':
        		case '\'':
        			if(quote == c) {
        				quote = 0;
        				quoted = true;
        			} else if(quote == 0) {
        				quote = c;
        			}
        			break;
        		}
        		break;
        	}
        	
        	pos++;
        }
        
        switch(state) {
        case STATE_PARAMETER_NAME:
        	parameterName = extractToken(this.value, start, pos, false);
        	parameter = new HeaderParameter(parameterName);
        	parameters.addParameter(parameter);
        	break;
        	
        case STATE_PARAMETER_VALUE:
			if(quoted) {
				parameterValue = extractToken(this.value, start + 1, pos - 1, false);
			} else {
				parameterValue = extractToken(this.value, start, pos, false);        					
			}

        	parameter.setValue(parameterValue);
        	break;
        	
        case STATE_SUB_PARAMETER_NAME:
        	parameterName = extractToken(this.value, start, pos, false);
        	parameter.addSubParameter(parameterName, null);
        	break;
        
        case STATE_SUB_PARAMETER_VALUE:
			if(quoted) {
				parameterValue = extractToken(this.value, start + 1, pos - 1, false);
			} else {
				parameterValue = extractToken(this.value, start, pos, false);        					
			}
			
			parameter.addSubParameter(parameterName, parameterValue);
        	break;
        }
        
        return parameters;
    }
    
    public Date parseDate() {
    	return parseDate(this.value);
    }
    
    /**
     * Parses the specified value into a date. The following date formats are tried in specified
     * order.
     * 
     * <ul>
     * <li>RFC 1123 format 'EEE, dd MMM yyyy HH:mm:ss zzz' using the US locale.</li>
     * <li>RFC 850 format 'EEEE, dd-MMM-yy HH:mm:ss zzz' using the US locale.</li>
     * <li>ANSI C format 'EEE MMM d hh:mm:ss yyyy' using the US locale.</li>
     * </ul>
     * 
     * @param value the value
     * @return the parsed date
     */
	public static Date parseDate(String value) {
		try {
			return rfc1123FormatLocal.get().parse(value);
		} catch(ParseException e) {}
		
		try {
			return rfc850FormatLocal.get().parse(value);
		} catch(ParseException e) {}
		
		try {
			return ansiCFormatLocal.get().parse(value);
		} catch(ParseException e) {}
		
		return null;
	}
	
	/**
	 * Formats the specified data into a date string with the RFC 1123 format 
	 * 'EEE, dd MMM yyyy HH:mm:ss zzz' using the US locale.
	 * 
	 * @param date the date to format
	 * @return the date string
	 */
	public static String formatDate(Date date) {
		return rfc1123FormatLocal.get().format(date);		
	}
	
	/**
	 * Extracts token from the specified header value starting at the specified start
	 * index and ending at the specified end index. Any whitespace is removed.
	 * 
	 * @param header the header value
	 * @param start start index in header value
	 * @param end end index in header value
	 * @param canBeQuoted whether or not token can be quoted
	 * @return the token
	 */
    private String extractToken(String header, int start, int end, boolean canBeQuoted) {
        while(start < end && Character.isWhitespace(header.charAt(start))) {
            start++;
        }

        while(end > start && Character.isWhitespace(header.charAt(end - 1))) {
            end--;
        }

        if(canBeQuoted && end - start >= 2) {
            if(header.charAt(start) == '"' && header.charAt(end - 1) == '"') {
                start++;
                end--;
            }
        }

        return header.substring(start, end);
    }    
}
