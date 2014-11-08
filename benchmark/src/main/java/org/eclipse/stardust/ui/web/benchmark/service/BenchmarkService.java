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

package org.eclipse.stardust.ui.web.benchmark.service;

import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
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
import org.eclipse.stardust.ui.web.benchmark.rest.JsonMarshaller;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

//import org.eclipse.stardust.ui.web.viewscommon.utils.XPathCacheManager;

public class BenchmarkService {
	private static final Logger trace = LogManager
			.getLogger(BenchmarkService.class);
	@Resource
	private SessionContext sessionContext;
	private final JsonMarshaller jsonIo = new JsonMarshaller();

	// @Resource(name = XPathCacheManager.BEAN_ID)
	// private XPathCacheManager xPathCacheManager;

	private DocumentManagementService documentManagementService;
	private UserService userService;
	private QueryService queryService;
	private WorkflowService workflowService;
	private AdministrationService administrationService;

	// TODO Removed as soon as real implementation is complete

	/**
    *
    */
	public BenchmarkService() {
	}

	/**
	 *
	 * @return
	 */
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
	 * Returns worklist.
	 *
	 * @return
	 */
	public JsonObject getActivityInstances() {
		JsonObject resultJson = new JsonObject();
		JsonArray activityInstancesJson = new JsonArray();

		resultJson.add("activityInstances", activityInstancesJson);

		ActivityInstanceQuery query = ActivityInstanceQuery.findAll();

		for (ActivityInstance activityInstance : getQueryService()
				.getAllActivityInstances(query)) {
			if (activityInstance.getActivity().getImplementationType()
					.equals(ImplementationType.SubProcess)) {
				continue;
			}

			JsonObject activityInstanceJson = new JsonObject();

			activityInstancesJson.add(activityInstanceJson);

			JsonObject activityJson = new JsonObject();

			activityInstanceJson.add("activity", activityJson);

			activityJson.addProperty("name", activityInstance.getActivity()
					.getName());
			activityJson.addProperty("id", activityInstance.getActivity()
					.getId());

			JsonObject processInstanceJson = new JsonObject();

			activityInstanceJson.add("processInstance", processInstanceJson);

			JsonObject processJson = new JsonObject();

			processInstanceJson.add("processDefinition", processJson);

			processJson.addProperty("id", activityInstance.getProcessInstance()
					.getProcessID());
			processJson.addProperty("name", activityInstance
					.getProcessInstance().getProcessName());

			processInstanceJson.addProperty("oid", activityInstance
					.getProcessInstance().getOID());
			processInstanceJson.addProperty("start", activityInstance
					.getProcessInstance().getStartTime().getTime());

			JsonObject rootProcessInstanceJson = new JsonObject();

			activityInstanceJson.add("rootProcessInstance",
					rootProcessInstanceJson);

			rootProcessInstanceJson.addProperty("oid", activityInstance
					.getProcessInstance().getRootProcessInstanceOID());

			activityInstanceJson.addProperty("oid", activityInstance.getOID());
			activityInstanceJson.addProperty("start", activityInstance
					.getStartTime().getTime());
			activityInstanceJson.addProperty("lastModification",
					activityInstance.getLastModificationTime().getTime());
			activityInstanceJson.addProperty("state", activityInstance
					.getState().toString());
			activityInstanceJson.addProperty("benchmark", "Criticality");
			activityInstanceJson.addProperty("criticality",
					activityInstance.getCriticality());

			if (activityInstance.getPerformedBy() != null) {
				JsonObject performedBy = new JsonObject();

				activityInstanceJson.add("performedBy", performedBy);

				performedBy.addProperty("name", activityInstance
						.getPerformedBy().getName());
			}

			if (activityInstance.getParticipantPerformerID() != null) {
				JsonObject participantPerformerJson = new JsonObject();				
				
				activityInstanceJson.add("participantPerformer", participantPerformerJson);
				
				participantPerformerJson.addProperty("id", activityInstance.getParticipantPerformerID());
				participantPerformerJson.addProperty("name", activityInstance.getParticipantPerformerName());
			}

			if (activityInstance.getUserPerformer() != null) {
				JsonObject userPerformer = new JsonObject();

				activityInstanceJson.add("userPerformer", userPerformer);

				userPerformer.addProperty("account", activityInstance
						.getUserPerformer().getAccount());
				userPerformer.addProperty("name", activityInstance
						.getUserPerformer().getName());
				userPerformer.addProperty("email", activityInstance
						.getUserPerformer().getEMail());
			}
		}

		return resultJson;
	}

