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

package com.nginious.http.service;

import java.util.Calendar;
import java.util.Date;

import com.nginious.http.annotation.Serializable;

@Serializable
public class TestBean2 {
	
	private boolean first;
	
	private double second;
	
	private float third;
	
	private int fourth;
	
	private long fifth;
	
	private short sixth;
	
	private String seventh;
	
	private Date eight;
	
	private Calendar ninth;
	
	public TestBean2() {
		super();
	}

	public boolean getFirst() {
		return this.first;
	}

	public void setFirst(boolean first) {
		this.first = first;
	}

	public double getSecond() {
		return this.second;
	}

	public void setSecond(double second) {
		this.second = second;
	}

	public float getThird() {
		return this.third;
	}

	public void setThird(float third) {
		this.third = third;
	}

	public int getFourth() {
		return this.fourth;
	}

	public void setFourth(int fourth) {
		this.fourth = fourth;
	}

	public long getFifth() {
		return this.fifth;
	}

	public void setFifth(long fifth) {
		this.fifth = fifth;
	}

	public short getSixth() {
		return this.sixth;
	}

	public void setSixth(short sixth) {
		this.sixth = sixth;
	}

	public String getSeventh() {
		return this.seventh;
	}

	public void setSeventh(String seventh) {
		this.seventh = seventh;
	}

	public Date getEight() {
		return this.eight;
	}

	public void setEight(Date eight) {
		this.eight = eight;
	}

	public Calendar getNinth() {
		return this.ninth;
	}

	public void setNinth(Calendar ninth) {
		this.ninth = ninth;
	}
}
