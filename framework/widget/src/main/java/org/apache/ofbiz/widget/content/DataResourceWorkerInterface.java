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
package org.apache.ofbiz.widget.content;

import org.apache.ofbiz.base.util.GeneralException;
import org.apache.ofbiz.entity.Delegator;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * ContentWorkerInterface
 */
public interface DataResourceWorkerInterface {
	public String renderDataResourceAsTextExt(Delegator delegator, String dataResourceId, Map<String, Object> templateContext,
	                                          Locale locale, String targetMimeTypeId, boolean cache) throws GeneralException, IOException;

	public void renderDataResourceAsTextExt(Delegator delegator, String dataResourceId, Appendable out, Map<String, Object> templateContext,
	                                        Locale locale, String targetMimeTypeId, boolean cache) throws GeneralException, IOException;
}
