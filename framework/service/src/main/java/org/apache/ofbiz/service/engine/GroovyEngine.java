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
package org.apache.ofbiz.service.engine;

import groovy.lang.Script;
import org.apache.ofbiz.base.util.*;
import org.apache.ofbiz.service.*;
import org.codehaus.groovy.runtime.InvokerHelper;

import javax.script.ScriptContext;
import java.util.*;

import static org.apache.ofbiz.base.util.UtilGenerics.cast;

/**
 * Groovy Script Service Engine
 */
public final class GroovyEngine extends GenericAsyncEngine {

	public static final String module = GroovyEngine.class.getName();
	protected static final Object[] EMPTY_ARGS = {};
	private static final Set<String> protectedKeys = createProtectedKeys();

	public GroovyEngine(ServiceDispatcher dispatcher) {
		super(dispatcher);
	}

	private static Set<String> createProtectedKeys() {
		Set<String> newSet = new HashSet<String>();
        /* Commenting out for now because some scripts write to the parameters Map - which should not be allowed.
        newSet.add(ScriptUtil.PARAMETERS_KEY);
        */
		newSet.add("dctx");
		newSet.add("dispatcher");
		newSet.add("delegator");
		return Collections.unmodifiableSet(newSet);
	}

	/**
	 * @see org.apache.ofbiz.service.engine.GenericEngine#runSyncIgnore(java.lang.String, org.apache.ofbiz.service.ModelService, java.util.Map)
	 */
	@Override
	public void runSyncIgnore(String localName, ModelService modelService, Map<String, Object> context) throws GenericServiceException {
		runSync(localName, modelService, context);
	}

	/**
	 * @see org.apache.ofbiz.service.engine.GenericEngine#runSync(java.lang.String, org.apache.ofbiz.service.ModelService, java.util.Map)
	 */
	@Override
	public Map<String, Object> runSync(String localName, ModelService modelService, Map<String, Object> context) throws GenericServiceException {
		return serviceInvoker(localName, modelService, context);
	}

	private Map<String, Object> serviceInvoker(String localName, ModelService modelService, Map<String, Object> context) throws GenericServiceException {
		if (UtilValidate.isEmpty(modelService.location)) {
			throw new GenericServiceException("Cannot run Groovy service with empty location");
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.putAll(context);

		Map<String, Object> gContext = new HashMap<String, Object>();
		gContext.putAll(context);
		gContext.put(ScriptUtil.PARAMETERS_KEY, params);

		DispatchContext dctx = dispatcher.getLocalContext(localName);
		gContext.put("dctx", dctx);
		gContext.put("dispatcher", dctx.getDispatcher());
		gContext.put("delegator", dispatcher.getDelegator());
		try {
			ScriptContext scriptContext = ScriptUtil.createScriptContext(gContext, protectedKeys);
			ScriptHelper scriptHelper = (ScriptHelper) scriptContext.getAttribute(ScriptUtil.SCRIPT_HELPER_KEY);
			if (scriptHelper != null) {
				gContext.put(ScriptUtil.SCRIPT_HELPER_KEY, scriptHelper);
			}
			Script script = InvokerHelper.createScript(GroovyUtil.getScriptClassFromLocation(this.getLocation(modelService)), GroovyUtil.getBinding(gContext));
			Object resultObj = null;
			if (UtilValidate.isEmpty(modelService.invoke)) {
				resultObj = script.run();
			} else {
				resultObj = script.invokeMethod(modelService.invoke, EMPTY_ARGS);
			}
			if (resultObj == null) {
				resultObj = scriptContext.getAttribute(ScriptUtil.RESULT_KEY);
			}
			if (resultObj != null && resultObj instanceof Map<?, ?>) {
				return cast(resultObj);
			}
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.putAll(modelService.makeValid(scriptContext.getBindings(ScriptContext.ENGINE_SCOPE), "OUT"));
			return result;
		} catch (GeneralException ge) {
			throw new GenericServiceException(ge);
		} catch (Exception e) {
			// detailMessage can be null.  If it is null, the exception won't be properly returned and logged, and that will
			// make spotting problems very difficult.  Disabling this for now in favor of returning a proper exception.
			// return ServiceUtil.returnError(e.getMessage());
			throw new GenericServiceException("Error running Groovy method [" + modelService.invoke + "] in Groovy file [" + modelService.location + "]: ", e);
		}
	}
}
