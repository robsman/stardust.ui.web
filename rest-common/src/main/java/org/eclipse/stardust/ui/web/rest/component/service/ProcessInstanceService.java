/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.component.service;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.activation.DataHandler;
import javax.annotation.Resource;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.dto.HistoricalData;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.EvaluationPolicy;
import org.eclipse.stardust.engine.api.query.HistoricalDataPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.ws.DocumentInfoXto;
import org.eclipse.stardust.engine.api.ws.DocumentTypeXto;
import org.eclipse.stardust.engine.api.ws.InputDocumentXto;
import org.eclipse.stardust.engine.api.ws.InputDocumentsXto;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.engine.core.runtime.command.impl.StartProcessWithDocumentsCommand;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.engine.ws.DocumentContentDataSource;
import org.eclipse.stardust.engine.ws.WsApiStartProcessUtils;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.component.message.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.rest.component.util.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.rest.component.util.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.rest.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.dto.ActivityInstanceDTO;
import org.eclipse.stardust.ui.web.rest.dto.AttachToCaseDTO;
import org.eclipse.stardust.ui.web.rest.dto.ColumnDTO;
import org.eclipse.stardust.ui.web.rest.dto.CreateCaseDTO;
import org.eclipse.stardust.ui.web.rest.dto.DataTableOptionsDTO;
import org.eclipse.stardust.ui.web.rest.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.dto.DocumentDataDTO;
import org.eclipse.stardust.ui.web.rest.dto.DocumentTypeDTO;
import org.eclipse.stardust.ui.web.rest.dto.HistoricalDataDTO;
import org.eclipse.stardust.ui.web.rest.dto.InstanceCountsDTO;
import org.eclipse.stardust.ui.web.rest.dto.JoinProcessDTO;
import org.eclipse.stardust.ui.web.rest.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.dto.NotificationMap;
import org.eclipse.stardust.ui.web.rest.dto.NotificationMap.NotificationDTO;
import org.eclipse.stardust.ui.web.rest.dto.NotificationMessageDTO;
import org.eclipse.stardust.ui.web.rest.dto.ProcessInstanceDTO;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.SwitchProcessDTO;
import org.eclipse.stardust.ui.web.rest.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.dto.builder.DocumentDTOBuilder;
import org.eclipse.stardust.ui.web.rest.dto.request.DetailLevelDTO;
import org.eclipse.stardust.ui.web.rest.dto.request.DocumentContentRequestDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.AddressBookDataPathValueDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.DataPathValueDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.FolderDTO;
import org.eclipse.stardust.ui.web.rest.util.AddressBookDataPathValueFilter;
import org.eclipse.stardust.ui.web.rest.util.DefaultDataPathValueFilter;
import org.eclipse.stardust.ui.web.rest.util.FileUploadUtils;
import org.eclipse.stardust.ui.web.rest.util.IDataPathValueFilter;
import org.eclipse.stardust.ui.web.rest.util.JsonMarshaller;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.common.converter.PriorityConverter;
import org.eclipse.stardust.ui.web.viewscommon.common.exceptions.I18NException;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorColumnUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.services.ContextPortalServices;
import org.eclipse.stardust.ui.web.viewscommon.utils.CorrespondencePanelPreferenceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component
public class ProcessInstanceService
{
   private static final Logger trace = LogManager.getLogger(ActivityInstanceUtils.class);
   
   @Resource
   private org.eclipse.stardust.ui.web.rest.component.util.ProcessInstanceUtils processInstanceUtilsREST;

   @Resource
   private ProcessDefinitionUtils processDefinitionUtils;
   
   @Resource
   private RepositoryService repositoryService;
   
   @Autowired
   private ActivityInstanceService activityInstanceService;
   
   @Resource
   private RestCommonClientMessages restCommonClientMessages;
   
   @Resource
   private UserService userService;

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;
   
