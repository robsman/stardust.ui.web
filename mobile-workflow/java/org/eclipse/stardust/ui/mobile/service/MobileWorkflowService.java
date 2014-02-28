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

import java.io.*;
import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.*;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.interactions.Interaction;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.ui.mobile.rest.JsonMarshaller;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.*;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.TypedDocument;

public class MobileWorkflowService {
	private ServiceFactory serviceFactory;
	private UserService userService;
	private QueryService queryService;
	private WorkflowService workflowService;
	private DocumentManagementService documentManagementService;
	private User loginUser;
	private Folder userDocumentsRootFolder;
	private Folder publicDocumentsRootFolder;

	public MobileWorkflowService() {
		super();

		new JsonMarshaller();
	}

	/**
	 * 
	 * @return
	 */
	private ServiceFactory getServiceFactory() {
		return serviceFactory;
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
	private DocumentManagementService getDocumentManagementService() {
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
	private User getLoginUser() {
		return loginUser;
	}

	/**
	 * 
	 * @return
	 */
	public JsonObject login(JsonObject credentialsJson) {
		Map<String, String> credentials = new HashMap<String, String>();

		String partition = credentialsJson.get("partition").getAsString();

		credentials.put(SecurityProperties.PARTITION, partition);

		System.out.println("Partition: " + partition);

		serviceFactory = ServiceFactoryLocator.get(
				credentialsJson.get("account").getAsString(), credentialsJson
						.get("password").getAsString(), credentials);
		loginUser = getServiceFactory().getWorkflowService().getUser();

		JsonObject userJson = marshalUser(loginUser);

		userDocumentsRootFolder = (Folder) getDocumentManagementService()
				.getFolder(getUserDocumentsRootFolderPath(),
						Folder.LOD_LIST_MEMBERS);
		publicDocumentsRootFolder = (Folder) getDocumentManagementService()
				.getFolder(getPublicDocumentsRootFolderPath(),
						Folder.LOD_LIST_MEMBERS);

		return userJson;
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

			processDefinitionJson.addProperty("id", processDefinition.getId());
			processDefinitionJson.addProperty("name",
					processDefinition.getName());
			processDefinitionJson.addProperty("description",
					processDefinition.getDescription());
		}

		return resultJson;
	}

   /**
    * 
    * @return
    */
   public JsonObject getWorklistCount() {
      JsonObject resultJson = new JsonObject();
      
      resultJson.addProperty("total", 100);
      
      return resultJson;
   }
   
	/**
	 * 
	 * @return
	 */
	public JsonObject getWorklist() {
		JsonObject resultJson = new JsonObject();
		JsonArray worklistJson = new JsonArray();

		resultJson.add("worklist", worklistJson);

		for (ActivityInstance activityInstance : (List<ActivityInstance>) getWorkflowService()
				.getWorklist(WorklistQuery.findCompleteWorklist())
				.getCumulatedItems()) {
			JsonObject activityInstanceJson = new JsonObject();

			worklistJson.add(activityInstanceJson);

			long timeInMillis = Calendar.getInstance().getTimeInMillis();
			if (activityInstance.getState() == ActivityInstanceState.Completed
					|| activityInstance.getState() == ActivityInstanceState.Aborted) {
				timeInMillis = activityInstance.getLastModificationTime().getTime();
			}
			long duration = timeInMillis - activityInstance.getStartTime().getTime();
			
			String lastPerformer;
			UserInfo userInfo = activityInstance.getPerformedBy();
			if (null != userInfo) {
			     User user = UserUtils.getUser(userInfo.getId());
			     lastPerformer = I18nUtils.getUserLabel(user);
			}
			else {
				lastPerformer = activityInstance.getPerformedByName();
			}

			activityInstanceJson.addProperty("oid", activityInstance.getOID());
			activityInstanceJson.addProperty("criticality", activityInstance.getCriticality());
			activityInstanceJson.addProperty("status", activityInstance.getState().getName()); // TODO: i18n
			
			activityInstanceJson.addProperty("lastPerformer", lastPerformer);
			activityInstanceJson.addProperty("assignedTo", "motu" /*ActivityInstanceUtils.getAssignedToLabel(activityInstance)*/); // TODO
			activityInstanceJson.addProperty("duration", duration);
			activityInstanceJson.addProperty("activityId", activityInstance
					.getActivity().getId());
			activityInstanceJson.addProperty("activityName", activityInstance
					.getActivity().getName());
			activityInstanceJson.addProperty("processId", activityInstance
					.getActivity().getProcessDefinitionId());
			activityInstanceJson.addProperty("processName", activityInstance
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
						(String) activityInstance.getDescriptorValue(dataPath
								.getId()));
			}
		}

		return resultJson;
	}

