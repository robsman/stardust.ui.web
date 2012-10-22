/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.dto.AuditTrailPersistence;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.LinkDirection;
import org.eclipse.stardust.engine.api.query.ProcessInstanceDetailsPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.PredefinedProcessInstanceLinkTypes;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.common.converter.PriorityConverter;
import org.eclipse.stardust.ui.web.viewscommon.common.notification.NotificationItem;
import org.eclipse.stardust.ui.web.viewscommon.common.notification.NotificationMessageBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.JoinProcessDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.SpawnProcessDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.SwitchProcessDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils.ParticipantType;
import org.eclipse.stardust.ui.web.viewscommon.views.casemanagement.AttachToCaseDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.views.casemanagement.CreateCaseDialogBean;



public class ProcessInstanceUtils
{
   public static final Logger trace = LogManager.getLogger(ProcessInstanceUtils.class);
   private static final String STATUS_PREFIX = "views.processTable.statusFilter.";
   
   public static String PRIORITY_LOW = "low";
   public static String PRIORITY_NORMAL = "normal";
   public static String PRIORITY_HIGH = "high";
   
   private static final Map<String, String> PRIORITY_COLOR_MAP = new HashMap<String, String>();
   static {
      PRIORITY_COLOR_MAP.put(PRIORITY_LOW, "/plugins/views-common/images/icons/flag-blue.png");
      PRIORITY_COLOR_MAP.put(PRIORITY_NORMAL, "/plugins/views-common/images/icons/flag-yellow.png");
      PRIORITY_COLOR_MAP.put(PRIORITY_HIGH, "/plugins/views-common/images/icons/flag.png");
   }

   /**
    * @param processInstance
    * @return
    */
   public static View openNotes(ProcessInstance processInstance)
   {
      return openNotes(processInstance, null);
   }

   /**
    * @param processInstance
    * @param additionalParams
    * @return
    */
   public static View openNotes(ProcessInstance processInstance, Map<String, Object> additionalParams)
   {
      if (null != processInstance)
      {
         Map<String, Object> params = CollectionUtils.newTreeMap();
         params.put("oid", Long.toString(processInstance.getOID()));
         params.put("processName", I18nUtils.getProcessName(ProcessDefinitionUtils.getProcessDefinition(processInstance
               .getModelOID(), processInstance.getProcessID())));
         
         if (CollectionUtils.isNotEmpty(additionalParams))
         {
            for (Entry<String, Object> entry : additionalParams.entrySet())
            {
               params.put(entry.getKey(), entry.getValue());
            }
         }
         return PortalApplication.getInstance().openViewById("notesPanel", "oid=" + processInstance.getOID(), params,
               null, true);
      }
      return null;
   }
   
   /**
    * Returns the notes for provided process instance
    * 
    * @param pi
    * @return
    */
   public static List<Note> getNotes(ProcessInstance pi)
   {
      ProcessInstance scopePi = pi.getScopeProcessInstance();
      ProcessInstanceAttributes attributes = scopePi.getAttributes();
      if (null != attributes)
      {
         return attributes.getNotes();
      }
      else
      {
         return null;
      }
   }

   /**
    * @param pi
    * @return
    */
   public static View openProcessContextExplorer(ProcessInstance pi)
   {
      Map<String, Object> params = CollectionUtils.newHashMap();
      params.put("processInstanceOID", String.valueOf(pi.getOID()));
      params.put("processInstanceName", pi.getProcessName());

      String key = "processInstanceOID=" + pi.getOID();
      if (pi.isCaseProcessInstance())
      {
         return PortalApplication.getInstance().openViewById("caseDetailsView", key, params, null, true);
      }

      return PortalApplication.getInstance().openViewById("processInstanceDetailsView", key, params, null, true);
   }

   /**
    * @param poids
    */
   public static void recoverProcessInstance(List<Long> poids)
   {
      AdministrationService adminService = ServiceFactoryUtils.getAdministrationService();
      if (poids != null && !CollectionUtils.isEmpty(poids))
      {
         if (adminService != null)
         {
            try
            {
               adminService.recoverProcessInstances(poids);
               MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
               MessageDialog.addInfoMessage(propsBean.getString("views.common.recoverMessage"));
            }
            catch (Exception e)
            {
               ExceptionHandler.handleException(e);
            }
         }
      }
   }

