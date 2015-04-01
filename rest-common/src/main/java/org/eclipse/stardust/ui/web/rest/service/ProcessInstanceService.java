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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.AttachToCaseDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.CreateCaseDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DescriptorDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.InstanceCountsDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMap;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMap.NotificationDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMessageDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.PriorityDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.StatusDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.converter.PriorityConverter;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentInfo;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDescriptor;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDocumentDescriptor;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
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

   public ProcessInstanceDTO removeProcessInstanceDocument(long processInstanceOid,
         String dataPathId, String documentId)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public DocumentDTO splitDocument(long processInstanceOid, String documentId,
         JsonObject json)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public ProcessInstanceDTO addProcessInstanceDocument(long parseLong,
         String dataPathId, JsonObject json)
   {
      // TODO Auto-generated method stub
      return null;
   }
   

   /**
    * Updates the process priorities
    * 
    * @param type
    * @param oidPriorityMap
    * @return
    */
   public String updatePriorities( Map<String, Integer> oidPriorityMap)
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
    * Returns all states
    * 
    * @return List
    */
   public List<StatusDTO> getAllProcessStates()
   {
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      List<StatusDTO> allStatusList = new ArrayList<StatusDTO>();
      
      allStatusList.add(new StatusDTO(ProcessInstanceState.CREATED, propsBean
            .getString("views.processTable.statusFilter.created")));
      allStatusList.add(new StatusDTO(ProcessInstanceState.ACTIVE, propsBean
            .getString("views.processTable.statusFilter.active")));
      
      allStatusList.add(new StatusDTO(ProcessInstanceState.INTERRUPTED, propsBean
            .getString("views.processTable.statusFilter.interrupted")));
      allStatusList.add(new StatusDTO(ProcessInstanceState.ABORTED, propsBean
            .getString("views.processTable.statusFilter.aborted")));
      allStatusList.add(new StatusDTO(ProcessInstanceState.COMPLETED, propsBean
            .getString("views.processTable.statusFilter.completed")));
      allStatusList.add(new StatusDTO(ProcessInstanceState.ABORTING, propsBean
            .getString("views.processTable.statusFilter.aborting")));
      
      return allStatusList;
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
      List<Long> processes = (List<Long>) GsonUtils.extractList(GsonUtils.extractJsonArray(json, "processes"), listType);

      if ("root".equalsIgnoreCase(scope))
      {
         notificationMap = processInstanceUtilsREST.abortProcesses(AbortScope.RootHierarchy, processes);
      }
      else
      {
         notificationMap = processInstanceUtilsREST.abortProcesses(AbortScope.SubHierarchy, processes);
      }

      return GsonUtils.toJsonHTMLSafeString(notificationMap);
   }
   
   public QueryResultDTO getProcessInstances(Options options)
   {
      QueryResult<? extends ProcessInstance> queryResult = processInstanceUtilsREST.getProcessInstances(options);
      return buildProcessListResult(queryResult);
   }
   
   /**
    * @param queryResult
    * @return
    */
   private QueryResultDTO buildProcessListResult(QueryResult<?> queryResult)
   {
      List<ProcessInstanceDTO> list = new ArrayList<ProcessInstanceDTO>();

      for (Object object : queryResult)
      {
         if (object instanceof ProcessInstance)
         {
            ProcessInstance processInstance = (ProcessInstance) object;

            ProcessInstanceDTO dto = new ProcessInstanceDTO();
            
            ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(processInstance.getModelOID(),
                  processInstance.getProcessID());
            
            dto.processInstanceRootOID = processInstance.getRootProcessInstanceOID();
            dto.oid = processInstance.getOID();
            
            PriorityDTO priority = new PriorityDTO();
            priority.value = processInstance.getPriority();
            priority.setLabel(processInstance.getPriority());
            priority.setName(processInstance.getPriority());
            
            dto.priority = priority;
            dto.startTime = processInstance.getStartTime();
            dto.duration = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils.getDuration(processInstance);
            dto.processName = I18nUtils.getProcessName(processDefinition);
            String startingUserLabel = UserUtils.getUserDisplayLabel(processInstance.getStartingUser());
            dto.createUser = startingUserLabel;
            dto.descriptorValues = getDescriptorValues(processInstance, processDefinition);
            dto.processDescriptorsList = getProcessDescriptor(processInstance, processDefinition);
            // Update Document Descriptors for process
            CommonDescriptorUtils.updateProcessDocumentDescriptors(((ProcessInstanceDetails) processInstance).getDescriptors(),
                  processInstance, processDefinition);
            
            dto.endTime = processInstance.getTerminationTime();
            dto.startingUser = startingUserLabel;
            dto.status = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils.getProcessStateLabel(processInstance);
            dto.enableTerminate = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils.isAbortable(processInstance);
            dto.enableRecover = true;
            dto.checkSelection = false;
            dto.modifyProcessInstance = AuthorizationUtils.hasPIModifyPermission(processInstance);
            
            List<Note> notes=org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils.getNotes(processInstance);
            if(null!=notes)
            {
               dto.notesCount = notes.size();   
            }
            dto.caseInstance = processInstance.isCaseProcessInstance();
            if (dto.caseInstance)
            {
               dto.caseOwner = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils.getCaseOwnerName(processInstance);
            }
            
            dto.oldPriority = dto.priority;

            list.add(dto);
         }
      }

      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = list;
      resultDTO.totalCount = queryResult.getTotalCount();

      return resultDTO;
   }
   
   /*
    * 
    */
   private Map<String, DescriptorDTO> getDescriptorValues(ProcessInstance processInstance, ProcessDefinition processDefinition)
   {
      if (processInstance != null && processDefinition != null)
      {
         List<ProcessDescriptor> processDescriptorsList = CollectionUtils.newList();

         ModelCache modelCache = ModelCache.findModelCache();
         Model model = modelCache.getModel(processInstance.getModelOID());
         ProcessInstanceDetails processInstanceDetails = (ProcessInstanceDetails) processInstance;
         Map<String, Object> descriptorValues = processInstanceDetails.getDescriptors();
         CommonDescriptorUtils.updateProcessDocumentDescriptors(descriptorValues, processInstance, processDefinition);
         if (processInstanceDetails.isCaseProcessInstance())
         {
            processDescriptorsList = CommonDescriptorUtils.createCaseDescriptors(
                  processInstanceDetails.getDescriptorDefinitions(), descriptorValues, processDefinition, true);
         }
         else
         {
            processDescriptorsList = CommonDescriptorUtils.createProcessDescriptors(descriptorValues, processDefinition, true,
                  true);

         }

         if (!processDescriptorsList.isEmpty())
         {
            return getProcessDescriptors(processDescriptorsList);
         }
      }

      return new LinkedHashMap<String, DescriptorDTO>();
   }

   /**
    * 
    */
   private Map<String, DescriptorDTO> getProcessDescriptors(List<ProcessDescriptor> processDescriptorsList)
   {
      Map<String, DescriptorDTO> descriptors = new LinkedHashMap<String, DescriptorDTO>();
      for (Object descriptor : processDescriptorsList)
      {
         if (descriptor instanceof ProcessDocumentDescriptor)
         {
            ProcessDocumentDescriptor desc = (ProcessDocumentDescriptor) descriptor;

            List<DocumentDTO> documents = new ArrayList<DocumentDTO>();

            for (DocumentInfo documentInfo : desc.getDocuments())
            {
               DocumentDTO documentDTO = new DocumentDTO();
               documentDTO.name = documentInfo.getName();
               documentDTO.uuid = documentInfo.getId();
               documentDTO.contentType = (MimeTypesHelper.detectMimeType(documentInfo.getName(), null).getType());
               documents.add(documentDTO);
            }

            DescriptorDTO descriptorDto = new DescriptorDTO(desc.getKey(), desc.getValue(), true, documents);
            descriptors.put(desc.getId(), descriptorDto);
         }
         else
         {
            ProcessDescriptor desc = (ProcessDescriptor) descriptor;
            DescriptorDTO descriptorDto = new DescriptorDTO(desc.getKey(), desc.getValue(), false, null);
            descriptors.put(desc.getId(), descriptorDto);
         }
      }
      return descriptors;
   }

   /*
     * 
     */
   private List<ProcessDescriptor> getProcessDescriptor(ProcessInstance processInstance, ProcessDefinition processDefinition)
   {
      List<ProcessDescriptor> processDescriptorsList = null;
      if (processDefinition != null)
      {
         ProcessInstanceDetails processInstanceDetails = (ProcessInstanceDetails) processInstance;

         if (processInstance.isCaseProcessInstance())
         {
            processDescriptorsList = CommonDescriptorUtils.createCaseDescriptors(
                  processInstanceDetails.getDescriptorDefinitions(), processInstanceDetails.getDescriptors(), processDefinition,
                  true);
         }
         else
         {
            processDescriptorsList = CommonDescriptorUtils.createProcessDescriptors(processInstanceDetails.getDescriptors(),
                  processDefinition, true);
         }
      }
      else
      {
         processDescriptorsList = CollectionUtils.newArrayList();
      }
      return processDescriptorsList;
   }

}