	/**
	 * 
	 * @return
	 */
	public JsonObject activateActivity(long activityInstanceOid) {
      ActivityInstance activityInstance = getWorkflowService().activate(activityInstanceOid);

		JsonObject activityJson = getActivityInstance(activityInstanceOid);

		return activityJson;
	}

	/**
	 *
	 * @return
	 */
	public JsonObject completeActivity(JsonObject activityInstanceJson, JsonObject outDataJson) {
		Map<String, Object> accMap = new HashMap<String, Object>();
		JsonObject accObj = outDataJson.get("accident").getAsJsonObject();
		accMap.put("CarsInvolvedInAccident", accObj.get("numVehicles").getAsInt());
		accMap.put("AccidentLocation", accObj.get("location").getAsString());
		accMap.put("YourVehicleTowed", accObj.get("vehicleTowed").getAsBoolean());
		JsonObject damagesObj = accObj.get("damageLocs").getAsJsonObject();
		Map<String, Object> damagesMap = new HashMap<String, Object>();
		damagesMap.put("Undercarriage", damagesObj.get("underCarriage").getAsBoolean());
		damagesMap.put("Rear", damagesObj.get("rear").getAsBoolean());
		damagesMap.put("DriverSide", damagesObj.get("driverSide").getAsBoolean());
		damagesMap.put("Front", damagesObj.get("front").getAsBoolean());
		damagesMap.put("PassengerSide", damagesObj.get("passSide").getAsBoolean());
		damagesMap.put("Hood", damagesObj.get("hood").getAsBoolean());
		accMap.put("WhereVehicleDamaged", damagesMap);

		Map<String, Object> outDataMap = new HashMap<String, Object>();
		outDataMap.put("AccidentInformation", accMap);
		ActivityInstance activityInstance = getWorkflowService().complete(
				activityInstanceJson.get("oid").getAsLong(), "default",
				outDataMap);

		activityInstanceJson = new JsonObject();
		return activityInstanceJson;
	}

