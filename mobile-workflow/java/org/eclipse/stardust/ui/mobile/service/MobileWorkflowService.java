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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
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
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.HistoricalEvent;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.interactions.Interaction;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.ui.mobile.rest.JsonMarshaller;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

		JsonObject userJson = new JsonObject();

		userJson.addProperty("id", loginUser.getId());
		userJson.addProperty("lastName", loginUser.getFirstName());
		userJson.addProperty("firstName", loginUser.getLastName());
		userJson.addProperty("eMail", loginUser.getEMail());

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
			activityInstanceJson.addProperty("assignedTo", ActivityInstanceUtils.getAssignedToLabel(activityInstance));
			activityInstanceJson.addProperty("startedBy", "TODO DOE");
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
	public JsonObject activateActivity(JsonObject activityInstanceJson) {
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
	}

	/**
	 * 
	 * @return
	 */
	public JsonObject completeActivity(JsonObject activityInstanceJson) {
		ActivityInstance activityInstance = getWorkflowService().complete(
				activityInstanceJson.get("oid").getAsLong(), "",
				new HashMap<String, Object>());

		activityInstanceJson = new JsonObject();

		// activityInstance.getActivity().g

		return activityInstanceJson;
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
//      List<TypedDocument> typedDocuments = TypedDocumentsUtil.getTypeDocuments(processInstance);
		for (Document document : processAttachments) {
			documentsJson.add(marshalDocument(document));
		}

		JsonArray notesJson = new JsonArray();

		processInstanceJson.add("notes", notesJson);

		for (Note note : processInstance.getAttributes().getNotes()) {
			JsonObject noteJson = new JsonObject();

			notesJson.add(noteJson);

			noteJson.addProperty("content", note.getText());
			noteJson.addProperty("timestamp", note.getTimestamp().getTime());
			// noteJson.addProperty("user", note.getUser());
		}

		// TODO Replace

		JsonObject participantsJson = new JsonObject();

		processInstanceJson.add("participants", participantsJson);

		UserQuery userQuery = UserQuery.findAll();

		for (User user : getQueryService().getAllUsers(userQuery)) {
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
	}

	/**
	 * 
	 * @return
	 */
	public JsonObject getNotes(long processInstanceOid) {
		ProcessInstance processInstance = getWorkflowService()
				.getProcessInstance(processInstanceOid);
		JsonObject resultJson = new JsonObject();
		JsonArray notesJson = new JsonArray();

		resultJson.add("notes", notesJson);

		for (Note note : processInstance.getAttributes().getNotes()) {
			notesJson.add(marshalNote(note));
		}

		return resultJson;
	}

	/**
	 * 
	 * @return
	 */
	public JsonObject getDocuments(long processInstanceOid) {
		ProcessInstance processInstance = getWorkflowService()
				.getProcessInstance(processInstanceOid);
		JsonObject resultJson = new JsonObject();
		JsonArray documentsJson = new JsonArray();

		List<Document> processAttachments = fetchProcessAttachments(processInstance);
//        List<TypedDocument> typedDocuments = TypedDocumentsUtil.getTypeDocuments(processInstance);
		resultJson.add("documents", documentsJson);

		for (Document document : processAttachments) {
			documentsJson.add(marshalDocument(document));
		}

		return resultJson;
	}

	/**
	 * 
	 * @return
	 */
	public JsonObject getFolders(String folderId) {
		JsonObject folderJson = new JsonObject();
		JsonArray subFoldersJson = new JsonArray();

		folderJson.add("subFolders", subFoldersJson);

		if (folderId == null || folderId.equals("null")) {
			JsonObject subFolderJson = new JsonObject();

			subFoldersJson.add(subFolderJson);

			subFolderJson.addProperty("id", publicDocumentsRootFolder.getId());
			subFolderJson.addProperty("path",
					publicDocumentsRootFolder.getPath());

			getFolderContent(subFolderJson, publicDocumentsRootFolder);

			// Overwrite name
			
			subFolderJson.addProperty("name", "Public Documents");

			if (userDocumentsRootFolder != null) {
				subFolderJson = new JsonObject();

				subFoldersJson.add(subFolderJson);

				subFolderJson
						.addProperty("id", userDocumentsRootFolder.getId());
				subFolderJson.addProperty("path",
						userDocumentsRootFolder.getPath());

				getFolderContent(subFolderJson, userDocumentsRootFolder);

				// Overwrite name 
				
				subFolderJson.addProperty("name", "Personal Documents");
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

		if (!folderJson.has("subFolders")) {
			folderJson.add("subFolders", subFoldersJson = new JsonArray());
		} else {
			subFoldersJson = folderJson.get("subFolders").getAsJsonArray();
		}

		for (Folder subFolder : (List<Folder>) folder.getFolders()) {
			JsonObject subFolderJson = new JsonObject();

			subFoldersJson.add(subFolderJson);

			subFolderJson.addProperty("id", subFolder.getId());
			subFolderJson.addProperty("name", subFolder.getName());
			subFolderJson.addProperty("path", subFolder.getPath());
		}

		JsonArray documentsJson = null;

		if (!folderJson.has("documents")) {
			folderJson.add("documents", documentsJson = new JsonArray());
		} else {
			documentsJson = folderJson.get("documents").getAsJsonArray();
		}

		for (Document document : (List<Document>) folder.getDocuments()) {
			JsonObject documentJson = new JsonObject();

			documentsJson.add(documentJson);

			documentJson.addProperty("id", document.getId());
			documentJson.addProperty("name", document.getName());
			documentJson.addProperty("contentType", document.getContentType());
			documentJson.addProperty("revisionId", document.getRevisionId());
			documentJson
					.addProperty("revisionName", document.getRevisionName());
			documentJson.addProperty("revisionComment",
					document.getRevisionComment());
			documentJson.addProperty("owner", document.getOwner());
			documentJson.addProperty("path", document.getPath());
			documentJson.addProperty("downloadToken",
					getDocumentManagementService()
							.requestDocumentContentDownload(document.getId()));
			documentJson.addProperty("content",
					new String(getDocumentManagementService()
							.retrieveDocumentContent(document.getId())));
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

		JsonObject userJson = new JsonObject();

		noteJson.add("user", userJson);

		userJson.addProperty("id", note.getUser().getId());
		userJson.addProperty("firstName", note.getUser().getFirstName());
		userJson.addProperty("lastName", note.getUser().getLastName());
		userJson.addProperty("eMail", note.getUser().getEMail());
		userJson.addProperty("description", note.getUser().getDescription());

		return noteJson;
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
		documentJson.addProperty("lastModifiedTimestamp", document.getDateLastModified().getTime());
		documentJson.addProperty("size", document.getSize());
		
		/*JsonObject userJson = new JsonObject();

		documentJson.add("user", userJson);

		documentJson.addProperty("id", note.getUser().getId());
		documentJson.addProperty("firstName", note.getUser().getFirstName());
		documentJson.addProperty("lastName", note.getUser().getLastName());
		documentJson.addProperty("eMail", note.getUser().getEMail());
		documentJson.addProperty("description", note.getUser().getDescription());*/

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
	      Folder folder = getDocumentManagementService().getFolder(folderPath, Folder.LOD_NO_MEMBERS);
	    
	         if (null == folder)
	         {
	            // folder does not exist yet, create it
	            String parentPath = folderPath.substring(0, folderPath.lastIndexOf('/'));
	            String childName = folderPath.substring(folderPath.lastIndexOf('/') + 1);

	            if (StringUtils.isEmpty(parentPath))
	            {
	               // top-level reached
	               return getDocumentManagementService().createFolder("/", DmsUtils.createFolderInfo(childName));
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


}
