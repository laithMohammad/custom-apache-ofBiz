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
package org.apache.ofbiz.passport.user;

import org.apache.ofbiz.base.util.UtilProperties;

import java.util.*;

/**
 * LinkedIn UserGroupMapper
 */
public class LinkedInUserGroupMapper {

	protected List<String> groups;

	public LinkedInUserGroupMapper(String[] groups) {
		this.groups = Arrays.asList(groups);
	}

	public LinkedInUserGroupMapper(String group) {
		if (groups == null) {
			groups = new ArrayList<String>();
		}
		groups.add(group);
	}

	public Set<String> getSecurityGroups() {
		Properties props = UtilProperties.getProperties(LinkedInAuthenticator.props);

		Set<String> secGroups = new HashSet<String>();
		boolean running = true;
		int index = 1;

		while (running) {
			String groupStr = (String) props.get("linkedin.group.map." + index);
			if (groupStr == null) {
				running = false;
			} else {
				String[] groupSplit = groupStr.split("=");
				if (groupSplit.length == 2) {
					if (groups.contains(groupSplit[0])) {
						secGroups.add(groupSplit[1]);
					}
				}
			}
			index++;
		}
		return secGroups;
	}
}
