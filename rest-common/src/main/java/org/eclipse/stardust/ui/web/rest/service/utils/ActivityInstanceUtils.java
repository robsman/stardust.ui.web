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

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.isActivatable;
import static org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.isSupportsWeb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalExecutionTimePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.DateRange;
import org.eclipse.stardust.engine.core.query.statistics.api.OpenActivitiesStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.OpenActivitiesStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.PostponedActivitiesStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.PostponedActivitiesStatistics.PostponedActivities;
import org.eclipse.stardust.engine.core.query.statistics.api.PostponedActivitiesStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.StatisticsDateRangePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatistics.Contribution;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatistics.PerformanceStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatisticsQuery;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.ui.event.ActivityEvent;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.OpenActivitiesCalculator;
import org.eclipse.stardust.ui.web.bcc.jsf.PostponedActivitiesCalculator;
import org.eclipse.stardust.ui.web.bcc.jsf.RoleItem;
import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.ColumnDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.CompletedActivitiesStatisticsDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.CompletedActivityPerformanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMap;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMap.NotificationDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.OpenActivitiesDynamicUserObjectDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.PathDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.PendingActivitiesStatisticsDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.PostponedActivitiesResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.PostponedActivitiesStatsDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.TrivialManualActivityDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ClientContextBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantWorklistCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessWorklistCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.SpecialWorklistCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.stereotype.Component;

/**
 * @author Anoop.Nair
 * @author Subodh.Godbole
 * @version $Revision: $
 */
@Component
public class ActivityInstanceUtils
{
   private static final Logger trace = LogManager.getLogger(ActivityInstanceUtils.class);

   private static final String STATUS_PREFIX = "views.activityTable.statusFilter.";

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private ModelUtils modelUtils;

   @Resource
   private DocumentUtils documentUtils;

   /**
    * @param oid
    * @return
    */
   public ActivityInstance getActivityInstance(long oid)
   {
      ActivityInstance ai = null;
      ArrayList<Long> oids = new ArrayList<Long>(Collections.singletonList(oid));
      List<ActivityInstance> ais = getActivityInstances(oids);
      if (!ais.isEmpty())
      {
         ai = ais.get(0);
      }

      return ai;
   }

   /**
    * @param activityOID
    * @return
    */
   @SuppressWarnings("unchecked")
   public List<ActivityInstance> getActivityInstances(List<Long> oids)
   {
      if (oids.size() == 0)
      {
         return new ArrayList<ActivityInstance>();
      }
      return (List<ActivityInstance>) getActivitiesByOids(null, oids);
   }

   /**
    * @param userId
    * @return
    */
   public QueryResult< ? > getActivityInstances(Options options)
   {
      return getActivitiesByOids(options, null);
   }

   /**
    * @param userId
    * @return
    */
<<<<<<< Upstream, based on origin/feature/ipp/portal-html5-contrib
   public QueryResult< ? > getActivitiesByOids(Options options, List<String> oids)
=======
   public QueryResult< ? > getActivitiesByOids(Options options, List<Long> oids)
>>>>>>> e927bc8 [CRNT-35275] : Removed redunant code for fetching activities using oids. Bug : CRNT-35275
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
      FilterTerm filter = query.getFilter();
      System.out.println(!CollectionUtils.isEmpty(oids));
      if (!CollectionUtils.isEmpty(oids))
      {
         FilterTerm orTerm = filter.addOrTerm();
         for (Long oid : oids)
         {
<<<<<<< Upstream, based on origin/feature/ipp/portal-html5-contrib
            FilterTerm orTerm = filter.addOrTerm();

            for (String oid : oids)
            {
               orTerm.add(ActivityInstanceQuery.OID.isEqual(Long.valueOf(oid)));
            }
         }
         else
         {
            filter.add(ActivityInstanceQuery.OID.isNull());
=======
            orTerm.add(ActivityInstanceQuery.OID.isEqual(Long.valueOf(oid)));
>>>>>>> e927bc8 [CRNT-35275] : Removed redunant code for fetching activities using oids. Bug : CRNT-35275
         }
      }

      if (options != null)
      {
         ActivityTableUtils.addDescriptorPolicy(options, query);

         ActivityTableUtils.addSortCriteria(query, options);

         ActivityTableUtils.addFilterCriteria(query, options);

         SubsetPolicy subsetPolicy = new SubsetPolicy(options.pageSize, options.skip, true);
         query.setPolicy(subsetPolicy);
      }

