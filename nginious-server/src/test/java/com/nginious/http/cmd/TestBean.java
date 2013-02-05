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

package com.nginious.http.cmd;

import com.nginious.http.annotation.CommandLine;

public class TestBean {
	
	private byte testByte;
	
	private short testShort;
	
	private int testInteger;
	
	private long testLong;
	
	private float testFloat;
	
	private double testDouble;
	
	private String testString;
	
	private boolean testBoolean;
	
	public TestBean() {
		super();
	}

	public byte getTestByte() {
		return this.testByte;
	}
	
	@CommandLine(longName="--testByte", shortName="-b", mandatory=false, description="description for byte value")
	public void setTestByte(byte testByte) {
		this.testByte = testByte;
	}

	public short getTestShort() {
		return this.testShort;
	}

	@CommandLine(longName="--testShort", shortName="-s", mandatory=false, description="description for short value")
	public void setTestShort(short testShort) {
		this.testShort = testShort;
	}

	public int getTestInteger() {
		return this.testInteger;
	}

	@CommandLine(longName="--testInt", shortName="-i", mandatory=false, description="description for int value")
	public void setTestInteger(int testInteger) {
		this.testInteger = testInteger;
	}

	public long getTestLong() {
		return this.testLong;
	}

	@CommandLine(longName="--testLong", shortName="-l", mandatory=false, description="description for long value")
	public void setTestLong(long testLong) {
		this.testLong = testLong;
	}

	public float getTestFloat() {
		return this.testFloat;
	}

	@CommandLine(longName="--testFloat", shortName="-f", mandatory=false, description="description for float value")
	public void setTestFloat(float testFloat) {
		this.testFloat = testFloat;
	}

	public double getTestDouble() {
		return this.testDouble;
	}

	@CommandLine(longName="--testDouble", shortName="-d", mandatory=false, description="description for double value")
	public void setTestDouble(double testDouble) {
		this.testDouble = testDouble;
	}

	public String getTestString() {
		return this.testString;
	}

	@CommandLine(longName="--testString", shortName="-t", mandatory=false, description="description for string value")
	public void setTestString(String testString) {
		this.testString = testString;
	}

	public boolean getTestBoolean() {
		return this.testBoolean;
	}

	@CommandLine(longName="--testBoolean", shortName="-o", mandatory=false, description="description for boolean value")
	public void setTestBoolean(boolean testBoolean) {
		this.testBoolean = testBoolean;
	}
}