   /**
    * @param processInstance
    * @return
    */
   public static boolean isAbortable(ProcessInstance processInstance)
   {
      boolean abortable = isAbortableState(processInstance) && AuthorizationUtils.hasAbortPermission(processInstance);
      return abortable;
   }
   
   /**
    * @param processInstance
    * @return
    */
   public static boolean isAbortableState(ProcessInstance processInstance)
   {
      boolean abortable = processInstance == null ? false : !ProcessInstanceState.Aborted.equals(processInstance
            .getState()) && !ProcessInstanceState.Completed.equals(processInstance.getState());
      return abortable;
   }
   
   /**
    * @param ai
    * @return
    */
   public static ProcessInstance getProcessInstance(ActivityInstance ai)
   {
      return getProcessInstance(ai.getProcessInstanceOID());
   }

   /**
    * @param oid
    * @return
    */
   public static ProcessInstance getProcessInstance(long oid)
   {
      return getProcessInstance(oid, false);
   }

   /**
    * @param oid
    * @param forceReload
    * @return
    */
   public static ProcessInstance getProcessInstance(long oid, boolean forceReload)
   {
      return getProcessInstance(oid, forceReload, false);
   }

   /**
    * @param oid
    * @param forceReload
    * @param withDescriptors
    * @return
    */
   public static ProcessInstance getProcessInstance(long oid, boolean forceReload, boolean withDescriptors)
   {
      List<ProcessInstance> processInstances = getProcessInstances(Arrays.asList(oid), forceReload, withDescriptors);
      return CollectionUtils.isNotEmpty(processInstances) ? processInstances.get(0) : null;     
   }

   
   /**
    * @param oids
    * @return
    */
   public static List<ProcessInstance> getProcessInstances(List<Long> oids)
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
   public static List<ProcessInstance> getProcessInstances(List<Long> oids, boolean forceReload, boolean withDescriptors)
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

            ProcessInstances pis = ServiceFactoryUtils.getQueryService().getAllProcessInstances(piQuery);
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
    * @param oids
    * @param forceReload
    * @return
    */
   public static List<ProcessInstance> getProcessInstances(List<Long> oids, boolean forceReload)
   {
      return getProcessInstances(oids, forceReload, true);
   }
   
   /**
    * @param result
    * @param forceReload
    * @return
    */
   public static Map<Long, ProcessInstance> getProcessInstancesAsMap(QueryResult<?> result, boolean forceReload)
   {
      List<Long> processInstanceIds = CollectionUtils.newList();
      for (Object resultObject : result)
      {
         if(resultObject instanceof ActivityInstance)
         {
            processInstanceIds.add(((ActivityInstance)resultObject).getProcessInstanceOID());
         }
         else if (resultObject instanceof ProcessInstance)
         {
            processInstanceIds.add(((ProcessInstance)resultObject).getOID());
         }
      }

      List<ProcessInstance> processInstances = getProcessInstances(processInstanceIds, forceReload, false);
      
      Map<Long, ProcessInstance> piMap  = CollectionUtils.newMap();
      for (ProcessInstance processInstance : processInstances)
      {
         piMap.put(processInstance.getOID(), processInstance);
      }

      return piMap;
   }
   
   /**
    * returns the process instance duration
    * @param processInstance
    * @return
    */
   public static String getDuration(ProcessInstance processInstance)
   {
      long startTime = processInstance.getStartTime().getTime();
      long endTime;

      if (processInstance.getTerminationTime() != null)
      {
         endTime = processInstance.getTerminationTime().getTime();
      }
      else
      {
         endTime = Calendar.getInstance().getTimeInMillis();
      }
      return DateUtils.formatDurationInHumanReadableFormat(endTime - startTime);
   }
   
