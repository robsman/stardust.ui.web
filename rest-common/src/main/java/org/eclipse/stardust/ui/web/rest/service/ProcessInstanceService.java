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
package org.eclipse.stardust.ui.web.rest.service;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.exception.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.AttachToCaseDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ColumnDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.CreateCaseDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.FileInfoDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.InstanceCountsDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.JoinProcessDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMap;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMap.NotificationDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMessageDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.SwitchProcessDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DocumentDTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.response.AddressBookDataPathValueDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.DataPathValueDTO;
import org.eclipse.stardust.ui.web.rest.service.helpers.AddressBookDataPathValueFilter;
import org.eclipse.stardust.ui.web.rest.service.helpers.DefaultDataPathValueFilter;
import org.eclipse.stardust.ui.web.rest.service.helpers.IDataPathValueFilter;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.FileUploadUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.converter.PriorityConverter;
import org.eclipse.stardust.ui.web.viewscommon.common.exceptions.I18NException;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.services.ContextPortalServices;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
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
   private org.eclipse.stardust.ui.web.rest.service.utils.ProcessInstanceUtils processInstanceUtilsREST;

   @Resource
   private ProcessDefinitionUtils processDefinitionUtils;
   
   @Resource
   private RestCommonClientMessages restCommonClientMessages;
   
   public ProcessInstanceDTO startProcess(JsonObject json)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List<ProcessInstanceDTO> getPendingProcesses(JsonObject json)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public ProcessInstanceDTO removeProcessInstanceDocument(long processInstanceOid, String dataPathId, String documentId)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public DocumentDTO splitDocument(long processInstanceOid, String documentId, JsonObject json)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public ProcessInstanceDTO addProcessInstanceDocument(long parseLong, String dataPathId, JsonObject json)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @param processOid
    * @param attachments
    * @return
    * @throws Exception
    */
   public List<DocumentDTO> addProcessAttachments(long processOid, List<Attachment> attachments) throws Exception
   {
      ProcessInstance processInstance = processInstanceUtilsREST.getProcessInstance(processOid);
      return addProcessAttachments(processInstance, attachments);
   }

   /**
    * @param processInstance
    * @param attachments
    * @return
    * @throws Exception
    */
   public List<DocumentDTO> addProcessAttachments(ProcessInstance processInstance, List<Attachment> attachments)
         throws Exception
   {
      List<DocumentDTO> documents = new ArrayList<DocumentDTO>();
      for (Attachment attachment : attachments)
      {
         DataHandler dataHandler = attachment.getDataHandler();
         InputStream inputStream = dataHandler.getInputStream();
         MultivaluedMap<String, String> headers = attachment.getHeaders();

         FileInfoDTO fileInfo = FileUploadUtils.getFileInfo(headers);

         String fileName = fileInfo.name;
         if (fileName.lastIndexOf("\\") > 0)
         {
            fileName = fileName.substring(fileName.lastIndexOf("\\") + 1, fileName.length());
         }

         Folder processAttachmentsFolder = RepositoryUtility.getProcessAttachmentsFolder(processInstance);

         String docName = RepositoryUtility.createDocumentName(processAttachmentsFolder, fileName, 0);

         // create document
         Document document = DocumentMgmtUtility.createDocument(processAttachmentsFolder.getId(), docName,
               FileUploadUtils.readEntryData(inputStream), null, fileInfo.contentType, null, null, null, null);

         // update process attachment
         boolean added = DMSHelper.addAndSaveProcessAttachment(processInstance, document);
         if (added)
         {
            documents.add(DocumentDTOBuilder.build(document));
         }
      }

      return documents;
   }

   /**
    * Returns process attachemtents and specific documents, if supported.
    * 
    * @param processInstanceOid
    * @return
    */
   public Map<String, List<DocumentDTO>> getProcessInstanceDocuments(
         long processInstanceOid)
   {
      Map<String, List<DocumentDTO>> docs = new HashMap<String, List<DocumentDTO>>();
      
      ProcessInstance processInstance = processInstanceUtilsREST.getProcessInstance(processInstanceOid);
      if (processInstanceUtilsREST.supportsProcessAttachments(processInstance)) {
         docs.put(DmsConstants.PATH_ID_ATTACHMENTS, getProcessInstanceDocumentsForDataPath(processInstanceOid, DmsConstants.PATH_ID_ATTACHMENTS));
      }

      // Get dataPath documents
      ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(
            processInstance.getModelOID(), processInstance.getProcessID());      
      List<DataPath> dataPaths = processDefinition.getAllDataPaths();
      Model model = ModelCache.findModelCache().getModel(processInstance.getModelOID());
      for (DataPath dataPath : dataPaths)
      {
         if (Direction.IN.equals(dataPath.getDirection())
               && !DmsConstants.PATH_ID_ATTACHMENTS.equals(dataPath.getId()))
         {
            DataDetails dataDetails = (DataDetails) model.getData(dataPath.getData());
            if (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataDetails.getTypeId())
                  || DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(dataDetails.getTypeId())) {
               List<Document> dataPathDocs = processInstanceUtilsREST.getProcessInstanceDocumentsForDataPath(
                     processInstance, dataPath.getId());
               if (dataPathDocs.size() > 0)
               {
                  docs.put(dataPath.getId(), DocumentDTOBuilder.build(dataPathDocs));
               }
            }
            break;
         }
      }

      return docs;
   }

   /**
    * Returns documents for the fivne data path.
    * dataPathId can either be PROCESS_ATTACHMENTS or a document or documentList dataPath id.
    * 
    * @param processInstanceOid
    * @return
    */
   public List<DocumentDTO> getProcessInstanceDocumentsForDataPath(
         long processInstanceOid, String dataPathId)
   {
      // Return empty array if no documents are found
      List<DocumentDTO> docs = new ArrayList<DocumentDTO>();
      if (DmsConstants.PATH_ID_ATTACHMENTS.equals(dataPathId))
      {
         // Get Process attachments
         List<Document> processAttachments = processInstanceUtilsREST.getProcessAttachments(processInstanceOid);
         if (null != processAttachments)
         {
            docs = DocumentDTOBuilder.build(processAttachments);
         }
      }
      else
      {
         // Get dataPath documents
         ProcessInstance processInstance = processInstanceUtilsREST.getProcessInstance(processInstanceOid);
         List<Document> dataPathDocs = processInstanceUtilsREST.getProcessInstanceDocumentsForDataPath(
               processInstance, dataPathId);
         if (null != dataPathDocs && dataPathDocs.size() > 0)
         {
            docs = DocumentDTOBuilder.build(dataPathDocs);
         }
      }

      return docs;
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

   public QueryResultDTO getProcessInstances(ProcessInstanceQuery query, Options options)
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
            processDTO.processId, processDTO.linkComment));
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
    * @return 
    * 
    */
   public ProcessInstanceDTO getProcessByOid(Long oid, boolean fetchDescriptors)
   {
      ProcessInstance process =  processInstanceUtilsREST.getProcessInstance(oid, fetchDescriptors);
      ProcessInstanceDTO dto =  processInstanceUtilsREST.buildProcessInstanceDTO(process);
      return dto;
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
      return (List<AddressBookDataPathValueDTO>)getDataPathValueDTO(getProcessInstance(processInstanceOid), new AddressBookDataPathValueFilter(), null);
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
   
   /**
    * @param processInstanceOid
    * @return
    */
   public ProcessInstance getProcessInstance(long processInstanceOid)
   {
      ProcessInstance processInstance = processInstanceUtilsREST.getProcessInstance(processInstanceOid);
      if (processInstance == null)
      {
         throw new I18NException(restCommonClientMessages.getParamString("processInstance.notFound",
               String.valueOf(processInstanceOid)));
      }
      return processInstance;
   }
}
