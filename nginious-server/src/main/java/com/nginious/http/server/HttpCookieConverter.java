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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.nginious.http.HttpCookie;

/**
 * Converts HTTP cookies between string and object representation.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class HttpCookieConverter {

	static final int MAX_COOKIES = 20;
	
	static final int STATE_NAME = 1;
	
	static final int STATE_VALUE = 2;
	
	private static final String[] weekdays = { "Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
	
	private static final String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan" };
	
	/**
	 * Parses the specified cookie header value into one or more HTTP cookies
	 * 
	 * @param header the HTTP cookie header
	 * @return the HTTP cookies
	 */
	public static HttpCookie[] parse(String header) {
		int index = 0;
		int startIndex = index;
		int length = header.length();
		int state = HttpCookieConverter.STATE_NAME;
		boolean inQuote = false;
		boolean wasInQuote = false;
		boolean inEscape = false;
		
		String name = null;
		String value = null;
		int version = 0;
		HttpCookie cookie = null;
		List<HttpCookie> cookies = new ArrayList<HttpCookie>(HttpCookieConverter.MAX_COOKIES);
		
		while(index < length) {
			char c = header.charAt(index);
			
			if(inEscape) {
				inEscape = false;
			} else if(inQuote) {
				switch(c) {
				case '"':
					inQuote = false;
					wasInQuote = true;
					
					if(index == length - 1) {
						if(state == HttpCookieConverter.STATE_NAME) {
							name = header.substring(startIndex, index + 1);
							value = "";
							cookie = new HttpCookie(name, value);
						} else if(state == HttpCookieConverter.STATE_VALUE) {
							value = header.substring(startIndex, index);
						}
					}
					break;
				
				case '\\':
					inEscape = true;
					break;
				}
			} else if(state == HttpCookieConverter.STATE_NAME) {
				switch(c) {
				case ' ':
				case '\t':
					// Skip whitespace
					if(startIndex == index) {
						startIndex++;
					}
					break;
					
				case '"':
					if(startIndex == index) {
						inQuote = true;
						startIndex++;
					}
					break;
				
				case ';':
				case ',':
					if(startIndex < index) {
						name = header.substring(startIndex, wasInQuote ? index : index + 1).trim();
						wasInQuote = false;
						value = "";
					}
					
					startIndex = index + 1;
					break;
				
				case '=':
					if(startIndex < index) {
						name = header.substring(startIndex, wasInQuote ? index - 1 : index).trim();
						wasInQuote = false;
					}
					
					startIndex = index + 1;
					state = HttpCookieConverter.STATE_VALUE;
					break;
					
				default:
					if(startIndex == length - 1) {
						name = header.substring(startIndex, wasInQuote ? index : index + 1);
						wasInQuote = false;
						value = "";
					}
					break;
				}
			} else if(state == HttpCookieConverter.STATE_VALUE) {
				switch(c) {
				case ' ':
				case '\t':
					
				case '"':
					if(startIndex == index) {
						inQuote = true;
						startIndex++;
					}
					break;
					
				case ';':
				case ',':
					if(startIndex < index) {
						value = header.substring(startIndex, wasInQuote ? index - 1 : index).trim();
						wasInQuote = false;
					} else {
						value = "";
					}
					
					startIndex = index + 1;
					state = HttpCookieConverter.STATE_NAME;
					break;
				
				default:
					if(index == length - 1) {
						value = header.substring(startIndex, wasInQuote ? index : index + 1).trim();
						wasInQuote = false;
					}
					break;
				}
			}
			
			if(name != null && value != null) {
				if(name.startsWith("$")) {
					String lowerName = name.toLowerCase();
					
					if(lowerName.equals("$path") && cookie != null) {
						cookie.setPath(value);
					} else if(lowerName.equals("$domain") && cookie != null) {
						cookie.setDomain(value);
					} else if(lowerName.equals("$port") && cookie != null) {
						cookie.setComment("port=" + value);
					} else if(lowerName.equals("$version")) {
						try { version = Integer.parseInt(value); } catch(NumberFormatException e) {}
					}
				} else {
					cookie = new HttpCookie(name, value);
					cookies.add(cookie);
					
					if(version > 0) {
						cookie.setVersion(version);
					}
				}
				
				name = null;
				value = null;
			}
			
			index++;
		}
		
		return cookies.toArray(new HttpCookie[cookies.size()]);
	}

	/**
	 * Formats the specified HTTP cookie into a string suitable for usage in a HTTP header
	 * sent in a HTTP response to a client.
	 * 
	 * @param cookie the cookie to format
	 * @return the HTTP cookie string
	 */
	public static String format(HttpCookie cookie) {
		String name = cookie.getName();
		String value = cookie.getValue();
		
		if(name == null || name.equals("")) {
			// TODO, exception, what type?
		}
		
		StringBuffer outCookie = new StringBuffer();
		
		escape(outCookie, name);
		outCookie.append('=');
		escape(outCookie, value);
		
		int version = cookie.getVersion();
		String comment = cookie.getComment();
		
		if(version > 0) {
			outCookie.append(";Version=");
			outCookie.append(version);
			
			if(comment != null && comment.length() > 0) {
				outCookie.append(";Comment=");
				escape(outCookie, comment);
			}
		}
		
		String path = cookie.getPath();
		
		if(path != null) {
			outCookie.append(";Path=");
			escape(outCookie, path);
		}
		
		String domain = cookie.getDomain();
		
		if(domain != null) {
			outCookie.append(";Domain=");
			escape(outCookie, domain);
		}
		
		int maxAge = cookie.getMaxAge();
		
		if(maxAge != 0) {
			if(version == 0) {
				outCookie.append(";Expires=");
				Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
				cal.setTimeInMillis(System.currentTimeMillis() + (maxAge * 1000));
				
				int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
				int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
				int month = cal.get(Calendar.MONTH);
				int year = cal.get(Calendar.YEAR);
				int hours = cal.get(Calendar.HOUR_OF_DAY);
				int mins = cal.get(Calendar.MINUTE);
				int secs = cal.get(Calendar.SECOND);
				
				outCookie.append(weekdays[dayOfWeek]);
				
				if(dayOfMonth < 10) {
					outCookie.append(", 0");
				} else {
					outCookie.append(", ");
				}
				
				outCookie.append(dayOfMonth);
				outCookie.append(' ');
				outCookie.append(months[month]);
				outCookie.append(' ');
				outCookie.append(year);
				
				if(hours < 10) {
					outCookie.append(" 0");
				} else {
					outCookie.append(' ');
				}
				
				outCookie.append(hours);
				
				if(mins < 10) {
					outCookie.append(":0");
				} else {
					outCookie.append(':');
				}
				
				outCookie.append(mins);
				
				if(secs < 10) {
					outCookie.append(":0");
				} else {
					outCookie.append(':');
				}
				
				outCookie.append(secs);
				outCookie.append(" GMT");
			} else {
				outCookie.append(";Max-Age=");
				outCookie.append(maxAge);
			}
		} else if(version > 0) {
			outCookie.append(";Discard");
		}
		
		return outCookie.toString();
	}
	
	/**
	 * Escapes characters in the specified value and add the value to the specified out cookie.
	 * Characters that must be escaped are '"', '\', '\n', '\r', '\t', '\f', '\b', '%', '+',
	 * ' ', ';', '='.
	 * 
	 * @param outCookie the out cookie for appending the escaped value to
	 * @param value the value to escape
	 */
	private static void escape(StringBuffer outCookie, String value) {
		boolean escape = false;
		int length = value.length();
		int index = 0;
		
		while(index < length && !escape) {
			char ch = value.charAt(index);
			
			switch(ch) {
			case '"':
			case '\\':
			case '\n':
			case '\r':
			case '\t':
			case '\f':
			case '\b':
			case '%':
			case '+':
			case ' ':
			case ';':
			case '=':
				escape = true;
				break;
			
			default:
				break;
			}
			
			index++;
		}
		
		if(escape) {
			outCookie.append('"');
			
			for(int i = 0; i < value.length(); i++) {
				char ch = value.charAt(i);
				
				switch(ch) {
                case '"':
                    outCookie.append("\\\"");
                    continue;
                case '\\':
                    outCookie.append("\\\\");
                    continue;
                case '\n':
                    outCookie.append("\\n");
                    continue;
                case '\r':
                    outCookie.append("\\r");
                    continue;
                case '\t':
                    outCookie.append("\\t");
                    continue;
                case '\f':
                    outCookie.append("\\f");
                    continue;
                case '\b':
                    outCookie.append("\\b");
                    continue;
					
				default:
					outCookie.append(ch);
					break;
				}
			}
			
			outCookie.append('"');
		} else {
			outCookie.append(value);
		}
	}
}