   /**
    * abort process
    * 
    * @param pi
    * @param abortScope
    */
   public static void abortProcess(ProcessInstance pi, AbortScope abortScope)
   {
      ProcessInstanceDetails processInstanceDetails = (ProcessInstanceDetails) pi;
      WorkflowService workflowService = ServiceFactoryUtils.getWorkflowService();
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
    * @param processInstanceOID
    * @param priority
    */
   public static void setProcessPriority(long processInstanceOID, int priority)
   {
      ServiceFactoryUtils.getAdministrationService().setProcessInstancePriority(processInstanceOID, priority,
            isPropagatePrioritySwitchOn());
   }
   
   /**
    * @return
    */
   public static Boolean isPropagatePrioritySwitchOn()
   {
      UserPreferencesHelper userPrefHelper = UserPreferencesHelper.getInstance(UserPreferencesEntries.M_VIEWS_COMMON,
            PreferenceScope.USER);
      return userPrefHelper.getBoolean(UserPreferencesEntries.V_WORKFLOW_EXEC_CONFIG_PANEL,
            UserPreferencesEntries.F_PROPAGATE_PRIORITY, false);
   }
   
   /**
    * @return AbortProcessScope from user preferences
    */
   public static AbortScope getAbortProcessScope()
   {
      UserPreferencesHelper userPrefHelper = UserPreferencesHelper.getInstance(UserPreferencesEntries.M_VIEWS_COMMON,
            PreferenceScope.USER);
      String abortProcessScope = userPrefHelper.getSingleString(UserPreferencesEntries.V_WORKFLOW_EXEC_CONFIG_PANEL,
            UserPreferencesEntries.F_PROCESS_ABORT_SCOPE);

      if (abortProcessScope.equals(AbortScope.ROOT_HIERARCHY))
      {
         return AbortScope.RootHierarchy;
      }
      else if (abortProcessScope.equals(AbortScope.SUB_HIERARCHY))
      {
         return AbortScope.SubHierarchy;
      }
      return null;
   }

   /**
    * update process priorities
    * 
    * @param changedProcess
    */
   public static void updatePriorities(Map<Object, Integer> changedProcess)
   {
      List<NotificationItem> successItemsList = new ArrayList<NotificationItem>();
      List<NotificationItem> failureItemsList = new ArrayList<NotificationItem>();

      for (Entry<Object, Integer> entry : changedProcess.entrySet())
      {
         String itemLabel;
         Long processOID;
         Integer priority;
         // if the action is invoked from process table
         if (entry.getKey() instanceof ProcessInstance)
         {
            ProcessInstance processInstance = (ProcessInstance) entry.getKey();
            itemLabel = getProcessLabel(processInstance);
            processOID = processInstance.getOID();
            priority = entry.getValue();
         }
         // if the action is invoked from activity table
         else if (entry.getKey() instanceof ActivityInstance)
         {
            ActivityInstance activityInstance = (ActivityInstance) entry.getKey();
            itemLabel = ActivityInstanceUtils.getActivityLabel(activityInstance);
            processOID = activityInstance.getProcessInstanceOID();
            priority = entry.getValue();
         }
         else
         {
            continue;
         }
         try
         {
            ProcessInstanceUtils.setProcessPriority(processOID, priority);
            successItemsList.add(new NotificationItem(itemLabel, MessagesViewsCommonBean.getInstance().getParamString(
                  "views.processTable.savePriorities.priorityChanged",
                  PriorityConverter.getPriorityLabel(priority))));
         }
         catch (AccessForbiddenException e)
         {
            failureItemsList.add(new NotificationItem(itemLabel, MessagesViewsCommonBean.getInstance().getString("common.authorization.msg")));
            trace.error("Authorization exception occurred while changing process priority: ", e);
         }
         catch (Exception e)
         {
            failureItemsList.add(new NotificationItem(itemLabel, ExceptionHandler.getExceptionMessage(e)));
            trace.error("Exception occurred while changing process priority: ", e);
         }
      }
      // show notifications
      String title = MessagesViewsCommonBean.getInstance().getString("views.processTable.savePriorities.status");
      String successTitle = title
            + MessagesViewsCommonBean.getInstance().getString("views.processTable.savePriorities.successMsg");
      String failureTitle = title
            + MessagesViewsCommonBean.getInstance().getString("views.processTable.savePriorities.failureMsg");
      String itemTitle = MessagesViewsCommonBean.getInstance().getString("common.notification.selectedItems");
      String itemStatusTitle = MessagesViewsCommonBean.getInstance().getString("common.notification.actionStatus");
      NotificationMessageBean.showNotifications(successItemsList, successTitle, failureItemsList, failureTitle,
            itemTitle, itemStatusTitle, null);
   }

   /**
    * @param instance
    * @return localized process name with OID appended
    */
   public static String getProcessLabel(ProcessInstance instance)
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
    * @param instance
    * @return process definition for process instance
    */
   public static  ProcessDefinition getProcessDefination(ProcessInstance processInstance)
   {
      if (null != processInstance)
      {
         return ProcessDefinitionUtils.getProcessDefinition(processInstance.getModelOID(),
               processInstance.getProcessID());
      }
      return null;
   }

   
   /**
    * @param pi
    * @return Localized process state name
    */
   public static String getProcessStateLabel(ProcessInstance pi)
   {
      return MessagesViewsCommonBean.getInstance().getString(STATUS_PREFIX + pi.getState().getName().toLowerCase());
   }
   
   /**
    * @param priorityIdent
    * @return
    */
   public static String getPriorityLabel(int priorityIdent)
   {
      return MessagesViewsCommonBean.getInstance().getString("common.process.priority.options." + priorityIdent);
   }
   
   /**
    * 
    * @param iconValue
    * @return
    */
   public static String getPriorityIcon(String iconValue)
   {
      return PRIORITY_COLOR_MAP.get(iconValue);
   }

   /**
    * Method will find the LinkedProcess in given direction and return the ProcessInstance
    * 
    * @param instance
    * @param linkDir
    * @return
    */
   public static ProcessInstance getLinkInfo(ProcessInstance instance, LinkDirection linkDir, String linkType)
   {
      if (null != instance)
      {
         ProcessInstanceQuery query = ProcessInstanceQuery.findLinked(instance.getOID(), linkDir, linkType);
         ProcessInstanceDetailsPolicy processInstanceDetailsPolicy = new ProcessInstanceDetailsPolicy(
               ProcessInstanceDetailsLevel.Default);
         processInstanceDetailsPolicy.getOptions().add(ProcessInstanceDetailsOptions.WITH_LINK_INFO);
         query.setPolicy(processInstanceDetailsPolicy);

         List<ProcessInstance> pis = ServiceFactoryUtils.getQueryService().getAllProcessInstances(query);
         if (CollectionUtils.isNotEmpty(pis))
            return pis.get(0);
         else
            return null;
      }
      return null;
   }

   /**
    * Method will find the LinkedProcess in given direction and return the ProcessInstance
    * 
    * @param instance
    * @param linkDir
    * @return
    */
   public static ProcessInstance getLinkInfo(ProcessInstance instance, LinkDirection linkDir, PredefinedProcessInstanceLinkTypes linkType)
   {
      if (null != instance)
      {
         ProcessInstanceQuery query = ProcessInstanceQuery.findLinked(instance.getOID(), linkDir, linkType);
         ProcessInstanceDetailsPolicy processInstanceDetailsPolicy = new ProcessInstanceDetailsPolicy(
               ProcessInstanceDetailsLevel.Default);
         processInstanceDetailsPolicy.getOptions().add(ProcessInstanceDetailsOptions.WITH_LINK_INFO);
         query.setPolicy(processInstanceDetailsPolicy);

         List<ProcessInstance> pis = ServiceFactoryUtils.getQueryService().getAllProcessInstances(query);
         if (CollectionUtils.isNotEmpty(pis))
            return pis.get(0);
         else
            return null;
      }
      return null;
   }

   /**
    * To check that all given ProcessInstance are in same model version for Non-Case
    * processes,return null if different modelOID else return the common ModelOID
    * If there is only 1 process i.e Case Process, return Case ModelOID.
    * 
    * @return
    */
   public static Integer getProcessModelOID(List<ProcessInstance> processInstances)
   {
      Integer modelOID = null;
      for (ProcessInstance pi : processInstances)
      {
         if (null == modelOID)
         {
            if (!pi.isCaseProcessInstance())
            {
               modelOID = pi.getModelOID();
            }
            else if (processInstances.size() == 1)
            {
               modelOID = pi.getModelOID();
            }

         }
         else
         {
            if (!pi.isCaseProcessInstance())
            {
               if (modelOID != pi.getModelOID())
               {
                  return null;
               }
            }
         }
      }
      return modelOID;
   }
   
   /**
    * method to find root process of given processes without descriptor
    * 
    * @param sourceProcessInstances
    * @return
    */
   public static List<ProcessInstance> getRootProcessInstances(final List<ProcessInstance> sourceProcessInstances)
   {
      List<ProcessInstance> rootProcessInstances = CollectionUtils.newArrayList();
      List<Long> oids = CollectionUtils.newArrayList();

      for (ProcessInstance pi : sourceProcessInstances)
      {
         if (pi.getRootProcessInstanceOID() != pi.getOID())
         {
            ProcessInstance rootProcessInstance = getProcessInstance(pi.getRootProcessInstanceOID());
            if (rootProcessInstance.isCaseProcessInstance())
            {
               oids.add(pi.getOID());
            }
            else
            {
               oids.add(pi.getRootProcessInstanceOID());
            }

         }
         else
         {
            rootProcessInstances.add(pi);
         }
      }
      if (CollectionUtils.isNotEmpty(oids))
      {
         rootProcessInstances.addAll(getProcessInstances(oids, false, false));
      }
      oids = null;
      return rootProcessInstances;
   }

   /**
    * method to find root process of given process without descriptor If checkCaseInstance
    * is true check for caseInstance , and return sourcePI/ParentPI rather than rootPI
    * 
    * @param sourceProcessInstance
    * @param checkCaseInstance
    * @return root process instance
    */
   public static ProcessInstance getRootProcessInstance(ProcessInstance sourceProcessInstance, boolean checkCaseInstance)
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
   
   public static List<ProcessInstance> findChildren(ProcessInstance rootPi)
   {
      ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAll();
      FilterAndTerm filter = piQuery.getFilter().addAndTerm();
      filter.and(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.isEqual(rootPi.getOID()));
      filter.and(ProcessInstanceQuery.OID.notEqual(rootPi.getOID()));
     
         piQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
    

      // Fetch the Data from Engine
      ProcessInstances pis = ServiceFactoryUtils.getQueryService().getAllProcessInstances(piQuery);
      return pis;
   }  
  
   /**
    * 
    * @param processInstance
    * @return
    */
   public static boolean isRootProcessInstance(ProcessInstance processInstance)
   {
      return processInstance.getRootProcessInstanceOID() == processInstance.getOID() ? true : false;
   }

   /**
    * 
    * @param processInstances
    * @return
    */
   public static boolean isRootProcessInstances(List<ProcessInstance> processInstances)
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
    * @param processInstance
    * @return
    */
   public static boolean isActiveProcessInstance(ProcessInstance processInstance)
   {
      if (ProcessInstanceState.ACTIVE != processInstance.getState().getValue())
      {
         return false;
      }
      return true;
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
   public static boolean isNonCaseProcessInstances(List<ProcessInstance> processInstances)
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
    * get Case Name from processInstance
    * @param processInstance
    * @return
    */
   public static String getCaseName(ProcessInstance processInstance)
   {
      ProcessInstanceDetails processInstanceDetails = (ProcessInstanceDetails) processInstance;
      Map<String, Object> descriptorValues = processInstanceDetails.getDescriptors();
      Object caseName = descriptorValues.get(PredefinedConstants.CASE_NAME_ELEMENT);
      return null != caseName ? caseName.toString() : null;
   }
   
   /**
    * 
    * @param processInstance
    * @return
    */
   public static Participant getCaseOwner(ProcessInstance processInstance)
   {
      ActivityInstance activityInstance = ActivityInstanceUtils.getActivityInstance(processInstance);
      if (null != activityInstance.getCurrentPerformer())
      {
         return ParticipantUtils.getParticipant(activityInstance.getCurrentPerformer());
      }
      return null;
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
    * @return
    */
   public static String getStartingUser(ProcessInstance processInstance)
   {
      return UserUtils.getUserDisplayLabel(processInstance.getStartingUser());
   }
   
   /**
    * 
    * @param processInstanceList
    */
   public static void openAttachToCase(List<ProcessInstance> processInstanceList)
   {
      openAttachToCase(processInstanceList, false, null);
   }

   /**
    * 
    */
   public static void openAttachToCase(List<ProcessInstance> processInstanceList, boolean skipNotification,
         ICallbackHandler callbackHandler)
   {
      MessagesViewsCommonBean commonMessage = MessagesViewsCommonBean.getInstance();
      List<ProcessInstance> selectedProcesses = processInstanceList;
      if (CollectionUtils.isEmpty(selectedProcesses))
      {
         MessageDialog.addErrorMessage(commonMessage.getString("views.attachToCase.selectProcessToAttachToCase"));
      }
      else
      {
         boolean isRootProcessInstances = ProcessInstanceUtils.isRootProcessInstances(selectedProcesses);

         if (isRootProcessInstances)
         {
            boolean isMixProcessInstances = ProcessInstanceUtils.isCaseWithNoncaseProcessInstances(selectedProcesses);
            boolean isCaseProcessInstances = ProcessInstanceUtils.isCaseProcessInstances(selectedProcesses);

            if (isMixProcessInstances)
            {
               MessageDialog.addErrorMessage(commonMessage.getString("views.attachToCase.selectCaseOrProcess.message"));
            }
            else if (isCaseProcessInstances && selectedProcesses.size()>1)
            {
               MessageDialog.addErrorMessage(commonMessage
                     .getString("views.attachToCase.caseProcess.notSelectMultipleCases"));
            }
            else if (isCaseProcessInstances && !AuthorizationUtils.hasManageCasePermission(selectedProcesses.get(0)))
            {
               MessageDialog.addErrorMessage(commonMessage
                     .getString("views.attachToCase.caseAttach.notAuthorizedToManageCase"));
            }
            else if (isCaseProcessInstances && ProcessInstanceUtils.isTerminatedProcessInstances(selectedProcesses))
            {
               MessageDialog.addErrorMessage(commonMessage.getString("views.attachToCase.selectActiveCaseToAttach"));
            }
            else if (!isCaseProcessInstances && !ProcessInstanceUtils.isActiveProcessInstances(selectedProcesses))
            {
               MessageDialog.addErrorMessage(commonMessage
                     .getString("views.attachToCase.selectActiveProcessesToAttach"));
            }
            else
            {
               AttachToCaseDialogBean attachToCaseDialog = AttachToCaseDialogBean.getInstance();
               attachToCaseDialog.setSourceProcessInstances(selectedProcesses);
               attachToCaseDialog.setSkipNotification(skipNotification);
               attachToCaseDialog.setCallbackHandler(callbackHandler);
               attachToCaseDialog.openPopup();
            }
         }
         else
         {
            MessageDialog.addErrorMessage(commonMessage
                  .getString("views.attachToCase.nonRootProcessSelectedToCreateCase"));
         }
      }
   }
   
   /**
    * 
    */
   public static void openCreateCase(List<ProcessInstance> processInstanceList)
   {
      MessagesViewsCommonBean commonMessage = MessagesViewsCommonBean.getInstance();
      List<ProcessInstance> selectedProcesses = processInstanceList;
      if (CollectionUtils.isEmpty(selectedProcesses))
      {
         MessageDialog.addErrorMessage(commonMessage.getString("views.attachToCase.selectProcessToCreateCase"));
      }
      else
      {
         boolean isActiveProcessInstances = ProcessInstanceUtils.isActiveProcessInstances(selectedProcesses);
         if (isActiveProcessInstances)
         {
            boolean isRootProcessInstances = ProcessInstanceUtils.isRootProcessInstances(selectedProcesses);
            if (isRootProcessInstances)
            {
               boolean isNonCaseProcessInstances = ProcessInstanceUtils.isNonCaseProcessInstances(selectedProcesses);
               if (isNonCaseProcessInstances)
               {
                  CreateCaseDialogBean createCaseDialog = CreateCaseDialogBean.getInstance();
                  createCaseDialog.setSourceProcessInstances(selectedProcesses);
                  createCaseDialog.openPopup();
               }
               else
               {
                  MessageDialog
                        .addErrorMessage(commonMessage.getString("views.attachToCase.selectOnlyProcess.message"));
               }
            }
            else
            {
               MessageDialog.addErrorMessage(commonMessage
                     .getString("views.attachToCase.selectOnlyProcess.message"));
            }
         }
         else
         {
            MessageDialog.addErrorMessage(commonMessage
                  .getString("views.attachToCase.selectActiveProcesses"));
         }
      }
   }

   /**
    * method to open  Abort and Switch process dialog
    */
   public static void openSwitchProcessDialog(ProcessInstance processInstance)
   {
      SwitchProcessDialogBean dialog = SwitchProcessDialogBean.getInstance();
      List<ProcessInstance> sourceList = new ArrayList<ProcessInstance>(1);
      sourceList.add(processInstance);
      dialog.setSourceProcessInstances(sourceList);
      dialog.openPopup();
   }

   /**
    * method to open Abort and Switch process dialog
    */
   public static void openSwitchProcessDialog(List<ProcessInstance> processInstances)
   {
      SwitchProcessDialogBean dialog = SwitchProcessDialogBean.getInstance();
      dialog.setSourceProcessInstances(processInstances);
      dialog.openPopup();
   }

   /**
    * method to open Spawn process dialog
    */
   public static void openSpawnProcessDialog(ProcessInstance processInstance)
   {
      SpawnProcessDialogBean dialog = SpawnProcessDialogBean.getInstance();
      dialog.setSourceProcessInstance(processInstance);
      dialog.openPopup();
   }
   
   /**
    * method to open Abort and Join process dialog
    */
   public static void openJoinProcessDialog(ProcessInstance processInstance)
   {
      JoinProcessDialogBean dialog = JoinProcessDialogBean.getInstance();
      dialog.setSourceProcessInstance(processInstance);
      dialog.openPopup();
   }

   /**
    * 
    * @param pi
    * @return
    */
   public static String getPriorityValue(ProcessInstance pi)
   {
      String priorityValue = null;

      if (null != pi)
      {
         int priority = pi.getPriority();

         switch (priority)
         {
         case ProcessInstancePriority.LOW:
            priorityValue = PRIORITY_LOW;
            break;
         case ProcessInstancePriority.NORMAL:
            priorityValue = PRIORITY_NORMAL;
            break;
         case ProcessInstancePriority.HIGH:
            priorityValue = PRIORITY_HIGH;
            break;
         default:
            priorityValue = PRIORITY_NORMAL;
            break;
         }
      }

      return priorityValue;

   }
   
   public static Map<String, String> getPriorityColorMap()
   {
      return PRIORITY_COLOR_MAP;
   }
   
   /**
    * @param pi
    * @return
    */
   public static boolean isTransientProcess(ProcessInstance pi)
   {
      AuditTrailPersistence auditTrailPersistence = (AuditTrailPersistence) pi.getRuntimeAttributes().get(
            AuditTrailPersistence.class.getName());
      if (AuditTrailPersistence.isTransientExecution(auditTrailPersistence))
      {
         return true;
      }
      return false;
   }

   /**
    * @param pi
    * @return
    */
   public static boolean isCompletedProcess(ProcessInstance pi)
   {
      if (ProcessInstanceState.COMPLETED == pi.getState().getValue())
      {
         return true;
      }
      return false;
   }
}