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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.AttachToCaseDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ColumnDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.CreateCaseDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.InstanceCountsDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.JoinProcessDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMap;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMap.NotificationDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMessageDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.SwitchProcessDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.converter.PriorityConverter;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
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

}
