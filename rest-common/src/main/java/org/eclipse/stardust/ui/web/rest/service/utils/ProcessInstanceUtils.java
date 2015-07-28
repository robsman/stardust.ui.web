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
package org.eclipse.stardust.ui.web.rest.service.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.CustomOrderCriterion;
import org.eclipse.stardust.engine.api.query.DataOrder;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessDefinitions;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.rest.FilterDTO.BooleanDTO;
import org.eclipse.stardust.ui.web.rest.FilterDTO.RangeDTO;
import org.eclipse.stardust.ui.web.rest.FilterDTO.TextSearchDTO;
import org.eclipse.stardust.ui.web.rest.JsonMarshaller;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.AbortNotificationDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DescriptorDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.InstanceCountsDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMap;
import org.eclipse.stardust.ui.web.rest.service.dto.StatusDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMap.NotificationDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMessageDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.PriorityDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessTableFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessTableFilterDTO.DescriptorFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.RelatedProcessesDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.response.ParticipantDTO;
import org.eclipse.stardust.ui.web.viewscommon.common.DateRange;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.GenericDescriptorFilterModel;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.NumberRange;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentInfo;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils.ParticipantType;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessContextCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDescriptor;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDocumentDescriptor;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component("ProcessInstanceUtilsREST")
public class ProcessInstanceUtils
{

   private static final Logger trace = LogManager.getLogger(ProcessInstanceUtils.class);

   private static final String COL_PROCESS_NAME = "processName";

   private static final String COL_PROCESS_INSTANCE_OID = "processOID";

   private static final String PROCESS_INSTANCE_ROOT_OID = "rootPOID";

   private static final String COL_START_TIME = "startTime";

   private static final String COL_END_TIME = "endTime";

   private static final String COL_STATUS = "status";

   private static final String COL_PRIOIRTY = "priority";

   private static final String STARTING_USER = "startingUser";

   private static final String COL_BENCHMARK = "benchmark";

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private ModelUtils modelUtils;

   @Resource
   private ProcessDefinitionUtils processDefinitionUtils;

   /**
    * @param oid
    * @return
    */
   public ProcessInstance getProcessInstance(long oid)
   {
      ProcessInstance pi = null;
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      query.where(ProcessInstanceQuery.OID.isEqual(oid));
      ProcessInstances pis = serviceFactoryUtils.getQueryService().getAllProcessInstances(query);

      if (!pis.isEmpty())
      {
         pi = pis.get(0);
      }

      return pi;
   }

   /**
    * @param instance
    * @return localized process name with OID appended
    */
   public String getProcessLabel(ProcessInstance instance)
   {
      if (null != instance)
      {
         StringBuilder processLabel = new StringBuilder(I18nUtils.getProcessName(processDefinitionUtils
               .getProcessDefinition(instance.getModelOID(), instance.getProcessID())));
         processLabel.append(" (").append("#").append(instance.getOID()).append(")");
         return processLabel.toString();
      }
      return "";
   }

   /**
    * 
    * @param processInstance
    * @return
    */
   public boolean isRootProcessInstance(ProcessInstance processInstance)
   {
      return processInstance.getRootProcessInstanceOID() == processInstance.getOID() ? true : false;
   }

   /**
    * 
    * @param processInstance
    * @return
    */
   public boolean isActiveProcessInstance(ProcessInstance processInstance)
   {
      if (ProcessInstanceState.ACTIVE != processInstance.getState().getValue())
      {
         return false;
      }
      return true;
   }

   private boolean hasManageCasePermission(List<ProcessInstance> sourceProcessInstances)
   {
      for (ProcessInstance pi : sourceProcessInstances)
      {
         if (!AuthorizationUtils.hasManageCasePermission(pi))
         {
            return false;
         }
      }
      return true;
   }

   /**
    * 
    * @param processInstances
    * @return
    */
   public boolean isRootProcessInstances(List<ProcessInstance> processInstances)
   {
      for (ProcessInstance processInstance : processInstances)
      {
         if (processInstance.getRootProcessInstanceOID() != processInstance.getOID())
         {
            return false;
         }
      }
      return true;
   }

   /**
    * @param oids
    * @return
    */
   public List<ProcessInstance> getProcessInstances(List<Long> oids)
   {
      return getProcessInstances(oids, false, false);
   }

   /**
    * 
    * @param oids
    * @param forceReload
    * @param withDescriptors
    * @return list of ProcessInstance
    */
   public List<ProcessInstance> getProcessInstances(List<Long> oids, boolean forceReload, boolean withDescriptors)
   {

      List<ProcessInstance> processInstances = ProcessContextCacheManager.getInstance().getProcessInstances(oids,
            forceReload, withDescriptors);

      if (null == processInstances)
      {
         processInstances = CollectionUtils.newList();
         if (!oids.isEmpty())
         {
            ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAll();
            FilterOrTerm orTerm = piQuery.getFilter().addOrTerm();

            if (withDescriptors)
            {
               piQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
            }

            // Prepare Data to fetch
            for (Long oid : oids)
            {
               orTerm.add(new ProcessInstanceFilter(oid, false));
            }

            // Fetch the Data from Engine

            ProcessInstances pis = serviceFactoryUtils.getQueryService().getAllProcessInstances(piQuery);
            if (null != pis)
            {
               for (ProcessInstance pi : pis)
               {
                  processInstances.add(pi);
               }
            }
         }
      }

      return processInstances;
   }

   /**
    * 
    * @param processInstance
    * @return
    */
   public static String getCaseOwnerName(ProcessInstance processInstance)
   {
      String caseOwnerLabel = null;
      ActivityInstance activityInstance = ActivityInstanceUtils.getActivityInstance(processInstance);
      if (null != activityInstance.getCurrentPerformer())
      {
         ParticipantType participantType = ParticipantUtils.getParticipantType(activityInstance.getCurrentPerformer());

         if (ParticipantType.SCOPED_ORGANIZATION.equals(participantType)
               || ParticipantType.SCOPED_ROLE.equals(participantType))
         {
            caseOwnerLabel = ModelHelper.getParticipantName(activityInstance.getCurrentPerformer());
         }
         else
         {
            Participant participant = ParticipantUtils.getParticipant(activityInstance.getCurrentPerformer());

            if (null != participant && participant instanceof User)
            {
               caseOwnerLabel = I18nUtils.getUserLabel((User) participant);
            }
            else if (null != participant)
            {
               caseOwnerLabel = I18nUtils.getParticipantName(participant);
            }
            else
            {
               caseOwnerLabel = activityInstance.getUserPerformerName();
            }
         }

      }
      if (null == caseOwnerLabel)
      {
         caseOwnerLabel = activityInstance.getParticipantPerformerName();
      }
      return caseOwnerLabel;

   }

