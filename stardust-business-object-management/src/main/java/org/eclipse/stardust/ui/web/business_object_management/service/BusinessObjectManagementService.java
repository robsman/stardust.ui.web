/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.business_object_management.service;

import javax.annotation.Resource;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.ui.web.business_object_management.rest.JsonMarshaller;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class BusinessObjectManagementService {
	private static final Logger trace = LogManager
			.getLogger(BusinessObjectManagementService.class);
	@Resource
	private SessionContext sessionContext;
	private DocumentManagementService documentManagementService;
	private UserService userService;
	private QueryService queryService;
	private WorkflowService workflowService;
	private AdministrationService administrationService;

	public BusinessObjectManagementService() {
		super();

		new JsonMarshaller();
	}

	private ServiceFactory getServiceFactory() {
		return sessionContext.getServiceFactory();
	}

	/**
	 * 
	 * @return
	 */
	DocumentManagementService getDocumentManagementService() {
		if (documentManagementService == null) {
			documentManagementService = getServiceFactory()
					.getDocumentManagementService();
		}

		return documentManagementService;
	}

	/**
	 * 
	 * @return
	 */
	private UserService getUserService() {
		if (userService == null) {
			userService = getServiceFactory().getUserService();
		}

		return userService;
	}

	/**
	 * 
	 * @return
	 */
	private QueryService getQueryService() {
		if (queryService == null) {
			queryService = getServiceFactory().getQueryService();
		}

		return queryService;
	}

	/**
	 * 
	 * @return
	 */
	private WorkflowService getWorkflowService() {
		if (workflowService == null) {
			workflowService = getServiceFactory().getWorkflowService();
		}

		return workflowService;
	}

	/**
	 * 
	 * @return
	 */
	private AdministrationService getAdministrationService() {
		if (administrationService == null) {
			administrationService = getServiceFactory()
					.getAdministrationService();
		}

		return administrationService;
	}

	/**
	 * 
	 * @return
	 */
	public JsonObject getBusinessObjects() {
		JsonObject resultJson = new JsonObject();
		JsonArray modelsJson = new JsonArray();

		resultJson.add("models", modelsJson);

		JsonObject modelJson = new JsonObject();

		modelsJson.add(modelJson);

		modelJson.addProperty("oid", 1);
		modelJson.addProperty("name", "General Claim Processing");

		JsonArray businessObjectsJson = new JsonArray();

		modelJson.add("businessObjects", businessObjectsJson);

		JsonObject businessObjectJson = null;
		JsonArray fieldsJson = null;
		JsonObject fieldJson = null;

		// Member

		businessObjectJson = new JsonObject();

		businessObjectsJson.add(businessObjectJson);

		businessObjectJson.addProperty("id", "Member");
		businessObjectJson.addProperty("name", "Member");

		fieldsJson = new JsonArray();

		businessObjectJson.add("fields", fieldsJson);

		fieldJson = new JsonObject();

		fieldsJson.add(fieldJson);

		fieldJson.addProperty("id", "id");
		fieldJson.addProperty("name", "Member ID");
		fieldJson.addProperty("type", "string");
		fieldJson.addProperty("key", true);
		fieldJson.addProperty("primaryKey", true);

		fieldJson = new JsonObject();

		fieldsJson.add(fieldJson);

		fieldJson.addProperty("id", "firstName");
		fieldJson.addProperty("name", "First Name");
		fieldJson.addProperty("type", "string");
		fieldJson.addProperty("key", true);
		fieldJson.addProperty("primaryKey", false);

		fieldJson = new JsonObject();

		fieldsJson.add(fieldJson);

		fieldJson.addProperty("id", "lastName");
		fieldJson.addProperty("name", "Last Name");
		fieldJson.addProperty("type", "string");
		fieldJson.addProperty("key", true);
		fieldJson.addProperty("primaryKey", false);

		fieldJson = new JsonObject();

		fieldsJson.add(fieldJson);

		fieldJson.addProperty("id", "scheme");
		fieldJson.addProperty("name", "Scheme Name");
		fieldJson.addProperty("type", "string");
		fieldJson.addProperty("key", false);
		fieldJson.addProperty("primaryKey", false);

		fieldJson = new JsonObject();

		fieldsJson.add(fieldJson);

		fieldJson.addProperty("id", "schemeId");
		fieldJson.addProperty("name", "Scheme ID");
		fieldJson.addProperty("type", "string");
		fieldJson.addProperty("key", false);
		fieldJson.addProperty("primaryKey", false);

		fieldJson = new JsonObject();

		fieldsJson.add(fieldJson);

		fieldJson.addProperty("id", "nationalId");
		fieldJson.addProperty("name", "National ID");
		fieldJson.addProperty("type", "string");
		fieldJson.addProperty("key", false);
		fieldJson.addProperty("primaryKey", false);

		// Member

		businessObjectJson = new JsonObject();

		businessObjectsJson.add(businessObjectJson);

		businessObjectJson.addProperty("id", "Fund");
		businessObjectJson.addProperty("name", "Fund");

		fieldsJson = new JsonArray();

		businessObjectJson.add("fields", fieldsJson);

		fieldJson = new JsonObject();

		fieldsJson.add(fieldJson);

		fieldJson.addProperty("id", "id");
		fieldJson.addProperty("name", "Fund Id");
		fieldJson.addProperty("type", "string");
		fieldJson.addProperty("key", true);
		fieldJson.addProperty("primaryKey", true);

		fieldJson = new JsonObject();

		fieldsJson.add(fieldJson);

		fieldJson.addProperty("id", "name");
		fieldJson.addProperty("name", "Fund Name");
		fieldJson.addProperty("type", "string");
		fieldJson.addProperty("key", true);
		fieldJson.addProperty("primaryKey", false);

		return resultJson;
	}

	/**
	 * 
	 * @param modelOid
	 * @param businessObjectId
	 * @return
	 */
	public JsonObject getBusinessObjectInstances(String modelOid,
			String businessObjectId) {
		JsonObject resultJson = new JsonObject();
		JsonArray businessObjectInstances = new JsonArray();

		resultJson.add("businessObjectInstances", businessObjectInstances);

		JsonObject businessObjectInstance = null;

		if (businessObjectId.equals("Member")) {
			businessObjectInstance = new JsonObject();

			businessObjectInstances.add(businessObjectInstance);

			businessObjectInstance.addProperty("id", "4711");
			businessObjectInstance.addProperty("firstName", "Haile");
			businessObjectInstance.addProperty("lastName", "Selassie");
			businessObjectInstance.addProperty("scheme", "1");
			businessObjectInstance.addProperty("schemeName", "Scheme-1");
			businessObjectInstance.addProperty("nationalId", "SA");

			businessObjectInstance = new JsonObject();

			businessObjectInstances.add(businessObjectInstance);

			businessObjectInstance.addProperty("id", "0815");
			businessObjectInstance.addProperty("firstName", "Jan");
			businessObjectInstance.addProperty("lastName", "Smuts");
			businessObjectInstance.addProperty("scheme", "2");
			businessObjectInstance.addProperty("schemeName", "Scheme-2");
			businessObjectInstance.addProperty("nationalId", "SA");
		} else if (businessObjectId.equals("Fund")) {
			businessObjectInstance = new JsonObject();

			businessObjectInstances.add(businessObjectInstance);

			businessObjectInstance.addProperty("id", "4711");
			businessObjectInstance.addProperty("name", "Haile");
		}

		return resultJson;
	}

	/**
	 * 
	 * @param modelOid
	 * @param businessObjectId
	 * @param primaryKey
	 * @return
	 */
	public JsonObject getBusinessObjectProcessInstances(String modelOid,
			String businessObjectId, String primaryKey) {
		JsonObject resultJson = new JsonObject();

		return resultJson;
	}
}
