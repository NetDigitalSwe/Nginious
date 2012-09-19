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

package com.nginious.http.rest;

import com.nginious.http.HttpServiceResult;
import com.nginious.http.rest.RestRequest;
import com.nginious.http.rest.RestResponse;
import com.nginious.http.rest.RestService;

public class TestService extends RestService<InBean, OutBean>{

	public HttpServiceResult executeGet(RestRequest<InBean> request, RestResponse<OutBean> response) {
		InBean bean = request.getBean();
		OutBean out = new OutBean();
		out.setFirst(bean.getFirst());
		out.setSecond(bean.getSecond());
		out.setThird(bean.getThird());
		out.setFourth(bean.getFourth());
		out.setFifth(bean.getFifth());
		out.setSixth(bean.getSixth());
		out.setSeventh(bean.getSeventh());
		out.setEight(bean.getEight());
		out.setNinth(bean.getNinth());
		response.setBean(out);
		return HttpServiceResult.DONE;
	}
}
