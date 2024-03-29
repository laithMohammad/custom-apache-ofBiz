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
package org.apache.ofbiz.content.content;

import org.apache.ofbiz.base.util.*;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.entityext.permission.EntityPermissionChecker;
import org.apache.ofbiz.security.Security;
import org.apache.ofbiz.service.*;

import java.util.*;


/**
 * ContentPermissionServices Class
 * <p>
 * Services for granting operation permissions on Content entities in a data-driven manner.
 */
public class ContentPermissionServices {

	public static final String module = ContentPermissionServices.class.getName();
	public static final String resource = "ContentUiLabels";

	public ContentPermissionServices() {
	}

	/**
	 * checkContentPermission
	 *
	 * @param dctx    The DispatchContext that this service is operating in
	 * @param context Map containing the input parameters
	 * @return Map with the result of the service, the output parameters
	 * <p>
	 * This service goes thru a series of test to determine if the user has
	 * authority to performed anyone of the passed in target operations.
	 * <p>
	 * It expects a Content entity in "currentContent"
	 * It expects a list of contentOperationIds in "targetOperationList" rather
	 * than a scalar because it is thought that sometimes more than one operation
	 * would fit the situation.
	 * Similarly, it expects a list of contentPurposeTypeIds in "contentPurposeList".
	 * Again, normally there will just be one, but it is possible that a Content
	 * entity could have multiple purposes associated with it.
	 * The userLogin GenericValue is also required.
	 * A list of roleTypeIds is also possible.
	 * <p>
	 * The basic sequence of testing events is:
	 * First the ContentPurposeOperation table is checked to see if there are any
	 * entries with matching purposes (and operations) with no roleTypeId (ie. _NA_).
	 * This is done because it would be the most common scenario and is quick to check.
	 * <p>
	 * Secondly, the CONTENTMGR permission is checked.
	 * <p>
	 * Thirdly, the ContentPurposeOperation table is rechecked to see if there are
	 * any conditions with roleTypeIds that match associated ContentRoles tied to the
	 * user.
	 * If a Party of "PARTY_GROUP" type is found, the PartyRelationship table is checked
	 * to see if the current user is linked to that group.
	 * <p>
	 * If no match is found to this point and the current Content entity has a value for
	 * ownerContentId, then the last step is recusively applied, using the ContentRoles
	 * associated with the ownerContent entity.
	 */
	public static Map<String, Object> checkContentPermission(DispatchContext dctx, Map<String, ? extends Object> context) {
		Debug.logWarning(new Exception(), "This service has been depricated in favor of [genericContentPermission]", module);

		Security security = dctx.getSecurity();
		Delegator delegator = dctx.getDelegator();
		//TODO this parameters is still not used but this service need to be replaced by genericContentPermission
		// String statusId = (String) context.get("statusId");
		//TODO this parameters is still not used but this service need to be replaced by genericContentPermission
		// String privilegeEnumId = (String) context.get("privilegeEnumId");
		GenericValue content = (GenericValue) context.get("currentContent");
		Boolean bDisplayFailCond = (Boolean) context.get("displayFailCond");
		boolean displayFailCond = false;
		if (bDisplayFailCond != null && bDisplayFailCond.booleanValue()) {
			displayFailCond = true;
		}
		Debug.logInfo("displayFailCond(0):" + displayFailCond, "");
		Boolean bDisplayPassCond = (Boolean) context.get("displayPassCond");
		boolean displayPassCond = false;
		if (bDisplayPassCond != null && bDisplayPassCond.booleanValue()) {
			displayPassCond = true;
		}
		Debug.logInfo("displayPassCond(0):" + displayPassCond, "");
		Map<String, Object> results = new HashMap<String, Object>();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String partyId = (String) context.get("partyId");
		if (UtilValidate.isEmpty(partyId)) {
			String passedUserLoginId = (String) context.get("userLoginId");
			if (UtilValidate.isNotEmpty(passedUserLoginId)) {
				try {
					userLogin = EntityQuery.use(delegator).from("UserLogin").where("userLoginId", passedUserLoginId).cache().queryOne();
					if (userLogin != null) {
						partyId = userLogin.getString("partyId");
					}
				} catch (GenericEntityException e) {
					return ServiceUtil.returnError(e.getMessage());
				}
			}
		}
		if (UtilValidate.isEmpty(partyId) && userLogin != null) {
			partyId = userLogin.getString("partyId");
		}


		// Do entity permission check. This will pass users with administrative permissions.
		boolean passed = false;
		// I realized, belatedly, that I wanted to be able to pass parameters in as
		// strings so this service could be used in an action event directly,
		// so I had to write this code to handle both list and strings
		List<String> passedPurposes = UtilGenerics.checkList(context.get("contentPurposeList"));
		String contentPurposeString = (String) context.get("contentPurposeString");
		if (UtilValidate.isNotEmpty(contentPurposeString)) {
			List<String> purposesFromString = StringUtil.split(contentPurposeString, "|");
			if (passedPurposes == null) {
				passedPurposes = new LinkedList<String>();
			}
			passedPurposes.addAll(purposesFromString);
		}

		EntityPermissionChecker.StdAuxiliaryValueGetter auxGetter = new EntityPermissionChecker.StdAuxiliaryValueGetter("ContentPurpose", "contentPurposeTypeId", "contentId");
		// Sometimes permissions need to be checked before an entity is created, so
		// there needs to be a method for setting a purpose list
		auxGetter.setList(passedPurposes);
		List<String> targetOperations = UtilGenerics.checkList(context.get("targetOperationList"));
		String targetOperationString = (String) context.get("targetOperationString");
		if (UtilValidate.isNotEmpty(targetOperationString)) {
			List<String> operationsFromString = StringUtil.split(targetOperationString, "|");
			if (targetOperations == null) {
				targetOperations = new LinkedList<String>();
			}
			targetOperations.addAll(operationsFromString);
		}
		EntityPermissionChecker.StdPermissionConditionGetter permCondGetter = new EntityPermissionChecker.StdPermissionConditionGetter("ContentPurposeOperation", "contentOperationId", "roleTypeId", "statusId", "contentPurposeTypeId", "privilegeEnumId");
		permCondGetter.setOperationList(targetOperations);

		EntityPermissionChecker.StdRelatedRoleGetter roleGetter = new EntityPermissionChecker.StdRelatedRoleGetter("Content", "roleTypeId", "contentId", "partyId", "ownerContentId", "ContentRole");
		List<String> passedRoles = UtilGenerics.checkList(context.get("roleTypeList"));
		if (passedRoles == null) passedRoles = new LinkedList<String>();
		String roleTypeString = (String) context.get("roleTypeString");
		if (UtilValidate.isNotEmpty(roleTypeString)) {
			List<String> rolesFromString = StringUtil.split(roleTypeString, "|");
			passedRoles.addAll(rolesFromString);
		}
		roleGetter.setList(passedRoles);

		String entityAction = (String) context.get("entityOperation");
		if (entityAction == null) entityAction = "_ADMIN";
		if (userLogin != null && entityAction != null) {
			passed = security.hasEntityPermission("CONTENTMGR", entityAction, userLogin);
		}

		StringBuilder errBuf = new StringBuilder();
		String permissionStatus = null;
		List<Object> entityIds = new LinkedList<Object>();
		if (passed) {
			results.put("permissionStatus", "granted");
			permissionStatus = "granted";
			if (displayPassCond) {
				errBuf.append("\n    hasEntityPermission(" + entityAction + "): PASSED");
			}

		} else {
			if (displayFailCond) {
				errBuf.append("\n    hasEntityPermission(" + entityAction + "): FAILED");
			}

			if (content != null)
				entityIds.add(content);
			String quickCheckContentId = (String) context.get("quickCheckContentId");
			if (UtilValidate.isNotEmpty(quickCheckContentId)) {
				List<String> quickList = StringUtil.split(quickCheckContentId, "|");
				if (UtilValidate.isNotEmpty(quickList)) entityIds.addAll(quickList);
			}
			try {
				boolean check = EntityPermissionChecker.checkPermissionMethod(delegator, partyId, "Content", entityIds, auxGetter, roleGetter, permCondGetter);
				if (check) {
					results.put("permissionStatus", "granted");
				} else {
					results.put("permissionStatus", "rejected");
				}
			} catch (GenericEntityException e) {
				return ServiceUtil.returnError(e.getMessage());
			}
			permissionStatus = (String) results.get("permissionStatus");
			errBuf.append("\n    permissionStatus:");
			errBuf.append(permissionStatus);
		}

		if ((permissionStatus.equals("granted") && displayPassCond)
				|| (permissionStatus.equals("rejected") && displayFailCond)) {
			// Don't show this if passed on 'hasEntityPermission'
			if (displayFailCond || displayPassCond) {
				if (!passed) {
					errBuf.append("\n    targetOperations:");
					errBuf.append(targetOperations);

					String errMsg = permCondGetter.dumpAsText();
					errBuf.append("\n");
					errBuf.append(errMsg);
					errBuf.append("\n    partyId:");
					errBuf.append(partyId);
					errBuf.append("\n    entityIds:");
					errBuf.append(entityIds);

					if (auxGetter != null) {
						errBuf.append("\n    auxList:");
						errBuf.append(auxGetter.getList());
					}

					if (roleGetter != null) {
						errBuf.append("\n    roleList:");
						errBuf.append(roleGetter.getList());
					}
				}

			}
		}
		Debug.logInfo("displayPass/FailCond(0), errBuf:" + errBuf.toString(), "");
		results.put(ModelService.ERROR_MESSAGE, errBuf.toString());
		return results;
	}