   /**
    * @param processInstanceOid
    * @return
    */
   public ProcessInstanceDTO getProcessSummary(Long processInstanceOid)
   {
      // Make sure it is root process oid
      List<EvaluationPolicy> policies = new ArrayList<EvaluationPolicy>();
      policies.add(HistoricalDataPolicy.INCLUDE_HISTORICAL_DATA);
      ProcessInstance rootProcessInstance = getProcessInstance(processInstanceOid, policies);
      
      if(rootProcessInstance.getRootProcessInstanceOID() != rootProcessInstance.getOID()){
         rootProcessInstance = getProcessInstance(rootProcessInstance.getRootProcessInstanceOID(), policies);
      }
      ProcessInstanceDTO rootProcessInstanceDTO  = processInstanceUtilsREST.buildProcessInstanceDTO(rootProcessInstance);
      traverseProcessInstanceHierarchy(rootProcessInstanceDTO, rootProcessInstance, policies);
      return rootProcessInstanceDTO;
   }
   
   /**
    * @param processInstanceDTO
    * @param processInstance
    * @return
    */
   public ProcessInstanceDTO traverseProcessInstanceHierarchy(ProcessInstanceDTO processInstanceDTO,
         ProcessInstance processInstance, List<EvaluationPolicy> policies)
   {
      List<DataPathValueDTO> dataPathV = getProcessInstanceDocuments(processInstance.getOID());
      
      // set historic data
      List<HistoricalData> histDataList = ((ProcessInstanceDetails) processInstance).getHistoricalData();
      if (CollectionUtils.isNotEmpty(histDataList))
      {
         List<HistoricalDataDTO> histDTOs = new ArrayList<HistoricalDataDTO>();
         for (HistoricalData historicalData : histDataList)
         {
            HistoricalDataDTO dataDTO = new HistoricalDataDTO();
            dataDTO.name = String.valueOf(historicalData.getDataType()); // convert to
                                                                         // Data name
            if (historicalData.getHistoricalDataValue() != null)
            {
               dataDTO.value = historicalData.getHistoricalDataValue().toString();
            }

            dataDTO.modificationTime = historicalData.getDataModificationTimestamp();
            dataDTO.contextAIOID = historicalData.getModifyingActivityInstanceOID();
            histDTOs.add(dataDTO);
         }
         processInstanceDTO.historicalData = histDTOs;
      }
      
      //set attachments 
      for (DataPathValueDTO dataPathValueDTO : dataPathV)
      {
         if(dataPathValueDTO.dataPath.id.equals("PROCESS_ATTACHMENTS")){
            DetailLevelDTO detailLevelDTO = new DetailLevelDTO();
            detailLevelDTO.userDetailsLevel = "true";
            DocumentDTOBuilder.setOwnerDetails(dataPathValueDTO.documents, detailLevelDTO, userService);
            processInstanceDTO.attachments = dataPathValueDTO.documents;  
         }
      }
      
      processInstanceDTO.activityInstances = activityInstanceService
            .getActivityInstancesForProcess(processInstance.getOID(), false);
      
      for (ActivityInstanceDTO adto : processInstanceDTO.activityInstances)
      {
         if ("Subprocess".equals(adto.activity.implementationTypeId))
         {
            ProcessInstanceQuery processInstanceQuery = ProcessInstanceQuery.findAll();
            processInstanceQuery.getFilter().add(
                  ProcessInstanceQuery.STARTING_ACTIVITY_INSTANCE_OID.isEqual(adto.activityOID));

            for (EvaluationPolicy policy : policies)
            {
               processInstanceQuery.setPolicy(policy);
            }

            ProcessInstances processInstances = serviceFactoryUtils.getQueryService().getAllProcessInstances(
                  processInstanceQuery);
            for (ProcessInstance processInstance2 : processInstances)
            {
               adto.startingProcessInstance = processInstanceUtilsREST.buildProcessInstanceDTO(processInstance2);
               traverseProcessInstanceHierarchy(adto.startingProcessInstance, processInstance2, policies);
            }
         }
      }
      return processInstanceDTO;
   }
   
   
   