   /**
    * method to find root process of given process without descriptor If checkCaseInstance
    * is true check for caseInstance , and return sourcePI/ParentPI rather than rootPI
    * 
    * @param sourceProcessInstance
    * @param checkCaseInstance
    * @return root process instance
    */
   public ProcessInstance getRootProcessInstance(ProcessInstance sourceProcessInstance, boolean checkCaseInstance)
   {
      if (sourceProcessInstance.getRootProcessInstanceOID() != sourceProcessInstance.getOID())
      {
         ProcessInstance rootProcessInstance = getProcessInstance(sourceProcessInstance.getRootProcessInstanceOID());
         if (checkCaseInstance)
         {
            if (rootProcessInstance.isCaseProcessInstance())
            {
               if (sourceProcessInstance.getParentProcessInstanceOid() > 0
                     & (sourceProcessInstance.getParentProcessInstanceOid() != sourceProcessInstance
                           .getRootProcessInstanceOID()))
               {
                  return getProcessInstance(sourceProcessInstance.getParentProcessInstanceOid());
               }
               else
               {
                  return sourceProcessInstance;
               }
            }
            else
            {
               return rootProcessInstance;
            }
         }
         else
         {
            return rootProcessInstance;
         }

      }
      return sourceProcessInstance;
   }

   /**
    * @param processInstance
    * @return
    */
   public boolean isAbortable(ProcessInstance processInstance)
   {
      boolean abortable = isAbortableState(processInstance) && AuthorizationUtils.hasAbortPermission(processInstance);
      return abortable;
   }

   /**
    * @param processInstance
    * @return
    */
   public boolean isAbortableState(ProcessInstance processInstance)
   {
      boolean abortable = processInstance == null ? false : !ProcessInstanceState.Aborted.equals(processInstance
            .getState()) && !ProcessInstanceState.Completed.equals(processInstance.getState());
      return abortable;
   }

   /**
    * 
    * @param processInstances
    * @return
    */
   public boolean isCaseWithNoncaseProcessInstances(List<ProcessInstance> processInstances)
   {
      if (processInstances.size() > 1)
      {
         boolean containCase = false;
         boolean containNonCase = false;
         for (ProcessInstance processInstance : processInstances)
         {
            if (processInstance.isCaseProcessInstance())
            {
               containCase = true;
            }
            else
            {
               containNonCase = true;
            }
            if (containCase && containNonCase)
            {
               return true;
            }

         }
         return false;
      }
      return false;

   }

   /**
    * 
    * @param processInstances
    * @return
    */
   public boolean isCaseProcessInstances(List<ProcessInstance> processInstances)
   {
      for (ProcessInstance processInstance : processInstances)
      {
         if (!processInstance.isCaseProcessInstance())
         {
            return false;
         }
      }
      return true;
   }

   /**
    * 
    * @param processInstances
    * @return
    */
   public boolean isActiveProcessInstances(List<ProcessInstance> processInstances)
   {
      for (ProcessInstance processInstance : processInstances)
      {
         if (ProcessInstanceState.ACTIVE != processInstance.getState().getValue())
         {
            return false;
         }
      }
      return true;
   }

   /**
    * 
    * @param processInstances
    * @return
    */
   public boolean isTerminatedProcessInstances(List<ProcessInstance> processInstances)
   {
      for (ProcessInstance processInstance : processInstances)
      {
         if (ProcessInstanceState.COMPLETED == processInstance.getState().getValue()
               || ProcessInstanceState.ABORTED == processInstance.getState().getValue())
         {
            return true;
         }
      }
      return false;
   }

   /**
    * 
    * @param processInstances
    * @return
    */
   public boolean isNonCaseProcessInstances(List<ProcessInstance> processInstances)
   {
      for (ProcessInstance processInstance : processInstances)
      {
         if (processInstance.isCaseProcessInstance())
         {
            return false;
         }
      }
      return true;
   }

   /**
    * @param oid
    * @return
    */
   public List<Document> getProcessAttachments(long oid)
   {
      List<Document> processAttachments = CollectionUtils.newArrayList();

      boolean supportsProcessAttachments = supportsProcessAttachments(oid);
      if (supportsProcessAttachments)
      {
         ProcessInstance processInstance = getProcessInstance(oid);

         WorkflowService ws = serviceFactoryUtils.getWorkflowService();
         Object o = ws.getInDataPath(processInstance.getOID(), DmsConstants.PATH_ID_ATTACHMENTS);

         DataDetails data = (DataDetails) modelUtils.getModel(processInstance.getModelOID()).getData(
               DmsConstants.PATH_ID_ATTACHMENTS);
         if (DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(data.getTypeId()))
         {
            processAttachments = (List<Document>) o;
         }
      }

      return processAttachments;
   }

   /**
    * return true if the provided Process Instance supports Process Attachments
    *
    * @param oid
    * @return
    */
   public boolean supportsProcessAttachments(long oid)
   {
      boolean supportsProcessAttachments = false;

      ProcessInstance processInstance = getProcessInstance(oid);

      if (processInstance != null)
      {
         Model model = modelUtils.getModel(processInstance.getModelOID());
         ProcessDefinition pd = model != null ? model.getProcessDefinition(processInstance.getProcessID()) : null;

         supportsProcessAttachments = processDefinitionUtils.supportsProcessAttachments(pd);
      }

      return supportsProcessAttachments;
   }

   /**
    * Get all process instances count
    * 
    * @return List
    */
   public InstanceCountsDTO getAllCounts()
   {

      InstanceCountsDTO countDTO = new InstanceCountsDTO();

      try
      {
         countDTO.aborted = getAbortedProcessInstancesCount();
         countDTO.active = getActiveProcessInstancesCount();
         countDTO.total = getTotalProcessInstancesCount();
         countDTO.interrupted = getInterruptedProcessInstancesCount();
         countDTO.completed = getCompletedProcessInstancesCount();
      }
      catch (PortalException e)
      {
         trace.error("Error occurred.", e);
      }

      return countDTO;

   }

   public void recoverProcesses(List<Long> processOids)
   {
      if (processOids != null && !CollectionUtils.isEmpty(processOids))
      {
         AdministrationService adminService = serviceFactoryUtils.getAdministrationService();
         if (adminService != null)
         {
            adminService.recoverProcessInstances(processOids);
         }
      }
   }

   public List<ProcessDefinition> getTargetProcessesForSpawnSwitch() throws Exception
   {
      ProcessDefinitions pds = serviceFactoryUtils.getQueryService().getProcessDefinitions(
            ProcessDefinitionQuery.findStartable());

      List<ProcessDefinition> filteredPds = new ArrayList<ProcessDefinition>(pds);
      processDefinitionUtils.sort(filteredPds);

      return filteredPds;
   }

