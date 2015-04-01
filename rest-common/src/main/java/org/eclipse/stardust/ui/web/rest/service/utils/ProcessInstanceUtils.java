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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.CustomOrderCriterion;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.rest.FilterDTO.BooleanDTO;
import org.eclipse.stardust.ui.web.rest.FilterDTO.RangeDTO;
import org.eclipse.stardust.ui.web.rest.FilterDTO.TextSearchDTO;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.InstanceCountsDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMap;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMap.NotificationDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMessageDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessTableFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessTableFilterDTO.DescriptorFilterDTO;
import org.eclipse.stardust.ui.web.viewscommon.common.DateRange;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.GenericDescriptorFilterModel;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.NumberRange;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessContextCacheManager;
import org.springframework.stereotype.Component;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component("ProcessInstanceUtilsREST")
public class ProcessInstanceUtils
{

   private static final Logger trace = LogManager.getLogger(ProcessInstanceUtils.class);
   
   private static final String COL_PROCESS_NAME = "processName";

   private static final String COL_PROCESS_INSTANCE_OID = "oid";
   
   private static final String PROCESS_INSTANCE_ROOT_OID = "processInstanceRootOID";

   private static final String COL_START_TIME = "startTime";

   private static final String COL_END_TIME = "endTime";

   private static final String COL_STATUS = "status";

   private static final String COL_PRIOIRTY = "priority";
   
   private static final String STARTING_USER = "startingUser";

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private ModelUtils modelUtils;

   @Resource
   private ProcessDefinitionUtils processDefinitionUtils;
   
   @Resource
   private ProcessDefinitionUtils processDefUtils;