   /**
    * 
    * @return
    */
   public JsonObject getActivityInstance(long activityInstanceOid) {
      JsonObject activityInstanceJson = new JsonObject();

      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery.findAll();

      activityInstanceQuery.where(ActivityInstanceQuery.OID.isEqual(activityInstanceOid));
      activityInstanceQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      
      ActivityInstanceDetails activityInstance = (ActivityInstanceDetails) getQueryService()
            .getAllActivityInstances(activityInstanceQuery).get(0);

      long timeInMillis = Calendar.getInstance().getTimeInMillis();
      if (activityInstance.getState() == ActivityInstanceState.Completed
            || activityInstance.getState() == ActivityInstanceState.Aborted) {
         timeInMillis = activityInstance.getLastModificationTime().getTime();
      }
      long duration = timeInMillis - activityInstance.getStartTime().getTime();
      
      String lastPerformer;
      UserInfo userInfo = activityInstance.getPerformedBy();
      if (null != userInfo) {
           User user = UserUtils.getUser(userInfo.getId());
           lastPerformer = I18nUtils.getUserLabel(user);
      }
      else {
         lastPerformer = activityInstance.getPerformedByName();
      }

      activityInstanceJson.addProperty("oid", activityInstance.getOID());
      activityInstanceJson.addProperty("criticality", activityInstance.getCriticality());
      activityInstanceJson.addProperty("status", activityInstance.getState().getName()); // TODO: i18n
      
      activityInstanceJson.addProperty("lastPerformer", lastPerformer);
      activityInstanceJson.addProperty("assignedTo", "motu" /*ActivityInstanceUtils.getAssignedToLabel(activityInstance)*/);
      activityInstanceJson.addProperty("duration", duration);
      activityInstanceJson.addProperty("activityId", activityInstance
            .getActivity().getId());
      activityInstanceJson.addProperty("activityName", activityInstance
            .getActivity().getName());
      activityInstanceJson.addProperty("processId", activityInstance
            .getActivity().getProcessDefinitionId());
      activityInstanceJson.addProperty("processName", activityInstance
            .getActivity().getProcessDefinitionId());
      activityInstanceJson.addProperty("processInstanceOid",
            activityInstance.getProcessInstanceOID());
      activityInstanceJson.addProperty("startTime", activityInstance
            .getStartTime().getTime());
      activityInstanceJson.addProperty("lastModificationTime",
            activityInstance.getLastModificationTime().getTime());
      activityInstanceJson.addProperty("activatable",
            ActivityInstanceUtils.isActivatable(activityInstance) && SpiUtils.getInteractionController(activityInstance.getActivity()) == SpiUtils.DEFAULT_EXTERNAL_WEB_APP_CONTROLLER);

      /*JsonObject descriptorsJson = new JsonObject();

      activityInstanceJson.add("descriptors", descriptorsJson);
      
      for (DataPath dataPath : activityInstance
            .getDescriptorDefinitions()) {
         
         descriptorsJson.addProperty(dataPath.getId(),
               (String) activityInstance.getDescriptorValue(dataPath
                     .getId()));
      }*/
      
      if (activityInstance.getState().getValue() == ActivityInstanceState.APPLICATION) {
      
      ApplicationContext applicationContext = null;
      JsonObject applicationContextsJson = new JsonObject();

      activityInstanceJson.add("contexts", applicationContextsJson);

      if (activityInstance.getActivity().getImplementationType() == ImplementationType.Manual) {
         applicationContext = activityInstance.getActivity()
               .getApplicationContext("default");
         activityInstanceJson.addProperty("implementation", "manual");

         JsonObject applicationContextJson = new JsonObject();

         applicationContextsJson.add("default", applicationContextJson);
      } else {
         activityInstanceJson.addProperty("implementation", "application");

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
      }
      
      JsonObject processInstanceJson = new JsonObject();
      ProcessInstanceQuery processInstanceQuery = ProcessInstanceQuery.findAll();

      processInstanceQuery.where(ProcessInstanceQuery.OID.isEqual(activityInstance.getProcessInstanceOID()));
      processInstanceQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      
      ProcessInstanceDetails processInstance = (ProcessInstanceDetails) getQueryService()
            .getAllProcessInstances(processInstanceQuery).get(0);
      
      activityInstanceJson.add("processInstance", processInstanceJson);

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

      JsonArray documentsJson = new JsonArray();
      processInstanceJson.add("documents", documentsJson);
      List<Document> processAttachments = fetchProcessAttachments(processInstance);
//    List<TypedDocument> typedDocuments = getTypeDocuments(processInstance);
      for (Document document : processAttachments) {
         documentsJson.add(marshalDocument(document));
      }

      JsonArray notesJson = new JsonArray();

      processInstanceJson.add("notes", notesJson);

      for (Note note : processInstance.getAttributes().getNotes()) {
         JsonObject noteJson = marshalNote(note);

         notesJson.add(noteJson);
      }

      return activityInstanceJson;
   }
   
   public JsonObject startProcessInstance(JsonObject request)
   {
      ProcessInstance processInstance = getWorkflowService().startProcess(request.get("processDefinitionId").getAsString(), null, true);
      
      JsonObject processInstanceJson = getProcessInstance(processInstance.getOID());
      
      if (!(ProcessInstanceUtils.isTransientProcess(processInstance) || ProcessInstanceUtils
            .isCompletedProcess(processInstance)))
      {
         ActivityInstance nextActivityInstance = getWorkflowService()
               .activateNextActivityInstanceForProcessInstance(processInstance.getOID());
         if (nextActivityInstance != null)
         {
            JsonObject activityInstanceJson = getActivityInstance(nextActivityInstance.getOID());
            
            processInstanceJson.add("activatedActivityInstance", activityInstanceJson);
         }
      }
      
      return processInstanceJson;
   }

   /**
	 * 
	 * @return
	 */
	public JsonObject getProcessInstance(long oid) {
		ProcessInstanceQuery processInstanceQuery = ProcessInstanceQuery
				.findAll();

		processInstanceQuery.where(ProcessInstanceQuery.OID.isEqual(oid));
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
		processInstanceJson.add("startingUser", marshalUser(processInstance.getStartingUser()));

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
		
		JsonArray documentsJson = new JsonArray();
		processInstanceJson.add("documents", documentsJson);
		List<Document> processAttachments = fetchProcessAttachments(processInstance);
//		List<TypedDocument> typedDocuments = getTypeDocuments(processInstance);
		for (Document document : processAttachments) {
			documentsJson.add(marshalDocument(document));
		}

		JsonArray notesJson = new JsonArray();

		processInstanceJson.add("notes", notesJson);

		for (Note note : processInstance.getAttributes().getNotes()) {
			JsonObject noteJson = marshalNote(note);

			notesJson.add(noteJson);
		}

		// TODO Replace

		JsonObject participantsJson = new JsonObject();

		processInstanceJson.add("participants", participantsJson);

		UserQuery userQuery = UserQuery.findAll();

		for (User user : getQueryService().getAllUsers(userQuery)) {
			participantsJson.add(user.getId(), marshalUser(user));
		}

		return processInstanceJson;
	}

   /**
    * returns IN data paths details associated with provided process instance data in
    * Association between IN datapath and corresponding OUT datapath is determined by
    * comparing document data in different datapaths datapaths having duplicate document
    * data will be ignored
    * 
    * @param processInstance
    * @return
    */
   public List<TypedDocument> getTypeDocuments(ProcessInstance processInstance)
   {
      DeployedModel model = getQueryService().getModel(processInstance.getModelOID());
      ProcessDefinition processDefinition = model.getProcessDefinition(processInstance
            .getProcessID());

      Map<String, TypedDocument> typedDocumentsData = new HashMap<String, TypedDocument>();
      Map<String, DataPath> outDataMappings = new HashMap<String, DataPath>();
      @SuppressWarnings("rawtypes")
      List dataPaths = processDefinition.getAllDataPaths();

      TypedDocument typedDocument;
      String dataDetailsQId;

      for (Object objectDataPath : dataPaths)
      {
         DataPath dataPath = (DataPath) objectDataPath;
         DataDetails dataDetails = (DataDetails) model.getData(dataPath.getData());
         if (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataDetails.getTypeId()))
         {
            dataDetailsQId = dataDetails.getQualifiedId();
            Direction direction = dataPath.getDirection();
            if (Direction.IN.equals(direction)
                  && !typedDocumentsData.containsKey(dataDetailsQId))
            {
               try
               {
                  typedDocument = new TypedDocument(processInstance, dataPath,
                        dataDetails);
                  if (outDataMappings.containsKey(dataDetailsQId))
                  {
                     typedDocument.setDataPath(outDataMappings.get(dataDetailsQId));
                     typedDocument.setOutMappingExist(true);
                  }
                  typedDocumentsData.put(dataDetailsQId, typedDocument);
               }
               catch (Exception e)
               {
                  System.out.println(e);
               }
            }
            else if (Direction.OUT.equals(direction))
            {
               if (typedDocumentsData.containsKey(dataDetailsQId))
               {
                  typedDocument = typedDocumentsData.get(dataDetailsQId);
                  if (!typedDocument.isOutMappingExist())
                  {
                     typedDocument.setDataPath(dataPath);
                     typedDocument.setOutMappingExist(true);
                  }
               }
               else
               {
                  outDataMappings.put(dataDetailsQId, dataPath);
               }
            }
         }
      }

      return new ArrayList<TypedDocument>((typedDocumentsData.values()));
   }

