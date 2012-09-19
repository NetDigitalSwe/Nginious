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

import java.util.Date;

import com.nginious.http.rest.Serializable;

/**
 * A serializable bean that contains information about an application version
 * and the time it was published. Used by REST service {@link ApplicationService} to return
 * information about published applications.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
@Serializable
public class ApplicationVersion {
	
	private int versionNumber;
	
	private Date publishTime;
	
	/**
	 * Constructs a new application version.
	 */
	public ApplicationVersion() {
		super();
	}
	
	/**
	 * Returns version number for this application version.
	 * 
	 * @return the version number
	 */
	public int getVersionNumber() {
		return this.versionNumber;
	}
	
	/**
	 * Sets version number for this application version to the specified version number.
	 * 
	 * @param versionNumber the version number
	 */
	public void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}
	
	/**
	 * Returns the time when the version that this application version represents was published.
	 * 
	 * @return the publish time
	 */
	public Date getPublishTime() {
		return this.publishTime;
	}
	
	/**
	 * Sets the time when the version that this application version represents was published.
	 * 
	 * @param publishTime the publish time
	 */
	public void setPublishTime(Date publishTime) {
		this.publishTime = publishTime;
	}
	
	
}
