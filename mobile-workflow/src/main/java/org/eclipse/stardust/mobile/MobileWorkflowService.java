/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.mobile;

import java.util.List;

import org.eclipse.stardust.engine.api.dto.ActivityInstanceDetails;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.mobile.form.AjaxStructureContainer;
import org.eclipse.stardust.mobile.form.FormCache;
import org.eclipse.stardust.mobile.form.ManualActivityForm;
import org.eclipse.stardust.ui.common.form.Indent;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * 
 * @author Ellie.Sepehri
 *
 */
public class MobileWorkflowService {
	private static final String OID_PROPERTY = "oid";
	private static final String ID_PROPERTY = "id";
	private static final String NAME_PROPERTY = "name";
	private static final String DESCRIPTION_PROPERTY = "description";
	private static final String ACCOUNT_PROPERTY = "account";
	private static final String PASSWORD_PROPERTY = "password";
	private static final String FIRST_NAME_PROPERTY = "firstName";
	private static final String LAST_NAME_PROPERTY = "lastName";
	private static final String ACTIVITY_ID_PROPERTY = "activityId";
	private static final String ACTIVITY_NAME_PROPERTY = "activityName";
	private static final String PROCESS_ID_PROPERTY = "processId";
	private static final String PROCESS_NAME_PROPERTY = "processName";
	private static final String LAST_MODIFICATION_TIME_PROPERTY = "lastModificationTime";
	private static final String START_TIME_PROPERTY = "startTime";
	private static final String FORM_HTML_PROPERTY = "formHtml";

	private ServiceFactory serviceFactory;
	private WorkflowService workflowService;
	private FormCache formCache;
	
	/**
	 * 
	 * @return
	 */
	public FormCache getFormCache() {
		return formCache;
	}

	/**
	 * 
	 * @param formCache
	 */
	public void setFormCache(FormCache formCache) {
		this.formCache = formCache;
	}

	/**
	 * 
	 * @param json
	 * @return
	 */
	public JsonObject login(JsonObject json) {
			User user = initializeServiceFactory(json.get(ACCOUNT_PROPERTY)
					.getAsString(), json.get(PASSWORD_PROPERTY).getAsString());
			JsonObject userJson = new JsonObject();

			userJson.addProperty(ACCOUNT_PROPERTY, user.getAccount());
			userJson.addProperty(FIRST_NAME_PROPERTY, user.getFirstName());
			userJson.addProperty(LAST_NAME_PROPERTY, user.getLastName());

			return userJson;
	}

	/**
	 * 
	 * @return
	 */
	public JsonArray getStartableProcesses() {
			JsonArray processDefinitionsJson = new JsonArray();

			for (ProcessDefinition processDefinition : getWorkflowService()
					.getStartableProcessDefinitions()) {
				JsonObject processDefinitionJson = new JsonObject();

				processDefinitionsJson.add(processDefinitionJson);

				processDefinitionJson.addProperty(ID_PROPERTY,
						processDefinition.getId());
				processDefinitionJson.addProperty(NAME_PROPERTY,
						processDefinition.getName());
				processDefinitionJson.addProperty(DESCRIPTION_PROPERTY,
						processDefinition.getDescription());
			}

			return processDefinitionsJson;
	}

	/**
	 * 
	 * @return
	 */
	public JsonArray getWorklist()
	{
		JsonArray activityInstancesJson = new JsonArray();
		WorklistQuery worklistQuery = WorklistQuery.findCompleteWorklist();
		//worklistQuery.orderBy(getOrderCriteria());

		for (ActivityInstanceDetails activityInstance: (List<ActivityInstanceDetails>)getWorkflowService()
				.getWorklist(worklistQuery).getCumulatedItems())
		{
			JsonObject activityInstanceJson = new JsonObject();

			activityInstancesJson.add(activityInstanceJson);

			activityInstanceJson.addProperty(OID_PROPERTY,
					activityInstance.getOID());
			activityInstanceJson.addProperty(START_TIME_PROPERTY,
					activityInstance.getStartTime().getTime());			
			activityInstanceJson.addProperty(LAST_MODIFICATION_TIME_PROPERTY,
					activityInstance.getLastModificationTime().getTime());			
			activityInstanceJson.addProperty(ACTIVITY_ID_PROPERTY,
					activityInstance.getActivity().getId());
			activityInstanceJson.addProperty(ACTIVITY_NAME_PROPERTY,
					activityInstance.getActivity().getName());
		}
		
		return activityInstancesJson;
	}

	/**
	 * 
	 * @return
	 */
	public JsonObject activateActivity(JsonObject json)
	{
		ActivityInstance activityInstance = getWorkflowService().activate(json.get(OID_PROPERTY).getAsLong());		
		JsonObject activityInstanceJson = new JsonObject();

		activityInstanceJson.addProperty(OID_PROPERTY,
				activityInstance.getOID());
		activityInstanceJson.addProperty(START_TIME_PROPERTY,
				activityInstance.getStartTime().getTime());			
		activityInstanceJson.addProperty(LAST_MODIFICATION_TIME_PROPERTY,
				activityInstance.getLastModificationTime().getTime());			
		activityInstanceJson.addProperty(ACTIVITY_ID_PROPERTY,
				activityInstance.getActivity().getId());
		activityInstanceJson.addProperty(ACTIVITY_NAME_PROPERTY,
				activityInstance.getActivity().getName());
		
		if (activityInstance.getActivity().getImplementationType() == ImplementationType.Manual) {
			ApplicationContext applicationContext = activityInstance.getActivity().getApplicationContext(
					"default");
			ManualActivityForm form = getFormCache().getForm(activityInstance, applicationContext, null/*binding*/, getWorkflowService());
			
			activityInstanceJson.addProperty(FORM_HTML_PROPERTY,
					((AjaxStructureContainer)form.getRootContainer()).generateMarkupCode(new Indent()));
		} 
		
		return activityInstanceJson;
	}

	/**
	 * 
	 * @param account
	 * @param password
	 * @return
	 */
	private User initializeServiceFactory(String account, String password) {
		serviceFactory = ServiceFactoryLocator.get(account, password);
		workflowService = serviceFactory.getWorkflowService();
		
		return workflowService.getUser();
	}

	/**
	 * 
	 * @return
	 */
	private WorkflowService getWorkflowService() {
		return workflowService;
	}
}
