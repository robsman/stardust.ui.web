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
package org.eclipse.stardust.ui.web.rest.component.util;

import static org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils.isAuxiliaryProcess;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.dto.DataPathDetails;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.CustomOrderCriterion;
import org.eclipse.stardust.engine.api.query.DataOrder;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.EvaluationPolicy;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.HistoricalEventPolicy;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstanceDetailsPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.RootProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DataCopyOptions;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.HistoricalEvent;
import org.eclipse.stardust.engine.api.runtime.ProcessDefinitions;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.SpawnOptions;
import org.eclipse.stardust.engine.api.runtime.SpawnOptions.SpawnMode;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.ui.web.rest.util.DescriptorColumnUtils.ColumnDataType;
import org.eclipse.stardust.ui.web.rest.dto.AbortNotificationDTO;
import org.eclipse.stardust.ui.web.rest.dto.BenchmarkDTO;
import org.eclipse.stardust.ui.web.rest.dto.DataTableOptionsDTO;
import org.eclipse.stardust.ui.web.rest.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.dto.DescriptorDTO;
import org.eclipse.stardust.ui.web.rest.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.dto.FilterDTO.BooleanDTO;
import org.eclipse.stardust.ui.web.rest.dto.FilterDTO.RangeDTO;
import org.eclipse.stardust.ui.web.rest.dto.FilterDTO.TextSearchDTO;
import org.eclipse.stardust.ui.web.rest.dto.InstanceCountsDTO;
import org.eclipse.stardust.ui.web.rest.dto.NotificationMap;
import org.eclipse.stardust.ui.web.rest.dto.NotificationMap.NotificationDTO;
import org.eclipse.stardust.ui.web.rest.dto.NotificationMessageDTO;
import org.eclipse.stardust.ui.web.rest.dto.PriorityDTO;
import org.eclipse.stardust.ui.web.rest.dto.ProcessInstanceDTO;
import org.eclipse.stardust.ui.web.rest.dto.ProcessTableFilterDTO;
import org.eclipse.stardust.ui.web.rest.dto.ProcessTableFilterDTO.DescriptorFilterDTO;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.RelatedProcessesDTO;
import org.eclipse.stardust.ui.web.rest.dto.StatusDTO;
import org.eclipse.stardust.ui.web.rest.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.dto.response.ParticipantDTO;
import org.eclipse.stardust.ui.web.rest.util.DescriptorUtils;
import org.eclipse.stardust.ui.web.rest.util.JsonMarshaller;
import org.eclipse.stardust.ui.web.rest.util.RelatedProcessSearchUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.common.DateRange;
import org.eclipse.stardust.ui.web.viewscommon.common.GenericDataMapping;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.ProcessPortalConstants;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DataMappingWrapper;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorColumnUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.GenericDescriptorFilterModel;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.NumberRange;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentInfo;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.processContextExplorer.DescriptorItemTableEntry;
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

   private static final String COL_ROOT_PROCESS_NAME = "rootProcessName";

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
   public ProcessInstance getProcessInstance(long oid, boolean fetchDescriptors, boolean withHierarchyInfo)
   {
      ProcessInstance pi = null;
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      query.where(ProcessInstanceQuery.OID.isEqual(oid));

      if (fetchDescriptors)
      {
         query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      }
      else
      {
         query.setPolicy(DescriptorPolicy.NO_DESCRIPTORS);
      }

      if (withHierarchyInfo)
      {
         ProcessInstanceDetailsPolicy processInstanceDetailsPolicy = new ProcessInstanceDetailsPolicy(
               ProcessInstanceDetailsLevel.Default);
         query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
         processInstanceDetailsPolicy.getOptions().add(ProcessInstanceDetailsOptions.WITH_HIERARCHY_INFO);
         query.setPolicy(processInstanceDetailsPolicy);
      }

      ProcessInstances pis = serviceFactoryUtils.getQueryService().getAllProcessInstances(query);

      if (!pis.isEmpty())
      {
         pi = pis.get(0);
      }

      return pi;
   }

   /**
    * @param oid
    * @param policies
    * @return
    */
   public ProcessInstance getProcessInstance(long oid, List<EvaluationPolicy> policies)
   {
      ProcessInstance pi = null;
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      query.where(ProcessInstanceQuery.OID.isEqual(oid));

      if (policies != null)
      {
         for (EvaluationPolicy evaluationPolicy : policies)
         {
            query.setPolicy(evaluationPolicy);
         }
      }

      ProcessInstances pis = serviceFactoryUtils.getQueryService().getAllProcessInstances(query);

      if (!pis.isEmpty())
      {
         pi = pis.get(0);
      }
      return pi;
   }

   /**
    * @param oid
    * @param fetchDescriptors
    * @param withEvents
    * @return
    * @throws ResourceNotFoundException
    */
   public ProcessInstances getAllProcessInstances(long oid, boolean fetchDescriptors, boolean withEvents)
         throws ResourceNotFoundException
   {
      ProcessInstance processInstance = getProcessInstance(oid);

      if (null == processInstance)
      {
         throw new ResourceNotFoundException(MessagesViewsCommonBean.getInstance()
               .getParamString("common.process.instance.notfound", String.valueOf(oid)));
      }

      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      query.getFilter()
            .and(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.isEqual(processInstance.getRootProcessInstanceOID()));
      query.orderBy(ProcessInstanceQuery.START_TIME);

      if (withEvents)
      {
         query.setPolicy(HistoricalEventPolicy.ALL_EVENTS);
      }

      if (fetchDescriptors)
      {
         query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      }

      ProcessInstanceDetailsPolicy processInstanceDetailsPolicy = new ProcessInstanceDetailsPolicy(
            ProcessInstanceDetailsLevel.Default);
      processInstanceDetailsPolicy.getOptions().add(ProcessInstanceDetailsOptions.WITH_HIERARCHY_INFO);
      query.setPolicy(processInstanceDetailsPolicy);

      return serviceFactoryUtils.getQueryService().getAllProcessInstances(query);
   }

   /**
    * 
    * @param oid
    * @return
    */
   public ProcessInstance getProcessInstance(long oid)
   {
      return getProcessInstance(oid, false, false);
   }

   public User getCurrentUser()
   {
      return serviceFactoryUtils.getUserService().getUser();
   }

   /**
    * @param instance
    * @return localized process name with OID appended
    */
   public String getProcessLabel(ProcessInstance instance)
   {
      if (null != instance)
      {
         StringBuilder processLabel = new StringBuilder(I18nUtils.getProcessName(
               processDefinitionUtils.getProcessDefinition(instance.getModelOID(), instance.getProcessID())));
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
               if (sourceProcessInstance.getParentProcessInstanceOid() > 0 & (sourceProcessInstance
                     .getParentProcessInstanceOid() != sourceProcessInstance.getRootProcessInstanceOID()))
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
      boolean abortable = processInstance == null
            ? false
            : !ProcessInstanceState.Aborted.equals(processInstance.getState())
                  && !ProcessInstanceState.Completed.equals(processInstance.getState());
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

      ProcessInstance processInstance = getProcessInstance(oid);
      boolean supportsProcessAttachments = supportsProcessAttachments(processInstance);
      if (supportsProcessAttachments)
      {
         WorkflowService ws = serviceFactoryUtils.getWorkflowService();
         Object o = ws.getInDataPath(processInstance.getOID(), DmsConstants.PATH_ID_ATTACHMENTS);

         DataDetails data = (DataDetails) modelUtils.getModel(processInstance.getModelOID())
               .getData(DmsConstants.PATH_ID_ATTACHMENTS);
         if (DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(data.getTypeId()))
         {
            processAttachments = (List<Document>) o;
         }
      }

      return processAttachments;
   }

   /**
    * @param oid
    * @return
    */
   public List<Document> getProcessInstanceDocumentsForDataPath(ProcessInstance processInstance, String dataPathId)
   {
      List<Document> docList = new ArrayList<Document>();
      Object objectDocument = serviceFactoryUtils.getWorkflowService().getInDataPath(processInstance.getOID(),
            dataPathId);
      if (objectDocument != null && objectDocument instanceof Document)
      {
         docList.add((Document) objectDocument);
      }

      return docList;
   }

   /**
    * return true if the provided Process Instance supports Process Attachments
    *
    * @param oid
    * @return
    */
   public boolean supportsProcessAttachments(long oid)
   {
      return supportsProcessAttachments(getProcessInstance(oid));
   }

   /**
    * return true if the provided Process Instance supports Process Attachments
    *
    * @param oid
    * @return
    */
   public boolean supportsProcessAttachments(ProcessInstance processInstance)
   {
      boolean supportsProcessAttachments = false;

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
         countDTO.halted = getHaltedProcessInstancesCount();
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
      ProcessDefinitions pds = serviceFactoryUtils.getQueryService()
            .getProcessDefinitions(ProcessDefinitionQuery.findStartable());

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

   public List<AbortNotificationDTO> switchProcess(List<Long> processInstOIDs, String processId, String linkComment,
         Boolean pauseParentProcess)
   {
      List<AbortNotificationDTO> newProcessInstances = new ArrayList<AbortNotificationDTO>();

      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();

      for (Long processInstOID : processInstOIDs)
      {
         ProcessInstance srcProcessInstance = getProcessInstance(processInstOID);

         // First check the permission
         if (!pauseParentProcess && (!AuthorizationUtils.hasAbortPermission(srcProcessInstance)
               || !isAbortable(srcProcessInstance) || srcProcessInstance.isCaseProcessInstance()))
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
            DataCopyOptions dataCopyOptions = new DataCopyOptions(true, null, null, true);
            ProcessInstance pi;
            SpawnOptions options;
            if (pauseParentProcess)
            {

               options = new SpawnOptions(null, SpawnMode.HALT, linkComment, dataCopyOptions);

            }
            else
            {
               options = new SpawnOptions(null, SpawnMode.ABORT, linkComment, dataCopyOptions);
            }

            pi = serviceFactoryUtils.getWorkflowService().spawnPeerProcessInstance(processInstOID, processId, options);

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
            trace.error(
                  "Unable to abort the process with oid: " + processInstOID + " and target process id: " + processId);
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
         targetProcessInstance = serviceFactoryUtils.getWorkflowService().mergeCases(targetProcessInstanceOid,
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
      else if (!caseScope && (ProcessInstanceState.ABORTED == targetInstance.getState().getValue()
            || ProcessInstanceState.COMPLETED == targetInstance.getState().getValue()))
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
                                    .getProcessLabel(processInstance),
                              MessagesViewsCommonBean.getInstance().getParamString(
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
                           org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
                                 .getProcessLabel(processInstance),
                           MessagesViewsCommonBean.getInstance().getParamString(
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
                           org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
                                 .getProcessLabel(processInstance),
                           MessagesViewsCommonBean.getInstance().getParamString(
                                 "views.common.process.abortProcess.failureMsg3",
                                 org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
                                       .getProcessStateLabel(processInstance))));
                  }
                  else
                  {
                     notificationMap.addFailure(new NotificationDTO(processInstanceOid,
                           org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
                                 .getProcessLabel(processInstance),
                           MessagesViewsCommonBean.getInstance()
                                 .getString("views.common.process.abortProcess.failureMsg1")));
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
   public ProcessInstances getProcessInstances(ProcessInstanceQuery query, DataTableOptionsDTO options)
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

   private long getHaltedProcessInstancesCount() throws PortalException
   {
      return getProcessInstancesCount(ProcessInstanceQuery.findInState(ProcessInstanceState.Halted));
   }

   /**
    * Adds filter criteria to the query
    *
    * @param query
    *           Query
    * @param options
    *           DataTableOptionsDTO
    */
   private void addFilterCriteria(Query query, DataTableOptionsDTO options)
   {

      if (options.filter == null)
      {
         return;
      }

      List<String> processFilter = null;

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
      if (null != filterDTO.processName)
      {
         FilterOrTerm or = filter.addOrTerm();
         if (!filterDTO.processName.processes.contains("-1"))
         {
            for (String processQId : filterDTO.processName.processes)
            {
               or.add(new ProcessDefinitionFilter(processQId, false));
            }

            processFilter = filterDTO.processName.processes;
         }
      }

      // Root process name Filter
      if (null != filterDTO.rootProcessName)
      {
         FilterOrTerm or = filter.addOrTerm();
         if (!filterDTO.rootProcessName.processes.contains("-1"))
         {
            for (String processName : filterDTO.rootProcessName.processes)
            {
               or.add(new RootProcessInstanceFilter(processName, null));
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
      addDescriptorFilters(query, filterDTO, processFilter);

   }

   /**
    * Add descriptor policy
    * 
    * @param options
    * @param query
    */

   private void addDescriptorPolicy(DataTableOptionsDTO options, Query query)
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
    * 
    * @param processFilter
    * @param descFilterMap
    * @return
    */
   public static Collection<DataPath> getRelevantDataPaths(List<String> processFilter,
         Map<String, DescriptorFilterDTO> descFilterMap)
   {
      Map<String, DataPath> descriptors = ProcessDefinitionUtils.getAllDescriptors(false);
      Map<String, Map<String, DataPath>> allFilterableDescriptorsByProcess = CommonDescriptorUtils
            .getAllDescriptorsByProcess(true);
      Collection<DataPath> dataPaths = null;

      for (Map.Entry<String, DescriptorFilterDTO> descriptor : descFilterMap.entrySet())
      {
         String dataId = descriptor.getKey();
         if (null != processFilter)
         {
            for (String processQId : processFilter)
            {
               Map<String, DataPath> procDescriptors = allFilterableDescriptorsByProcess.get(processQId);
               if (null != procDescriptors && procDescriptors.containsKey(dataId))
               {
                  dataPaths = procDescriptors.values();

                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Descriptor Filtering:: Using Descriptors from Process: " + processQId
                           + ", because of filtering on dataId: " + dataId);
                  }

                  break;
               }
            }
         }
      }

      if (null == dataPaths)
      {
         dataPaths = descriptors.values();
         if (trace.isDebugEnabled())
         {
            trace.debug("Descriptor Filtering:: Using Descriptors from Default Process");
         }
      }

      return dataPaths;
   }

   /**
    * Add filter on descriptor columns .
    * 
    * @param query
    * @param processListFilterDTO
    */
   public static void addDescriptorFilters(Query query, ProcessTableFilterDTO processListFilterDTO,
         List<String> processFilter)
   {

      Map<String, DescriptorFilterDTO> descFilterMap = processListFilterDTO.descriptorFilterMap;

      if (null != descFilterMap)
      {
         Collection<DataPath> dataPaths = getRelevantDataPaths(processFilter, descFilterMap);

         GenericDescriptorFilterModel filterModel = GenericDescriptorFilterModel.create(dataPaths);
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
            else if (descriptor.getValue().type.equals(ColumnDataType.DATETIME.toString())
                  || descriptor.getValue().type.equals(ColumnDataType.DATE.toString()))
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
            // Descriptors of type Multi cardinality
            else if (descriptor.getValue().type.equals(ColumnDataType.LIST.toString()))
            {
               value = ((TextSearchDTO) descriptor.getValue().value).textSearch;
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
   private void addSortCriteria(Query query, DataTableOptionsDTO options)
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
      else if (COL_ROOT_PROCESS_NAME.equals(options.orderBy))
      {
         query.orderBy(ProcessInstanceQuery.ROOT_PROC_DEF_NAME.ascendig(options.asc));
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
            trace.debug("ProcessInstanceUtils.addSortCriteria(Query, DataTableOptionsDTO): Sorting not implemented for "
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

      if (queryResult != null)
      {
         for (Object object : queryResult)
         {
            if (object instanceof ProcessInstance)
            {

               ProcessInstanceDTO dto = buildProcessInstanceDTO((ProcessInstance) object);

               list.add(dto);
            }
         }

      }

      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = list;
      resultDTO.totalCount = (queryResult == null) ? 0 : queryResult.getTotalCount();

      return resultDTO;
   }

   /**
    * 
    * @param processInstance
    * @return
    */
   public ProcessInstanceDTO buildProcessInstanceDTO(ProcessInstance pi)
   {

      ProcessInstanceDTO dto = new ProcessInstanceDTO();

      ProcessDefinition processDefinition = processDefinitionUtils.getProcessDefinition(pi.getModelOID(),
            pi.getProcessID());

      dto.auxillary = isAuxiliaryProcess(processDefinition);
      dto.processInstanceRootOID = pi.getRootProcessInstanceOID();
      dto.parentProcessInstanceOID = pi.getParentProcessInstanceOid();
      dto.oid = pi.getOID();
      dto.qualifiedId = processDefinition.getQualifiedId();

      PriorityDTO priority = new PriorityDTO();
      priority.value = pi.getPriority();
      priority.setLabel(pi.getPriority());
      priority.setName(pi.getPriority());
      dto.priority = priority;

      dto.startTime = pi.getStartTime().getTime();
      dto.duration = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils.getDuration(pi);
      dto.processName = I18nUtils.getProcessName(processDefinition);
      dto.rootProcessName = pi.getRootProcessInstanceName();

      String startingUserLabel = UserUtils.getUserDisplayLabel(pi.getStartingUser());
      dto.createUser = startingUserLabel;

      // Update Document Descriptors for process
      dto.descriptorValues = getDescriptorValues(pi, processDefinition);
      // For descriptor column display values
      dto.processDescriptorsValues = getProcessDescriptorValues(processDefinition,
            ((ProcessInstanceDetails) pi).getDescriptors(), pi);
      dto.supportsProcessAttachments = processDefinitionUtils.supportsProcessAttachments(processDefinition);

      CommonDescriptorUtils.updateProcessDocumentDescriptors(((ProcessInstanceDetails) pi).getDescriptors(), pi,
            processDefinition);

      if (null != pi.getTerminationTime())
      {
         dto.endTime = pi.getTerminationTime().getTime();
      }
      dto.startingUser = startingUserLabel;
      dto.status = new StatusDTO(pi.getState().getValue(),
            org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils.getProcessStateLabel(pi));

      dto.enableTerminate = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils.isAbortable(pi);
      dto.enableRecover = true;
      dto.checkSelection = false;
      dto.modifyProcessInstance = AuthorizationUtils.hasPIModifyPermission(pi);

      List<Note> notes = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils.getNotes(pi);
      if (null != notes)
      {
         dto.notesCount = notes.size();
      }
      dto.caseInstance = pi.isCaseProcessInstance();
      if (dto.caseInstance)
      {
         dto.caseOwner = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils.getCaseOwnerName(pi);
      }

      dto.oldPriority = dto.priority;
      dto.benchmark = getProcessBenchmark(pi);

      return dto;
   }

   private Map<String, DescriptorDTO> getProcessDescriptorValues(ProcessDefinition processDefinition,
         Map<String, Object> descriptors, ProcessInstance pi)
   {
      List<ProcessDescriptor> processDescriptorsList = CommonDescriptorUtils
            .getProcessDescriptorValues(processDefinition, descriptors);
      Map<String, DescriptorDTO> processDescriptorValues = new LinkedHashMap<String, DescriptorDTO>();

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
            boolean isDocument = true;
            if (documents.isEmpty())
            {
               isDocument = false;
            }

            DescriptorDTO descriptorDto = new DescriptorDTO(desc.getKey(), desc.getValue(), isDocument, documents);
            processDescriptorValues.put(desc.getId(), descriptorDto);
         }
         else
         {
            ProcessDescriptor desc = (ProcessDescriptor) descriptor;
            DescriptorDTO descriptorDto = null;
            if (desc.isLink())
            {
               // Fetch the dataPath from Process Instance to read instance 'Link Text'
               // attribute value
               List<DataPath> dataPaths = pi.getDescriptorDefinitions();
               String linkText = DescriptorColumnUtils.getLinkDescriptorText(desc.getId(), dataPaths);
               descriptorDto = new DescriptorDTO(desc.getKey(), desc.getValue(), false, null, desc.isLink(), linkText);
            }
            else
            {
               descriptorDto = new DescriptorDTO(desc.getKey(), desc.getValue(), false, null);
            }

            processDescriptorValues.put(desc.getId(), descriptorDto);
         }
      }
      return processDescriptorValues;
   }

   /**
    * 
    * @param processInstance
    * @return
    */
   private BenchmarkDTO getProcessBenchmark(ProcessInstance processInstance)
   {
      BenchmarkDTO dto = null;
      if (null != processInstance.getBenchmarkResult())
      {
         dto = new BenchmarkDTO();
         dto.color = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
               .getBenchmarkColor(processInstance);
         dto.label = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
               .getBenchmarkLabel(processInstance);
         dto.value = processInstance.getBenchmarkResult().getCategory();
         dto.oid = processInstance.getBenchmark();
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
         List<ProcessDescriptor> processDescriptorsList = null;

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
            processDescriptorsList = CommonDescriptorUtils.createProcessDescriptors(descriptorValues, processDefinition,
                  true, true);

         }

         if (!processDescriptorsList.isEmpty())
         {
            return getProcessDescriptors(processDescriptorsList, processInstance);
         }
      }

      return null;
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
            processDescriptorsList = CommonDescriptorUtils
                  .createProcessDescriptors(processInstanceDetails.getDescriptors(), processDefinition, true);
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
   private Map<String, DescriptorDTO> getProcessDescriptors(List<ProcessDescriptor> processDescriptorsList,
         ProcessInstance processInstance)
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

            boolean isDocument = true;
            if (documents.isEmpty())
            {
               isDocument = false;
            }
            DescriptorDTO descriptorDto = new DescriptorDTO(desc.getKey(), desc.getValue(), isDocument, documents);
            descriptors.put(desc.getId(), descriptorDto);
         }
         else
         {
            ProcessDescriptor desc = (ProcessDescriptor) descriptor;
            DescriptorDTO descriptorDto = null;
            if (desc.isLink())
            {
               String linkText = DescriptorColumnUtils.getLinkDescriptorText(desc.getId(),
                     processInstance.getDescriptorDefinitions());
               descriptorDto = new DescriptorDTO(desc.getKey(), desc.getValue(), false, null, desc.isLink(), linkText);
            }
            else
            {
               descriptorDto = new DescriptorDTO(desc.getKey(), desc.getValue(), false, null);
            }
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
   public static DataTableOptionsDTO populatePostData(DataTableOptionsDTO options, String postData,
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
            else if (ColumnDataType.DATETIME.toString().equals(descriptorColumnDTO.type)
                  || ColumnDataType.DATE.toString().equals(descriptorColumnDTO.type)
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
            else if (ColumnDataType.LIST.toString().equals(descriptorColumnDTO.type))
            {
               filterDTO = new Gson().fromJson(descriptorColumnsFilterJson.get(id),
                     ProcessTableFilterDTO.TextSearchDTO.class);

            }
            descriptorColumnMap.put(id, new DescriptorFilterDTO(descriptorColumnDTO.type, filterDTO));
         }
      }

      processListFilterDTO.descriptorFilterMap = descriptorColumnMap;
   }

   /**
    * 
    * @param aOid
    * @return
    */
   public ProcessInstance findByStartingActivityOid(Long aOid)
   {
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      query.getFilter().add(ProcessInstanceQuery.STARTING_ACTIVITY_INSTANCE_OID.isEqual(aOid));
      query.setPolicy(DescriptorPolicy.NO_DESCRIPTORS);

      ProcessInstances pis = serviceFactoryUtils.getQueryService().getAllProcessInstances(query);
      ProcessInstance pi = null;
      if (!pis.isEmpty())
      {
         pi = pis.get(0);
      }
      return pi;
   }

   /**
    * @param pi
    * @return
    */
   public List<DescriptorItemTableEntry> fetchDescriptorsWithLastModified(ProcessInstance pi)
   {

      List<DescriptorItemTableEntry> descriptorList = new ArrayList<DescriptorItemTableEntry>();

      Map<String, DataPathDetails> inDataPathsMap = CollectionUtils.newHashMap();
      Map<String, DataPathDetails> outDataPathsMap = CollectionUtils.newHashMap();
      Map<String, HistoricalEvent> dataPathHistoryMap = CollectionUtils.newHashMap();

      // Populate in and out data path maps
      ProcessDefinition processDef = ProcessDefinitionUtils.getProcessDefinition(pi.getModelOID(), pi.getProcessID());
      List<DataPathDetails> dataPaths = processDef.getAllDataPaths();
      DataPathDetails dataPathDetails;
      int size = dataPaths.size();
      for (int i = 0; i < size; i++)
      {
         dataPathDetails = (DataPathDetails) dataPaths.get(i);
         if (null != dataPathDetails.getDirection())
         {
            if (dataPathDetails.getDirection().equals(Direction.OUT))
            {
               outDataPathsMap.put(dataPathDetails.getId(), dataPathDetails);
            }
            else
            {
               inDataPathsMap.put(dataPathDetails.getId(), dataPathDetails);
            }
         }
      }

      dataPathHistoryMap = getDataPathHistoryMap(pi, outDataPathsMap);

      descriptorList = fetchProcessDescriptorsAndAddHistoryDetails(pi, dataPathHistoryMap, inDataPathsMap,
            outDataPathsMap);

      return descriptorList;
   }

   /**
    * @param pi
    * @param outDataPathsMap
    * @return
    */
   private Map<String, HistoricalEvent> getDataPathHistoryMap(ProcessInstance pi,
         Map<String, DataPathDetails> outDataPathsMap)
   {
      Map<String, HistoricalEvent> dataPathHistoryMap = CollectionUtils.newHashMap();
      List<HistoricalEvent> events = CollectionUtils.newArrayList();
      events = DescriptorColumnUtils.getProcessDescriptorsHistory(pi);
      for (HistoricalEvent event : events)
      {
         String descriptorDetails = (String) event.getDetails();
         if (StringUtils.isNotEmpty(descriptorDetails))
         {
            if (descriptorDetails.contains("'"))
            {
               String[] token = descriptorDetails.split("'");
               String dataPathId = token[5];
               DataPath dataPath = outDataPathsMap.get(dataPathId);
               if (null != dataPath)
               {
                  dataPathId = I18nUtils.getDataPathName(dataPath);
               }
               dataPathHistoryMap.put(dataPathId, event);
            }
         }
      }
      return dataPathHistoryMap;
   }

   /**
    * @param pi
    * @param dataPathHistoryMap
    * @param inDataPathsMap
    * @param outDataPathsMap
    * @return
    */
   private List<DescriptorItemTableEntry> fetchProcessDescriptorsAndAddHistoryDetails(ProcessInstance pi,
         Map<String, HistoricalEvent> dataPathHistoryMap, Map<String, DataPathDetails> inDataPathsMap,
         Map<String, DataPathDetails> outDataPathsMap)
   {

      List<DescriptorItemTableEntry> decsriptorList = CollectionUtils.newArrayList();

      GenericDataMapping mapping;
      DataMappingWrapper dmWrapper;

      List<ProcessDescriptor> processDescriptors = CommonDescriptorUtils.createProcessDescriptors(pi, false);
      boolean suppressBlankDescriptors = CommonDescriptorUtils.isSuppressBlankDescriptorsEnabled();
      for (ProcessDescriptor processDescriptor : processDescriptors)
      {
         DataPathDetails inDataPath = inDataPathsMap.get(processDescriptor.getId());
         if (CollectionUtils.isNotEmpty(outDataPathsMap))
         {
            DataPathDetails outDataPath = fetchRespectiveOutDataPath(inDataPath, outDataPathsMap);
            if (null != outDataPath)
            {
               Class dataClass = outDataPath.getMappedType();
               mapping = new GenericDataMapping(outDataPath);
               dmWrapper = new DataMappingWrapper(mapping, null, false);
               String type = dmWrapper.getType();

               boolean hideTime = inDataPath.getAttribute(PredefinedConstants.HIDE_TIME) != null
                     ? (Boolean) inDataPath.getAttribute(PredefinedConstants.HIDE_TIME)
                     : false;
               boolean useServerTimezone = inDataPath.getAttribute(PredefinedConstants.USE_SERVERTIME) != null
                     ? (Boolean) inDataPath.getAttribute(PredefinedConstants.USE_SERVERTIME)
                     : false;

               boolean isEditable = true;

               if (dataClass.getName().equals(Calendar.class.getName()))
               {
                  type = ProcessPortalConstants.CALENDER_TYPE;
               }
               Object value = processDescriptor.getValue();

               DescriptorItemTableEntry descriptorTableRowObj = new DescriptorItemTableEntry(processDescriptor.getKey(),
                     value, processDescriptor.getId(), type, dataClass, isEditable);
               if (dataPathHistoryMap.containsKey(processDescriptor.getId()))
               {
                  HistoricalEvent event = dataPathHistoryMap.get(processDescriptor.getId());
                  descriptorTableRowObj.setLastModified(event.getEventTime());
                  descriptorTableRowObj.setModifiedBy(event.getUser().getName());
               }
               descriptorTableRowObj.setHideTime(hideTime);
               descriptorTableRowObj.setUseServerTimeZone(useServerTimezone);
               decsriptorList.add(descriptorTableRowObj);
            }
            else
            {
               if (!suppressBlankDescriptors || (suppressBlankDescriptors
                     && (null != processDescriptor.getValue() && StringUtils.isNotEmpty(processDescriptor.getValue()))))
               {
                  DescriptorItemTableEntry descriptorTableRowObj = new DescriptorItemTableEntry(
                        processDescriptor.getKey(), processDescriptor.getValue());
                  decsriptorList.add(descriptorTableRowObj);
               }
            }
         }
         else if (!suppressBlankDescriptors || (suppressBlankDescriptors
               && (null != processDescriptor.getValue() && StringUtils.isNotEmpty(processDescriptor.getValue()))))
         {
            {
               if (processDescriptor.isLink())
               {
                  List<DataPath> dataPaths = pi.getDescriptorDefinitions();
                  String linkText = DescriptorColumnUtils.getLinkDescriptorText(processDescriptor.getId(), dataPaths);

                  decsriptorList
                        .add(new DescriptorItemTableEntry(processDescriptor.getKey(), processDescriptor.getValue(),
                              processDescriptor.getId(), "Link", String.class, false, linkText));
               }
               else
               {
                  decsriptorList
                        .add(new DescriptorItemTableEntry(processDescriptor.getKey(), processDescriptor.getValue()));
               }
            }
         }
      }
      return decsriptorList;
   }

   /**
    * @param inDataPath
    * @param outDataPathsMap
    * @return
    */
   private DataPathDetails fetchRespectiveOutDataPath(DataPathDetails inDataPath,
         Map<String, DataPathDetails> outDataPathsMap)
   {
      if (CollectionUtils.isNotEmpty(outDataPathsMap))
      {
         // read all OUT dataPath for given Data
         DataPathDetails outDataPath = outDataPathsMap.get(inDataPath.getId());
         if (null != outDataPath)
         {
            // Filter dataPath with same AccessPoint and on same Qualified Model
            if (outDataPath.getAccessPath().equals(inDataPath.getAccessPath()))
            {
               String data = inDataPath.getData();
               Data data1 = DescriptorFilterUtils.getData(inDataPath);
               Data data2 = DescriptorFilterUtils.getData(outDataPath);
               if (data1.equals(data2))
               {
                  return outDataPath;
               }
            }
         }
      }

      return null;
   }

}
