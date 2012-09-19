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
import java.io.InputStream;
import java.io.OutputStream;

import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpService;
import com.nginious.http.HttpServiceResult;
import com.nginious.http.HttpStatus;
import com.nginious.http.application.Service;
import com.nginious.http.upload.FilePart;

@Service(path = "/upload")
public class TestUploadService extends HttpService {
	
	public TestUploadService() {
		super();
	}
	
	public HttpServiceResult executePost(HttpRequest request, HttpResponse response) throws IOException {
		FilePart part = request.getFile("pics");
		
		if(part != null) {
			InputStream in = part.getInputStream();
			int len = part.getSize();
			
			response.setContentLength(len);
			
			byte[] data = new byte[len];
			in.read(data);
			
			OutputStream out = response.getOutputStream();
			out.write(data);
			in.close();
		} else {
			response.setStatus(HttpStatus.NO_CONTENT, "No file found");
		}
		
		return HttpServiceResult.DONE;
	}
}