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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.OrderCriteria;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
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
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;

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
	private ModelCache modelCache;
	private long temporaryOid = 0;

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
	private long getTemporaryOid() {
		--temporaryOid;

		return temporaryOid;
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
	 *
	 * @return
	 */
	private ModelCache getModelCache() {
		if (modelCache == null) {
			modelCache = ModelCache.findModelCache();
		}

		return modelCache;
	}

	/**
	 * 
	 * @return
	 */
	public JsonObject getModels() {
		JsonObject resultJson = new JsonObject();
		JsonArray modelsJson = new JsonArray();

		resultJson.add("models", modelsJson);

		for (DeployedModel deployedModel : getModelCache().getAllModels()) {
			if (!PredefinedConstants.PREDEFINED_MODEL_ID.equals(deployedModel
					.getId()) && deployedModel.isActive()) {

				JsonObject modelJson = new JsonObject();

				modelsJson.add(modelJson);

				modelJson.addProperty("oid", deployedModel.getElementOID());
				modelJson.addProperty("id", deployedModel.getId());
				modelJson.addProperty("name", deployedModel.getName());
				modelJson.addProperty("description",
						deployedModel.getDescription());

				JsonArray processDefinitionsJson = new JsonArray();

				modelJson.add("processDefinitions", processDefinitionsJson);

				for (ProcessDefinition processDefinition : (List<ProcessDefinition>) deployedModel
						.getAllProcessDefinitions()) {
					JsonObject processDefinitionJson = new JsonObject();

					processDefinitionsJson.add(processDefinitionJson);

					processDefinitionJson.addProperty("oid",
							processDefinition.getElementOID());
					processDefinitionJson.addProperty("id",
							processDefinition.getId());
					processDefinitionJson.addProperty("name",
							processDefinition.getName());
					processDefinitionJson.addProperty("description",
							processDefinition.getDescription());

					JsonArray activitiesJson = new JsonArray();

					processDefinitionJson.add("activities", activitiesJson);

					for (Activity activity : (List<Activity>) processDefinition
							.getAllActivities()) {
						JsonObject activityJson = new JsonObject();

						activitiesJson.add(activityJson);

						activityJson.addProperty("oid",
								activity.getElementOID());
						activityJson.addProperty("id", activity.getId());
						activityJson.addProperty("name", activity.getName());
						activityJson.addProperty("description",
								activity.getDescription());
					}
				}
			}
		}

		return resultJson;
	}

	/**
	 * Returns worklist.
	 * 
	 * TODO Needs to become a central service.
	 *
	 * @return
	 */
	public JsonObject getActivityInstances() {
		JsonObject resultJson = new JsonObject();
		JsonArray activityInstancesJson = new JsonArray();

		resultJson.add("activityInstances", activityInstancesJson);

		OrderCriteria ordering = new OrderCriteria();
		ordering.and(ActivityInstanceQuery.PROCESS_INSTANCE_OID).and(
				ActivityInstanceQuery.LAST_MODIFICATION_TIME);

		ActivityInstanceQuery query = ActivityInstanceQuery.findAll();

		query.orderBy(ordering);

		ActivityInstance referenceActivityInstance = null;
		Map<String, Activity> activityMap = new HashMap<String, Activity>();

		for (ActivityInstance activityInstance : getQueryService()
				.getAllActivityInstances(query)) {
			// Do not consider subprocesses

			if (activityInstance.getActivity().getImplementationType()
					.equals(ImplementationType.SubProcess)) {
				continue;
			}

			// Add planned Activity Instances for previous Process Instance

			if (referenceActivityInstance != null
					&& activityInstance.getProcessInstanceOID() != referenceActivityInstance
							.getProcessInstanceOID()) {
				addPlannedActivityInstances(
						getProcessDefinition(referenceActivityInstance
								.getActivity().getModelOID(),
								referenceActivityInstance
										.getProcessDefinitionId()),
						referenceActivityInstance.getProcessInstance(),
						referenceActivityInstance.getProcessInstance()
								.getRootProcessInstanceOID(),
						getStartTimeForNextActivity(referenceActivityInstance),
						activityMap, activityInstancesJson);
			}

			activityMap.put(activityInstance.getActivity().getId(),
					activityInstance.getActivity());

			referenceActivityInstance = activityInstance;

			activityInstancesJson
					.add(marshalActivityInstance(activityInstance));
		}

		// Add planned Activity Instances for previous Process Instance

		if (referenceActivityInstance != null) {
			addPlannedActivityInstances(
					getProcessDefinition(referenceActivityInstance
							.getActivity().getModelOID(),
							referenceActivityInstance.getProcessDefinitionId()),
					referenceActivityInstance.getProcessInstance(),
					referenceActivityInstance.getProcessInstance()
							.getRootProcessInstanceOID(),
					getStartTimeForNextActivity(referenceActivityInstance),
					activityMap,
					activityInstancesJson);
		}

		return resultJson;
	}

	/**
	 * 
	 * @param oid
	 */
	public ProcessDefinition getProcessDefinition(long modelOid,
			String processDefinitionId) {
		DeployedModel model = getModelCache().getModel(modelOid);
		return model.getProcessDefinition(processDefinitionId);
	};

	/**
	 * 
	 * @param oid
	 */
	public long getStartTimeForNextActivity(ActivityInstance activityInstance) {
		if (activityInstance.getState().equals(ActivityInstanceState.Suspended)) {
			return System.currentTimeMillis() + 60 * 60 * 1000; // TODO Add 1h
																// more as dummy
		} else if (activityInstance.getState().equals(
				ActivityInstanceState.Completed)) {
			// TODO Possible?

			return activityInstance.getLastModificationTime().getTime();
		} else {
			return System.currentTimeMillis();
		}
	}

	/**
	 * 
	 * @param oid
	 */
	public long addPlannedActivityInstances(
			ProcessDefinition processDefinition,
			ProcessInstance processInstance, long rootProcessInstanceOid,
			long nextStartTime, 
			Map<String, Activity> activityMap, JsonArray activityInstancesJson) {

		// TODO This loop mocks the traversal

		for (Activity activity : (List<Activity>) processDefinition
				.getAllActivities()) {
			if (!activityMap.containsKey(activity.getId())) {
				nextStartTime = createPlannedActivityInstance(activity,
						processInstance, rootProcessInstanceOid, nextStartTime,
						activityInstancesJson);

				activityMap.put(activity.getId(), activity);
			}
		}

		activityMap.clear();

		return nextStartTime;
	};

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

		ActivityInstance referenceActivityInstance = null;
		Map<String, Activity> activityMap = new HashMap<String, Activity>();

		for (ActivityInstance activityInstance : getQueryService()
				.getAllActivityInstances(
						ActivityInstanceQuery
								.findForProcessInstance(processInstance
										.getOID()))) {
			if (processInstance.getOID() != activityInstance
					.getProcessInstance().getOID()) {
				continue;
			}

			referenceActivityInstance = activityInstance;
			activityMap.put(activityInstance.getActivity().getId(),
					activityInstance.getActivity());

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

		addPlannedActivityInstances(
				getProcessDefinition(referenceActivityInstance.getActivity()
						.getModelOID(),
						referenceActivityInstance.getProcessDefinitionId()),
				referenceActivityInstance.getProcessInstance(),
				referenceActivityInstance.getProcessInstance()
						.getRootProcessInstanceOID(),
				getStartTimeForNextActivity(referenceActivityInstance),
				activityMap,
				activityInstancesJson);

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

	/**
	 * 
	 * @param activityInstance
	 * @return
	 */
	public JsonObject marshalActivityInstance(ActivityInstance activityInstance) {
		JsonObject activityInstanceJson = new JsonObject();

		activityInstanceJson.addProperty("oid", activityInstance.getOID());
		activityInstanceJson.addProperty("start", activityInstance
				.getStartTime().getTime());

		if (activityInstance.getLastModificationTime() != null) {
			activityInstanceJson.addProperty("lastModification",
					activityInstance.getLastModificationTime().getTime());
		}

		if (activityInstance.getState().equals(ActivityInstanceState.Completed)
				|| activityInstance.getState().equals(
						ActivityInstanceState.Aborted)) {
			activityInstanceJson.addProperty("end", activityInstance
					.getLastModificationTime().getTime());
		} else {
			activityInstanceJson.addProperty("end",
					System.currentTimeMillis() + 60 * 60 * 1000); // TODO Mock,
																	// has to
																	// come from
																	// Benchmark
																	// as
																	// maximum
																	// of
																	// current
																	// duration
																	// and
																	// expected
																	// duration
																	// + x
		}

		activityInstanceJson.addProperty("benchmark", "Criticality");
		activityInstanceJson.addProperty("criticality",
				calculateCriticality(activityInstance));

		activityInstanceJson.addProperty("state", activityInstance.getState()
				.getName());

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

		JsonObject processInstanceJson = new JsonObject();

		activityInstanceJson.add("processInstance", processInstanceJson);

		processInstanceJson.addProperty("oid", activityInstance
				.getProcessInstance().getOID());

		JsonObject processDefinitionJson = new JsonObject();

		processInstanceJson.add("processDefinition", processDefinitionJson);

		processDefinitionJson.addProperty("id", activityInstance
				.getProcessInstance().getProcessID());
		processDefinitionJson.addProperty("name", activityInstance
				.getProcessInstance().getProcessName());

		JsonObject rootProcessInstanceJson = new JsonObject();

		activityInstanceJson
				.add("rootProcessInstance", rootProcessInstanceJson);

		rootProcessInstanceJson.addProperty("oid", activityInstance
				.getProcessInstance().getRootProcessInstanceOID());

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

	/**
	 * 
	 * @param activity
	 * @param processInstance
	 *            Process Instance in which the Planned Activity instance is
	 *            created
	 * @param rootProcessInstanceOid
	 *            OID of the Root Process Instance in which the Planned Activity
	 *            instance is created
	 * @param nextStartTime
	 * @param criticality
	 *            Temporary
	 * @return
	 */
	public long createPlannedActivityInstance(Activity activity,
			ProcessInstance processInstance, long rootProcessInstanceOid,
			long nextStartTime,
			JsonArray activityInstancesJson) {
		JsonObject activityInstanceJson = new JsonObject();

		activityInstanceJson.addProperty("oid", getTemporaryOid());
		activityInstanceJson.addProperty("start", nextStartTime);

		if (activity.getImplementationType().equals(
				ImplementationType.SubProcess)) {
			JsonObject subProcessInstanceJson = new JsonObject();

			subProcessInstanceJson.addProperty("oid", getTemporaryOid());

			JsonObject processJson = new JsonObject();

			ProcessDefinition processDefinition = getProcessDefinition(
					processInstance.getModelOID(),
					activity.getImplementationProcessDefinitionId());
			subProcessInstanceJson.add("processDefinition", processJson);

			processJson.addProperty("name", processDefinition.getName());
			processJson.addProperty("id", processDefinition.getId());

			JsonArray subActivityInstancesJson = new JsonArray();

			subProcessInstanceJson.add("activityInstances",
					subActivityInstancesJson);

			nextStartTime = addPlannedActivityInstances(processDefinition,
					null, getTemporaryOid(), nextStartTime, 
					new HashMap<String, Activity>(), subActivityInstancesJson);

			activityInstanceJson.add("subProcessInstance",
					subProcessInstanceJson);
		} else {
			nextStartTime += 1000 * 60 * 60; // TODO Expected duration mocked by
												// adding 1h
		}

		activityInstanceJson.addProperty("end", nextStartTime);

		activityInstanceJson.addProperty("benchmark", "Criticality");
		activityInstanceJson.addProperty("criticality", calculateCriticality(null)); 
		activityInstanceJson.addProperty("state", "Planned");

		JsonObject activityJson = new JsonObject();

		activityInstanceJson.add("activity", activityJson);

		activityJson.addProperty("id", activity.getId());
		activityJson.addProperty("name", activity.getName());
		activityJson.addProperty("description", activity.getDescription());
		activityJson.addProperty("type", activity.getImplementationType()
				.getId());

		if (activity.getDefaultPerformer() != null) {
			JsonObject defaultPerformer = new JsonObject();

			activityJson.add("defaultPerformer", defaultPerformer);

			defaultPerformer.addProperty("id", activity.getDefaultPerformer()
					.getId());
			defaultPerformer.addProperty("name", activity.getDefaultPerformer()
					.getName());
			defaultPerformer.addProperty("description", activity
					.getDefaultPerformer().getDescription());
		}

		if (activity.getApplication() != null) {
			JsonObject applicationJson = new JsonObject();

			activityJson.add("application", applicationJson);

			applicationJson
					.addProperty("id", activity.getApplication().getId());
			applicationJson.addProperty("name", activity.getApplication()
					.getName());
		}

		JsonObject processInstanceJson = new JsonObject();

		activityInstanceJson.add("processInstance", processInstanceJson);

		JsonObject processDefinitionJson = new JsonObject();

		processInstanceJson.add("processDefinition", processDefinitionJson);

		if (processInstance != null) {
			processInstanceJson.addProperty("oid", processInstance.getOID());
			processDefinitionJson.addProperty("id",
					processInstance.getProcessID());
			processDefinitionJson.addProperty("name",
					processInstance.getProcessName());
		} else {
			processInstanceJson.addProperty("oid", getTemporaryOid());
			processDefinitionJson.addProperty("id",
					activity.getProcessDefinitionId());
			processDefinitionJson.addProperty("name",
					activity.getProcessDefinitionId()); // TODO Why is the name
														// not exposed?
		}

		JsonObject rootProcessInstanceJson = new JsonObject();

		activityInstanceJson
				.add("rootProcessInstance", rootProcessInstanceJson);

		rootProcessInstanceJson.addProperty("oid", rootProcessInstanceOid);

		activityInstancesJson.add(activityInstanceJson);

		return nextStartTime;
	}

	public double calculateCriticality(ActivityInstance activityInstance) {
		return 100 + System.currentTimeMillis() % 3 * 300;
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
	
	public JsonObject getProcessInstanceDocumentToken(long processInstanceOid,
            String dataPathId, String documentId)
{
     JsonObject resultJson = new JsonObject();
     String downloadToken = "";

     // Special case for PROCESS_ATTACHMENTS
     if (CommonProperties.PROCESS_ATTACHMENTS.equals(dataPathId))
     {
            downloadToken = getDocumentManagementService().requestDocumentContentDownload(documentId); 
     }
     // Handle other Document Data Paths
     else
     {
            // Find Process Definition for provided PI OID
         ProcessInstanceQuery processInstanceQuery = ProcessInstanceQuery.findAll();

         processInstanceQuery.where(ProcessInstanceQuery.OID.isEqual(processInstanceOid));
         ProcessInstanceDetails processInstance = (ProcessInstanceDetails) getQueryService().getAllProcessInstances(processInstanceQuery).get(0);

         ProcessDefinition processDefinition = getQueryService().getProcessDefinition(
                 processInstance.getModelOID(), processInstance.getProcessID());
         
         // Get Specific Documents for Process Definition
         @SuppressWarnings("unchecked")
         List<DataPath> dataPaths = (List<DataPath>) processDefinition.getAllDataPaths();
         
         String dataId;
         DeployedModel deployedModel; 
         Data data;
         for (DataPath dataPath : dataPaths)
         {
           dataId = dataPath.getData();
           deployedModel = ModelCache.findModelCache().getModel(processDefinition.getModelOID());
           data = deployedModel.getData(dataId);
           
           // Only consider data with type DOCUMENT_DATA (dmsDocument) with Direction.IN 
            if (!dataPath.getDirection().equals(Direction.OUT)
                  || !PredefinedConstants.DOCUMENT_DATA.equals(data.getTypeId()))
            {
               continue;
            }

            if (dataPath.getId().equals(dataPathId))
            {
                  Document doc = (Document) getWorkflowService().getInDataPath(processInstanceOid, dataPath.getId());
                  downloadToken = getDocumentManagementService().requestDocumentContentDownload(doc.getId());
            }
         }
     }
   
  resultJson.addProperty("downloadToken", downloadToken); 
     return resultJson;
}
}
