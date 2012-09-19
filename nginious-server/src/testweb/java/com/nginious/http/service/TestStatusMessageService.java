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

import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpService;
import com.nginious.http.HttpServiceResult;
import com.nginious.http.HttpStatus;
import com.nginious.http.application.Service;

@Service(path = "/status")
public class TestStatusMessageService extends HttpService {
	
	public TestStatusMessageService() {
		super();
	}
	
	public HttpServiceResult executeGet(HttpRequest request, HttpResponse response) throws IOException {
		response.setStatus(HttpStatus.BAD_REQUEST, "GET method");
		return HttpServiceResult.DONE;
	}

	public HttpServiceResult executePost(HttpRequest request, HttpResponse response) throws IOException {
		response.setStatus(HttpStatus.BAD_REQUEST, "POST method");
		return HttpServiceResult.DONE;
	}

	public HttpServiceResult executePut(HttpRequest request, HttpResponse response) throws IOException {
		response.setStatus(HttpStatus.BAD_REQUEST, "PUT method");
		return HttpServiceResult.DONE;
	}

	public HttpServiceResult executeDelete(HttpRequest request, HttpResponse response) throws IOException {
		response.setStatus(HttpStatus.BAD_REQUEST, "DELETE method");
		return HttpServiceResult.DONE;
	}
}
