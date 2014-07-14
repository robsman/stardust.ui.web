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

package org.eclipse.stardust.ui.web.documenttriage.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.FolderInfo;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.ui.web.documenttriage.rest.JsonMarshaller;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;

public class DocumentTriageService {
	private static final Logger trace = LogManager
			.getLogger(DocumentTriageService.class);
	@Resource
	private SessionContext sessionContext;
	private DocumentManagementService documentManagementService;
	private UserService userService;
	private QueryService queryService;
	private WorkflowService workflowService;
	private AdministrationService administrationService;

	public DocumentTriageService() {
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
	 * @param json
	 * @return
	 */
	public JsonObject getPendingProcesses(JsonObject json) {
		ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
				.findAlive();

		JsonObject resultJson = new JsonObject();
		JsonArray processInstancesJson = new JsonArray();

		resultJson.add("processInstances", processInstancesJson);

		Map<Long, JsonObject> processInstancesMap = new HashMap<Long, JsonObject>();

		for (ActivityInstance activityInstance : getQueryService()
				.getAllActivityInstances(activityInstanceQuery)) {
			System.out.println("Activity Instance Performer");
			System.out.println(activityInstance.getParticipantPerformerID());

			if (activityInstance.getParticipantPerformerID() == null
					|| !activityInstance.getParticipantPerformerID().equals(
							"DocumentRendezvous")) {
				continue;
			}

			JsonObject processInstanceJson = processInstancesMap
					.get(activityInstance.getProcessInstanceOID());
			JsonArray pendingActivityInstances = null;

			if (processInstanceJson == null) {
				processInstanceJson = loadProcessInstance(activityInstance
						.getProcessInstanceOID());

				processInstancesMap.put(
						activityInstance.getProcessInstanceOID(),
						processInstanceJson);
				processInstancesJson.add(processInstanceJson);

				pendingActivityInstances = new JsonArray();

				processInstanceJson.add("pendingActivityInstances",
						pendingActivityInstances);
			} else {
				pendingActivityInstances = processInstanceJson.get(
						"pendingActivityInstances").getAsJsonArray();
			}

			JsonObject activityInstanceJson = new JsonObject();

			pendingActivityInstances.add(activityInstanceJson);

			activityInstanceJson.addProperty("oid", activityInstance.getOID());
			activityInstanceJson.addProperty("start", activityInstance
					.getStartTime().getTime());

			JsonObject activityJson = new JsonObject();

			activityInstanceJson.add("activity", activityJson);

			activityJson.addProperty("name", activityInstance.getActivity()
					.getName());
		}

		return resultJson;
	}

	private JsonObject loadProcessInstance(long oid) {
		ProcessInstance processInstance = getWorkflowService()
				.getProcessInstance(oid);
		JsonObject processInstanceJson = new JsonObject();

		processInstanceJson.addProperty("oid", processInstance.getOID());
		processInstanceJson.addProperty("start", processInstance.getStartTime()
				.getTime());

		if (processInstance.getTerminationTime() != null) {
			processInstanceJson.addProperty("end", processInstance
					.getTerminationTime().getTime());
		}

		ProcessDefinition processDefinition = getQueryService()
				.getProcessDefinition(processInstance.getModelOID(),
						processInstance.getProcessID());

		JsonObject processJson = new JsonObject();

		processInstanceJson.add("processDefinition", processJson);

		processJson.addProperty("name", processInstance.getProcessName());
		processJson.addProperty("id", processInstance.getProcessID());
		processJson.addProperty("description",
				processDefinition.getDescription());

		processInstanceJson.add("specificDocuments",
				getSpecificDocuments(processDefinition));

		JsonArray descriptorsJson = new JsonArray();

		processInstanceJson.add("descriptors", descriptorsJson);

		for (DataPath dataPath : processInstance.getDescriptorDefinitions()) {
			JsonObject descriptorJson = new JsonObject();

			descriptorsJson.add(descriptorJson);

			descriptorJson.addProperty("id", dataPath.getId());
			descriptorJson.addProperty("name", dataPath.getName());
			descriptorJson.addProperty("value", processInstance
					.getDescriptorValue(dataPath.getId()).toString());
		}

		processInstanceJson.add("processAttachments",
				getProcessAttachments(processInstance));

		return processInstanceJson;
	}

	/**
	 * TODO Reuse!!!
	 * 
	 * @param path
	 * @return
	 */
	public JsonArray getProcessAttachments(ProcessInstance processInstance) {
		String path = DocumentMgmtUtility
				.getProcessAttachmentsFolderPath(processInstance);
		Folder processAttFolder = getOrCreateFolder(path);

		processAttFolder = getDocumentManagementService().getFolder(
				processAttFolder.getId(), Folder.LOD_LIST_MEMBERS);

		JsonArray attachmentsJson = new JsonArray();

		for (Document attachment : (List<Document>) processAttFolder
				.getDocuments()) {
			JsonObject attachmentJson = new JsonObject();

			attachmentsJson.add(attachmentJson);

			attachmentJson.addProperty("name", attachment.getName());
			attachmentJson.addProperty("creationTimestamp", attachment.getDateCreated().getTime());
			attachmentJson.addProperty("contentType",
					attachment.getContentType());
			attachmentJson.addProperty("path", attachment.getPath());
			attachmentJson.addProperty("uuid", attachment.getId());
		}

		return attachmentsJson;
	}

	/**
	 * TODO Reuse!!!
	 * 
	 * @param path
	 * @return
	 */
	public Folder getOrCreateFolder(String path) {
		String[] pathSteps = path.split("/");

		String parentPath = "/";
		String currentPath = "";

		Folder folder = null;

		for (String pathStep : pathSteps) {
			if (pathStep.trim().length() == 0) {
				continue;
			}

			currentPath += "/";
			currentPath += pathStep;

			folder = getDocumentManagementService().getFolder(currentPath);

			if (folder == null) {
				FolderInfo folderInfo = DmsUtils.createFolderInfo(pathStep);

				folder = getDocumentManagementService().createFolder(
						parentPath, folderInfo);
			}

			parentPath = currentPath;
		}

		return folder;
	}

	/**
	 * 
	 * @param json
	 * @return
	 */
	public JsonObject completeRendezvous(JsonObject json) {
		ActivityInstance activityInstance = getWorkflowService()
				.getActivityInstance(
						json.get("pendingActivityInstance").getAsJsonObject()
								.get("oid").getAsLong());
		
		// TODO Evaluate data Mapping

		getWorkflowService().activateAndComplete(activityInstance.getOID(),
				null, null);

		return getPendingProcesses(null);
	}

	/**
	 * 
	 * @return
	 */
	public JsonObject getStartableProcesses() {
		JsonObject resultJson = new JsonObject();
		JsonArray processDefinitionsJson = new JsonArray();

		resultJson.add("processDefinitions", processDefinitionsJson);

		for (ProcessDefinition processDefinition : getWorkflowService()
				.getStartableProcessDefinitions()) {
			JsonObject processDefinitionJson = new JsonObject();

			processDefinitionsJson.add(processDefinitionJson);

			processDefinitionJson.addProperty("modelOid",
					processDefinition.getModelOID());
			processDefinitionJson.addProperty("id", processDefinition.getId());
			processDefinitionJson.addProperty("name",
					processDefinition.getName());

			processDefinitionJson.add("specificDocuments",
					getSpecificDocuments(processDefinition));
		}

		return resultJson;
	}

	/**
	 * 
	 * @return
	 */
	private JsonArray getSpecificDocuments(ProcessDefinition processDefinition) {
		JsonArray specificDocumentsJson = new JsonArray();

		for (DataPath dataPath : (List<DataPath>) processDefinition
				.getAllDataPaths()) {
			if (!dataPath.getDirection().equals(Direction.OUT)
					|| dataPath.getId().equals("PROCESS_ATTACHMENTS")) {
				continue;
			}

			JsonObject specificDocumentJson = new JsonObject();

			specificDocumentsJson.add(specificDocumentJson);

			specificDocumentJson.addProperty("name", dataPath.getName());
			specificDocumentJson.addProperty("type", dataPath.getMappedType()
					.getName());
		}

		return specificDocumentsJson;
	};

	/**
	 * 
	 * @param activityInstanceOid
	 * @return
	 */
	public JsonObject getProcessesAttachments(long activityInstanceOid) {
		JsonObject resultJson = new JsonObject();
		ActivityInstance activityInstance = getWorkflowService()
				.getActivityInstance(activityInstanceOid);
		ProcessInstance processInstance = getWorkflowService()
				.getProcessInstance(activityInstance.getProcessInstanceOID());

		resultJson.add("processAttachments",
				getProcessAttachments(processInstance));

		return resultJson;
	}

	/**
	 * 
	 * @param activityInstanceOid
	 * @return
	 */
	public JsonObject startProcess(JsonObject parameters) {
		getWorkflowService().startProcess(
				parameters.get("startableProcess").getAsJsonObject().get("id")
						.getAsString(), null, true);

		return parameters;
	}

}