   /**
    * 
    * @return
    */
   public JsonObject getNotes(long processInstanceOid)
   {
      ProcessInstance processInstance = getWorkflowService().getProcessInstance(
            processInstanceOid);
      JsonObject resultJson = new JsonObject();
      JsonArray notesJson = new JsonArray();

      resultJson.add("notes", notesJson);

      for (Note note : processInstance.getAttributes().getNotes())
      {
         notesJson.add(marshalNote(note));
      }

      return resultJson;
   }

   /**
    * 
    * @return
    */
   public JsonObject getProcessInstanceDocuments(long processInstanceOid)
   {
      ProcessInstance processInstance = getWorkflowService().getProcessInstance(
            processInstanceOid);
      JsonObject resultJson = new JsonObject();
      JsonArray documentsJson = new JsonArray();

      List<Document> processAttachments = fetchProcessAttachments(processInstance);
      // List<TypedDocument> typedDocuments =
      // TypedDocumentsUtil.getTypeDocuments(processInstance);
      resultJson.add("documents", documentsJson);

      for (Document document : processAttachments)
      {
         documentsJson.add(marshalDocument(document));
      }

      return resultJson;
   }

   public JsonObject getProcessInstanceDocument(long processInstanceOid, String documentId)
   {
      Document document = getDocumentManagementService().getDocument(documentId);
      
      JsonObject documentJson = marshalDocument(document);
      
      return documentJson;
   }
   