	public static Map<String, Object> checkAssocPermission(DispatchContext dctx, Map<String, ? extends Object> context) {
		Map<String, Object> results = new HashMap<String, Object>();
		// Security security = dctx.getSecurity();
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Boolean bDisplayFailCond = (Boolean) context.get("displayFailCond");
		String contentIdFrom = (String) context.get("contentIdFrom");
		String contentIdTo = (String) context.get("contentIdTo");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String entityAction = (String) context.get("entityOperation");
		Locale locale = (Locale) context.get("locale");
		if (entityAction == null) entityAction = "_ADMIN";
		String permissionStatus = null;

		GenericValue contentTo = null;
		GenericValue contentFrom = null;
		try {
			contentTo = EntityQuery.use(delegator).from("Content").where("contentId", contentIdTo).cache().queryOne();
			contentFrom = EntityQuery.use(delegator).from("Content").where("contentId", contentIdFrom).cache().queryOne();
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(UtilProperties.getMessage(resource,
					"ContentContentToOrFromErrorRetriving", locale));
		}
		if (contentTo == null || contentFrom == null) {
			return ServiceUtil.returnError(UtilProperties.getMessage(resource,
					"ContentContentToOrFromIsNull",
					UtilMisc.toMap("contentTo", contentTo, "contentFrom", contentFrom), locale));
		}
		Map<String, Object> permResults = new HashMap<String, Object>();

		// Use the purposes from the from entity for both cases.
		List<String> relatedPurposes = EntityPermissionChecker.getRelatedPurposes(contentFrom, null);
		List<String> relatedPurposesTo = EntityPermissionChecker.getRelatedPurposes(contentTo, relatedPurposes);
		Map<String, Object> serviceInMap = new HashMap<String, Object>();
		serviceInMap.put("userLogin", userLogin);
		serviceInMap.put("targetOperationList", UtilMisc.toList("CONTENT_LINK_TO"));
		serviceInMap.put("contentPurposeList", relatedPurposesTo);
		serviceInMap.put("currentContent", contentTo);
		serviceInMap.put("displayFailCond", bDisplayFailCond);

		try {
			permResults = dispatcher.runSync("checkContentPermission", serviceInMap);
		} catch (GenericServiceException e) {
			Debug.logError(e, "Problem checking permissions", "ContentServices");
		}
		permissionStatus = (String) permResults.get("permissionStatus");
		if (permissionStatus == null || !permissionStatus.equals("granted")) {
			if (bDisplayFailCond != null && bDisplayFailCond.booleanValue()) {
				String errMsg = (String) permResults.get(ModelService.ERROR_MESSAGE);
				results.put(ModelService.ERROR_MESSAGE, errMsg);
			}
			return results;
		}
		serviceInMap.put("currentContent", contentFrom);
		serviceInMap.put("targetOperationList", UtilMisc.toList("CONTENT_LINK_FROM"));
		serviceInMap.put("contentPurposeList", relatedPurposes);
		try {
			permResults = dispatcher.runSync("checkContentPermission", serviceInMap);
		} catch (GenericServiceException e) {
			Debug.logError(e, "Problem checking permissions", "ContentServices");
		}
		permissionStatus = (String) permResults.get("permissionStatus");
		if (permissionStatus != null && permissionStatus.equals("granted")) {
			results.put("permissionStatus", "granted");
		} else {
			if (bDisplayFailCond != null && bDisplayFailCond.booleanValue()) {
				String errMsg = (String) permResults.get(ModelService.ERROR_MESSAGE);
				results.put(ModelService.ERROR_MESSAGE, errMsg);
			}
		}
		return results;
	}

}
