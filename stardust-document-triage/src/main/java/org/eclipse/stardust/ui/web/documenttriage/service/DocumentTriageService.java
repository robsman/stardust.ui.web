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

import java.util.*;

import javax.annotation.Resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.documenttriage.rest.JsonMarshaller;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
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
		ProcessInstanceQuery processInstanceQuery = ProcessInstanceQuery
				.findAll();

		processInstanceQuery.where(ProcessInstanceQuery.OID.isEqual(oid));
		processInstanceQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
		ProcessInstance processInstance = (ProcessInstanceDetails) getQueryService()
				.getAllProcessInstances(processInstanceQuery).get(0);

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

		JsonArray specificDocuments = getSpecificDocuments(processDefinition);
		String inDataPathId = "";
		boolean isEmpty = true;
		for (JsonElement specificDocument : specificDocuments)
      {
		   inDataPathId = specificDocument.getAsJsonObject().get("inDataPathId").getAsString();
		   Document doc=null; 
		   if(inDataPathId !=""){
			   doc = (Document) getWorkflowService().getInDataPath(oid, inDataPathId);
		   }
		   if (doc != null) {
	         isEmpty = false;
		   }
         specificDocument.getAsJsonObject().addProperty("isEmpty", isEmpty);
      }
		
		processInstanceJson.add("specificDocuments", specificDocuments);
		
		JsonArray descriptorsJson = new JsonArray();

		processInstanceJson.add("descriptors", descriptorsJson);

		for (DataPath dataPath : processInstance.getDescriptorDefinitions()) {
			JsonObject descriptorJson = new JsonObject();

			descriptorsJson.add(descriptorJson);

			descriptorJson.addProperty("id", dataPath.getId());
			descriptorJson.addProperty("name", dataPath.getName());
			if (processInstance.getDescriptorValue(dataPath.getId()) != null) {
				descriptorJson.addProperty("value", processInstance
						.getDescriptorValue(dataPath.getId()).toString());
			} else {
				descriptorJson.addProperty("value", "");
			}
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
    private JsonArray getProcessAttachments(ProcessInstance processInstance) {
        JsonArray attachmentsJson = new JsonArray();
        List<Document> documents = fetchProcessAttachments(processInstance);

        String MIMETYPE_PDF = "application/pdf", MIMETYPE_TIFF = "image/tiff";
        for (Document attachment : documents) {
              JsonObject attachmentJson = new JsonObject();

              attachmentsJson.add(attachmentJson);

              attachmentJson.addProperty("name", attachment.getName());
              attachmentJson.addProperty("creationTimestamp", attachment
                          .getDateCreated().getTime());
              attachmentJson.addProperty("contentType",
                          attachment.getContentType());
              attachmentJson.addProperty("path", attachment.getPath());
              attachmentJson.addProperty("uuid", attachment.getId());

              byte[] data = getDocumentManagementService()
                          .retrieveDocumentContent(attachment.getId());
              if (MIMETYPE_PDF.equalsIgnoreCase(attachment.getContentType())) {
                    attachmentJson.addProperty("numPages",
                                PdfPageCapture.getNumPages(data));
              } else if (MIMETYPE_TIFF.equalsIgnoreCase(attachment
                          .getContentType())) {
                    attachmentJson.addProperty("numPages",
                                TiffReader.getNumPages(data));
              } else { // Any other type, except PDF or TIFF
                    attachmentJson.addProperty("numPages", 0);
              }
              
              JsonObject documentTypeJson = new JsonObject();
              DocumentType documentType = attachment.getDocumentType();
              if (documentType != null) {
                 documentTypeJson.addProperty("documentTypeId", documentType.getDocumentTypeId());
                 documentTypeJson.addProperty("name", documentType.getDocumentTypeId().substring(documentType.getDocumentTypeId().lastIndexOf('}') + 1));
                 // TODO: Get "name" using name = model.getTypeDeclaration(documentType).getName();
                 documentTypeJson.addProperty("schemaLocation", documentType.getSchemaLocation());
              }
              attachmentJson.add("documentType", documentTypeJson);
        }

        return attachmentsJson;
  }

	/**
	 * TODO Reuse!!!
	 * 
	 * @param path
	 * @return
	 */
	private Folder getOrCreateFolder(String path) {
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

		Document sourceDocument = getDocumentManagementService().getDocument(
				json.get("document").getAsJsonObject().get("uuid")
						.getAsString());

		// TODO: Code assumes that there is always exactly one Document OUT data
		// mapping
		String APPLICATION_CONTEXT_DEFAULT = "default";

		// Get Data Id for the (Document Rendezvous) OUT Data Mapping
		ApplicationContext defaultContext = activityInstance.getActivity()
				.getApplicationContext(APPLICATION_CONTEXT_DEFAULT);
		DataMapping outDataMapping = (DataMapping) defaultContext
				.getAllOutDataMappings().get(0);
		String dataMappingId = outDataMapping.getId();

		Map<String, Object> outData = new HashMap<String, Object>();
		outData.put(dataMappingId, (Object) sourceDocument);

		getWorkflowService().activateAndComplete(activityInstance.getOID(),
				APPLICATION_CONTEXT_DEFAULT, outData);

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
			processDefinitionJson.addProperty("id", processDefinition.getQualifiedId());
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
			
			specificDocumentJson.addProperty("id", dataPath.getId());
			specificDocumentJson.addProperty("name", dataPath.getName());
			specificDocumentJson.addProperty("type", dataPath.getMappedType()
					.getName());
         specificDocumentJson.addProperty("data", dataPath.getData());
         specificDocumentJson.addProperty("inDataPathId", getInDataPathId(processDefinition, dataPath.getData()));
		}

		return specificDocumentsJson;
	};

   private String getInDataPathId(ProcessDefinition processDefinition, String dataId) {
      String inDataPathId = "";
      
      for (DataPath dataPath : (List<DataPath>) processDefinition
            .getAllDataPaths()) {
         if (dataPath.getDirection().equals(Direction.IN)
               && dataPath.getData().equals(dataId)) {
            inDataPathId = dataPath.getId();
            break;
         }
      }

      return inDataPathId;
   }

   /**
	 * 
	 * @param activityInstanceOid
	 * @return
	 */
	public JsonObject getProcessAttachmentsForActivityInstance(long activityInstanceOid) {
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
		// TODO: Use "Business Object" concepts here for Process Data
		JsonObject businessObject = parameters.get("businessObject").getAsJsonObject();
      Map<String, String> memberInfo = new HashMap<String, String>();
      memberInfo.put("id", businessObject.get("id").getAsString());
      memberInfo.put("firstName", businessObject.get("firstName").getAsString());
      memberInfo.put("lastName", businessObject.get("lastName").getAsString());
      memberInfo.put("dateOfBirth", "");
      
		Map<String, Object> processData = new HashMap<String, Object>();
		processData.put("Member", memberInfo);
		
      // Start new Process Instance
      String processDefinitionId = parameters.get("processDefinitionId").getAsString();
      ProcessInstance pi = getWorkflowService().startProcess(processDefinitionId, processData, true);

      // Specific Documents
      Map<String, Document> documentDataPathMap = new HashMap<String, Document>();
      String dataPathId, documentId;
      Document document;

      JsonArray specificDocuments = parameters.get("specificDocuments").getAsJsonArray();
      for (JsonElement specificDocument : specificDocuments)
      {
         dataPathId = specificDocument.getAsJsonObject().get("dataPathId").getAsString();
         
         documentId = specificDocument.getAsJsonObject().get("document").getAsJsonObject().get("uuid").getAsString();
         document = getDocumentManagementService().getDocument(documentId);
            
         documentDataPathMap.put(dataPathId, document);
      }

      addSpecificDocuments(pi.getOID(), documentDataPathMap);
      
		// Process Attachments
		JsonArray processAttachments = parameters.get("processAttachments").getAsJsonArray();
		addProcessAttachments(pi.getOID(), processAttachments);
		
		return parameters;
	}

   private void addProcessAttachments(long processInstanceOid, JsonArray processAttachments)
   {
      List<String> sourceDocumentIds = new ArrayList<String>();
      
		for (JsonElement processAttachment : processAttachments) {
			sourceDocumentIds.add(processAttachment.getAsJsonObject()
					.get("uuid").getAsString());
		}

		if (sourceDocumentIds.size() > 0) {
			List<Document> sourceDocuments = getDocumentManagementService()
					.getDocuments(sourceDocumentIds);
			// Add attachments
         for (Document document : sourceDocuments) {
            addProcessAttachment(processInstanceOid, document);
			}
		}
   }

	/**
	 * @param documentId
	 * @param pageNumber
	 * @return
	 */
	public byte[] getDocumentImage(String documentId, int pageNumber) {
		byte[] image = null;

		Document document = getDocumentManagementService().getDocument(
				documentId);
		String contentType = document.getContentType();
		byte[] data = documentManagementService
				.retrieveDocumentContent(documentId);

		String MIMETYPE_PDF = "application/pdf", MIMETYPE_TIFF = "image/tiff";
		if (MIMETYPE_PDF.equalsIgnoreCase(contentType)) {
			image = PdfPageCapture.getPageImage(data, pageNumber);
		} else if (MIMETYPE_TIFF.equalsIgnoreCase(contentType)) {
			image = TiffReader.getPageImage(data, pageNumber);
		} else { // Any other type, except PDF or TIFF
		   // TODO
		}

		return image;
	}

   public JsonObject splitDocument(long processInstanceOid, String documentId, JsonObject postedData) {
      JsonObject resultJson = new JsonObject();
      
      byte[] image = null;
      Set<Integer> pageNumbers = new HashSet<Integer>();
      
      JsonArray pages = postedData.get("pages").getAsJsonArray();
      for (JsonElement jsonElement : pages)
      {
         pageNumbers.add(jsonElement.getAsInt());
      }

      Document document = getDocumentManagementService().getDocument(
            documentId);
      String contentType = document.getContentType();
      byte[] data = documentManagementService
            .retrieveDocumentContent(documentId);

      String MIMETYPE_PDF = "application/pdf", MIMETYPE_TIFF = "image/tiff";
      if (MIMETYPE_PDF.equalsIgnoreCase(contentType)) {
         image = PdfPageCapture.getSplitTiff(data, pageNumbers);
      } else if (MIMETYPE_TIFF.equalsIgnoreCase(contentType)) {
         image = TiffReader.getSplitTiff(data, pageNumbers);
      } else { // Any other type, except PDF or TIFF
         // TODO
      }
      
      String fileName = document.getName(), extension = "";

      int i = fileName.lastIndexOf('.');
      if (i > 0) {
          extension = fileName.substring(i+1);
          fileName = fileName.substring(0, i - 1);
      }
      
      fileName = fileName + "_" + UUID.randomUUID() + ".tiff";
      
      Document createdDocument = addProcessAttachment(processInstanceOid, fileName, image);
      
      return resultJson;
   }
   
	/**
	 * @param pi
	 * @return
	 */
	private List<Document> fetchProcessAttachments(
			ProcessInstance processInstance) {
		List<Document> processAttachments = new ArrayList<Document>();

		DeployedModel model = getQueryService().getModel(
				processInstance.getModelOID());
		ProcessDefinition processDefinition = model
				.getProcessDefinition(processInstance.getProcessID());
		List dataPaths = processDefinition.getAllDataPaths();

		for (int n = 0; n < dataPaths.size(); ++n) {
			DataPath dataPath = (DataPath) dataPaths.get(n);

			if (!dataPath.getDirection().equals(Direction.IN)) {
				continue;
			}

			try {
				if (dataPath.getId().equals(
						CommonProperties.PROCESS_ATTACHMENTS)) {
					Object object = getWorkflowService().getInDataPath(
							processInstance.getOID(), dataPath.getId());

					if (object != null) {
						processAttachments.addAll((Collection) object);
						break;
					}
				}
			} catch (Exception e) {
				System.out.println("Error fetching Process Attachments: "
						+ e.getMessage());
			}
		}

		return processAttachments;
	}
	
	public JsonArray getDocumentTypes() {
	   
      JsonArray documentTypesJson = new JsonArray();
       
	   List<DocumentType> activeDocumentTypes = getActiveDocumentTypes();
	   for (DocumentType documentType : activeDocumentTypes)
      {
         JsonObject documentTypeJson = new JsonObject();
         documentTypeJson.addProperty("documentTypeId", documentType.getDocumentTypeId());
         documentTypeJson.addProperty("name", documentType.getDocumentTypeId().substring(documentType.getDocumentTypeId().lastIndexOf('}') + 1));
         // TODO: Get "name" using name = model.getTypeDeclaration(documentType).getName();
         documentTypeJson.addProperty("schemaLocation", documentType.getSchemaLocation());
         
         documentTypesJson.add(documentTypeJson);
      }
	   
	   return documentTypesJson;
	}
	
   public JsonObject getDocumentType(String documentId) {
      
      JsonObject documentTypeJson = new JsonObject();
      
      DocumentType documentType = getDocumentManagementService().getDocument(documentId).getDocumentType();

      documentTypeJson.addProperty("documentTypeId", documentType.getDocumentTypeId());
      documentTypeJson.addProperty("name", documentType.getDocumentTypeId().substring(documentType.getDocumentTypeId().lastIndexOf('}') + 1));
      // TODO: Get "name" using name = model.getTypeDeclaration(documentType).getName();
      documentTypeJson.addProperty("schemaLocation", documentType.getSchemaLocation());

      return documentTypeJson; 
   }

	public JsonObject setDocumentType(String documentId, JsonObject json) {
	 
      JsonObject resultJson = new JsonObject();
      
      String documentTypeId = json.get("documentTypeId").getAsString();
      String schemaLocation = json.get("schemaLocation").getAsString();
      
      Document document = setDocumentType(documentId, documentTypeId, schemaLocation);

      return resultJson; 
	}

	public Document addProcessAttachment(long processOid, String fileName,
			byte[] bytes) {
		ProcessInstance processInstance = getWorkflowService()
				.getProcessInstance(processOid);
		List<Document> processAttachments = fetchProcessAttachments(processInstance);

		DocumentInfo docInfo = DmsUtils.createDocumentInfo(fileName);
		String folderPath = DocumentMgmtUtility
				.getProcessAttachmentsFolderPath(processInstance);
		getOrCreateFolder(folderPath);
		Document document = getDocumentManagementService().createDocument(
				folderPath, docInfo, bytes, "");

		processAttachments.add(document);
		getWorkflowService().setOutDataPath(processInstance.getOID(),
				CommonProperties.PROCESS_ATTACHMENTS, processAttachments);
		
		return document;
	}
	
	public JsonObject addProcessInstanceDocument(long processInstanceOid, String dataPathId, JsonObject json)
	{
      JsonObject resultJson = new JsonObject();
      
      if (CommonProperties.PROCESS_ATTACHMENTS.equals(dataPathId)) {
         JsonArray processAttachments = json.get("data").getAsJsonArray();
         addProcessAttachments(processInstanceOid, processAttachments);
      }
      else {
         // TODO: Currently assume that it is always a Document (and not a DocumentList)
         
         String sourceDocumentId = json.get("data").getAsJsonObject().get("uuid").getAsString();
         Document document = getDocumentManagementService().getDocument(sourceDocumentId);

         Map<String, Document> documentDataPathMap = new HashMap<String, Document>();
         documentDataPathMap.put(dataPathId, document);
         
         addSpecificDocuments(processInstanceOid, documentDataPathMap);
      }
	   
      return getPendingProcesses(null); 
	}
   
   public JsonObject removeProcessInstanceDocument(long processInstanceOid, String dataPathId, String documentId)
   {
      JsonObject resultJson = new JsonObject();
      
      if (CommonProperties.PROCESS_ATTACHMENTS.equals(dataPathId)) {
         removeProcessAttachment(processInstanceOid, documentId);
      }
      else {
         // TODO: Currently assume that it is always a Document (and not a DocumentList)
         
         Map<String, Document> documentDataPathMap = new HashMap<String, Document>();
         documentDataPathMap.put(dataPathId, null);
         
         removeSpecificDocuments(processInstanceOid, documentDataPathMap);
      }
      
      return getPendingProcesses(null); 
   }
   
   private void addSpecificDocuments(long processInstanceOid, Map<String, Document> documentDataPathMap) {
      getWorkflowService().setOutDataPaths(processInstanceOid, documentDataPathMap);
   }
   
   private void removeSpecificDocuments(long processInstanceOid, Map<String, Document> documentDataPathMap) {
      getWorkflowService().setOutDataPaths(processInstanceOid, documentDataPathMap);
   }
   
   private void addProcessAttachment(long processInstanceOid, Document document) {
      ProcessInstance processInstance = getWorkflowService()
            .getProcessInstance(processInstanceOid);
      List<Document> processAttachments = fetchProcessAttachments(processInstance);

      processAttachments.add(document);
      getWorkflowService().setOutDataPath(processInstance.getOID(),
            CommonProperties.PROCESS_ATTACHMENTS, processAttachments);
   }
   
   private void removeProcessAttachment(long processInstanceOid, String documentId) {
      ProcessInstance processInstance = getWorkflowService()
            .getProcessInstance(processInstanceOid);
      List<Document> processAttachments = fetchProcessAttachments(processInstance);

      // Remove the document from the PROCESS_ATTACHMENTS list
      for (Iterator<Document> iter = processAttachments.listIterator(); iter.hasNext(); ) {
         Document document = iter.next();
         if (document.getId().equals(documentId)) {
             iter.remove();
         }
      }
      
      getWorkflowService().setOutDataPath(processInstance.getOID(),
            CommonProperties.PROCESS_ATTACHMENTS, processAttachments);
   }
   
   private List<DocumentType> getActiveDocumentTypes() {
      List<DocumentType> allDocumentTypes = new ArrayList<DocumentType>();
      
      // Get all "active" models
      Models deployedModelDescriptions = getQueryService().getModels(DeployedModelQuery.findActive());
      
      List<DeployedModel> deployedModels = new ArrayList<DeployedModel>();
      for (DeployedModelDescription deployedModelDescription : deployedModelDescriptions)
      {
         deployedModels.add(getQueryService().getModel(deployedModelDescription.getModelOID()));
      }
      
      // Get Document Types in each "active" model
      for (DeployedModel deployedModel : deployedModels)
      {
         allDocumentTypes.addAll(DocumentTypeUtils.getDeclaredDocumentTypes(deployedModel));
      }
      
      return allDocumentTypes;
   }

   private Document setDocumentType(String documentId, String documentTypeId, String schemaLocation) {
	      DocumentType documentType = null;
	      Document document = getDocumentManagementService().getDocument(documentId);

	      if (documentTypeId != null && documentTypeId.length() > 0) {
	         documentType = new DocumentType(documentTypeId, schemaLocation);
	      }
	      
	      document.setDocumentType(documentType);
	      
	      Document updatedDocument = getDocumentManagementService().updateDocument(document, false, "", "", false);
	      
	      return updatedDocument;
	   }

}
