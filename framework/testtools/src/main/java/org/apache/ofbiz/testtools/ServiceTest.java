/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.apache.ofbiz.testtools;

import junit.framework.AssertionFailedError;
import junit.framework.TestResult;
import org.apache.ofbiz.base.util.UtilGenerics;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.ModelService;
import org.apache.ofbiz.service.testtools.OFBizTestCase;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

public class ServiceTest extends OFBizTestCase {

	public static final String module = ServiceTest.class.getName();

	protected String serviceName;

	/**
	 * Tests of Service
	 *
	 * @param caseName    test case name
	 * @param mainElement DOM main element
	 */
	public ServiceTest(String caseName, Element mainElement) {
		super(caseName);
		this.serviceName = mainElement.getAttribute("service-name");
	}

	@Override
	public int countTestCases() {
		return 1;
	}

	@Override
	public void run(TestResult result) {
		result.startTest(this);

		try {

			Map<String, Object> serviceResult = dispatcher.runSync(serviceName, UtilMisc.toMap("test", this, "testResult", result));

			// do something with the errorMessage
			String errorMessage = (String) serviceResult.get(ModelService.ERROR_MESSAGE);
			if (UtilValidate.isNotEmpty(errorMessage)) {
				result.addFailure(this, new AssertionFailedError(errorMessage));
			}

			// do something with the errorMessageList
			List<Object> errorMessageList = UtilGenerics.checkList(serviceResult.get(ModelService.ERROR_MESSAGE_LIST));
			if (UtilValidate.isNotEmpty(errorMessageList)) {
				for (Object message : errorMessageList) {
					result.addFailure(this, new AssertionFailedError(message.toString()));
				}
			}

			// do something with the errorMessageMap
			Map<String, Object> errorMessageMap = UtilGenerics.cast(serviceResult.get(ModelService.ERROR_MESSAGE_MAP));
			if (!UtilValidate.isEmpty(errorMessageMap)) {
				for (Map.Entry<String, Object> entry : errorMessageMap.entrySet()) {
					result.addFailure(this, new AssertionFailedError(entry.getKey() + ": " + entry.getValue()));
				}
			}

		} catch (GenericServiceException e) {
			result.addError(this, e);
		}

		result.endTest(this);
	}
}