	/**
	 * 
	 * @return
	 */
	public JsonObject getRepositoryFolder(String folderId) {
		JsonObject folderJson = new JsonObject();
		JsonObject childrenJson = new JsonObject();
		JsonArray subFoldersJson = new JsonArray();
      JsonArray documentsJson = new JsonArray();

      childrenJson.add("folders", subFoldersJson);
      childrenJson.add("documents", documentsJson);
		folderJson.add("children", childrenJson);

		if (folderId == null || folderId.equals("") || folderId.equals("root")) {
	      folderJson.addProperty("id", "root");
	      folderJson.addProperty("name", "Root");
	      folderJson.addProperty("path", "/");
	      
			JsonObject subFolderJson = new JsonObject();

			subFoldersJson.add(subFolderJson);

			subFolderJson.addProperty("id", publicDocumentsRootFolder.getId());
			subFolderJson.addProperty("path",
					publicDocumentsRootFolder.getPath());

//			getFolderContent(subFolderJson, publicDocumentsRootFolder);

			// Overwrite name
			
			subFolderJson.addProperty("name", "Common Documents");

			if (userDocumentsRootFolder != null) {
				subFolderJson = new JsonObject();

				subFoldersJson.add(subFolderJson);

				subFolderJson
						.addProperty("id", userDocumentsRootFolder.getId());
				subFolderJson.addProperty("path",
						userDocumentsRootFolder.getPath());

//				getFolderContent(subFolderJson, userDocumentsRootFolder);

				// Overwrite name 
				
				subFolderJson.addProperty("name", "My Documents");
			}
		} else {
			getFolderContent(folderJson, getDocumentManagementService()
					.getFolder(folderId, Folder.LOD_LIST_MEMBERS));
		}

		return folderJson;
	}

