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
package org.apache.ofbiz.example;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class ExampleServices {
	public static final String module = ExampleServices.class.getName();

	public static Map<String, Object> sendExamplePushNotifications(DispatchContext dctx, Map<String, ? extends Object> context) {
		String exampleId = (String) context.get("exampleId");
		String message = (String) context.get("message");
		Set<Session> clients = (Set<Session>) ExampleWebSockets.getClients();
		try {
			synchronized (clients) {
				for (Session client : clients) {
					client.getBasicRemote().sendText(message + ": " + exampleId);
				}
			}
		} catch (IOException e) {
			Debug.logError(e.getMessage(), module);
		}
		return ServiceUtil.returnSuccess();
	}
}