   public List<AbortNotificationDTO> checkIfProcessesAbortable(List<Long> processInstOIDs, String abortType)
   {
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      List<AbortNotificationDTO> notAbortableProcesses = new ArrayList<AbortNotificationDTO>();

      for (Long processInstOID : processInstOIDs)
      {
         ProcessInstance processInstance = getProcessInstance(processInstOID);
         processInstance = getRootProcessInstance(processInstance, true);

         ProcessInstanceDTO processInstanceDTO = new ProcessInstanceDTO();
         processInstanceDTO.processName = getProcessLabel(processInstance);
         processInstanceDTO.oid = processInstance.getOID();

         AbortNotificationDTO switchNotificationDTO = null;

         if ("abortandstart".equals(abortType))
         {
            if (!AuthorizationUtils.hasAbortPermission(processInstance))
            {
               switchNotificationDTO = new AbortNotificationDTO();
               switchNotificationDTO.statusMessage = propsBean.getString("common.authorization.msg");
            }
            else if (!isAbortable(processInstance))
            {
               switchNotificationDTO = new AbortNotificationDTO();
               switchNotificationDTO.statusMessage = propsBean.getString("common.notifyProcessAlreadyAborted");
            }
            else if (processInstance.isCaseProcessInstance())
            {
               switchNotificationDTO = new AbortNotificationDTO();
               switchNotificationDTO.statusMessage = propsBean.getString("views.switchProcessDialog.caseAbort.message");
            }
         }
         else if ("abortandjoin".equals(abortType))
         {
            if (processInstance.isCaseProcessInstance() && !AuthorizationUtils.hasManageCasePermission(processInstance))
            {
               switchNotificationDTO = new AbortNotificationDTO();
               switchNotificationDTO.statusMessage = propsBean.getString("common.authorization.msg");
            }
            else if (!isAbortableState(processInstance))
            {
               switchNotificationDTO = new AbortNotificationDTO();
               switchNotificationDTO.statusMessage = propsBean.getString("common.notifyProcessAlreadyAborted");
            }
         }

         if (switchNotificationDTO != null)
         {
            switchNotificationDTO.abortedProcess = processInstanceDTO;

            notAbortableProcesses.add(switchNotificationDTO);
         }
      }

      return notAbortableProcesses;
   }

   public List<AbortNotificationDTO> switchProcess(List<Long> processInstOIDs, String processId, String linkComment)
   {
      List<AbortNotificationDTO> newProcessInstances = new ArrayList<AbortNotificationDTO>();

      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();

      for (Long processInstOID : processInstOIDs)
      {
         ProcessInstance srcProcessInstance = getProcessInstance(processInstOID);

         // First check the permission
         if (!AuthorizationUtils.hasAbortPermission(srcProcessInstance) || !isAbortable(srcProcessInstance)
               || srcProcessInstance.isCaseProcessInstance())
         {
            continue;
         }

         ProcessInstanceDTO source = new ProcessInstanceDTO();
         source.processName = getProcessLabel(srcProcessInstance);
         source.oid = srcProcessInstance.getOID();

         ProcessInstanceDTO target = null;

         AbortNotificationDTO switchNotificationDTO = new AbortNotificationDTO();
         switchNotificationDTO.abortedProcess = source;

         try
         {
            ProcessInstance pi = serviceFactoryUtils.getWorkflowService().spawnPeerProcessInstance(processInstOID,
                  processId, true, null, true, linkComment);

            if (pi != null)
            {
               target = new ProcessInstanceDTO();
               target.processName = getProcessLabel(pi);
               target.oid = pi.getOID();

               switchNotificationDTO.targetProcess = target;
               switchNotificationDTO.statusMessage = propsBean.getString("common.success");
            }
         }
         catch (Exception e)
         {
            trace.error("Unable to abort the process with oid: " + processInstOID + " and target process id: "
                  + processId);
            trace.error(e, e);

            switchNotificationDTO.statusMessage = propsBean.getString("common.fail");
         }

         newProcessInstances.add(switchNotificationDTO);
      }

      return newProcessInstances;
   }

   public AbortNotificationDTO abortAndJoinProcess(Long sourceProcessInstanceOid, Long targetProcessInstanceOid,
         String linkComment)
   {
      boolean caseScope = false;
      if (sourceProcessInstanceOid != null)
      {
         ProcessInstance processInstance = getProcessInstance(sourceProcessInstanceOid);
         caseScope = processInstance.isCaseProcessInstance();
      }

      ProcessInstance srcProcessInstance = getProcessInstance(sourceProcessInstanceOid);

      AbortNotificationDTO joinNotificationDTO = new AbortNotificationDTO();

      ProcessInstanceDTO source = new ProcessInstanceDTO();
      source.processName = getProcessLabel(srcProcessInstance);
      source.oid = srcProcessInstance.getOID();
      joinNotificationDTO.abortedProcess = source;

      // Validation cases
      NotificationMessageDTO validationMessage = validateAbortAndJoin(caseScope, sourceProcessInstanceOid,
            targetProcessInstanceOid);
      if (validationMessage != null)
      {
         // Validation fails
         joinNotificationDTO.statusMessage = validationMessage.message;

         return joinNotificationDTO;
      }

      ProcessInstance targetProcessInstance = null;

      if (!srcProcessInstance.isCaseProcessInstance())
      {
         targetProcessInstance = serviceFactoryUtils.getWorkflowService().joinProcessInstance(sourceProcessInstanceOid,
               targetProcessInstanceOid, linkComment);
      }
      else
      {
         targetProcessInstance = serviceFactoryUtils.getWorkflowService().mergeCases(sourceProcessInstanceOid,
               new long[] {sourceProcessInstanceOid}, linkComment);

         CommonDescriptorUtils.reCalculateCaseDescriptors(srcProcessInstance);
         CommonDescriptorUtils.reCalculateCaseDescriptors(targetProcessInstance);
      }

      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();

      if (targetProcessInstance != null)
      {
         ProcessInstanceDTO target = new ProcessInstanceDTO();
         target.processName = getProcessLabel(targetProcessInstance);
         target.oid = targetProcessInstance.getOID();
         joinNotificationDTO.targetProcess = target;
      }

      joinNotificationDTO.abortedProcess = source;

      joinNotificationDTO.statusMessage = propsBean.getString("common.success");

      return joinNotificationDTO;
   }

   public List<RelatedProcessesDTO> getRelatedProcesses(List<Long> processInstOIDs, boolean matchAny,
         boolean searchCases)
   {
      List<ProcessInstance> sourceProcessInstances = new ArrayList<ProcessInstance>();
      for (Long processInstOID : processInstOIDs)
      {
         ProcessInstance srcProcessInstance = getProcessInstance(processInstOID);
         sourceProcessInstances.add(srcProcessInstance);
      }

      List<ProcessInstance> result = RelatedProcessSearchUtils.getProcessInstances(sourceProcessInstances, matchAny,
            searchCases);

      List<RelatedProcessesDTO> relatedProcesses = new ArrayList<RelatedProcessesDTO>();

      for (ProcessInstance pi : result)
      {
         relatedProcesses.add(getRelatedProcessesDTO(pi));
      }

      return relatedProcesses;
   }

