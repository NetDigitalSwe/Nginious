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

import com.nginious.http.annotation.Serializable;

@Serializable
public class SerializableArrayBean {
	
	private boolean[] booleanArrayValue;
	
	private double[] doubleArrayValue;
	
	private float[] floatArrayValue;
	
	private int[] intArrayValue;
	
	private long[] longArrayValue;
	
	private short[] shortArrayValue;
	
	private String[] stringArrayValue;
	
	public SerializableArrayBean() {
		super();
	}

	public boolean[] getBooleanArrayValue() {
		return booleanArrayValue;
	}

	public void setBooleanArrayValue(boolean[] booleanArrayValue) {
		this.booleanArrayValue = booleanArrayValue;
	}

	public double[] getDoubleArrayValue() {
		return doubleArrayValue;
	}

	public void setDoubleArrayValue(double[] doubleArrayValue) {
		this.doubleArrayValue = doubleArrayValue;
	}

	public float[] getFloatArrayValue() {
		return floatArrayValue;
	}

	public void setFloatArrayValue(float[] floatArrayValue) {
		this.floatArrayValue = floatArrayValue;
	}

	public int[] getIntArrayValue() {
		return intArrayValue;
	}

	public void setIntArrayValue(int[] intArrayValue) {
		this.intArrayValue = intArrayValue;
	}

	public long[] getLongArrayValue() {
		return longArrayValue;
	}

	public void setLongArrayValue(long[] longArrayValue) {
		this.longArrayValue = longArrayValue;
	}

	public short[] getShortArrayValue() {
		return shortArrayValue;
	}

	public void setShortArrayValue(short[] shortArrayValue) {
		this.shortArrayValue = shortArrayValue;
	}

	public String[] getStringArrayValue() {
		return this.stringArrayValue;
	}

	public void setStringArrayValue(String[] stringArrayValue) {
		this.stringArrayValue = stringArrayValue;
	}

}
