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

import com.nginious.http.rest.Serializable;

/**
 * A serializable bean that represents a list of all published applications.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
@Serializable
public class ApplicationsInfo {
	
	private Collection<ApplicationInfo> applications;
	
	/**
	 * Constructs a new applications.
	 */
	public ApplicationsInfo() {
		this.applications = new ArrayList<ApplicationInfo>();
	}
	
	/**
	 * Adds the specified application info to this applications.
	 * 
	 * @param info the application info
	 */
	void addApplication(ApplicationInfo info) {
		applications.add(info);
	}
	
	/**
	 * Adds all application infos from the specified collection. This
	 * replaces any previously added application infos.
	 * 
	 * @param applications the collection of application infos to add
	 */
	public void setContexts(Collection<ApplicationInfo> applications) {
		this.applications = applications;
	}
	
	/**
	 * Returns all application infos
	 * 
	 * @return collection of application infos
	 */
	public Collection<ApplicationInfo> getApplications() {
		return this.applications;
	}
}