	/**
	 * Returns worklist.
	 *
	 * @return
	 */
	public JsonObject getProcessInstance(long oid) {
		ProcessInstance processInstance = getWorkflowService()
				.getProcessInstance(oid);

		return traverseProcessInstanceHierarchy(processInstance);
	}

	/**
	 *
	 * @param activityInstanceOid
	 * @return
	 */
	public JsonObject traverseProcessInstanceHierarchy(
			ProcessInstance processInstance) {
		JsonObject processInstanceJson = new JsonObject();

		processInstanceJson.addProperty("oid", processInstance.getOID());
		processInstanceJson.addProperty("start", processInstance.getStartTime()
				.getTime());

		if (processInstance.getTerminationTime() != null) {
			processInstanceJson.addProperty("end", processInstance
					.getTerminationTime().getTime());
		}

		JsonObject processJson = new JsonObject();

		processInstanceJson.add("processDefinition", processJson);

		processJson.addProperty("name", processInstance.getProcessName());
		processJson.addProperty("id", processInstance.getProcessID());
		// processJson.addProperty("description", processInstance.get

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

		String path = DocumentMgmtUtility
				.getProcessAttachmentsFolderPath(processInstance);

		Folder processAttFolder = getOrCreateFolder(path);

		processAttFolder = getDocumentManagementService().getFolder(
				processAttFolder.getId(), Folder.LOD_LIST_MEMBERS);

		JsonArray attachmentsJson = new JsonArray();

		processInstanceJson.add("attachments", attachmentsJson);

		@SuppressWarnings("unchecked")
		List<Document> documents = (List<Document>) processAttFolder
				.getDocuments();
		for (Document attachment : documents) {
			JsonObject attachmentJson = new JsonObject();

			attachmentsJson.add(attachmentJson);

			attachmentJson.addProperty("name", attachment.getName());
			attachmentJson.addProperty("contentType",
					attachment.getContentType());
			attachmentJson.addProperty("path", attachment.getPath());
			attachmentJson.addProperty("uuid", attachment.getId());
		}

		JsonArray activityInstancesJson = new JsonArray();

		processInstanceJson.add("activityInstances", activityInstancesJson);

		for (ActivityInstance activityInstance : getQueryService()
				.getAllActivityInstances(
						ActivityInstanceQuery
								.findForProcessInstance(processInstance
										.getOID()))) {
			if (processInstance.getOID() != activityInstance
					.getProcessInstance().getOID()) {
				continue;
			}

			JsonObject activityInstanceJson = marshalActivityInstance(activityInstance);

			activityInstancesJson.add(activityInstanceJson);

			descriptorsJson = new JsonArray();

			activityInstanceJson.add("descriptors", descriptorsJson);

			for (DataPath dataPath : activityInstance
					.getDescriptorDefinitions()) {
				JsonObject descriptorJson = new JsonObject();

				descriptorsJson.add(descriptorJson);

				descriptorJson.addProperty("id", dataPath.getId());
				descriptorJson.addProperty("name", dataPath.getName());

				if (activityInstance.getDescriptorValue(dataPath.getId()) != null) {
					descriptorJson.addProperty("value", activityInstance
							.getDescriptorValue(dataPath.getId()).toString());
				}
			}
			// TODO Notes on activity level?

			marshalNotes(activityInstance.getProcessInstance(),
					activityInstanceJson);

			if (activityInstance.getActivity().getImplementationType()
					.equals(ImplementationType.SubProcess)) {
				ProcessInstanceQuery processInstanceQuery = ProcessInstanceQuery
						.findAll();
				processInstanceQuery.getFilter().add(
						ProcessInstanceQuery.STARTING_ACTIVITY_INSTANCE_OID
								.isEqual(activityInstance.getOID()));

				for (ProcessInstance subProcessInstance : getQueryService()
						.getAllProcessInstances(processInstanceQuery)) {
					activityInstanceJson
							.add("subProcessInstance",
									traverseProcessInstanceHierarchy(subProcessInstance));

					break;
				}
			}
		}

		return processInstanceJson;
	}

