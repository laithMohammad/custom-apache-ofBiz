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
package org.apache.ofbiz.base.util;

import java.io.Serializable;
import java.util.*;

/**
 * Contains extra information about Messages
 */
@SuppressWarnings("serial")
public class MessageString implements Serializable {

	public static final String module = MessageString.class.getName();

	protected String message;
	protected String fieldName;
	protected String toFieldName;
	protected Throwable sourceError;
	protected Locale locale;
	protected String propertyResource;
	protected String propertyName;
	protected boolean isError = true;

	/**
	 * @param message
	 * @param fieldName
	 * @param locale
	 * @param propertyResource
	 * @param propertyName
	 */
	public MessageString(String message, String fieldName, String propertyResource, String propertyName, Locale locale, boolean isError) {
		this.message = message;
		this.fieldName = fieldName;
		this.locale = locale;
		this.propertyResource = propertyResource;
		this.propertyName = propertyName;
		this.isError = isError;
	}

	/**
	 * @param message
	 * @param fieldName
	 */
	public MessageString(String message, String fieldName, boolean isError) {
		this.message = message;
		this.fieldName = fieldName;
		this.isError = isError;
	}

	/**
	 * @param message
	 * @param fieldName
	 * @param toFieldName
	 * @param sourceError
	 */
	public MessageString(String message, String fieldName, String toFieldName, Throwable sourceError) {
		this.message = message;
		this.fieldName = fieldName;
		this.toFieldName = toFieldName;
		this.sourceError = sourceError;
		this.isError = true;
	}

	/**
	 * @param message
	 * @param sourceError
	 */
	public MessageString(String message, Throwable sourceError) {
		this.message = message;
		this.sourceError = sourceError;
		this.isError = true;
	}

	public static List<Object> getMessagesForField(String fieldName, boolean convertToStrings, List<Object> messageStringList) {
		if (fieldName == null) {
			return Collections.emptyList();
		}
		Set<String> fieldSet = new TreeSet<String>();
		fieldSet.add(fieldName);
		return getMessagesForField(fieldSet, convertToStrings, messageStringList);
	}

	public static List<Object> getMessagesForField(String fieldName1, String fieldName2, String fieldName3, String fieldName4, boolean convertToStrings, List<Object> messageStringList) {
		Set<String> fieldSet = new TreeSet<String>();
		if (UtilValidate.isNotEmpty(fieldName1)) fieldSet.add(fieldName1);
		if (UtilValidate.isNotEmpty(fieldName2)) fieldSet.add(fieldName2);
		if (UtilValidate.isNotEmpty(fieldName3)) fieldSet.add(fieldName3);
		if (UtilValidate.isNotEmpty(fieldName4)) fieldSet.add(fieldName4);
		return getMessagesForField(fieldSet, convertToStrings, messageStringList);
	}

	public static List<Object> getMessagesForField(Set<String> fieldNameSet, boolean convertToStrings, List<Object> messageStringList) {
		if (messageStringList == null || UtilValidate.isEmpty(fieldNameSet)) {
			return Collections.emptyList();
		}
		List<Object> outList = new ArrayList<Object>(messageStringList.size());
		for (Object messageStringCur : messageStringList) {
			if (messageStringCur instanceof MessageString) {
				MessageString messageString = (MessageString) messageStringCur;
				if (messageString.isForField(fieldNameSet)) {
					if (convertToStrings) {
						outList.add(messageString.toString());
					} else {
						outList.add(messageString);
					}
				}
			} else {
				// not a MessageString, don't know if it is for this field so skip it
				continue;
			}
		}
		return outList;
	}

	/**
	 * @return Returns the fieldName.
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName The fieldName to set.
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public boolean isForField(Set<String> fieldNameSet) {
		if (fieldNameSet == null) {
			return true;
		}
		return fieldNameSet.contains(this.fieldName);
	}

	public boolean isForField(String fieldName) {
		if (this.fieldName == null) {
			if (fieldName == null) {
				return true;
			} else {
				return false;
			}
		} else {
			return this.fieldName.equals(fieldName);
		}
	}

	/**
	 * @return Returns the message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message The message to set.
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return Returns the sourceError.
	 */
	public Throwable getSourceError() {
		return sourceError;
	}

	/**
	 * @param sourceError The sourceError to set.
	 */
	public void setSourceError(Throwable sourceError) {
		this.sourceError = sourceError;
	}

	/**
	 * @return Returns the toFieldName.
	 */
	public String getToFieldName() {
		return toFieldName;
	}

	/**
	 * @param toFieldName The toFieldName to set.
	 */
	public void setToFieldName(String toFieldName) {
		this.toFieldName = toFieldName;
	}

	/**
	 * @return Returns the locale.
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale The locale to set.
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return Returns the propertyName.
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * @param propertyName The propertyName to set.
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * @return Returns the propertyResource.
	 */
	public String getPropertyResource() {
		return propertyResource;
	}

	/**
	 * @param propertyResource The propertyResource to set.
	 */
	public void setPropertyResource(String propertyResource) {
		this.propertyResource = propertyResource;
	}

	/**
	 * @return Returns the isError.
	 */
	public boolean isError() {
		return isError;
	}

	/**
	 * @param isError The isError to set.
	 */
	public void setError(boolean isError) {
		this.isError = isError;
	}

	@Override
	public String toString() {
		return this.message;
	}
}