   /**
    * @param oid
    * @return
    */
   public ProcessInstance getProcessInstance(long oid)
   {
      ProcessInstance pi = null;
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      query.where(ProcessInstanceQuery.OID.isEqual(oid));
      ProcessInstances pis = serviceFactoryUtils.getQueryService()
            .getAllProcessInstances(query);

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
         StringBuilder processLabel = new StringBuilder(I18nUtils.getProcessName(ProcessDefinitionUtils
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
         if(!AuthorizationUtils.hasManageCasePermission(pi))
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
            FilterOrTerm orTerm =  piQuery.getFilter().addOrTerm();
   
            if (withDescriptors)
            {
               piQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
            }
            
            // Prepare Data to fetch
            for (Long oid : oids)
            {
               orTerm.add(new ProcessInstanceFilter(oid,false));
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
   public static boolean isCaseWithNoncaseProcessInstances(List<ProcessInstance> processInstances)
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
   public static boolean isCaseProcessInstances(List<ProcessInstance> processInstances)
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
   public static boolean isActiveProcessInstances(List<ProcessInstance> processInstances)
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
   public static boolean isTerminatedProcessInstances(List<ProcessInstance> processInstances)
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
         Object o = ws.getInDataPath(processInstance.getOID(),
               DmsConstants.PATH_ID_ATTACHMENTS);

         DataDetails data = (DataDetails) modelUtils.getModel(
               processInstance.getModelOID()).getData(DmsConstants.PATH_ID_ATTACHMENTS);
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
         ProcessDefinition pd = model != null ? model
               .getProcessDefinition(processInstance.getProcessID()) : null;

         supportsProcessAttachments = processDefinitionUtils
               .supportsProcessAttachments(pd);
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
         countDTO.waiting = getInterruptedProcessInstancesCount();
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
      if(processOids != null && !CollectionUtils.isEmpty(processOids))
      {
         AdministrationService adminService = serviceFactoryUtils.getAdministrationService();
         if (adminService != null)
         {
            adminService.recoverProcessInstances(processOids);
         }
      }
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
      if (sourceProcessInstanceOids.size() == 1) {
         ProcessInstance processInstance = getProcessInstance(sourceProcessInstanceOids.get(0));
         caseScope = processInstance.isCaseProcessInstance();
      }
      
      // Validation cases
      NotificationMessageDTO validationMessage = validateAttachToCase(caseScope, sourceProcessInstanceOids, targetOid);
      if (validationMessage != null) {
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
   
   public NotificationMessageDTO createCase(List<Long> sourceProcessInstanceOids, String caseName, String description, String note) {
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      try
      {
         // Validation cases
         NotificationMessageDTO validationMessage = validateCreateCase(sourceProcessInstanceOids, caseName);
         if (validationMessage != null) {
            return validationMessage;
         }
         
         // create Case
         long[] processOIDs = new long[sourceProcessInstanceOids.size()];
         for (int i = 0; i < processOIDs.length; i++)
         {
            processOIDs[i] = sourceProcessInstanceOids.get(i);
         }
         ProcessInstance caseProcessInstance = serviceFactoryUtils.getWorkflowService().createCase(caseName,description, processOIDs);
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
         exception.message = propsBean.getString("views.createCase.caseException") + " : "
               + e.getLocalizedMessage();
         
         return exception;
      }
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
      } else if (null == sourceProcessInstanceOids || sourceProcessInstanceOids.isEmpty()) {
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
   
   private NotificationMessageDTO validateAttachToCase(boolean caseScope, List<Long> sourceProcessInstanceOids, Long targetOid)
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
      
      if(null == targetOid)
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
      else if (!caseScope && isTargetCase && ! AuthorizationUtils.hasManageCasePermission(targetInstance))
      {
         validationMessage.message = propsBean.getString("views.attachToCase.caseAttach.notAuthorizedToManageCase");
         return validationMessage;
      }
      else if(!caseScope && !isRootProcessInstance(targetInstance))
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
               if (processInstance != null
                     && isAbortable(processInstance))
               {
                  try
                  {
                     if (processInstance.isCaseProcessInstance())
                     {
                        notificationMap
                              .addFailure(new NotificationDTO(processInstanceOid,
                                    org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
                                          .getProcessLabel(processInstance), MessagesViewsCommonBean.getInstance()
                                          .getParamString(
                                                "views.switchProcessDialog.caseAbort.message",
                                                org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
                                                      .getProcessStateLabel(processInstance))));
                     }
                     else
                     {
                        abortProcess(processInstance,
                              abortScope);
                        notificationMap
                              .addSuccess(new NotificationDTO(processInstanceOid,
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
                           getProcessLabel(processInstance),
                           MessagesViewsCommonBean.getInstance().getParamString("views.common.process.abortProcess.failureMsg2",
                                 ExceptionHandler.getExceptionMessage(e))));
                  }
               }
               else
               {
                  if (ProcessInstanceState.Aborted.equals(processInstance.getState())
                        || ProcessInstanceState.Completed.equals(processInstance.getState()))
                  {
                     notificationMap.addFailure(new NotificationDTO(processInstanceOid,
                           getProcessLabel(processInstance),
                           MessagesViewsCommonBean.getInstance().getParamString(
                                 "views.common.process.abortProcess.failureMsg3",
                                 org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils
                                       .getProcessStateLabel(processInstance))));
                  }
                  else
                  {
                     notificationMap.addFailure(new NotificationDTO(processInstanceOid,
                           getProcessLabel(processInstance),
                           MessagesViewsCommonBean.getInstance().getString("views.common.process.abortProcess.failureMsg1")));
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
    * @param options
    * @return
    */
   public ProcessInstances getProcessInstances(Options options)
   {
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      
      addDescriptorPolicy(options, query);

      addSortCriteria(query, options);

      addFilterCriteria(query, options);
      
      SubsetPolicy subsetPolicy = new SubsetPolicy(options.pageSize, options.skip,
            true);
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
      if (null != filterDTO.processInstanceRootOID)
      {

         if (null != filterDTO.processInstanceRootOID.from)
         {
            filter.and(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.greaterOrEqual(filterDTO.processInstanceRootOID.from));
         }
         if (null != filterDTO.processInstanceRootOID.to)
         {
            filter.and(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.lessOrEqual(filterDTO.processInstanceRootOID.to));
         }
      }
      // process instance OID
      if (null != filterDTO.oid)
      {
         if (null != filterDTO.oid.from)
         {
            filter.and(ProcessInstanceQuery.OID.greaterOrEqual(filterDTO.oid.from));
         }
         if (null != filterDTO.oid.to)
         {
            filter.and(ProcessInstanceQuery.OID.greaterOrEqual(filterDTO.oid.to));
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

            filter.and(ProcessInstanceQuery.START_TIME.greaterOrEqual(toDate.getTime()));
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
         for (String processQId : filterDTO.processName.like)
         {
            or.add(new ProcessDefinitionFilter(processQId, false));
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
         // TODO

         // FilterOrTerm or = filter.addOrTerm();
         // for (UserWrapper user : users)
         // {
         // or.add(new
         // org.eclipse.stardust.engine.api.query.StartingUserFilter(user.getUser().getOID()));
         // }
      }

      // descriptors Filter
      addDescriptorFilters(query, filterDTO);

   }
   /**
    * Add descriptor policy
    * @param options
    * @param query
    */

   private void addDescriptorPolicy(Options options, Query query)
   {
      
      if(options.allDescriptorsVisible){
         query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      }else if(CollectionUtils.isNotEmpty(options.visibleDescriptorColumns)){          
         query.setPolicy(DescriptorPolicy.withIds(new HashSet<String>(options.visibleDescriptorColumns)));
      }else{
         query.setPolicy(DescriptorPolicy.NO_DESCRIPTORS);
      }
   }

   /**
    * Add filter on descriptor columns .
    * @param query
    * @param processListFilterDTO
    */
   private void addDescriptorFilters(Query query, ProcessTableFilterDTO processListFilterDTO)
   {

      Map<String, DescriptorFilterDTO> descFilterMap = processListFilterDTO.descriptorFilterMap;

      if (null != descFilterMap)
      {
        
         Map<String, DataPath> descriptors = processDefUtils.getAllDescriptors(false);
         GenericDescriptorFilterModel filterModel = GenericDescriptorFilterModel
               .create(descriptors.values());
         filterModel.setFilterEnabled(true);

         for (Map.Entry<String, DescriptorFilterDTO> descriptor : descFilterMap
               .entrySet())
         {
            Object value = null;
            String key = StringUtils.substringAfter(descriptor.getKey(),
                  "descriptorValues.");

            // Boolean type desc
            if (descriptor.getValue().type.equals(ColumnDataType.BOOLEAN.toString()))
            {
               value = ((BooleanDTO)descriptor.getValue().value).equals;
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

         DescriptorFilterUtils.applyFilters(query, filterModel);
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

      //TODO sorting for descriptors
      
      
      if (COL_PROCESS_NAME.equals(options.orderBy))
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
      else
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("ProcessInstanceUtils.addSortCriteria(Query, Options): Sorting not implemented for " + options.asc);
         }
      }
   }
}