   public NotificationMessageDTO attachToCase(List<Long> sourceProcessInstanceOids, Long targetOid)
   {
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();

      if (null == sourceProcessInstanceOids || CollectionUtils.isEmpty(sourceProcessInstanceOids))
      {
         NotificationMessageDTO exception = new NotificationMessageDTO();
         exception.success = false;
         exception.message = propsBean.getString("views.attachToCase.selectProcessToAttachToCase");

         return exception;
      }

      boolean caseScope = false;
      if (sourceProcessInstanceOids.size() == 1)
      {
         ProcessInstance processInstance = getProcessInstance(sourceProcessInstanceOids.get(0));
         caseScope = processInstance.isCaseProcessInstance();
      }

      // Validation cases
      NotificationMessageDTO validationMessage = validateAttachToCase(caseScope, sourceProcessInstanceOids, targetOid);
      if (validationMessage != null)
      {
         return validationMessage;
      }

      long[] members;
      Long caseOID = null;
      if (!caseScope)
      {
         caseOID = targetOid;
         members = new long[sourceProcessInstanceOids.size()];
         for (int i = 0; i < members.length; i++)
         {
            members[i] = sourceProcessInstanceOids.get(i);
         }
      }
      else
      {
         caseOID = sourceProcessInstanceOids.get(0);
         members = new long[] {targetOid};
      }

      ProcessInstance caseInstance = getProcessInstance(caseOID);
      if (AuthorizationUtils.hasManageCasePermission(caseInstance))
      {
         serviceFactoryUtils.getWorkflowService().joinCase(caseOID, members);
         CommonDescriptorUtils.reCalculateCaseDescriptors(caseInstance);

         String message = "";
         if (caseScope)
         {

            message = propsBean.getParamString("views.attachToCase.successProcessAttachToCase.message",
                  getProcessLabel(getProcessInstance(sourceProcessInstanceOids.get(0))), getProcessLabel(caseInstance));
         }
         else
         {
            message = propsBean.getParamString("views.attachToCase.successProcessesAttachToCase.message",
                  getProcessLabel(caseInstance));
         }

         NotificationMessageDTO success = new NotificationMessageDTO();
         success.success = true;
         success.message = message;

         return success;
      }
      else
      {
         NotificationMessageDTO exception = new NotificationMessageDTO();
         exception.success = false;
         exception.message = propsBean.getString("views.attachToCase.caseAttach.notAuthorizedToManageCase");

         return exception;
      }
   }

   public NotificationMessageDTO createCase(List<Long> sourceProcessInstanceOids, String caseName, String description,
         String note)
   {
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      try
      {
         // Validation cases
         NotificationMessageDTO validationMessage = validateCreateCase(sourceProcessInstanceOids, caseName);
         if (validationMessage != null)
         {
            return validationMessage;
         }

         // create Case
         long[] processOIDs = new long[sourceProcessInstanceOids.size()];
         for (int i = 0; i < processOIDs.length; i++)
         {
            processOIDs[i] = sourceProcessInstanceOids.get(i);
         }
         ProcessInstance caseProcessInstance = serviceFactoryUtils.getWorkflowService().createCase(caseName,
               description, processOIDs);
         CommonDescriptorUtils.reCalculateCaseDescriptors(caseProcessInstance);

         // add notes
         if (StringUtils.isNotEmpty(note))
         {
            ProcessInstanceAttributes attributes = caseProcessInstance.getAttributes();
            attributes.addNote(note, ContextKind.ProcessInstance, caseProcessInstance.getOID());
            serviceFactoryUtils.getWorkflowService().setProcessInstanceAttributes(attributes);
         }

         NotificationMessageDTO success = new NotificationMessageDTO();
         success.success = true;
         success.message = String.valueOf(caseProcessInstance.getOID());

         return success;
      }
      catch (Exception e)
      {
         NotificationMessageDTO exception = new NotificationMessageDTO();
         exception.success = false;
         exception.message = propsBean.getString("views.createCase.caseException") + " : " + e.getLocalizedMessage();

         return exception;
      }
   }

   /**
    * 
    * @param pi
    * @return
    */
   private RelatedProcessesDTO getRelatedProcessesDTO(ProcessInstance pi)
   {
      RelatedProcessesDTO dto = new RelatedProcessesDTO();
      MessagesViewsCommonBean COMMON_MESSAGE_BEAN = MessagesViewsCommonBean.getInstance();
      ProcessDefinition processDefinition = processDefinitionUtils.getProcessDefinition(pi.getModelOID(),
            pi.getProcessID());

      dto.processName = I18nUtils.getProcessName(processDefinition);
      ;
      dto.oid = pi.getOID();
      if (pi.getPriority() == 1)
      {
         dto.priority = COMMON_MESSAGE_BEAN.getString("common.priorities.high");
      }
      else if (pi.getPriority() == -1)
      {
         dto.priority = COMMON_MESSAGE_BEAN.getString("common.priorities.low");
      }
      else
      {
         dto.priority = COMMON_MESSAGE_BEAN.getString("common.priorities.normal");
      }

      dto.descriptorValues = ((ProcessInstanceDetails) pi).getDescriptors();
      dto.startTime = pi.getStartTime();

      dto.caseInstance = pi.isCaseProcessInstance();
      if (pi.isCaseProcessInstance())
      {
         dto.caseOwner = getCaseOwnerName(pi);
      }
      else
      {
         dto.caseOwner = null;
      }

      return dto;
   }

   private NotificationMessageDTO validateAbortAndJoin(boolean caseScope, Long sourceProcessInstanceOid,
         Long targetProcessInstanceOid)
   {
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      NotificationMessageDTO validationMessage = new NotificationMessageDTO();
      ProcessInstance targetProcessInstance;

      if (null == targetProcessInstanceOid)
      {
         if (!caseScope)
         {
            validationMessage.message = propsBean.getString("views.joinProcessDialog.inputProcess.message");
         }
         else
         {
            validationMessage.message = propsBean.getString("views.joinCaseDialog.inputProcess.message");
         }
         return validationMessage;
      }
      else
      {
         targetProcessInstance = getProcessInstance(targetProcessInstanceOid);
      }

      if (null == targetProcessInstance)
      {
         if (!caseScope)
         {
            validationMessage.message = propsBean.getString("views.common.process.invalidProcess.message");
         }
         else
         {
            validationMessage.message = propsBean.getString("views.attachToCase.inputIsProcess.message");
         }

         return validationMessage;
      }
      else if (targetProcessInstance.getState().getValue() == ProcessInstanceState.ABORTED)
      {
         if (!caseScope)
         {

            validationMessage.message = propsBean.getString("common.notifyProcessAlreadyAborted");
         }
         else
         {
            validationMessage.message = propsBean.getString("views.attachToCase.specifyActiveCase");
         }

         return validationMessage;
      }
      else if (targetProcessInstance.getState().getValue() == ProcessInstanceState.COMPLETED)
      {
         if (!caseScope)
         {
            validationMessage.message = propsBean.getString("common.notifyProcessAlreadyCompleted");
         }
         else
         {
            validationMessage.message = propsBean.getString("views.attachToCase.specifyActiveCase");
         }

         return validationMessage;
      }
      else if (targetProcessInstance.getOID() == sourceProcessInstanceOid)
      {
         if (!caseScope)
         {
            validationMessage.message = propsBean.getString("views.common.process.invalidTargetProcess.message");
         }
         else
         {
            validationMessage.message = propsBean.getString("views.joinCaseDialog.invalidCase.message");
         }

         return validationMessage;
      }
      else if (!caseScope & processDefinitionUtils.isCaseProcess(targetProcessInstance.getProcessID()))
      {
         validationMessage.message = propsBean.getString("views.common.process.invalidProcess.message");

         return validationMessage;
      }
      else if (caseScope & !(processDefinitionUtils.isCaseProcess(targetProcessInstance.getProcessID())))
      {
         validationMessage.message = propsBean.getString("views.joinCaseDialog.invalidCase.message1");

         return validationMessage;
      }

      return null;
   }