	/**
	 * 
	 * @param folderJson
	 * @param folder
	 * @return
	 */
	private JsonObject getFolderContent(JsonObject folderJson, Folder folder) {
		folderJson.addProperty("id", folder.getId());
		folderJson.addProperty("name", folder.getName());
		folderJson.addProperty("path", folder.getPath());
		
		JsonArray subFoldersJson = null;

		/*if (!folderJson.has("subFolders")) {
			folderJson.add("subFolders", subFoldersJson = new JsonArray());
		} else {
			subFoldersJson = folderJson.get("subFolders").getAsJsonArray();
		}*/

      subFoldersJson = folderJson.get("children").getAsJsonObject().get("folders").getAsJsonArray();
		
		for (Folder subFolder : (List<Folder>) folder.getFolders()) {
			JsonObject subFolderJson = new JsonObject();

			subFoldersJson.add(subFolderJson);

			subFolderJson.addProperty("id", subFolder.getId());
			subFolderJson.addProperty("name", subFolder.getName());
			subFolderJson.addProperty("path", subFolder.getPath());
		}

		JsonArray documentsJson = null;

		/*if (!folderJson.has("documents")) {
			folderJson.add("documents", documentsJson = new JsonArray());
		} else {
			documentsJson = folderJson.get("documents").getAsJsonArray();
		}*/
		documentsJson = folderJson.get("children").getAsJsonObject().get("documents").getAsJsonArray();

		for (Document document : (List<Document>) folder.getDocuments()) {
			JsonObject documentJson = marshalDocument(document);

			documentsJson.add(documentJson);
		}

		return folderJson;
	};

	private String getUserDocumentsRootFolderPath() {
		return "/realms/" + getLoginUser().getRealm().getId() + "/users/"
				+ getLoginUser().getAccount() + "/documents";
	}

	private String getPublicDocumentsRootFolderPath() {
		return "/documents";
	}

	/**
	 * 
	 * @param request
	 */
	public JsonObject createNote(JsonObject request) {
		ProcessInstance processInstance = getWorkflowService()
				.getProcessInstance(
						request.get("processInstanceOid").getAsLong());
		ProcessInstanceAttributes attributes = processInstance.getAttributes();

		Note note = attributes.addNote(request.get("content").getAsString(),
				ContextKind.ProcessInstance, processInstance.getOID());
		getWorkflowService().setProcessInstanceAttributes(attributes);

		JsonObject noteJson = new JsonObject();

		noteJson.addProperty("content", note.getText());
		noteJson.addProperty("timestamp", new Date().getTime());

		return noteJson;
	}

	/**
	 * 
	 * @param request
	 */
	private JsonObject marshalNote(Note note) {
		JsonObject noteJson = new JsonObject();

		noteJson.addProperty("content", note.getText());
		noteJson.addProperty("timestamp", note.getTimestamp().getTime());

		JsonObject userJson = marshalUser(note.getUser());
		noteJson.add("user", userJson);

		return noteJson;
	}

   /**
    * 
    * @param request
    */
   private JsonObject marshalUser(User user) {
      JsonObject userJson = new JsonObject();

      userJson.addProperty("id", user.getId());
      userJson.addProperty("firstName", user.getFirstName());
      userJson.addProperty("lastName", user.getLastName());
      userJson.addProperty("name", user.getName());
      userJson.addProperty("eMail", user.getEMail());
      userJson.addProperty("description", user.getDescription());

      return userJson;
   }

	/**
	 * 
	 * @param request
	 */
	private JsonObject marshalDocument(Document document) {
		JsonObject documentJson = new JsonObject();

		documentJson.addProperty("id", document.getId());
		documentJson.addProperty("name", document.getName());
		documentJson.addProperty("contentType", document.getContentType());
      documentJson.addProperty("createdTimestamp", document.getDateCreated().getTime());
		documentJson.addProperty("lastModifiedTimestamp", document.getDateLastModified().getTime());
		documentJson.addProperty("size", document.getSize());
      documentJson.addProperty("downloadToken",
            getDocumentManagementService()
                  .requestDocumentContentDownload(document.getId()));

		return documentJson;
	}

