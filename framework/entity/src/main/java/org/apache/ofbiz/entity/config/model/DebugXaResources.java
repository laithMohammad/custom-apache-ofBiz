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
package org.apache.ofbiz.entity.config.model;

import org.apache.ofbiz.base.lang.ThreadSafe;
import org.apache.ofbiz.entity.GenericEntityConfException;
import org.w3c.dom.Element;

/**
 * An object that models the <code>&lt;debug-xa-resources&gt;</code> element.
 *
 * @see <code>entity-config.xsd</code>
 */
@ThreadSafe
public final class DebugXaResources {

	private final boolean value; // type = xs:string

	DebugXaResources(Element element) throws GenericEntityConfException {
		String lineNumberText = EntityConfig.createConfigFileLineNumberText(element);
		String value = element.getAttribute("value").intern();
		if (value.isEmpty()) {
			throw new GenericEntityConfException("<debug-xa-resources> element value attribute is empty" + lineNumberText);
		}
		this.value = "true".equals(value);
	}

	/**
	 * Returns the value of the <code>value</code> attribute.
	 */
	public boolean getValue() {
		return this.value;
	}
}