   private NotificationMessageDTO validateCreateCase(List<Long> sourceProcessInstanceOids, String caseName)
   {
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      NotificationMessageDTO validationMessage = new NotificationMessageDTO();
      validationMessage.success = false;
      if (StringUtils.isEmpty(caseName))
      {
         validationMessage.message = propsBean.getString("views.attachToCase.caseRequired.message");
         return validationMessage;
      }
      else if (null == sourceProcessInstanceOids || sourceProcessInstanceOids.isEmpty())
      {
         validationMessage.message = propsBean.getString("views.attachToCase.selectProcessToCreateCase");
         return validationMessage;
      }

      List<ProcessInstance> processInstances = getProcessInstances(sourceProcessInstanceOids);
      boolean isActiveProcessInstances = isActiveProcessInstances(processInstances);
      if (isActiveProcessInstances)
      {
         boolean isRootProcessInstances = isRootProcessInstances(processInstances);
         if (isRootProcessInstances)
         {
            boolean isNonCaseProcessInstances = isNonCaseProcessInstances(processInstances);
            if (!isNonCaseProcessInstances)
            {
               validationMessage.message = propsBean.getString("views.attachToCase.selectOnlyProcess.message");
               return validationMessage;
            }
         }
         else
         {
            validationMessage.message = propsBean.getString("views.attachToCase.selectOnlyProcess.message");
            return validationMessage;
         }
      }
      else
      {
         validationMessage.message = propsBean.getString("views.attachToCase.selectActiveProcesses");
         return validationMessage;
      }
      return null;
   }

   private NotificationMessageDTO validateAttachToCase(boolean caseScope, List<Long> sourceProcessInstanceOids,
         Long targetOid)
   {
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      NotificationMessageDTO validationMessage = new NotificationMessageDTO();
      validationMessage.success = false;
      ProcessInstance targetInstance;

      List<ProcessInstance> sourceProcessInstances = getProcessInstances(sourceProcessInstanceOids);

      boolean isRootProcessInstances = isRootProcessInstances(sourceProcessInstances);

      if (isRootProcessInstances)
      {
         boolean isMixProcessInstances = isCaseWithNoncaseProcessInstances(sourceProcessInstances);
         boolean isCaseProcessInstances = isCaseProcessInstances(sourceProcessInstances);

         if (isMixProcessInstances)
         {
            validationMessage.message = propsBean.getString("views.attachToCase.selectCaseOrProcess.message");
            return validationMessage;
         }
         else if (isCaseProcessInstances && sourceProcessInstances.size() > 1)
         {
            validationMessage.message = propsBean.getString("views.attachToCase.caseProcess.notSelectMultipleCases");
            return validationMessage;
         }
         else if (isCaseProcessInstances && !AuthorizationUtils.hasManageCasePermission(sourceProcessInstances.get(0)))
         {
            validationMessage.message = propsBean.getString("views.attachToCase.caseAttach.notAuthorizedToManageCase");
            return validationMessage;
         }
         else if (isCaseProcessInstances && isTerminatedProcessInstances(sourceProcessInstances))
         {
            validationMessage.message = propsBean.getString("views.attachToCase.selectActiveCaseToAttach");
            return validationMessage;
         }
         else if (!isCaseProcessInstances && !isActiveProcessInstances(sourceProcessInstances))
         {
            validationMessage.message = propsBean.getString("views.attachToCase.selectActiveProcessesToAttach");
            return validationMessage;
         }
      }
      else
      {
         validationMessage.message = propsBean.getString("views.attachToCase.nonRootProcessSelectedToCreateCase");
         return validationMessage;
      }

      if (caseScope && !hasManageCasePermission(sourceProcessInstances))
      {
         validationMessage.message = propsBean.getString("views.attachToCase.caseAttach.notAuthorizedToManageCase");
         return validationMessage;
      }

      if (null == targetOid)
      {
         validationMessage.success = false;
         if (caseScope)
         {
            validationMessage.message = propsBean.getString("views.attachToCase.processRequired.message");
         }
         else
         {
            validationMessage.message = propsBean.getString("views.attachToCase.caseRequired.message");
         }
         return validationMessage;
      }

      targetInstance = getProcessInstance(targetOid);

      if (null == targetInstance)
      {
         validationMessage.success = false;
         if (caseScope)
         {
            validationMessage.message = propsBean.getString("views.attachToCase.inputIsCase.message");
         }
         else
         {
            validationMessage.message = propsBean.getString("views.attachToCase.inputIsProcess.message");
         }
         return validationMessage;
      }

      boolean isTargetCase = targetInstance.isCaseProcessInstance();

      if (!caseScope && !isTargetCase)
      {
         validationMessage.message = propsBean.getString("views.attachToCase.inputIsProcess.message");
         return validationMessage;
      }
      else if (!caseScope && isTargetCase && !AuthorizationUtils.hasManageCasePermission(targetInstance))
      {
         validationMessage.message = propsBean.getString("views.attachToCase.caseAttach.notAuthorizedToManageCase");
         return validationMessage;
      }
      else if (!caseScope && !isRootProcessInstance(targetInstance))
      {
         validationMessage.message = propsBean.getString("views.attachToCase.nonRootProcessSelectedToCreateCase");
         return validationMessage;
      }
      else if (caseScope && isTargetCase)
      {
         validationMessage.message = propsBean.getString("views.attachToCase.inputIsCase.message");
         return validationMessage;
      }
      else if (caseScope && !isActiveProcessInstance(targetInstance))
      {
         validationMessage.message = propsBean.getString("views.attachToCase.specifyActiveProcess");
         return validationMessage;
      }
      else if (!caseScope
            && (ProcessInstanceState.ABORTED == targetInstance.getState().getValue() || ProcessInstanceState.COMPLETED == targetInstance
                  .getState().getValue()))
      {
         validationMessage.message = propsBean.getString("views.attachToCase.specifyActiveCase");
         return validationMessage;
      }
      return null;
   }

