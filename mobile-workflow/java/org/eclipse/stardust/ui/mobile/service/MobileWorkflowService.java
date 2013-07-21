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

package org.eclipse.stardust.ui.mobile.service;

import java.util.HashMap;
import java.util.List;

import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.HistoricalEvent;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.interactions.Interaction;
import org.eclipse.stardust.engine.ws.WebServiceEnv;
import org.eclipse.stardust.ui.mobile.rest.JsonMarshaller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MobileWorkflowService extends ClientEnvironmentAware {
	private UserService userService;
	private QueryService queryService;
	private WorkflowService workflowService;
	private JsonMarshaller jsonIo;

	public MobileWorkflowService() {
		super();

		jsonIo = new JsonMarshaller();
	}

	/**
	 * 
	 */
	private void prepareEnvironment() {
		String[] userPwd = usernamePassword();
		WebServiceEnv.setCurrentCredentials(userPwd[0], userPwd[1]);
		WebServiceEnv.setCurrentSessionProperties(properties());
	}

	/**
	 * 
	 */
	private void unwindEnvironment() {
		WebServiceEnv.removeCurrent();
	}

	/**
	 * 
	 * @return
	 */
	private ServiceFactory getServiceFactory() {
		return WebServiceEnv.currentWebServiceEnvironment().getServiceFactory();
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
	public JsonObject login() {
		try {
			prepareEnvironment();

			return null;
		} finally {
			unwindEnvironment();
		}
	}

	/**
	 * 
	 * @return
	 */
	public JsonObject getStartableProcesses() {
		try {
			prepareEnvironment();

			JsonObject resultJson = new JsonObject();
			JsonArray processDefinitionsJson = new JsonArray();

			resultJson.add("processDefinitions", processDefinitionsJson);

			for (ProcessDefinition processDefinition : getWorkflowService()
					.getStartableProcessDefinitions()) {
				JsonObject processDefinitionJson = new JsonObject();

				processDefinitionsJson.add(processDefinitionJson);

				processDefinitionJson.addProperty("id",
						processDefinition.getId());
				processDefinitionJson.addProperty("name",
						processDefinition.getName());
				processDefinitionJson.addProperty("description",
						processDefinition.getDescription());
			}

			return resultJson;
		} finally {
			unwindEnvironment();
		}
	}

	/**
	 * 
	 * @return
	 */
	public JsonObject getWorklist() {
		try {
			prepareEnvironment();

			JsonObject resultJson = new JsonObject();
			JsonArray worklistJson = new JsonArray();

			resultJson.add("worklist", worklistJson);

			for (ActivityInstance activityInstance : (List<ActivityInstance>) getWorkflowService()
					.getWorklist(WorklistQuery.findCompleteWorklist())
					.getCumulatedItems()) {
				JsonObject activityInstanceJson = new JsonObject();

				worklistJson.add(activityInstanceJson);

				activityInstanceJson.addProperty("oid",
						activityInstance.getOID());
				activityInstanceJson.addProperty("activityId", activityInstance
						.getActivity().getId());
				activityInstanceJson.addProperty("activityName",
						activityInstance.getActivity().getName());
				activityInstanceJson.addProperty("processId", activityInstance
						.getActivity().getProcessDefinitionId());
				activityInstanceJson
						.addProperty("processName", activityInstance
								.getActivity().getProcessDefinitionId());
				activityInstanceJson.addProperty("processInstanceOid",
						activityInstance.getProcessInstanceOID());
				activityInstanceJson.addProperty("startTime", activityInstance
						.getStartTime().getTime());
				activityInstanceJson.addProperty("lastModificationTime",
						activityInstance.getLastModificationTime().getTime());

				JsonObject descriptorsJson = new JsonObject();

				activityInstanceJson.add("descriptors", descriptorsJson);

				for (DataPath dataPath : activityInstance
						.getDescriptorDefinitions()) {
					descriptorsJson.addProperty(dataPath.getId(),
							(String) activityInstance
									.getDescriptorValue(dataPath.getId()));
				}
			}

			return resultJson;
		} finally {
			unwindEnvironment();
		}
	}

	/**
	 * 
	 * @return
	 */
	public JsonObject activateActivity(JsonObject activityInstanceJson) {
		try {
			prepareEnvironment();

			ActivityInstance activityInstance = getWorkflowService().activate(
					activityInstanceJson.get("oid").getAsLong());
			JsonObject activityJson = new JsonObject();

			activityInstanceJson.add("activity", activityJson);

			ApplicationContext applicationContext = null;
			JsonObject applicationContextsJson = new JsonObject();

			activityJson.add("contexts", applicationContextsJson);

			if (activityInstance.getActivity().getImplementationType() == ImplementationType.Manual) {
				applicationContext = activityInstance.getActivity()
						.getApplicationContext("default");
				activityJson.addProperty("implementation", "manual");

				JsonObject applicationContextJson = new JsonObject();

				applicationContextsJson.add("default", applicationContextJson);
			} else {
				activityJson.addProperty("implementation", "application");

				JsonObject applicationContextJson = new JsonObject();

				// TODO Handle others

				applicationContextsJson.add("externalWebApp",
						applicationContextJson);

				applicationContext = activityInstance.getActivity()
						.getApplicationContext("externalWebApp");

				applicationContextJson
						.addProperty(
								"carnot:engine:ui:externalWebApp:uri",
								(String) applicationContext
										.getAttribute("carnot:engine:ui:externalWebApp:uri"));
				applicationContextJson.addProperty("interactionId",
						Interaction.getInteractionId(activityInstance));
			}

			return activityInstanceJson;
		} finally {
			unwindEnvironment();
		}
	}

	/**
	 * 
	 * @return
	 */
	public JsonObject completeActivity(JsonObject activityInstanceJson) {
		try {
			prepareEnvironment();

			ActivityInstance activityInstance = getWorkflowService().complete(
					activityInstanceJson.get("oid").getAsLong(), "",
					new HashMap<String, Object>());

			// activityInstance.getActivity().g

			return activityInstanceJson;
		} finally {
			unwindEnvironment();
		}
	}

	/**
	 * 
	 * @return
	 */
	public JsonObject getProcessInstance(long oid) {
		try {
			prepareEnvironment();

			ProcessInstanceQuery processInstanceQuery = ProcessInstanceQuery.findAll();
			
			processInstanceQuery.where(ProcessInstanceQuery.OID
					.isEqual(oid));			

			processInstanceQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);

			ProcessInstanceDetails processInstance = (ProcessInstanceDetails) getQueryService()
					.getAllProcessInstances(processInstanceQuery).get(0);

			JsonObject processInstanceJson = new JsonObject();

			processInstanceJson.addProperty("processId",
					processInstance.getProcessID());
			processInstanceJson.addProperty("processName",
					processInstance.getProcessName());
			processInstanceJson.addProperty("startTimestamp", processInstance
					.getStartTime().getTime());

			if (processInstance.getTerminationTime() != null) {
				processInstanceJson.addProperty("terminationTimestamp",
						processInstance.getTerminationTime().getTime());
			}

			processInstanceJson.addProperty("state", processInstance.getState()
					.getName());
			processInstanceJson.addProperty("priority",
					processInstance.getPriority());

			JsonObject descriptorsJson = new JsonObject();

			processInstanceJson.add("descriptors", descriptorsJson);

			// Map descriptors

			for (String key : ((ProcessInstanceDetails) processInstance)
					.getDescriptors().keySet()) {
				Object value = ((ProcessInstanceDetails) processInstance)
						.getDescriptorValue(key);

				JsonObject descriptorJson = new JsonObject();

				descriptorsJson.add(key, descriptorJson);

				descriptorJson.addProperty("id", key);
				descriptorJson.addProperty("name", key);

				if (value == null) {
					descriptorJson.addProperty("value", (String) null);
				} else if (value instanceof Boolean) {
					descriptorJson.addProperty("value", (Boolean) value);

				} else if (value instanceof Character) {
					descriptorJson.addProperty("value", (Character) value);

				} else if (value instanceof Number) {
					descriptorJson.addProperty("value", (Number) value);

				} else {
					descriptorJson.addProperty("value", value.toString());
				}
			}

			JsonObject historicalEventsJson = new JsonObject();

			processInstanceJson.add("events", historicalEventsJson);

			for (HistoricalEvent historicalEvent : processInstance
					.getHistoricalEvents()) {
				JsonObject historicalEventJson = new JsonObject();

				historicalEventJson.addProperty("timestamp", historicalEvent
						.getEventTime().getTime());
				historicalEventJson.addProperty("type", historicalEvent
						.getEventType().getName());
			}

			// TODO Replace
			
			JsonObject participantsJson = new JsonObject();

			processInstanceJson.add("participants", participantsJson);

			UserQuery userQuery = UserQuery.findAll();

			for (User user : getQueryService().getAllUsers(userQuery))
			{
				JsonObject userJsonObject = new JsonObject();
				
				participantsJson.add(user.getId(), userJsonObject);
				
				userJsonObject.addProperty("id", user.getId());
				userJsonObject.addProperty("firstName", user.getFirstName());
				userJsonObject.addProperty("lastName", user.getLastName());
				userJsonObject.addProperty("name", user.getName());
				userJsonObject.addProperty("eMail", user.getEMail());
				userJsonObject.addProperty("description", user.getDescription());
			}
			
			return processInstanceJson;
		} finally {
			unwindEnvironment();
		}
	}
}
