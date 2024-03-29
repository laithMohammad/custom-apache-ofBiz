/*
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
 */
package org.apache.ofbiz.entity;

import org.apache.ofbiz.base.concurrent.ExecutionPool;
import org.apache.ofbiz.base.lang.Factory;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilObject;

import java.util.concurrent.*;

/**
 * <code>Delegator</code> factory abstract class.
 */
public abstract class DelegatorFactory implements Factory<Delegator, String> {
	public static final String module = DelegatorFactory.class.getName();
	private static final ConcurrentHashMap<String, Future<Delegator>> delegators = new ConcurrentHashMap<String, Future<Delegator>>();
	private static final ThreadGroup DELEGATOR_THREAD_GROUP = new ThreadGroup("DelegatorFactory");
	private static final ScheduledExecutorService executor = ExecutionPool.getScheduledExecutor(DELEGATOR_THREAD_GROUP, "delegator-startup", Runtime.getRuntime().availableProcessors(), 10, true);

	public static Delegator getDelegator(String delegatorName) {
		Future<Delegator> future = getDelegatorFuture(delegatorName);
		try {
			return future.get();
		} catch (ExecutionException e) {
			Debug.logError(e, module);
			return null;
		} catch (InterruptedException e) {
			Debug.logError(e, module);
			return null;
		}
	}

	public static Future<Delegator> getDelegatorFuture(String delegatorName) {
		if (delegatorName == null) {
			delegatorName = "default";
			//Debug.logWarning(new Exception("Location where getting delegator with null name"), "Got a getGenericDelegator call with a null delegatorName, assuming default for the name.", module);
		}
		do {
			Future<Delegator> future = delegators.get(delegatorName);
			if (future != null) {
				//Debug.logInfo("got delegator(future(" + delegatorName + ")) from cache", module);
				return future;
			}
			FutureTask<Delegator> futureTask = new FutureTask<Delegator>(new DelegatorConfigurable(delegatorName));
			//Debug.logInfo("putting delegator(future(" + delegatorName + ")) into cache", module);
			if (delegators.putIfAbsent(delegatorName, futureTask) != null) {
				continue;
			}
			executor.submit(futureTask);
		} while (true);
	}

	public static final class DelegatorConfigurable implements Callable<Delegator> {
		private final String delegatorName;

		public DelegatorConfigurable(String delegatorName) {
			this.delegatorName = delegatorName;
		}

		public Delegator call() throws ClassNotFoundException {
			try {
				Delegator delegator = UtilObject.getObjectFromFactory(DelegatorFactory.class, delegatorName);

				// setup the Entity ECA Handler
				delegator.initEntityEcaHandler();

				// setup the distributed CacheClear
				delegator.initDistributedCacheClear();

				return delegator;
			} catch (ClassNotFoundException e) {
				Debug.logError(e, module);
				throw e;
			}
		}
	}
}
