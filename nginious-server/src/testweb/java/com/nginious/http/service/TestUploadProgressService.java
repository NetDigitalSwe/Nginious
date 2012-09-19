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

import java.io.IOException;
import java.io.PrintWriter;

import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpService;
import com.nginious.http.HttpServiceResult;
import com.nginious.http.application.Service;
import com.nginious.http.upload.UploadTracker;

@Service(path = "/progress")
public class TestUploadProgressService extends HttpService {
	
	public TestUploadProgressService() {
		super();
	}
	
	public HttpServiceResult executeGet(HttpRequest request, HttpResponse response) throws IOException {
		UploadTracker tracker = request.getUploadTracker();
		response.setContentType("text/plain");
		String value = null;
		
		if(tracker != null) {
			value = Integer.toString(tracker.getUploadedLength());
		} else {
			value = "0";
		}
		
		response.setContentLength(value.length());
		PrintWriter writer = response.getWriter();
		writer.print(value);
		return HttpServiceResult.DONE;
	}
}