   /**
    * @param pi
    * @return
    */
   private List<Document> fetchProcessAttachments(ProcessInstance processInstance)
   {
      List<Document> processAttachments = new ArrayList<Document>();

      DeployedModel model = getQueryService().getModel(processInstance.getModelOID());
      ProcessDefinition processDefinition = model.getProcessDefinition(processInstance.getProcessID()) ;
      List dataPaths = processDefinition.getAllDataPaths();
      
      for (int n = 0; n < dataPaths.size(); ++n)
      {
         DataPath dataPath = (DataPath) dataPaths.get(n);

         if (!dataPath.getDirection().equals(Direction.IN))
         {
            continue;
         }

         try
         {
            if (dataPath.getId().equals(CommonProperties.PROCESS_ATTACHMENTS))
            {
               Object object = getWorkflowService().getInDataPath(processInstance.getOID(), dataPath.getId());

               if (object != null)
               {
                  processAttachments.addAll((Collection) object);
                  break;
               }
            }
         }
         catch (Exception e)
         {
            System.out.println("Error fetching Process Attachments: " + e.getMessage());
         }
      }
      
      return processAttachments;
   }

	public void addProcessAttachment(long processOid, File uploadedFile) {
		  ProcessInstance processInstance = getWorkflowService().getProcessInstance(processOid);
	      List<Document> processAttachments = fetchProcessAttachments(processInstance);

	        FileInputStream fis = null;
			try {
				fis = new FileInputStream(uploadedFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        //System.out.println(file.exists() + "!!");
	        //InputStream in = resource.openStream();
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        byte[] buf = new byte[1024];
	        try {
	            for (int readNum; (readNum = fis.read(buf)) != -1;) {
	                bos.write(buf, 0, readNum); //no doubt here is 0
	                //Writes len bytes from the specified byte array starting at offset off to this byte array output stream.
	                System.out.println("read " + readNum + " bytes,");
	            }
	        } catch (IOException ex) {
	            System.out.println(ex);
	        }
	        byte[] bytes = bos.toByteArray();	      
	        
	      DocumentInfo docInfo = DmsUtils.createDocumentInfo(uploadedFile.getName());
	      String folderPath = DocumentMgmtUtility.getProcessAttachmentsFolderPath(processInstance);
	      createFolderIfNotExists(folderPath);
	      Document document = getDocumentManagementService().createDocument(folderPath, docInfo, bytes, "");
	      
	      processAttachments.add(document);
	      getWorkflowService().setOutDataPath(processInstance.getOID(),
	              CommonProperties.PROCESS_ATTACHMENTS, processAttachments);
	}

   /**
    * Returns the folder if exist otherwise create new folder
    * 
    * @param folderPath
    * @return
    */
   private Folder createFolderIfNotExists(String folderPath)
   {
      Folder folder = getDocumentManagementService().getFolder(folderPath,
            Folder.LOD_NO_MEMBERS);

      if (null == folder)
      {
         // folder does not exist yet, create it
         String parentPath = folderPath.substring(0, folderPath.lastIndexOf('/'));
         String childName = folderPath.substring(folderPath.lastIndexOf('/') + 1);

         if (StringUtils.isEmpty(parentPath))
         {
            // top-level reached
            return getDocumentManagementService().createFolder("/",
                  DmsUtils.createFolderInfo(childName));
         }
         else
         {
            Folder parentFolder = createFolderIfNotExists(parentPath);
            return getDocumentManagementService().createFolder(parentFolder.getId(),
                  DmsUtils.createFolderInfo(childName));
         }
      }
      else
      {
         return folder;
      }
   }

   public JsonObject getRepositoryDocument(String folderId, String documentId)
   {
      Document document = getDocumentManagementService().getDocument(documentId);
      
      JsonObject documentJson = marshalDocument(document);
      
      return documentJson;
   }
}
