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
package org.apache.ofbiz.minilang.operation;

import org.apache.ofbiz.base.util.UtilXml;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * <b>Title:</b> A MakeInStringOperation that appends the specified constant string
 */
public class ConstantOper extends MakeInStringOperation {

	String constant;

	public ConstantOper(Element element) {
		super(element);
		constant = UtilXml.elementValue(element);
	}

	@Override
	public String exec(Map<String, Object> inMap, List<Object> messages, Locale locale, ClassLoader loader) {
		return constant;
	}
}