   public ProcessInstanceDTO startProcess(List<Attachment> attachments)
   {
      String processDefinitionId = null;
      DocumentDataDTO documentDataDTO = null;
      DocumentContentRequestDTO documentInfoDTO = null;
      InputStream inputStream = null;
      JsonMarshaller jsonIo = new JsonMarshaller();
      ProcessInstance pi = null;
      try
      {
         for (Attachment attachment : attachments)
         {
            DataHandler dataHandler = attachment.getDataHandler();
            inputStream = dataHandler.getInputStream();
            if ("processDefinitionId".equals(dataHandler.getName()))
            {
               processDefinitionId = inputStream.toString();
            }
            else if ("documentData".equals(dataHandler.getName()))
            {
               inputStream = dataHandler.getInputStream();
               Map<String, Type> customTokens = new HashMap<String, Type>();
               customTokens.put("documentType", new TypeToken<DocumentTypeDTO>()
               {
               }.getType());
               JsonObject data = jsonIo.readJsonObject(inputStream.toString());
               documentDataDTO = DTOBuilder.buildFromJSON(data, DocumentDataDTO.class, customTokens);
            }
            else
            {
               List<Attachment> attachmentList = CollectionUtils.newArrayList();
               attachmentList.add(attachment);
               documentInfoDTO = FileUploadUtils.parseAttachments(attachmentList).get(0);
            }

         }
         ProcessDefinition processDef = processDefinitionUtils.getProcessDefinition(processDefinitionId);
         Model model = ModelCache.findModelCache().getModel(processDef.getModelOID());
         final InputDocumentsXto processDocuments = createProcessDocuments(documentDataDTO, documentInfoDTO);
         StartProcessWithDocumentsCommand command = new StartProcessWithDocumentsCommand(processDefinitionId,
               model.getModelOID(), null, true,
               WsApiStartProcessUtils.unmarshalToSerializable(processDocuments, model), null);

         pi = (ProcessInstance) org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils.getWorkflowService()
               .execute(command);
         return processInstanceUtilsREST.buildProcessInstanceDTO(pi);
      }
      catch (Exception e)
      {
         throw new I18NException(restCommonClientMessages.getParamString("processInstance.startProcess.error",
               processDefinitionId, documentDataDTO.name));

      }

   }

   private InputDocumentsXto createProcessDocuments(DocumentDataDTO documentData, DocumentContentRequestDTO documentInfo)
   {
      final InputDocumentsXto inputDocs = new InputDocumentsXto();
      final InputDocumentXto inputDoc = new InputDocumentXto();
      DocumentInfoXto docInfo = new DocumentInfoXto();
      DocumentTypeXto docType = new DocumentTypeXto();
      if (null != documentData.documentType && null != documentData.documentType.getDocumentTypeId())
      {
         docType.setDocumentTypeId(documentData.documentType.getDocumentTypeId());
         docType.setSchemaLocation(documentData.documentType.getSchemaLocation());
      }
      // Specific Documents will be stored at PATH
      if (!DmsConstants.DATA_ID_ATTACHMENTS.equals(documentData.id))
      {
         inputDoc.setGlobalVariableId(documentData.id);
      }
      docInfo.setDocumentType(docType);
      docInfo.setName(documentInfo.name);
      docInfo.setDateCreated(new Date());
      docInfo.setContentType(documentInfo.contentType);
      docInfo.setOwner(processInstanceUtilsREST.getCurrentUser().getAccount());
      inputDoc.setDocumentInfo(docInfo);
      inputDoc.setContent(createContent(documentData, documentInfo));
      inputDocs.getInputDocument().add(inputDoc);
      return inputDocs;
   }

