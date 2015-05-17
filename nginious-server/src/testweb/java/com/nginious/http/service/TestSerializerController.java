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
import java.util.Calendar;
import java.util.Date;

import com.nginious.http.HttpException;
import com.nginious.http.HttpMethod;
import com.nginious.http.HttpRequest;
import com.nginious.http.HttpResponse;
import com.nginious.http.HttpStatus;
import com.nginious.http.annotation.Controller;
import com.nginious.http.annotation.Request;
import com.nginious.http.serialize.Serializer;
import com.nginious.http.serialize.SerializerException;
import com.nginious.http.serialize.SerializerFactory;
import com.nginious.http.serialize.SerializerFactoryException;

@Controller(path = "/serializer")
public class TestSerializerController {
	
	@Request(methods = { HttpMethod.GET })
	public void executeGet(HttpRequest request, HttpResponse response, SerializerFactory factory) throws HttpException, IOException {
		execute(request, response, factory);
	}
	
	@Request(methods = { HttpMethod.POST })
	public void executePost(HttpRequest request, HttpResponse response, SerializerFactory factory) throws IOException {
		execute(request, response, factory);
	}

	@Request(methods = { HttpMethod.PUT })
	public void executePut(HttpRequest request, HttpResponse response, SerializerFactory factory) throws IOException {
		execute(request, response, factory);
	}
	
	@Request(methods = { HttpMethod.DELETE })
	public void executeDelete(HttpRequest request, HttpResponse response, SerializerFactory factory) throws IOException {
		execute(request, response, factory);
	}
	
	private void execute(HttpRequest request, HttpResponse response, SerializerFactory factory) throws HttpException, IOException {
		try {
			String accept = request.getHeader("Accept");
			
			TestBean1 bean = new TestBean1();
			bean.setFirst(true);
			bean.setSecond(1.1d);
			bean.setThird(1.2f);
			bean.setFourth(2);
			bean.setFifth(5L);
			bean.setSixth((short)3);
			bean.setSeventh("Seven");
			bean.setEight(new Date());
			bean.setNinth(Calendar.getInstance());
			
			Serializer<TestBean1> serializer = factory.createSerializer(TestBean1.class, accept);
			response.setContentType(serializer.getMimeType());
			response.setCharacterEncoding("utf-8");
			PrintWriter writer = response.getWriter();
			serializer.serialize(writer, bean);
		} catch(SerializerException e) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed" ,e);
		} catch(SerializerFactoryException e) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed", e);
		}		
	}
}
