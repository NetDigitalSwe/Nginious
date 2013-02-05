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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.nginious.http.annotation.Serializable;

/**
 * A serializable bean that represents information about an application and
 * all its versions including the time they where published. Used by REST service 
 * {@link ApplicationController}  to return information about application contexts.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
@Serializable
public class ApplicationInfo {
	
	private String name;
	
	private Collection<ApplicationVersion> versions;
	
	/**
	 * Constructs a new application info.
	 */
	public ApplicationInfo() {
		super();
		this.versions = new ArrayList<ApplicationVersion>();
	}
	
	/**
	 * Sets name to the specified name for this application info.
	 * 
	 * @param name the name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns name of the application that this application info represents.
	 * 
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Adds a version with the specified version number and publish time to this
	 * application info.
	 * 
	 * @param versionNumber the version number
	 * @param publishTimeMillis the publish time in milliseconds
	 */
	void addVersion(int versionNumber, long publishTimeMillis) {
		ApplicationVersion version = new ApplicationVersion();
		version.setVersionNumber(versionNumber);
		version.setPublishTime(new Date(publishTimeMillis));
		versions.add(version);
	}
	
	/**
	 * Sets versions for this application info to the specified collection of versions. The versions 
	 * may be sorted in descending version number order. A call to this method replaces any added versions 
	 * with the {@link #addVersion(int, long)} method.
	 * 
	 * @param versions collection of versions
	 */
	public void setVersions(Collection<ApplicationVersion> versions) {
		this.versions = versions;
	}
	
	/**
	 * Returns all versions for the application that this application info represents.
	 * 
	 * @return collection of versions
	 */
	public Collection<ApplicationVersion> getVersions() {
		return this.versions;
	}
}