   public NotificationMap abortProcesses(AbortScope abortScope, List<Long> processesToBeAborted)
   {
      NotificationMap notificationMap = new NotificationMap();

      if (CollectionUtils.isNotEmpty(processesToBeAborted))
      {
         ProcessInstance processInstance;
         for (Long processInstanceOid : processesToBeAborted)
         {
            if (null != processInstanceOid)
            {
               processInstance = this.getProcessInstance(processInstanceOid.longValue());
               if (processInstance != null && isAbortable(processInstance))
               {
                  try
                  {
                     if (processInstance.isCaseProcessInstance())
                     {
                        notificationMap.addFailure(new NotificationDTO(processInstanceOid,
                              org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
                                    .getProcessLabel(processInstance), MessagesViewsCommonBean.getInstance()
                                    .getParamString(
                                          "views.switchProcessDialog.caseAbort.message",
                                          org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
                                                .getProcessStateLabel(processInstance))));
                     }
                     else
                     {
                        abortProcess(processInstance, abortScope);
                        notificationMap.addSuccess(new NotificationDTO(processInstanceOid,
                              org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
                                    .getProcessLabel(processInstance),
                              org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
                                    .getProcessStateLabel(processInstance)));
                     }
                  }
                  catch (Exception e)
                  {
                     // It is very to rare that any exception would occur
                     // here
                     trace.error(e);
                     notificationMap.addFailure(new NotificationDTO(processInstanceOid,
                           getProcessLabel(processInstance), MessagesViewsCommonBean.getInstance().getParamString(
                                 "views.common.process.abortProcess.failureMsg2",
                                 ExceptionHandler.getExceptionMessage(e))));
                  }
               }
               else
               {
                  if (ProcessInstanceState.Aborted.equals(processInstance.getState())
                        || ProcessInstanceState.Completed.equals(processInstance.getState()))
                  {
                     notificationMap.addFailure(new NotificationDTO(processInstanceOid,
                           getProcessLabel(processInstance), MessagesViewsCommonBean.getInstance().getParamString(
                                 "views.common.process.abortProcess.failureMsg3",
                                 org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
                                       .getProcessStateLabel(processInstance))));
                  }
                  else
                  {
                     notificationMap.addFailure(new NotificationDTO(processInstanceOid,
                           getProcessLabel(processInstance), MessagesViewsCommonBean.getInstance().getString(
                                 "views.common.process.abortProcess.failureMsg1")));
                  }
               }
            }
         }
      }
      return notificationMap;
   }

   /**
    * abort process
    * 
    * @param pi
    * @param abortScope
    */
   public void abortProcess(ProcessInstance pi, AbortScope abortScope)
   {
      ProcessInstanceDetails processInstanceDetails = (ProcessInstanceDetails) pi;
      WorkflowService workflowService = serviceFactoryUtils.getWorkflowService();
      long startingActivityInstanceOid = processInstanceDetails.getStartingActivityInstanceOID();
      if (startingActivityInstanceOid != 0)
      {
         workflowService.abortActivityInstance(startingActivityInstanceOid, abortScope);
      }
      else
      {
         // startingActivityInstanceOid = 0 means you don't have AI reference, and through
         // PI will be aborted(sub hierarchy or root process hierarchy) as per abort scope
         workflowService.abortProcessInstance(processInstanceDetails.getOID(), abortScope);
      }
   }

   /**
    * 
    * @param query
    * @param options
    * @return
    */
   public ProcessInstances getProcessInstances(ProcessInstanceQuery query, Options options)
   {
      if (query == null)
      {
         query = ProcessInstanceQuery.findAll();
      }

      addDescriptorPolicy(options, query);

      addSortCriteria(query, options);

      addFilterCriteria(query, options);

      SubsetPolicy subsetPolicy = new SubsetPolicy(options.pageSize, options.skip, true);
      query.setPolicy(subsetPolicy);

      return serviceFactoryUtils.getQueryService().getAllProcessInstances(query);
   }

   private Long getProcessInstancesCount(ProcessInstanceQuery query)
   {
      QueryService service = serviceFactoryUtils.getQueryService();
      return new Long(service.getProcessInstancesCount(query));
   }

   private long getTotalProcessInstancesCount() throws PortalException
   {
      return getProcessInstancesCount(ProcessInstanceQuery.findAll());
   }

   private long getActiveProcessInstancesCount() throws PortalException
   {
      return getProcessInstancesCount(ProcessInstanceQuery.findActive());
   }

   private long getInterruptedProcessInstancesCount() throws PortalException
   {
      return getProcessInstancesCount(ProcessInstanceQuery.findInterrupted());
   }

   private long getCompletedProcessInstancesCount() throws PortalException
   {
      return getProcessInstancesCount(ProcessInstanceQuery.findCompleted());
   }

   private long getAbortedProcessInstancesCount() throws PortalException
   {
      return getProcessInstancesCount(ProcessInstanceQuery.findInState(ProcessInstanceState.Aborted));
   }

   /**
    * Adds filter criteria to the query
    *
    * @param query
    *           Query
    * @param options
    *           Options
    */
   private void addFilterCriteria(Query query, Options options)
   {

      if (options.filter == null)
      {
         return;
      }

      ProcessTableFilterDTO filterDTO = (ProcessTableFilterDTO) options.filter;

      FilterAndTerm filter = query.getFilter().addAndTerm();

      // Root process instance OID
      if (null != filterDTO.rootPOID)
      {

         if (null != filterDTO.rootPOID.from)
         {
            filter.and(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.greaterOrEqual(filterDTO.rootPOID.from));
         }
         if (null != filterDTO.rootPOID.to)
         {
            filter.and(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.lessOrEqual(filterDTO.rootPOID.to));
         }
      }
      // process instance OID
      if (null != filterDTO.processOID)
      {
         if (null != filterDTO.processOID.from)
         {
            filter.and(ProcessInstanceQuery.OID.greaterOrEqual(filterDTO.processOID.from));
         }
         if (null != filterDTO.processOID.to)
         {
            filter.and(ProcessInstanceQuery.OID.lessOrEqual(filterDTO.processOID.to));
         }
      }

      // Start Filter
      if (null != filterDTO.startTime)
      {

         if (filterDTO.startTime.from != null)
         {
            Date fromDate = new Date(filterDTO.startTime.from);

            filter.and(ProcessInstanceQuery.START_TIME.greaterOrEqual(fromDate.getTime()));
         }

         if (filterDTO.startTime.to != null)
         {
            Date toDate = new Date(filterDTO.startTime.to);

            filter.and(ProcessInstanceQuery.START_TIME.lessOrEqual(toDate.getTime()));
         }
      }

      // endTime Filter
      if (null != filterDTO.endTime)
      {

         if (filterDTO.endTime.from != null)
         {
            Date fromDate = new Date(filterDTO.endTime.from);

            filter.and(ProcessInstanceQuery.TERMINATION_TIME.greaterOrEqual(fromDate.getTime()));
         }

         if (filterDTO.endTime.to != null)
         {
            Date toDate = new Date(filterDTO.endTime.to);

            filter.and(ProcessInstanceQuery.TERMINATION_TIME.lessOrEqual(toDate.getTime()));
         }
      }

      // status Filter
      if (null != filterDTO.status)
      {
         FilterOrTerm or = filter.addOrTerm();
         for (String status : filterDTO.status.like)
         {
            or.add(ProcessInstanceQuery.STATE.isEqual(Long.parseLong(status)));
         }
      }

      // process name Filter
      else if (null != filterDTO.processName)
      {
         FilterOrTerm or = filter.addOrTerm();
         if (!filterDTO.processName.processes.contains("-1"))
         {
            for (String processQId : filterDTO.processName.processes)
            {
               or.add(new ProcessDefinitionFilter(processQId, false));
            }
         }
      }

      // Priority Filter
      if (null != filterDTO.priority)
      {
         FilterOrTerm or = filter.addOrTerm();
         for (String priority : filterDTO.priority.like)
         {
            or.or(ProcessInstanceQuery.PRIORITY.isEqual(Integer.valueOf(priority)));
         }
      }

      // starting user Filter
      if (null != filterDTO.startingUser)
      {
         FilterOrTerm or = filter.addOrTerm();
         for (ParticipantDTO user : filterDTO.startingUser.participants)
         {
            or.add(new org.eclipse.stardust.engine.api.query.StartingUserFilter(user.OID));
         }
      }

      // descriptors Filter
      addDescriptorFilters(query, filterDTO);

   }