      ActivityInstances activityInstances = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);

      return activityInstances;
   }

   /**
    * @param ai
    * @param context
    * @return
    */
   public String getAllDataMappingsAsJson(ActivityInstance ai, String context)
   {
      // TODO: Add process-portal dependency. Till then use reflection!
      try
      {
         Object manualActivityUi = Reflect.createInstance(
               "org.eclipse.stardust.ui.web.processportal.view.manual.ManualActivityUi", new Class< ? >[] {
                     ActivityInstance.class, ApplicationContext.class, QueryService.class}, new Object[] {
                     ai, ai.getActivity().getApplicationContext(context), serviceFactoryUtils.getQueryService()});

         Object manualActivityPath = ReflectionUtils.invokeGetterMethod(manualActivityUi, "manualActivityPath");
         Object json = ReflectionUtils.invokeMethod(manualActivityPath, "toJsonString");

         return json.toString();
      }
      catch (Exception e)
      {
         trace.error("Error in processing data mappings", e);
      }

      return "";
   }

   /**
    * @param oids
    * @param context
    * @return
    */
   public Map<String, TrivialManualActivityDTO> getTrivialManualActivitiesDetails(List<Long> oids, String context)
   {
      Map<String, TrivialManualActivityDTO> ret = new LinkedHashMap<String, TrivialManualActivityDTO>();

      Map<String, List<PathDTO>> cache = new LinkedHashMap<String, List<PathDTO>>();

      List<ActivityInstance> ais = getActivityInstances(oids);
      for (ActivityInstance ai : ais)
      {
         if (isTrivialManualActivity(ai))
         {
            String cacheKey = ai.getModelOID() + "_" + ai.getActivity().getId();
            if (!cache.containsKey(cacheKey))
            {
               // Get Data Mappings
               List<PathDTO> dataMappings = PathDTO.toList(getAllDataMappingsAsJson(ai, context));

               // Remove readonly mappings
               Iterator<PathDTO> it = dataMappings.iterator();
               while (it.hasNext())
               {
                  PathDTO pathDto = it.next();
                  if (pathDto.readonly || !pathDto.isPrimitive)
                  {
                     it.remove();
                  }
               }

               cache.put(cacheKey, dataMappings);
            }

            TrivialManualActivityDTO dto = new TrivialManualActivityDTO();
            dto.dataMappings = cache.get(cacheKey);

            // Get (IN_)OUT Data
            dto.inOutData = new LinkedHashMap<String, Serializable>();
            Map<String, Serializable> dataValues = getAllInDataValues(ai, context);
            for (Entry<String, Serializable> entry : dataValues.entrySet())
            {
               for (PathDTO pathDto : dto.dataMappings)
               {
                  if (entry.getKey().equals(pathDto.id))
                  {
                     dto.inOutData.put(entry.getKey(), entry.getValue());
                     break;
                  }
               }
            }

            ret.put(String.valueOf(ai.getOID()), dto);
         }
         else
         {
            trace.debug("Skipping Activity Instance.. Not trivial... OID: " + ai.getOID());
         }
      }

      return ret;
   }

   /**
    * @param ai
    * @param context
    * @return
    */
   public static boolean isTrivialManualActivity(ActivityInstance ai)
   {
      Boolean trivialManualActivity = (Boolean) ai.getActivity().getAttribute("stardust:model:trivialManualActivity");
      return (null != trivialManualActivity && trivialManualActivity == true);
   }

   /**
    * @param ai
    * @param context
    * @return
    */
   public Map<String, Serializable> getAllInDataValues(ActivityInstance ai, String context)
   {
      return serviceFactoryUtils.getWorkflowService().getInDataValues(ai.getOID(), context, null);
   }

   /**
    * @param oid
    * @param outData
    * @return
    */
   public ActivityInstance complete(long oid, String context, Map<String, Serializable> outData)
   {
      return serviceFactoryUtils.getWorkflowService().activateAndComplete(oid, context, outData);
   }

   /**
    * @param oid
    * @return
    */
   public List<Document> getProcessAttachments(long oid)
   {
      List<Document> processAttachments = CollectionUtils.newArrayList();

      ActivityInstance ai = getActivityInstance(oid);

      if (ai != null)
      {
         /*
          * processAttachments = processInstanceUtils.getProcessAttachments(ai
          * .getProcessInstanceOID());
          */
      }

      return processAttachments;
   }

   /**
    * @param oid
    * @param documentId
    * @return
    */
   public ActivityInstance completeRendezvous(long oid, String documentId)
   {
      ActivityInstance completedAi = null;

      ActivityInstance ai = getActivityInstance(oid);
      Document document = documentUtils.getDocument(documentId);

      if (ai != null && document != null)
      {
         ApplicationContext defaultContext = ai.getActivity()
               .getApplicationContext(PredefinedConstants.DEFAULT_CONTEXT);

         if (defaultContext != null)
         {
            // TODO: Code assumes that there is exactly one Document OUT data mapping
            @SuppressWarnings("unchecked")
            List<DataMapping> outDataMappings = defaultContext.getAllOutDataMappings();

            if (outDataMappings != null && outDataMappings.size() == 1)
            {
               DataMapping outDataMapping = outDataMappings.get(0);
               String dataMappingId = outDataMapping.getId();

               Map<String, Object> outData = CollectionUtils.newHashMap();
               outData.put(dataMappingId, (Object) document);

               completedAi = serviceFactoryUtils.getWorkflowService().activateAndComplete(oid,
                     PredefinedConstants.DEFAULT_CONTEXT, outData);
            }
         }
      }

      return completedAi;
   }

   /**
    * @author Yogesh.Manware note: copied from AbortActivityBean#private boolean
    *         abortActivities(AbortScope abortScope)
    * 
    * @param abortScope
    * @param activitiesToBeAborted
    * 
    * @return
    */
   public NotificationMap abortActivities(AbortScope abortScope, List<Long> activitiesToBeAborted)
   {
      NotificationMap notificationMap = new NotificationMap();

      if (CollectionUtils.isNotEmpty(activitiesToBeAborted))
      {
         WorkflowService workflowService = serviceFactoryUtils.getWorkflowService();
         
         List<ActivityInstance> activityInstaces = getActivityInstances(activitiesToBeAborted);
         for (ActivityInstance activityInstance : activityInstaces)
         {
            if (null != activityInstance)
            {
               if (!isDefaultCaseActivity(activityInstance))
               {
                  try
                  {
                     workflowService.abortActivityInstance(activityInstance.getOID(), abortScope);

                     // publish event
                     ClientContextBean.getCurrentInstance().getClientContext()
                           .sendActivityEvent(ActivityEvent.aborted(activityInstance));

                     notificationMap.addSuccess(new NotificationDTO(activityInstance.getOID(),
                           getActivityLabel(activityInstance), getActivityStateLabel(activityInstance)));
                  }
                  catch (Exception e)
                  {
                     // It is very to rare that any exception would occur
                     // here
                     trace.error(e);
                     notificationMap.addFailure(new NotificationDTO(activityInstance.getOID(),
                           getActivityLabel(activityInstance), MessagesViewsCommonBean.getInstance().getParamString(
                                 "views.common.activity.abortActivity.failureMsg2",
                                 ExceptionHandler.getExceptionMessage(e))));
                  }
               }
               else
               {
                  if (isDefaultCaseActivity(activityInstance))
                  {
                     notificationMap.addFailure(new NotificationDTO(activityInstance.getOID(),
                           getActivityLabel(activityInstance), MessagesViewsCommonBean.getInstance().getString(
                                 "views.switchProcessDialog.caseAbort.message")));
                  }
                  else if (ActivityInstanceState.Aborted.equals(activityInstance.getState())
                        || ActivityInstanceState.Completed.equals(activityInstance.getState()))
                  {
                     notificationMap.addFailure(new NotificationDTO(activityInstance.getOID(),
                           getActivityLabel(activityInstance), MessagesViewsCommonBean.getInstance().getParamString(
                                 "views.common.activity.abortActivity.failureMsg3",
                                 ActivityInstanceUtils.getActivityStateLabel(activityInstance))));
                  }
                  else
                  {
                     notificationMap.addFailure(new NotificationDTO(activityInstance.getOID(),
                           getActivityLabel(activityInstance), MessagesViewsCommonBean.getInstance().getString(
                                 "views.common.activity.abortActivity.failureMsg1")));
                  }
               }
            }
         }
      }

      return notificationMap;
   }

   /**
    * to check Activity of type is Default Case Activity
    * 
    * @param ai
    * @return
    */
   public static boolean isDefaultCaseActivity(ActivityInstance ai)
   {
      if (null != ai && PredefinedConstants.DEFAULT_CASE_ACTIVITY_ID.equals(ai.getActivity().getId()))
      {
         return true;
      }
      return false;
   }

   /**
    * @param ai
    * @param context
    * @param data
    * @return
    */
   public ActivityInstance suspendToUserWorklist(ActivityInstance ai, String context, Map<String, ? > data)
   {
      ActivityInstance suspendedAi = null;

      if (trace.isDebugEnabled())
      {
         trace.debug("Suspending Activity '" + ai.getActivity().getName() + "' to User Worklist, with out data = "
               + data);
      }

      if (isEmpty(context))
      {
         suspendedAi = serviceFactoryUtils.getWorkflowService().suspendToUser(ai.getOID());
      }
      else
      {
         suspendedAi = serviceFactoryUtils.getWorkflowService().suspendToUser(ai.getOID(), context, data);
      }

      sendActivityEvent(ai, ActivityEvent.suspended(suspendedAi));
      return suspendedAi;
   }

   /**
    * @param oldAi
    * @param activityEvent
    */
   public void sendActivityEvent(ActivityInstance oldAi, ActivityEvent activityEvent)
   {
      ParticipantWorklistCacheManager.getInstance().handleActivityEvent(oldAi, activityEvent);
      if (ProcessWorklistCacheManager.isInitialized())
      {
         ProcessWorklistCacheManager.getInstance().handleActivityEvent(oldAi, activityEvent);
      }
      SpecialWorklistCacheManager.getInstance().handleActivityEvent(oldAi, activityEvent);
      ClientContextBean.getCurrentInstance().getClientContext().sendActivityEvent(activityEvent);
   }

   /**
    * @param instance
    * @return localized activity name with OID appended
    */
   public String getActivityLabel(ActivityInstance instance)
   {
      if (null != instance)
      {
         return I18nUtils.getActivityName(instance.getActivity());
      }
      return "";
   }

   /**
    * @param ai
    * @return Localized activity state name
    */
   public static String getActivityStateLabel(ActivityInstance ai)
   {
      return MessagesViewsCommonBean.getInstance().getString(STATUS_PREFIX + ai.getState().getName().toLowerCase());
   }

   /**
    * @param activities
    * @return
    */
   public List<ActivityInstance> getActivityInstancesFor(Long[] activities)
   {
      List<ActivityInstance> activityInstances = new ArrayList<ActivityInstance>();

      if (activities == null)
      {
         return activityInstances;
      }
      for (Long activityInstanceOid : activities)
      {
         ActivityInstance ai = getActivityInstance(activityInstanceOid.longValue());
         if (ai != null)
         {
            activityInstances.add(ai);
         }
      }
      return activityInstances;
   }

   /**
    * gets the duration
    * 
    * @param ai
    * @return
    */
   public static String getDuration(ActivityInstance ai)
   {
      long timeInMillis = Calendar.getInstance().getTimeInMillis();
      if (ai.getState() == ActivityInstanceState.Completed || ai.getState() == ActivityInstanceState.Aborted)
      {
         timeInMillis = ai.getLastModificationTime().getTime();
      }
      return DateUtils.formatDurationInHumanReadableFormat(timeInMillis - ai.getStartTime().getTime());
   }

   /**
	 * 
	 */

   public static String getPerformedByName(ActivityInstance activityInstance)
   {
      UserInfo userInfo = activityInstance.getPerformedBy();
      if (null != userInfo)
      {
         return ParticipantUtils.getParticipantName(userInfo);
      }
      else
      {
         return activityInstance.getPerformedByName();
      }
   }

   /**
     * 
     */

   public NotificationMap activate(Long activityOID)
   {
      NotificationMap notification = new NotificationMap();
      ActivityInstance ai = org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils
            .getActivityInstance(activityOID);
      if (!isSupportsWeb(ai.getActivity()))
      {
         notification.addFailure(new NotificationDTO(activityOID, ai.getActivity().getName(), MessagesViewsCommonBean
               .getInstance().getString("views.common.notSupportedOnWeb")));
         return notification;
      }

      if (!isActivatable(ai))
      {
         notification.addFailure(new NotificationDTO(activityOID, ai.getActivity().getName(), MessagesViewsCommonBean
               .getInstance().getString("views.common.notActivatable")));
         return notification;
      }
      ai = serviceFactoryUtils.getWorkflowService().activate(activityOID);
      notification.addSuccess(new NotificationDTO(activityOID, ai.getActivity().getName(), null));
      return notification;
   }

   /**
    * 
    * @param ais
    */
   public void delegateToDefaultPerformer(List<ActivityInstance> ais)
   {
      if (null != ais)
      {
         try
         {
            WorkflowService workflowService = serviceFactoryUtils.getWorkflowService();
            for (ActivityInstance activityInstance : ais)
            {
               if (ActivityInstanceState.Application.equals(activityInstance.getState()))
               {
                  forceSuspend(activityInstance);
               }
               workflowService.delegateToDefaultPerformer(activityInstance.getOID());
            }
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }
      }
   }

   /**
    * Activity instance is in Application state, force suspend will be done
    * 
    * @param ai
    */
   public void forceSuspend(ActivityInstance ai)
   {
      AdministrationService adminService = serviceFactoryUtils.getAdministrationService();

      boolean forceSuspend = AuthorizationUtils.canForceSuspend();
      try
      {
         if (forceSuspend && adminService != null)
         {
            ai = adminService.forceSuspendToDefaultPerformer(ai.getOID());
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * @return
    * 
    */
   public List<CompletedActivitiesStatisticsDTO> getCompletedActivies()
   {

      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      Users users = getRelevantUsers();
      UserPerformanceStatistics userStatistics = getUserStatisticsForCompletedActivities();
      Iterator<UserItem> userIter = facade.getAllUsersAsUserItems(users).iterator();
      Collection participants = facade.getAllRolesExceptCasePerformer();
      UserItem userItem;
      List<ProcessDefinition> processes;

      ProcessDefinition process;
      PerformanceStatistics pStatistics;
      Contribution con = null;
      RoleItem roleItem;

      List<CompletedActivitiesStatisticsDTO> completedActivitiesList = new ArrayList<CompletedActivitiesStatisticsDTO>();

      processes = ProcessDefinitionUtils.getAllBusinessRelevantProcesses();
      while (userIter.hasNext())
      {
         userItem = (UserItem) userIter.next();
         CompletedActivitiesStatisticsDTO activityStatsDTO = new CompletedActivitiesStatisticsDTO();

         UserDTO userDTO = DTOBuilder.build(userItem.getUser(), UserDTO.class);
         userDTO.displayName = UserUtils.getUserDisplayLabel(userItem.getUser());
         activityStatsDTO.teamMember = userDTO;
         CompletedActivityPerformanceDTO performanceStatsDTO = null;

         Map<String, CompletedActivityPerformanceDTO> processStats = new HashMap<String, CompletedActivityPerformanceDTO>();
         if (processes != null)
         {
            for (int i = 0; i < processes.size(); i++)
            {
               process = (ProcessDefinition) processes.get(i);
               pStatistics = userStatistics != null ? userStatistics.getStatisticsForUserAndProcess(userItem.getUser()
                     .getOID(), process.getQualifiedId()) : null;

               int nAisCompletedToday = 0;
               int nAisCompletedWeek = 0;
               int nAisCompletedMonth = 0;

               if (pStatistics != null)
               {
                  for (Iterator<Participant> iter = participants.iterator(); iter.hasNext();)
                  {
                     roleItem = (RoleItem) iter.next();
                     con = pStatistics.findContribution(roleItem.getRole());
                     nAisCompletedToday += con.getOrCreatePerformanceInInterval(DateRange.TODAY).getnAisCompleted();
                     nAisCompletedWeek += con.getOrCreatePerformanceInInterval(DateRange.THIS_WEEK).getnAisCompleted();
                     nAisCompletedMonth += con.getOrCreatePerformanceInInterval(DateRange.THIS_MONTH)
                           .getnAisCompleted();
                  }
               }

               performanceStatsDTO = new CompletedActivityPerformanceDTO(nAisCompletedToday, nAisCompletedWeek,
                     nAisCompletedMonth);
               processStats.put(I18nUtils.getProcessName(process), performanceStatsDTO);
            }

            activityStatsDTO.statisticsByProcess = processStats;
         }

         completedActivitiesList.add(activityStatsDTO);
      }

      return completedActivitiesList;
   }

   /**
    * 
    */
   public List<ColumnDTO> getParticipantColumns()
   {
      Set<ModelParticipantInfo> participantList = geUsertRelevantModelParticipants();

      List<ColumnDTO> participantDTOList = new ArrayList<ColumnDTO>();

      for (ModelParticipantInfo modelParticipantInfo : participantList)
      {
         ColumnDTO column = new ColumnDTO(modelParticipantInfo.getQualifiedId(),
               ModelHelper.getParticipantName(modelParticipantInfo));
         participantDTOList.add(column);
      }
      return participantDTOList;
   }

   /**
    * 
    * @return
    */
   public List<PostponedActivitiesResultDTO> getPostponedActivities()
   {

      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();

      Set<ModelParticipantInfo> participantList = geUsertRelevantModelParticipants();

      User user = facade.getUser();

      long totalCount, exceededDurationCount;
      Set<Long> allActivityOids = CollectionUtils.newHashSet();
      Set<Long> exceededActivityOids = CollectionUtils.newHashSet();
      String avgDuration;
      PostponedActivitiesStatistics pStat = getUserStatsForPostponedActivities();
      Users users = getRelevantUsers();
      List<UserItem> userItems = facade.getAllUsersAsUserItems(users);

      List<PostponedActivitiesResultDTO> resultList = new ArrayList<PostponedActivitiesResultDTO>();

      for (UserItem userItem : userItems)
      {
         user = userItem.getUser();

         PostponedActivities pActivities = pStat != null
               ? pStat.getPostponedActivities(userItem.getUser().getOID())
               : null;

         Collection<PostponedActivities> list = pStat.getPostponedActivities();
         for (PostponedActivities postponedActivities : list)
         {
            if (userItem.getUser().getOID() == postponedActivities.userOid)
            {
               pActivities = postponedActivities;
            }
         }
         Map<String, PostponedActivitiesStatsDTO> statsByParticipant = new HashMap<String, PostponedActivitiesStatsDTO>();
         if (pActivities != null)
         {
            PostponedActivitiesCalculator calc = new PostponedActivitiesCalculator(pActivities);
            for (ModelParticipantInfo mp : participantList)
            {
               if (calc != null)
               {
                  if (calc.getTotalCount(mp) != null && calc.getExceededDurationCount(mp) != null)
                  {
                     totalCount = calc.getTotalCount(mp);
                     avgDuration = calc.getAvgDuration(mp);
                     exceededDurationCount = calc.getExceededDurationCount(mp);
                     allActivityOids = calc.getAllActivityOIDs(mp);
                     exceededActivityOids = calc.getExceededActivityOIDs(mp);
                     PostponedActivitiesStatsDTO statsDTO = new PostponedActivitiesStatsDTO(totalCount, avgDuration,
                           exceededDurationCount, allActivityOids, exceededActivityOids);
                     statsByParticipant.put( ModelHelper.getParticipantName(mp), statsDTO);
                  }
                  else
                  {
                     PostponedActivitiesStatsDTO statsDTO = new PostponedActivitiesStatsDTO(0, StringUtils.EMPTY, 0,
                           allActivityOids, exceededActivityOids);
                     statsByParticipant.put(ModelHelper.getParticipantName(mp), statsDTO);
                  }
               }
            }
         }
         else
         {
            for (ModelParticipantInfo mp : participantList)
            {
               totalCount = 0;
               avgDuration = "";
               exceededDurationCount = 0;
               allActivityOids = CollectionUtils.newHashSet();
               exceededActivityOids = CollectionUtils.newHashSet();
               PostponedActivitiesStatsDTO statsDTO = new PostponedActivitiesStatsDTO(totalCount, avgDuration,
                     exceededDurationCount, allActivityOids, exceededActivityOids);
               statsByParticipant.put(ModelHelper.getParticipantName(mp), statsDTO);
            }

         }
         PostponedActivitiesResultDTO resultDTO = new PostponedActivitiesResultDTO();
         UserDTO userDTO = DTOBuilder.build(user, UserDTO.class);
         userDTO.displayName = UserUtils.getUserDisplayLabel(user);
         resultDTO.teamMember = userDTO;
         resultDTO.statsByParticipant = statsByParticipant;
         resultList.add(resultDTO);
      }
      return resultList;
   }

   /**
    * 
    * @return
    */
   private PostponedActivitiesStatistics getUserStatsForPostponedActivities()
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      PostponedActivitiesStatisticsQuery query = PostponedActivitiesStatisticsQuery.forAllUsers();
      query.setPolicy(new CriticalExecutionTimePolicy(Constants.getCriticalDurationThreshold(
            ProcessInstancePriority.LOW, 1.0f), Constants.getCriticalDurationThreshold(ProcessInstancePriority.NORMAL,
            1.0f), Constants.getCriticalDurationThreshold(ProcessInstancePriority.HIGH, 1.0f)));

      if (query.getOrderCriteria().getCriteria().size() == 0)
      {
         query.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(UserQuery.ACCOUNT);
      }
      PostponedActivitiesStatistics pStat = (PostponedActivitiesStatistics) facade.getAllUsers(query);
      return pStat;
   }

   /**
    * 
    */
   private Set<ModelParticipantInfo> geUsertRelevantModelParticipants()
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      User user = facade.getUser();
      List<Grant> userGrants = user.getAllGrants();
      ModelParticipantInfo modelParticipantInfo;
      Participant participant;
      ModelParticipant modelParticipant;
      Department department;

      Set<ModelParticipantInfo> participantList = new HashSet<ModelParticipantInfo>();
      for (Grant grant : userGrants)
      {
         participant = facade.getParticipant(grant.getQualifiedId());
         if (participant instanceof ModelParticipant)
         {
            modelParticipant = (ModelParticipant) participant;
            department = grant.getDepartment();
            modelParticipantInfo = (department == null) ? modelParticipant : department
                  .getScopedParticipant(modelParticipant);
            participantList.add(modelParticipantInfo);
         }
      }
      return participantList;
   }

   /**
    * 
    * @return
    */
   private Users getRelevantUsers()
   {

      UserQuery query = WorkflowFacade.getWorkflowFacade().getTeamQuery(true);
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Core);
      userPolicy.setPreferenceModules(UserPreferencesEntries.M_ADMIN_PORTAL);
      query.setPolicy(userPolicy);

      if (query.getOrderCriteria().getCriteria().size() == 0)
      {
         query.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(UserQuery.ACCOUNT);
      }

      Users users = facade.getAllUsers((UserQuery) query);

      return users;
   }

   /**
    * 
    * @return
    */
   private UserPerformanceStatistics getUserStatisticsForCompletedActivities()
   {

      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();

      List<DateRange> dateRange = CollectionUtils.newArrayList();
      dateRange.add(DateRange.TODAY);
      dateRange.add(DateRange.THIS_WEEK);
      dateRange.add(DateRange.THIS_MONTH);

      UserPerformanceStatisticsQuery userPerformanceStatisticsQuery = UserPerformanceStatisticsQuery.forAllUsers();
      userPerformanceStatisticsQuery.setPolicy(new StatisticsDateRangePolicy(dateRange));

      UserPerformanceStatistics userStatistics = (UserPerformanceStatistics) facade
            .getAllUsers(userPerformanceStatisticsQuery);

      return userStatistics;
   }

   /**
    * 
    * @return
    */
   public List<PendingActivitiesStatisticsDTO> getPendingActivities()
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      OpenActivitiesStatisticsQuery query = OpenActivitiesStatisticsQuery.forAllProcesses();
      query.setPolicy(new CriticalExecutionTimePolicy(Constants.getCriticalDurationThreshold(
            ProcessInstancePriority.LOW, 1.0f), Constants.getCriticalDurationThreshold(ProcessInstancePriority.NORMAL,
            1.0f), Constants.getCriticalDurationThreshold(ProcessInstancePriority.HIGH, 1.0f)));
      OpenActivitiesStatistics openActivityStatistics = (OpenActivitiesStatistics) facade
            .getAllActivityInstances(query);

      Collection<ProcessDefinition> processDefinition = facade.getAllProcessDefinitions();

      OpenActivitiesCalculator openActivitiesCalculator = new OpenActivitiesCalculator(processDefinition,
            openActivityStatistics);

      List<PendingActivitiesStatisticsDTO> pendingActivitiesList = new ArrayList<PendingActivitiesStatisticsDTO>();
      Map<String, OpenActivitiesDynamicUserObjectDTO> pendingActDynamicMap = new HashMap<String, OpenActivitiesDynamicUserObjectDTO>();
      Map<String, OpenActivitiesDynamicUserObjectDTO> pendingCriticalActDynamicMap = new HashMap<String, OpenActivitiesDynamicUserObjectDTO>();
      List<RoleItem> participantList = facade.getAllRolesExceptCasePerformer();

      if (participantList != null)
      {
         RoleItem roleItem;
         ModelParticipantInfo roledetails;
         Map totalOpenActivities;
         Map criticalOpenActivities;
         for (int i = 0; i < participantList.size(); i++)
         {
            roleItem = participantList.get(i);
            roledetails = roleItem.getRole();

            totalOpenActivities = openActivitiesCalculator.getTotalOpenActivities(roledetails);
            criticalOpenActivities = openActivitiesCalculator.getCriticalOpenActivities(roledetails);

            Long openActivitiesToday = new Long(
                  ((Double) totalOpenActivities.get(OpenActivitiesCalculator.OPEN_ACTIVITIES_TODAY)).longValue());
            Long openActivitiesYesterday = new Long(
                  ((Double) totalOpenActivities.get(OpenActivitiesCalculator.OPEN_ACTIVITIES_YESTERDAY)).longValue());
            Double openActivitiesAvg = (Double) totalOpenActivities.get(OpenActivitiesCalculator.OPEN_ACTIVITIES_AVG);
            Long hibernatedActivitiesCount = (Long) totalOpenActivities
                  .get(OpenActivitiesCalculator.OPEN_ACTIVITY_HIBERNATED);
            Set<Long> openActivitiesOids = (Set<Long>) totalOpenActivities
                  .get(OpenActivitiesCalculator.OPEN_ACTIVITIES_TODAY_OIDS);
            Set<Long> openActivitiesYesterdayOids = (Set<Long>) totalOpenActivities
                  .get(OpenActivitiesCalculator.OPEN_ACTIVITIES_YESTERDAY_OIDS);
            Set<Long> openActivityHibernateOids = (Set<Long>) totalOpenActivities
                  .get(OpenActivitiesCalculator.OPEN_ACTIVITY_HIBERNATE_OIDS);

            OpenActivitiesDynamicUserObjectDTO dyna = new OpenActivitiesDynamicUserObjectDTO(openActivitiesToday,
                  openActivitiesYesterday, openActivitiesAvg, hibernatedActivitiesCount);
            dyna.setOpenActivitiesTodayOids(openActivitiesOids);
            dyna.setOpenActivitiesYesterdayOids(openActivitiesYesterdayOids);
            dyna.setOpenActivitiesHibernateOids(openActivityHibernateOids);
            pendingActDynamicMap.put(roleItem.getRoleName(), dyna);

            openActivitiesToday = new Long(
                  ((Double) criticalOpenActivities.get(OpenActivitiesCalculator.OPEN_ACTIVITIES_TODAY)).longValue());
            openActivitiesYesterday = new Long(
                  ((Double) criticalOpenActivities.get(OpenActivitiesCalculator.OPEN_ACTIVITIES_YESTERDAY)).longValue());
            openActivitiesAvg = (Double) criticalOpenActivities.get(OpenActivitiesCalculator.OPEN_ACTIVITIES_AVG);
            hibernatedActivitiesCount = (Long) criticalOpenActivities
                  .get(OpenActivitiesCalculator.OPEN_ACTIVITY_HIBERNATED);
            openActivitiesOids = (Set<Long>) criticalOpenActivities
                  .get(OpenActivitiesCalculator.OPEN_ACTIVITIES_TODAY_OIDS);
            openActivitiesYesterdayOids = (Set<Long>) criticalOpenActivities
                  .get(OpenActivitiesCalculator.OPEN_ACTIVITIES_YESTERDAY_OIDS);
            openActivityHibernateOids = (Set<Long>) criticalOpenActivities
                  .get(OpenActivitiesCalculator.OPEN_ACTIVITY_HIBERNATE_OIDS);

            dyna = new OpenActivitiesDynamicUserObjectDTO(openActivitiesToday, openActivitiesYesterday,
                  openActivitiesAvg, hibernatedActivitiesCount);
            dyna.setOpenActivitiesTodayOids(openActivitiesOids);
            dyna.setOpenActivitiesYesterdayOids(openActivitiesYesterdayOids);
            dyna.setOpenActivitiesHibernateOids(openActivityHibernateOids);
            pendingCriticalActDynamicMap.put(roleItem.getRoleName(), dyna);
         }
         pendingActivitiesList.add(new PendingActivitiesStatisticsDTO("Total Open Activities", pendingActDynamicMap));
         pendingActivitiesList.add(new PendingActivitiesStatisticsDTO("Critical Open Activities",
               pendingCriticalActDynamicMap));

      }
      return pendingActivitiesList;
   }

   /**
    * 
    * @return
    */
   public List<ColumnDTO> getAllRoles()
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      List<RoleItem> participantList = facade.getAllRolesExceptCasePerformer();
      List<ColumnDTO> roleList = new ArrayList<ColumnDTO>();
      for (RoleItem role : participantList)
      {
         roleList.add(new ColumnDTO(role.getRole().getId(), role.getRoleName()));
      }
      return roleList;
   }

}
