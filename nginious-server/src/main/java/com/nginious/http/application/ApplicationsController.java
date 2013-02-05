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

import java.io.IOException;
import java.util.List;

import com.nginious.http.HttpException;
import com.nginious.http.HttpMethod;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpStatus;
import com.nginious.http.annotation.Controller;
import com.nginious.http.annotation.Request;

/**
 * A REST controller which provides information about published applications including all versions.
 * 
 * <p>
 * <ul>
 * <li>GET http://[host]/admin/applications - returns information about all published applications including all versions.
 * 	A serialized {@link ApplicationsInfo} bean is returned in JSON or XML format depending on the <code>Accept</code> header 
 *  in the request to this method.</li>
 * </ul>
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB.
 *
 */
@Controller(path = "/applications")
public class ApplicationsController {
	
	private ApplicationManagerImpl manager;
	
	ApplicationsController(ApplicationManagerImpl manager) {
		this.manager = manager;
	}
	
	/**
	 * Returns information about all published applications including all versions. The information is returned in the 
	 * specified response with the specified type {@link ApplicationsInfo}.
	 * 
	 * @param request the REST request
	 * @param response the REST response
	 * @return result of service execution
	 * @throws HttpException if any of the request data is invalid causing a HTTP error to the client
	 * @throws IOException if an I/O error occurs
	 */
	@Request(methods = { HttpMethod.GET })
	public ApplicationsInfo executeGet(HttpResponse response) throws HttpException, IOException {
		List<Application> applications = manager.getApplications();
		
		if(applications.size() == 0) {
			response.setContentLength(0);
			response.setStatus(HttpStatus.NO_CONTENT);
		}
			
		ApplicationsInfo outApplications = new ApplicationsInfo();
		boolean added = false;
			
		for(Application application : applications) {
			ApplicationInfo info = manager.createApplicationInfo((ApplicationImpl)application);
			outApplications.addApplication(info);
			added = true;
		}
		
		return added ? outApplications : null;
	}
}
