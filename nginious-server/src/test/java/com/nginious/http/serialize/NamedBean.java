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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.nginious.http.annotation.Serializable;

@Serializable(name = "testNamedBean")
public class NamedBean {
	
	private boolean booleanValue;
	
	private double doubleValue;
	
	private float floatValue;
	
	private int intValue;
	
	private long longValue;
	
	private short shortValue;
	
	private String stringValue;
	
	private Date dateValue;
	
	private Calendar calendarValue;
	
	private Object objectValue;
	
	private InBean beanValue;
	
	private List<String> stringListValue;
	
	private List<InBean> beanListValue;
	
	public NamedBean() {
		super();
	}
	
	@Serializable(name = "testBooleanValue")
	public boolean getBooleanValue() {
		return this.booleanValue;
	}

	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	@Serializable(name = "testDoubleValue")
	public double getDoubleValue() {
		return this.doubleValue;
	}

	public void setDoubleValue(double doubleValue) {
		this.doubleValue = doubleValue;
	}

	@Serializable(name = "testFloatValue")
	public float getFloatValue() {
		return this.floatValue;
	}

	public void setFloatValue(float floatValue) {
		this.floatValue = floatValue;
	}

	@Serializable(name = "testIntValue")
	public int getIntValue() {
		return this.intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

	@Serializable(name = "testLongValue")
	public long getLongValue() {
		return this.longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}

	@Serializable(name = "testShortValue")
	public short getShortValue() {
		return this.shortValue;
	}

	public void setShortValue(short shortValue) {
		this.shortValue = shortValue;
	}

	@Serializable(name = "testStringValue")
	public String getStringValue() {
		return this.stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	@Serializable(name = "testDateValue")
	public Date getDateValue() {
		return this.dateValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	@Serializable(name = "testCalendarValue")
	public Calendar getCalendarValue() {
		return this.calendarValue;
	}

	public void setCalendarValue(Calendar calendarValue) {
		this.calendarValue = calendarValue;
	}

	@Serializable(name = "testObjectValue")
	public Object getObjectValue() {
		return this.objectValue;
	}

	public void setObjectValue(Object objectValue) {
		this.objectValue = objectValue;
	}

	@Serializable(name = "testBeanValue")
	public InBean getBeanValue() {
		return this.beanValue;
	}

	public void setBeanValue(InBean beanValue) {
		this.beanValue = beanValue;
	}

	@Serializable(name = "testStringListValue")
	public List<String> getStringListValue() {
		return this.stringListValue;
	}

	public void setStringListValue(List<String> stringListValue) {
		this.stringListValue = stringListValue;
	}

	@Serializable(name = "testBeanListValue")
	public List<InBean> getBeanListValue() {
		return this.beanListValue;
	}

	public void setBeanListValue(List<InBean> beanListValue) {
		this.beanListValue = beanListValue;
	}
}