   /**
    * Add descriptor policy
    * 
    * @param options
    * @param query
    */

   private void addDescriptorPolicy(Options options, Query query)
   {

      if (options.allDescriptorsVisible)
      {
         query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      }
      else if (CollectionUtils.isNotEmpty(options.visibleDescriptorColumns))
      {
         query.setPolicy(DescriptorPolicy.withIds(new HashSet<String>(options.visibleDescriptorColumns)));
      }
      else
      {
         query.setPolicy(DescriptorPolicy.NO_DESCRIPTORS);
      }
   }

   /**
    * Add filter on descriptor columns .
    * 
    * @param query
    * @param processListFilterDTO
    */
   public static void addDescriptorFilters(Query query, ProcessTableFilterDTO processListFilterDTO)
   {

      Map<String, DescriptorFilterDTO> descFilterMap = processListFilterDTO.descriptorFilterMap;

      if (null != descFilterMap)
      {

         Map<String, DataPath> descriptors = ProcessDefinitionUtils.getAllDescriptors(false);
         GenericDescriptorFilterModel filterModel = GenericDescriptorFilterModel.create(descriptors.values());
         filterModel.setFilterEnabled(true);

         for (Map.Entry<String, DescriptorFilterDTO> descriptor : descFilterMap.entrySet())
         {
            Object value = null;
            String key = descriptor.getKey();

            // Boolean type desc
            if (descriptor.getValue().type.equals(ColumnDataType.BOOLEAN.toString()))
            {
               value = ((BooleanDTO) descriptor.getValue().value).equals;
            }
            // String type desc
            else if (descriptor.getValue().type.equals(ColumnDataType.STRING.toString()))
            {
               value = ((TextSearchDTO) descriptor.getValue().value).textSearch;
            }
            // Number type desc
            else if (descriptor.getValue().type.equals(ColumnDataType.NUMBER.toString()))
            {
               Number from = ((RangeDTO) descriptor.getValue().value).from;
               Number to = ((RangeDTO) descriptor.getValue().value).to;
               value = new NumberRange(from, to);
            }
            // Date type desc
            else if (descriptor.getValue().type.equals(ColumnDataType.DATE.toString()))
            {
               Long from = ((RangeDTO) descriptor.getValue().value).from;
               Long to = ((RangeDTO) descriptor.getValue().value).to;
               value = new DateRange();
               if (null != from)
               {
                  ((DateRange) value).setFromDateValue(new Date(from));
               }
               if (null != to)
               {
                  ((DateRange) value).setToDateValue(new Date(to));
               }
            }

            filterModel.setFilterValue(key, (Serializable) value);
         }

         try
         {
            DescriptorFilterUtils.applyFilters(query, filterModel);
         }
         catch (Exception e)
         {
            trace.error("Error occurred while applying filter to descriptors..", e);
         }
      }

   }