	/**
	 *
	 * @return
	 */
	public void marshalNotes(ProcessInstance processInstance,
			JsonObject containerJson) {
		ProcessInstance scopeProcessInstance = getWorkflowService()
				.getProcessInstance(
						processInstance.getScopeProcessInstance().getOID());
		ProcessInstanceAttributes attributes = scopeProcessInstance
				.getAttributes();

		if (null != attributes) {
			JsonArray notesJson = new JsonArray();

			containerJson.add("notes", notesJson);

			for (Note note : attributes.getNotes()) {
				JsonObject noteJson = new JsonObject();

				notesJson.add(noteJson);

				noteJson.addProperty("context", note.getContextKind().getName());
				noteJson.addProperty("text", note.getText());
				noteJson.addProperty("time", note.getTimestamp().getTime());
				noteJson.addProperty("user", note.getUser().getName());
			}
		}
	}

	public JsonObject marshalActivityInstance(ActivityInstance activityInstance) {
		JsonObject activityInstanceJson = new JsonObject();

		activityInstanceJson.addProperty("oid", activityInstance.getOID());
		activityInstanceJson.addProperty("start", activityInstance
				.getStartTime().getTime());

		if (activityInstance.getLastModificationTime() != null) {
			activityInstanceJson.addProperty("lastModification",
					activityInstance.getLastModificationTime().getTime());
		}

		activityInstanceJson.addProperty("state", activityInstance.getState()
				.getValue());

		if (activityInstance.getPerformedBy() != null) {
			JsonObject performedBy = new JsonObject();

			activityInstanceJson.add("performedBy", performedBy);

			performedBy.addProperty("name", activityInstance.getPerformedBy()
					.getName());
		}

		if (activityInstance.getUserPerformer() != null) {
			JsonObject userPerformer = new JsonObject();

			activityInstanceJson.add("userPerformer", userPerformer);

			userPerformer.addProperty("account", activityInstance
					.getUserPerformer().getAccount());
			userPerformer.addProperty("name", activityInstance
					.getUserPerformer().getName());
			userPerformer.addProperty("email", activityInstance
					.getUserPerformer().getEMail());
		}

		JsonObject activityJson = new JsonObject();

		activityInstanceJson.add("activity", activityJson);

		activityJson.addProperty("id", activityInstance.getActivity().getId());
		activityJson.addProperty("name", activityInstance.getActivity()
				.getName());
		activityJson.addProperty("description", activityInstance.getActivity()
				.getDescription());
		activityJson.addProperty("type", activityInstance.getActivity()
				.getImplementationType().getId());

		if (activityInstance.getActivity().getDefaultPerformer() != null) {
			JsonObject defaultPerformer = new JsonObject();

			activityJson.add("defaultPerformer", defaultPerformer);

			defaultPerformer.addProperty("id", activityInstance.getActivity()
					.getDefaultPerformer().getId());
			defaultPerformer.addProperty("name", activityInstance.getActivity()
					.getDefaultPerformer().getName());
			defaultPerformer.addProperty("description", activityInstance
					.getActivity().getDefaultPerformer().getDescription());
		}

		if (activityInstance.getActivity().getApplication() != null) {
			JsonObject applicationJson = new JsonObject();

			activityJson.add("application", applicationJson);

			applicationJson.addProperty("id", activityInstance.getActivity()
					.getApplication().getId());
			applicationJson.addProperty("name", activityInstance.getActivity()
					.getApplication().getName());
		}

		// Quality Control

		if (activityInstance.getQualityAssuranceInfo() != null
				&& activityInstance.getQualityAssuranceInfo()
						.getMonitoredInstance() != null) {
			JsonObject monitoredActivityInstanceJson = marshalActivityInstance(activityInstance
					.getQualityAssuranceInfo().getMonitoredInstance());

			activityInstanceJson.add("monitoredActivityInstance",
					monitoredActivityInstanceJson);
			JsonObject qualityControlPerformerJson = new JsonObject();

			activityInstanceJson.add("qualityControlPerformer",
					qualityControlPerformerJson);

			qualityControlPerformerJson.addProperty("account", activityInstance
					.getQualityAssuranceInfo().getMonitoredInstance()
					.getPerformedBy().getId());
			qualityControlPerformerJson.addProperty("name", activityInstance
					.getQualityAssuranceInfo().getMonitoredInstance()
					.getPerformedBy().getName());
		}

		return activityInstanceJson;
	}

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
}
