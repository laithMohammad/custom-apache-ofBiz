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
package org.apache.ofbiz.entity.transaction;

import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.datasource.GenericHelperInfo;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * TransactionFactory - central source for JTA objects
 */
public interface TransactionFactory {

	public TransactionManager getTransactionManager();

	public UserTransaction getUserTransaction();

	public String getTxMgrName();

	public Connection getConnection(GenericHelperInfo helperInfo) throws SQLException, GenericEntityException;

	public void shutdown();
}