   /**
    * @param query
    * @param options
    */
   private void addSortCriteria(Query query, Options options)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("options.orderBy = " + options.orderBy);
      }

      if (options.visibleDescriptorColumns.contains(options.orderBy))
      {
         Map<String, DataPath> allDescriptors = ProcessDefinitionUtils.getAllDescriptors(false);
         String descriptorName = options.orderBy;
         if (allDescriptors.containsKey(descriptorName))
         {
            DescriptorUtils.applyDescriptorPolicy(query, options);
            String columnName = DescriptorUtils.getDescriptorColumnName(descriptorName, allDescriptors);
            if (CommonDescriptorUtils.isStructuredData(allDescriptors.get(descriptorName)))
            {
               query.orderBy(new DataOrder(columnName, DescriptorUtils.getXpathName(descriptorName, allDescriptors),
                     options.asc));
            }
            else
            {
               query.orderBy(new DataOrder(columnName, options.asc));
            }
         }
      }
      else if (COL_PROCESS_NAME.equals(options.orderBy))
      {
         query.orderBy(ProcessInstanceQuery.PROC_DEF_NAME.ascendig(options.asc));
      }
      else if (PROCESS_INSTANCE_ROOT_OID.equals(options.orderBy))
      {
         query.orderBy(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID, options.asc);
      }
      else if (COL_PROCESS_INSTANCE_OID.equals(options.orderBy))
      {
         query.orderBy(ProcessInstanceQuery.OID, options.asc);
      }
      else if (COL_START_TIME.equals(options.orderBy))
      {
         query.orderBy(ProcessInstanceQuery.START_TIME, options.asc);
      }
      else if (COL_END_TIME.equals(options.orderBy))
      {
         query.orderBy(ProcessInstanceQuery.TERMINATION_TIME, options.asc);
      }
      else if (COL_PRIOIRTY.equals(options.orderBy))
      {
         query.orderBy(ProcessInstanceQuery.PRIORITY, options.asc);
      }
      else if (COL_STATUS.equals(options.orderBy))
      {
         query.orderBy(ProcessInstanceQuery.STATE, options.asc);
      }
      else if (STARTING_USER.equals(options.orderBy))
      {
         CustomOrderCriterion o = ProcessInstanceQuery.USER_ACCOUNT.ascendig(options.asc);
         query.orderBy(o);
      }
      else if (COL_BENCHMARK.equals(options.orderBy))
      {
         query.orderBy(ProcessInstanceQuery.BENCHMARK_VALUE, options.asc);
      }
      else
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("ProcessInstanceUtils.addSortCriteria(Query, Options): Sorting not implemented for "
                  + options.asc);
         }
      }
   }

   /**
    * @param queryResult
    * @return
    */
   public QueryResultDTO buildProcessListResult(QueryResult< ? > queryResult)
   {
      List<ProcessInstanceDTO> list = new ArrayList<ProcessInstanceDTO>();

      for (Object object : queryResult)
      {
         if (object instanceof ProcessInstance)
         {

            ProcessInstanceDTO dto = buildProcessInstanceDTO((ProcessInstance) object, false);

            list.add(dto);
         }
      }

      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = list;
      resultDTO.totalCount = queryResult.getTotalCount();

      return resultDTO;
   }

   
   /**
    * 
    * @param processInstance
    * @return 
    */
   public ProcessInstanceDTO buildProcessInstanceDTO(ProcessInstance pi , boolean skipDescriptors){

      ProcessInstanceDTO dto = new ProcessInstanceDTO();

      ProcessDefinition processDefinition = processDefinitionUtils.getProcessDefinition(
            pi.getModelOID(), pi.getProcessID());

      dto.processInstanceRootOID = pi.getRootProcessInstanceOID();
      dto.oid = pi.getOID();

      PriorityDTO priority = new PriorityDTO();
      priority.value = pi.getPriority();
      priority.setLabel(pi.getPriority());
      priority.setName(pi.getPriority());
      dto.priority = priority;
      
      dto.startTime = pi.getStartTime().getTime();
      dto.duration = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
            .getDuration(pi);
      dto.processName = I18nUtils.getProcessName(processDefinition);
      String startingUserLabel = UserUtils.getUserDisplayLabel(pi.getStartingUser());
      dto.createUser = startingUserLabel;
      
      // Update Document Descriptors for process
      if(!skipDescriptors){
         dto.descriptorValues = getDescriptorValues(pi, processDefinition);
         dto.processDescriptorsList = getProcessDescriptor(pi, processDefinition);
         
         CommonDescriptorUtils.updateProcessDocumentDescriptors(
               ((ProcessInstanceDetails) pi).getDescriptors(), pi, processDefinition);
      }
     
      if (null != pi.getTerminationTime())
      {
         dto.endTime = pi.getTerminationTime().getTime();
      }
      dto.startingUser = startingUserLabel;
      dto.status = new StatusDTO(pi.getState().getValue(),  org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
            .getProcessStateLabel(pi));
      
      dto.enableTerminate = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
            .isAbortable(pi);
      dto.enableRecover = true;
      dto.checkSelection = false;
      dto.modifyProcessInstance = AuthorizationUtils.hasPIModifyPermission(pi);

      List<Note> notes = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
            .getNotes(pi);
      if (null != notes)
      {
         dto.notesCount = notes.size();
      }
      dto.caseInstance = pi.isCaseProcessInstance();
      if (dto.caseInstance)
      {
         dto.caseOwner = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
               .getCaseOwnerName(pi);
      }

      dto.oldPriority = dto.priority;
      dto.benchmark = getProcessBenchmark(pi);

      return dto;
   }

   /**
    * 
    * @param processInstance
    * @return
    */
   private BenchmarkDTO getProcessBenchmark(ProcessInstance processInstance)
   {
      BenchmarkDTO dto = new BenchmarkDTO();
      if (null != processInstance.getBenchmarkResult())
      {
         dto.color = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
               .getBenchmarkColor(processInstance);
         dto.label = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
               .getBenchmarkLabel(processInstance);
         dto.value = processInstance.getBenchmarkResult().getCategory();
         dto.oid =  processInstance.getBenchmark();
      }
      return dto;
   }

   /*
    * 
    */
   private Map<String, DescriptorDTO> getDescriptorValues(ProcessInstance processInstance,
         ProcessDefinition processDefinition)
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
            processDescriptorsList = CommonDescriptorUtils.createProcessDescriptors(descriptorValues,
                  processDefinition, true, true);

         }

         if (!processDescriptorsList.isEmpty())
         {
            return getProcessDescriptors(processDescriptorsList);
         }
      }

      return new LinkedHashMap<String, DescriptorDTO>();
   }

   /*
    * 
    */
   private List<ProcessDescriptor> getProcessDescriptor(ProcessInstance processInstance,
         ProcessDefinition processDefinition)
   {
      List<ProcessDescriptor> processDescriptorsList = null;
      if (processDefinition != null)
      {
         ProcessInstanceDetails processInstanceDetails = (ProcessInstanceDetails) processInstance;

         if (processInstance.isCaseProcessInstance())
         {
            processDescriptorsList = CommonDescriptorUtils.createCaseDescriptors(
                  processInstanceDetails.getDescriptorDefinitions(), processInstanceDetails.getDescriptors(),
                  processDefinition, true);
         }
         else
         {
            processDescriptorsList = CommonDescriptorUtils.createProcessDescriptors(
                  processInstanceDetails.getDescriptors(), processDefinition, true);
         }
      }
      else
      {
         processDescriptorsList = CollectionUtils.newArrayList();
      }
      return processDescriptorsList;
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

   /**
    * Populate the options with the post data.
    * 
    * @param options
    * @param postData
    * @return
    */
   public static Options populatePostData(Options options, String postData,
         List<DescriptorColumnDTO> availableDescriptors)
   {

      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject postJSON = jsonIo.readJsonObject(postData);

      // For filter
      JsonObject filters = postJSON.getAsJsonObject("filters");
      if (null != filters)
      {
         options.filter = getFilters(filters.toString(), availableDescriptors);
      }

      JsonArray visbleColumns = postJSON.getAsJsonObject("descriptors").get("visibleColumns").getAsJsonArray();
      List<String> columnsList = new ArrayList<String>();
      for (JsonElement jsonElement : visbleColumns)
      {
         columnsList.add(jsonElement.getAsString());
      }
      options.visibleDescriptorColumns = columnsList;
      options.allDescriptorsVisible = postJSON.getAsJsonObject("descriptors").get("fetchAll").getAsBoolean();

      return options;
   }

   /**
    * Get the filters from the JSON string
    * 
    * @param jsonFilterString
    * @return
    */
   public static ProcessTableFilterDTO getFilters(String jsonFilterString,
         List<DescriptorColumnDTO> availableDescriptors)
   {
      ProcessTableFilterDTO processListFilterDTO = null;
      if (StringUtils.isNotEmpty(jsonFilterString))
      {
         try
         {
            JsonMarshaller jsonIo = new JsonMarshaller();
            JsonObject json = jsonIo.readJsonObject(jsonFilterString);
            processListFilterDTO = DTOBuilder.buildFromJSON(json, ProcessTableFilterDTO.class,
                  ProcessTableFilterDTO.getCustomTokens());
            populateDescriptorFilters(processListFilterDTO, json, availableDescriptors);
         }
         catch (Exception e)
         {
            trace.error("Error in Deserializing filter JSON", e);
         }
      }

      return processListFilterDTO;
   }

   /**
    * Populates the descriptor filter values.
    * 
    * @param worklistFilter
    * @param descriptorColumnsFilterJson
    */
   public static void populateDescriptorFilters(ProcessTableFilterDTO processListFilterDTO,
         JsonObject descriptorColumnsFilterJson, List<DescriptorColumnDTO> descriptorColumns)
   {

      Map<String, DescriptorFilterDTO> descriptorColumnMap = new HashMap<String, DescriptorFilterDTO>();

      for (DescriptorColumnDTO descriptorColumnDTO : descriptorColumns)
      {
         Object filterDTO = null;
         String id = descriptorColumnDTO.id;
         if (null != descriptorColumnsFilterJson.get(id))
         {
            // String TYPE
            if (ColumnDataType.STRING.toString().equals(descriptorColumnDTO.type))
            {
               filterDTO = new Gson().fromJson(descriptorColumnsFilterJson.get(id),
                     ProcessTableFilterDTO.TextSearchDTO.class);

            }
            else if (ColumnDataType.DATE.toString().equals(descriptorColumnDTO.type)
                  || ColumnDataType.NUMBER.toString().equals(descriptorColumnDTO.type))
            {
               filterDTO = new Gson().fromJson(descriptorColumnsFilterJson.get(id),
                     ProcessTableFilterDTO.RangeDTO.class);
            }
            else if (ColumnDataType.BOOLEAN.toString().equals(descriptorColumnDTO.type))
            {
               filterDTO = new Gson().fromJson(descriptorColumnsFilterJson.get(id),
                     ProcessTableFilterDTO.BooleanDTO.class);
            }
            descriptorColumnMap.put(id, new DescriptorFilterDTO(descriptorColumnDTO.type, filterDTO));
         }
      }

      processListFilterDTO.descriptorFilterMap = descriptorColumnMap;
   }

}