   private DataHandler createContent(DocumentDataDTO documentData, DocumentContentRequestDTO documentInfo)
   {
      
      final DocumentInfo docInfo = DmsUtils.createDocumentInfo(documentInfo.name);
      docInfo.setContentType(documentInfo.contentType);
      docInfo.setOwner(processInstanceUtilsREST.getCurrentUser().getAccount());
      return new DataHandler(new DocumentContentDataSource(docInfo, documentInfo.contentBytes));
   }
   public List<ProcessInstanceDTO> getPendingProcesses(JsonObject json)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public DocumentDTO splitDocument(long processInstanceOid, String documentId, JsonObject json)
   {
      // TODO Auto-generated method stub
      return null;
   }
 
   /**
    * @author Yogesh.Manware
    * @param processOid
    * @param attachments
    * @param dataPathId
    * @return
    * @throws Exception
    */
   public Map<String, Object> addProcessDocuments(long processOid, List<Attachment> attachments, String dataPathId)
         throws Exception
   {
      Map<String, Object> result = null;

      ProcessInstance processInstance = processInstanceUtilsREST.getProcessInstance(processOid);
      // parse attachments
      List<DocumentContentRequestDTO> uploadedDocuments = FileUploadUtils.parseAttachments(attachments);

      if (DmsConstants.PATH_ID_ATTACHMENTS.equals(dataPathId))
      {
         result = repositoryService.createProcessDocuments(uploadedDocuments, processInstance, true);
      }
      else
      {
         // check if OUT dataPath exist
         ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(
               processInstance.getModelOID(), processInstance.getProcessID());
         ModelCache modelCache = ModelCache.findModelCache();
         Model model = modelCache.getModel(processInstance.getModelOID());
         List<DataPath> dataPaths = processDefinition.getAllDataPaths();

         DataDetails dataDetails = null;

         boolean outDataMappingExist = false;
         for (DataPath dataPath : dataPaths)
         {
            if (!dataPath.getId().equals(dataPathId))
            {
               continue;
            }
            dataDetails = (DataDetails) model.getData(dataPath.getData());

            if (Direction.OUT.equals(dataPath.getDirection()) || Direction.IN_OUT.equals(dataPath.getDirection())
                  || (!DescriptorColumnUtils.isCompositeOrLinkDescriptor(dataPath) && DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataDetails.getTypeId())))
            {
               outDataMappingExist = true;
               break;
            }
         }

         if (!outDataMappingExist)
         {
            throw new I18NException(restCommonClientMessages.getParamString("processInstance.outDataPath.error",
                  dataPathId));
         }

         // determine DocumentType
         DocumentType documentType = null != dataDetails
               ? org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils.getDocumentTypeFromData(model,
                     dataDetails)
               : null;

         for (DocumentContentRequestDTO documentInfoDTO : uploadedDocuments)
         {
            if (documentType != null)
            {
               documentInfoDTO.documentType = documentType;
            }
            documentInfoDTO.parentFolderPath = DocumentMgmtUtility.getTypedDocumentsFolderPath(processInstance);
            documentInfoDTO.dataPathId = dataPathId;
         }
         result = repositoryService.createProcessDocuments(uploadedDocuments, processInstance, false);
      }
      return result;
   }

   /**
    *  @author Yogesh.Manware
    * @param processOid
    * @param dataPathId
    * @param documentId
    * @return
    * @throws Exception
    */
   public void removeProcessDocument(long processOid, String dataPathId, String documentId)
         throws Exception
   {
      ProcessInstance processInstance = processInstanceUtilsREST.getProcessInstance(processOid);

      if (DmsConstants.PATH_ID_ATTACHMENTS.equals(dataPathId))
      {
         ArrayList<String> documentIds = new ArrayList<String>();
         documentIds.add(documentId);
         repositoryService.detachProcessAttachments(documentIds, processInstance);
      }
      else
      {
         serviceFactoryUtils.getWorkflowService().setOutDataPath(processOid, dataPathId, null);
      }
   }
   
   /**
    * Returns process attachemtents and specific documents, if supported.
    * 
    * @param processInstanceOid
    * @return
    */
   public List<DataPathValueDTO> getProcessInstanceDocuments(long processInstanceOid)
   {
      List<DataPathValueDTO> allDPs = getAllDataPathValuesDTO(processInstanceOid);
      List<DataPathValueDTO> datPaths = new ArrayList<DataPathValueDTO>();
      for (DataPathValueDTO dp : allDPs)
      {
         if (null != dp.documents)
         {
            datPaths.add(dp);
         }
      }

      return datPaths;
   }

   /**
    * Updates the process priorities
    * 
    * @param type
    * @param oidPriorityMap
    * @return
    */
   public String updatePriorities(Map<String, Integer> oidPriorityMap)
   {

      NotificationMap notificationMap = new NotificationMap();
      for (Entry<String, Integer> entry : oidPriorityMap.entrySet())
      {
         Long oid = Long.valueOf(entry.getKey());
         try
         {
            ProcessInstanceUtils.setProcessPriority(oid, entry.getValue());
            notificationMap.addSuccess(new NotificationDTO(oid, null, MessagesViewsCommonBean.getInstance()
                  .getParamString("views.processTable.savePriorities.priorityChanged",
                        PriorityConverter.getPriorityLabel(entry.getValue()))));

         }
         catch (AccessForbiddenException exception)
         {
            notificationMap.addFailure(new NotificationDTO(oid, null, MessagesViewsCommonBean.getInstance().getString(
                  "common.authorization.msg")));
            trace.error("Authorization exception occurred while changing process priority: ", exception);
         }
         catch (Exception exception)
         {
            notificationMap.addFailure(new NotificationDTO(oid, null, exception.getMessage()));
            trace.error("Exception occurred while changing process priority: ", exception);
         }
      }
      return GsonUtils.toJsonHTMLSafeString(notificationMap);
   }

   /**
    * Get all process instances count
    * 
    * @return List
    */
   public InstanceCountsDTO getAllCounts()
   {
      return processInstanceUtilsREST.getAllCounts();
   }

   /**
    * 
    * @param request
    * @return
    */
   public String recoverProcesses(String request)
   {
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      Map<String, String> returnValue = new HashMap<String, String>();
      List<Long> processes = JsonDTO.getAsList(request, Long.class);

      try
      {
         processInstanceUtilsREST.recoverProcesses(processes);
         returnValue.put("success", "true");
         returnValue.put("message", propsBean.getString("views.common.recoverMessage"));
      }
      catch (AccessForbiddenException e)
      {
         returnValue.put("success", "false");
         returnValue.put("message", propsBean.getString("common.authorization.msg"));
      }
      catch (Exception e)
      {
         returnValue.put("success", "false");
         returnValue.put("message", propsBean.getString("common.exception"));
      }

      return GsonUtils.toJsonHTMLSafeString(returnValue);
   }

   /**
    * 
    * @param request
    * @return
    */
   public String attachToCase(String request)
   {
      AttachToCaseDTO attachToCaseDTO = GsonUtils.fromJson(request, AttachToCaseDTO.class);
      List<Long> sourceProcessInstanceOids = attachToCaseDTO.sourceProcessOIDs;
      Long targetProcessInstanceOid = Long.parseLong(attachToCaseDTO.targetProcessOID);

      NotificationMessageDTO returnValue = processInstanceUtilsREST.attachToCase(sourceProcessInstanceOids,
            targetProcessInstanceOid);

      return GsonUtils.toJsonHTMLSafeString(returnValue);
   }

   public String createCase(String request)
   {
      CreateCaseDTO createCaseDTO = GsonUtils.fromJson(request, CreateCaseDTO.class);

      NotificationMessageDTO returnValue = processInstanceUtilsREST.createCase(createCaseDTO.sourceProcessOIDs,
            createCaseDTO.caseName, createCaseDTO.description, createCaseDTO.note);

      return GsonUtils.toJsonHTMLSafeString(returnValue);
   }

   /**
    * 
    * @param request
    * @return
    */
   public String abortProcesses(String request)
   {
      NotificationMap notificationMap = new NotificationMap();

      JsonObject json = GsonUtils.readJsonObject(request);
      String scope = GsonUtils.extractString(json, "scope");

      Type listType = new TypeToken<List<Long>>()
      {
      }.getType();

      @SuppressWarnings("unchecked")
      List<Long> processes = (List<Long>) GsonUtils
            .extractList(GsonUtils.extractJsonArray(json, "processes"), listType);

      if (AbortScope.RootHierarchy.toString().equalsIgnoreCase(scope))
      {
         notificationMap = processInstanceUtilsREST.abortProcesses(AbortScope.RootHierarchy, processes);
      }
      else
      {
         notificationMap = processInstanceUtilsREST.abortProcesses(AbortScope.SubHierarchy, processes);
      }

      return GsonUtils.toJsonHTMLSafeString(notificationMap);
   }

   public QueryResultDTO getProcessInstances(ProcessInstanceQuery query, DataTableOptionsDTO options)
   {
      QueryResult< ? extends ProcessInstance> queryResult = processInstanceUtilsREST
            .getProcessInstances(query, options);
      return processInstanceUtilsREST.buildProcessListResult(queryResult);
   }

   public String getTargetProcessesForSpawnSwitch()
   {
      try
      {
         List<ProcessDefinition> pds = processInstanceUtilsREST.getTargetProcessesForSpawnSwitch();
         return GsonUtils.toJsonHTMLSafeString(pds);
      }
      catch (Exception e)
      {
         trace.error(e, e);
         NotificationMessageDTO exception = new NotificationMessageDTO();
         exception.message = e.getMessage();
         return GsonUtils.toJsonHTMLSafeString(exception);
      }
   }

   public String checkIfProcessesAbortable(String postedData, String type)
   {
      List<Long> processInstOIDs = JsonDTO.getAsList(postedData, Long.class);
      return GsonUtils.toJsonHTMLSafeString(processInstanceUtilsREST.checkIfProcessesAbortable(processInstOIDs, type));
   }

   public String switchProcess(String processData)
   {
      SwitchProcessDTO processDTO = GsonUtils.fromJson(processData, SwitchProcessDTO.class);

      return GsonUtils.toJsonHTMLSafeString(processInstanceUtilsREST.switchProcess(processDTO.processInstaceOIDs,
            processDTO.processId, processDTO.linkComment, processDTO.pauseParentProcess));
   }

   public String abortAndJoinProcess(String processData)
   {
      JoinProcessDTO processDTO = GsonUtils.fromJson(processData, JoinProcessDTO.class);

      return GsonUtils.toJsonHTMLSafeString(processInstanceUtilsREST.abortAndJoinProcess(
            Long.parseLong(processDTO.sourceProcessOID), Long.parseLong(processDTO.targetProcessOID),
            processDTO.linkComment));
   }

   public String getRelatedProcesses(String processData, boolean matchAny, boolean searchCases)
   {
      List<Long> processInstOIDs = JsonDTO.getAsList(processData, Long.class);

      return GsonUtils.toJsonHTMLSafeString(processInstanceUtilsREST.getRelatedProcesses(processInstOIDs, matchAny,
            searchCases));
   }

   /**
    * 
    */
   public List<ColumnDTO> getProcessesColumns()
   {
      List<ProcessDefinition> processes = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils
            .getAllBusinessRelevantProcesses();
      List<ColumnDTO> processColumns = new ArrayList<ColumnDTO>();

      for (ProcessDefinition processDefinition : processes)
      {
         processColumns.add(new ColumnDTO(processDefinition.getId(), I18nUtils.getProcessName(processDefinition)));
      }
      return processColumns;
   }

   /**
    * @param oid
    * @param fetchDescriptors
    * @return
    */
   public ProcessInstanceDTO getProcessByOid(Long oid, boolean fetchDescriptors)
   {
      ProcessInstance process = processInstanceUtilsREST.getProcessInstance(oid, fetchDescriptors, false);
      ProcessInstanceDTO dto = processInstanceUtilsREST.buildProcessInstanceDTO(process);
      return dto;
   }

   /**
    * @param oid
    * @return
    */
   public ProcessInstanceDTO getCorrespondenceProcessInstanceDTO(Long oid)
   {
      ProcessInstance processInstance = ProcessInstanceUtils.getCorrespondenceProcessInstance(oid);
      return processInstanceUtilsREST.buildProcessInstanceDTO(processInstance);   
   }
   
   /**
    * @param oid
    * @return
    * @throws ResourceNotFoundException 
    */
   public FolderDTO getCorrespondenceFolderDTO(Long oid) throws ResourceNotFoundException
   {
      ProcessInstance processInstance = ProcessInstanceUtils.getProcessInstance(oid);
      String correspondenceFolderPath = DocumentMgmtUtility.getCorrespondenceFolderPath(processInstance.getOID());
      return repositoryService.getFolder(correspondenceFolderPath, 2, false);
   }

   /**
    * 
    */
   public AbstractDTO findByStartingActivityOid(Long aOid)
   {
      ProcessInstance process =  processInstanceUtilsREST.findByStartingActivityOid(aOid);
      ProcessInstanceDTO dto =  processInstanceUtilsREST.buildProcessInstanceDTO(process);
      return dto;
   }
   
   /**
    * @param processInstanceOid
    * @return
    */
   @SuppressWarnings("unchecked")
   public List<AddressBookDataPathValueDTO> getAddressBookDTO(long processInstanceOid)
   {
      String faxFormat = CorrespondencePanelPreferenceUtils.getCorrespondencePreferenceForUser(
            processInstanceUtilsREST.getCurrentUser(), UserPreferencesEntries.F_CORRESPONDENCE_NUMBER_FORMAT);
      
      return (List<AddressBookDataPathValueDTO>) getDataPathValueDTO(getProcessInstance(processInstanceOid),
            new AddressBookDataPathValueFilter(faxFormat), null);
   }

   /**
    * @param processInstanceOid
    * @return
    */
   @SuppressWarnings({"unchecked"})
   public List<DataPathValueDTO> getAllDataPathValuesDTO(long processInstanceOid)
   {
      return (List<DataPathValueDTO>)getDataPathValueDTO(getProcessInstance(processInstanceOid), new DefaultDataPathValueFilter(), null);
   }

   /**
    * @param processInstanceOid
    * @param dataPathId
    * @return
    */
   @SuppressWarnings({"unchecked"})
   public List<DataPathValueDTO> getDataPathValueFor(long processInstanceOid, String dataPathId)
   {
      return (List<DataPathValueDTO>) getDataPathValueDTO(getProcessInstance(processInstanceOid), new DefaultDataPathValueFilter(), dataPathId);
   }
   
   /**
    * @param processInstance
    * @param dataPathValueFilter
    * @return
    */
   public List<? extends AbstractDTO> getDataPathValueDTO(ProcessInstance processInstance,
         IDataPathValueFilter dataPathValueFilter, String dataPathId)
   {
      List<? extends AbstractDTO> dataPathDtoList = new ArrayList<AbstractDTO>();
      ProcessDefinition processDefinition = processDefinitionUtils.getProcessDefinition(processInstance.getModelOID(),
            processInstance.getProcessID());

      if (dataPathValueFilter == null)
      {
         dataPathValueFilter = new DefaultDataPathValueFilter();
      }

      if (processDefinition != null)
      {
         List<DataPath> dataPaths = new ArrayList<DataPath>();
         if (StringUtils.isEmpty(dataPathId))
         {
            dataPaths = processDefinition.getAllDataPaths();
         }
         else
         {
            dataPaths.add(processDefinition.getDataPath(dataPathId));
         }

         Set<String> dataPathIds = new HashSet<String>();
         List<DataPath> inDataPaths = new ArrayList<DataPath>();
         
         for (DataPath dataPath : dataPaths)
         {
            if (dataPath == null)
            {
               trace.debug("There is no datapath one or more datapathIds:");
               continue;
            }

            if (dataPath.getDirection().equals(Direction.IN) || dataPath.getDirection().equals(Direction.IN_OUT))
            {
               dataPathIds.add(dataPath.getId());
               inDataPaths.add(dataPath);
            }
         }

         Map<String, Serializable> dataValues = ContextPortalServices.getWorkflowService().getInDataPaths(
               processInstance.getOID(), dataPathIds);

         for (DataPath dataPath : inDataPaths)
         {
            Object dataValue = dataValues.get(dataPath.getId());
            dataPathDtoList.addAll((List) dataPathValueFilter.filter(dataPath, dataValue));
         }
      }
      return dataPathDtoList;
   }
   
   public boolean setDataPaths(long processInstanceOid, Map<String, Object> dataPathMap)
   {
      try
      {
         ProcessInstance processInstance = processInstanceUtilsREST.getProcessInstance(processInstanceOid);
         ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(
               processInstance.getModelOID(), processInstance.getProcessID());
         List<DataPath> dataPaths = processDefinition.getAllDataPaths();
         Map<String, DataPath> outDataPathMap = CollectionUtils.newHashMap();
         Map<String, Object> outDataPathValues = CollectionUtils.newHashMap();
         trace.info("Process Instance OID ::"+ processInstanceOid);
         trace.info("List of DataPath received at setDataPath ::");
         for(Entry<String, Object> path : dataPathMap.entrySet())
         {
            trace.info("Data Path ID ---> "+ path.getKey() + " --> Value ---> " + path.getValue());
         }
         for (DataPath dataPath : dataPaths)
         {
            if (DescriptorFilterUtils.isDataFilterable(dataPath)
                  && !DmsConstants.PATH_ID_ATTACHMENTS.equals(dataPath.getId()))
            {
               
               if (dataPathMap.containsKey(dataPath.getId()) && !Direction.IN.equals(dataPath.getDirection()))
               {
                  outDataPathMap.put(dataPath.getId(), dataPath);
               }
            }
            else
            {
               trace.error(" Nested or Complex DataPath --> " + dataPath.getId() + " --> Type  "
                     + dataPath.getMappedType() + " --> Direction -->" + dataPath.getDirection());
            }
            
         }
         if (CollectionUtils.isNotEmpty(outDataPathMap))
         {
            for (Entry<String, DataPath> outPath : outDataPathMap.entrySet())
            {
               DataPath outDataPath = outPath.getValue();
               Object value = dataPathMap.get(outPath.getKey());
               outDataPathValues.put(outDataPath.getId(),
                     DescriptorFilterUtils.convertDataPathValue(outDataPath.getMappedType(), value));
            }
            
            ContextPortalServices.getWorkflowService().setOutDataPaths(processInstance.getOID(), outDataPathValues);
         }
         else
         {
            throw new I18NException(restCommonClientMessages.getString("processInstance.outDataPath.empty.error"));
         }
         return true;
      }
      catch (I18NException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new I18NException(restCommonClientMessages.getParamString("processInstance.dataPath.conversionError",
               String.valueOf(processInstanceOid)));
      }
   }

   /**
    * @param processInstanceOid
    * @return
    */
   public ProcessInstance getProcessInstance(long processInstanceOid)
   {
      return getProcessInstance(processInstanceOid, null);
   }
   
   /**
    * @param processInstanceOid
    * @param policies
    * @return
    */
   public ProcessInstance getProcessInstance(long processInstanceOid, List<EvaluationPolicy> policies)
   {
      ProcessInstance processInstance = processInstanceUtilsREST.getProcessInstance(processInstanceOid, policies);
      if (processInstance == null)
      {
         throw new I18NException(restCommonClientMessages.getParamString("processInstance.notFound",
               String.valueOf(processInstanceOid)));
      }
      return processInstance;
   }
}
