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

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.EvaluateByWorkitemsPolicy;
import org.eclipse.stardust.engine.api.query.ExcludeUserPolicy;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.PerformedByUserFilter;
import org.eclipse.stardust.engine.api.query.PerformingParticipantFilter;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.UserService;
import org.eclipse.stardust.ui.web.rest.service.dto.UserDTO;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantWorklistCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessWorklistCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.ResubmissionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ResubmissionUtils.ModelResubmissionActivity;
import org.eclipse.stardust.ui.web.viewscommon.utils.SpecialWorklistCacheManager;
import org.springframework.stereotype.Component;

/**
 * @author Subodh.Godbole
 * @author Johnson.Quadras
 * @version $Revision: $
 */
@Component
public class WorklistUtils
{
   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private ProcessDefinitionUtils processDefUtils;

   @Resource
   private UserService userService;

   /**
    * @param participantQId
    * @return
    */
   public QueryResult< ? > getWorklistForParticipant(String participantQId, String userId, Options options)
   {
      //If the userId is not passed consider the user to be the logged in user.
      //User id is required to differentiate between the particpants when the deputy logs in 
      
      if (StringUtils.isEmpty(userId))
      {
         userId = userService.getLoggedInUser().id;
      }
      ParticipantInfo participantInfo = ParticipantWorklistCacheManager.getInstance().getParticipantInfoFromCache(
            participantQId);
      WorklistQuery query = (WorklistQuery) ParticipantWorklistCacheManager.getInstance().getWorklistQuery(
            participantInfo, userId);

      ActivityTableUtils.addCriterias(query, options);
      Worklist worklist = serviceFactoryUtils.getWorkflowService().getWorklist((WorklistQuery) query);
      QueryResult< ? > queryResult = extractParticipantWorklist(worklist, participantInfo);

      if (options.filter == null)
      {
         updateParticipantManagerCache(participantInfo, userId, queryResult);
      }

      return queryResult;

   }

   /**
    * @param userId
    * @return
    */
   public QueryResult< ? > getWorklistForUser(String userId, Options options, boolean fetchAllStates)
   {
      User user = serviceFactoryUtils.getUserService().getUser(userId);

      if (null != user)
      {
         ActivityInstanceQuery query = null;
         if (fetchAllStates)
         {
            query = ActivityInstanceQuery.findAll();
         }
         else
         {
            query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
                  ActivityInstanceState.Application, ActivityInstanceState.Suspended});
         }

         FilterOrTerm or = query.getFilter().addOrTerm();
         or.add(ActivityInstanceQuery.CURRENT_USER_PERFORMER_OID.isEqual(user.getOID()));

         ActivityTableUtils.addCriterias(query, options);

         ActivityInstances activityInstances = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);

         ParticipantInfo participantInfo = ParticipantWorklistCacheManager.getInstance().getParticipantInfoFromCache(
               user.getQualifiedId());

         if (options.filter == null)
         {
            if (fetchAllStates)
            {

               updateActivitiyQueryCache(options.worklistId, activityInstances);
            }
            else
            {

               updateParticipantManagerCache(participantInfo, userId, activityInstances);
            }

         }

         return activityInstances;
      }
      else
      {
         throw new ObjectNotFoundException("UserId not found");
      }
   }

   /**
    * @param userId
    * @return
    */
   public QueryResult< ? > getUnifiedWorklistForUser(String userId, Options options)
   {
      User user = serviceFactoryUtils.getUserService().getUser(userId);

      if (null != user)
      {
         ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
               ActivityInstanceState.Application, ActivityInstanceState.Suspended});

         FilterOrTerm or = query.getFilter().addOrTerm();
         or.add(new PerformingUserFilter(user.getOID()));
         Set<ParticipantInfo> participants = ParticipantWorklistCacheManager.getInstance().getWorklistParticipants()
               .get(user.getQualifiedId());
         for (ParticipantInfo participantInfo : participants)
         {
            if (!(participantInfo instanceof UserInfo))
            {
               or.add(PerformingParticipantFilter.forParticipant(participantInfo));
            }
         }
         ActivityTableUtils.addCriterias(query, options);

         ActivityInstances activityInstances = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);

         return activityInstances;
      }
      else
      {
         throw new ObjectNotFoundException("UserId not found");
      }
   }

   /**
    * 
    * @param criticalityValue
    * @param options
    * @return
    */
   public QueryResult< ? > getWorklistForHighCriticality(Options options)
   {
      ActivityInstanceQuery criticalActivitiesQuery = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Application, ActivityInstanceState.Suspended});

      FilterOrTerm or = criticalActivitiesQuery.getFilter().addOrTerm();
      or.add(PerformingParticipantFilter.ANY_FOR_USER).add(PerformingUserFilter.CURRENT_USER);
      criticalActivitiesQuery.setPolicy(ExcludeUserPolicy.EXCLUDE_USER);
      criticalActivitiesQuery.setPolicy(EvaluateByWorkitemsPolicy.WORKITEMS);
      List<CriticalityCategory> criticalityConfigs = CriticalityUtils.getCriticalityConfiguration();
      CriticalityCategory highCriticality = CriticalityUtils.getCriticalityCategory(
            CriticalityConfigurationUtil.PORTAL_CRITICALITY_MAX, criticalityConfigs);
      criticalActivitiesQuery.where(ActivityInstanceQuery.CRITICALITY.between(highCriticality.getRangeFrom()
            / ActivityTableUtils.PORTAL_CRITICALITY_MUL_FACTOR, highCriticality.getRangeTo()
            / ActivityTableUtils.PORTAL_CRITICALITY_MUL_FACTOR));

      ActivityTableUtils.addCriterias(criticalActivitiesQuery, options);

      QueryResult< ? > result = serviceFactoryUtils.getQueryService().getAllActivityInstances(criticalActivitiesQuery);

      if (null == options.filter)
      {
         updateActivitiyQueryCache(options.worklistId, result);
      }
      return result;

   }

   /**
    * 
    * @param criticalityValue
    * @param options
    * @return
    */
   public QueryResult< ? > getAllAssignedWorkItems(Options options)
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Application, ActivityInstanceState.Suspended});
      FilterOrTerm or = query.getFilter().addOrTerm();
      or.add(PerformingParticipantFilter.ANY_FOR_USER).add(PerformingUserFilter.CURRENT_USER);
      query.setPolicy(ExcludeUserPolicy.EXCLUDE_USER);
      query.setPolicy(EvaluateByWorkitemsPolicy.WORKITEMS);

      ActivityTableUtils.addCriterias(query, options);

      QueryResult< ? > result = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);

      if (null == options.filter)
      {
         updateActivitiyQueryCache(options.worklistId, result);
      }
      return result;
   }

   /**
    * 
    * @param criticalityValue
    * @param options
    * @return
    */
   public QueryResult< ? > getWorklistItemsFromDate(String dateId, Options options)
   {
      Date fromDate = ActivityTableUtils.determineDate(dateId);
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Suspended, ActivityInstanceState.Completed, ActivityInstanceState.Created,
            ActivityInstanceState.Interrupted, ActivityInstanceState.Application});
      FilterTerm where = query.getFilter().addAndTerm();
      where.add(ActivityInstanceQuery.LAST_MODIFICATION_TIME.greaterOrEqual(fromDate.getTime()));
      where.addOrTerm().add(PerformingUserFilter.CURRENT_USER).add(PerformedByUserFilter.CURRENT_USER);

      ActivityTableUtils.addCriterias(query, options);

      return serviceFactoryUtils.getQueryService().getAllActivityInstances(query);
   }

   /**
    * 
    * @param criticalityValue
    * @param options
    * @return
    */
   public QueryResult< ? > getWorklistByProcess(String processQId, Options options)
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Application, ActivityInstanceState.Suspended});

      FilterOrTerm where = query.getFilter().addOrTerm();
      where.add(PerformingParticipantFilter.ANY_FOR_USER).add(PerformingUserFilter.CURRENT_USER);
      query.setPolicy(ExcludeUserPolicy.EXCLUDE_USER);
      query.setPolicy(EvaluateByWorkitemsPolicy.WORKITEMS);
      query.where(new ProcessDefinitionFilter(processQId, false));

      ActivityTableUtils.addCriterias(query, options);

      QueryResult< ? > result = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);

      if (null == options.filter)
      {
         updateActivitiyQueryCache(options.worklistId, result);
      }
      return result;
   }

   /**
    * 
    * @param options
    * @return
    */
   public QueryResult< ? > getWorklistForResubmissionActivities(Options options)
   {

      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(ActivityInstanceState.Hibernated);
   // new PerformingUserFilter(0) : For activities created in non-interactive context (such as activity threads started by daemons)
      query.getFilter().addOrTerm().or(PerformingUserFilter.CURRENT_USER).or(new PerformingUserFilter(0));
      List<ModelResubmissionActivity> resubmissionActivities = CollectionUtils.newList();
      ResubmissionUtils.fillListWithResubmissionActivities(resubmissionActivities);

      if (resubmissionActivities.isEmpty())
      {
         query.getFilter().add(ActivityInstanceQuery.ACTIVITY_OID.isNull());
      }
      else
      {
         FilterOrTerm or = query.getFilter().addOrTerm();
         for (Iterator<ModelResubmissionActivity> as = resubmissionActivities.iterator(); as.hasNext();)
         {
            ModelResubmissionActivity activity = as.next();
            or.add(ActivityFilter.forProcess(activity.getActivityId(), activity.getProcessId(),
                  activity.getModelOids(), false));
         }
      }

      ActivityTableUtils.addCriterias(query, options);

      QueryResult< ? > result = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);

      if (null == options.filter)
      {
         updateActivitiyQueryCache(options.worklistId, result);
      }

      return result;
   }

   /**
    * 
    * @param options
    * @return
    */
   public QueryResult< ? > getWorklistForLoggedInUser(Options options)
   {

      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Application, ActivityInstanceState.Suspended});
      query.getFilter().add(PerformingUserFilter.CURRENT_USER);

      ActivityTableUtils.addCriterias(query, options);

      QueryResult< ? > result = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);

      UserDTO loggedInUser = userService.getLoggedInUser();

      ParticipantInfo participantInfo = ParticipantWorklistCacheManager.getInstance().getParticipantInfoFromCache(
            loggedInUser.qualifiedId);

      if (null == options.filter)
      {
         updateParticipantManagerCache(participantInfo, loggedInUser.id, result);
      }

      return result;
   }

   /**
    * 
    * @param options
    * @return
    */
   public QueryResult< ? > getAllActivable(Options options)
   {

      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Hibernated, ActivityInstanceState.Application, ActivityInstanceState.Suspended});

      ActivityTableUtils.addCriterias(query, options);

      QueryResult< ? > result = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);

      if (null == options.filter)
      {
         updateActivitiyQueryCache(options.worklistId, result);
      }

      return result;
   }

   /**
    * 
    * @param options
    * @return
    */
   public QueryResult< ? > getWorklistForProcessInstances(Options options, List<String> pInstanceOids)
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findAlive();
      FilterOrTerm orTerm = query.getFilter().addOrTerm();
      for (String oid : pInstanceOids)
      {
         orTerm.add(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(Long.valueOf(oid)));
      }

      ActivityTableUtils.addCriterias(query, options);

      QueryResult< ? > result = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);

      return result;
   }

   /**
    * 
    * @param participantInfo
    * @param userId
    * @param queryResult
    */
   private <T> void updateParticipantManagerCache(ParticipantInfo participantInfo, String userId,
         QueryResult<T> queryResult)
   {
      ParticipantWorklistCacheManager.getInstance().setWorklistCount(participantInfo, userId,
            queryResult.getTotalCount());
      ParticipantWorklistCacheManager.getInstance().setWorklistThresholdCount(participantInfo, userId,
            queryResult.getTotalCountThreshold());
   }

   /**
    * 
    * @param worklistId
    * @param queryResult
    */
   private <T> void updateActivitiyQueryCache(String worklistId, QueryResult<T> queryResult)
   {
      if (SpecialWorklistCacheManager.isSpecialWorklist(worklistId))
      {
         SpecialWorklistCacheManager.getInstance().setWorklistCount(worklistId, queryResult.getTotalCount());
         SpecialWorklistCacheManager.getInstance().setWorklistThresholdCount(worklistId,
               queryResult.getTotalCountThreshold());
      }
      else
      {
         if (ProcessWorklistCacheManager.isInitialized())
         {
            ProcessWorklistCacheManager.getInstance().setWorklistCount(worklistId, queryResult.getTotalCount());
            ProcessWorklistCacheManager.getInstance().setWorklistThresholdCount(worklistId,
                  queryResult.getTotalCountThreshold());
         }
      }
   }

   /**
    * @param worklist
    * @param participantInfo
    * @return
    */
   @SuppressWarnings("unchecked")
   private Worklist extractParticipantWorklist(Worklist worklist, ParticipantInfo participantInfo)
   {
      Worklist extractedWorklist = null;

      switch (ParticipantUtils.getParticipantType(participantInfo))
      {
      case ORGANIZATION:
      case ROLE:
      case SCOPED_ORGANIZATION:
      case SCOPED_ROLE:
      case USERGROUP:
         Iterator<Worklist> worklistIter1 = worklist.getSubWorklists();
         Worklist subWorklist;
         while (worklistIter1.hasNext())
         {
            subWorklist = worklistIter1.next();
            if (ParticipantUtils.areEqual(participantInfo, subWorklist.getOwner()))
            {
               extractedWorklist = subWorklist;
               break;
            }
         }
         break;

      case USER:
         if (ParticipantUtils.areEqual(participantInfo, worklist.getOwner()))
         {
            extractedWorklist = worklist;
            break;
         }
         else
         {
            // User-Worklist(Deputy Of) is contained in Sub-worklist of
            // User worklist(Deputy)
            Iterator<Worklist> subWorklistIter = worklist.getSubWorklists();
            Worklist subWorklist1;
            while (subWorklistIter.hasNext())
            {
               subWorklist1 = subWorklistIter.next();
               if (ParticipantUtils.areEqual(participantInfo, subWorklist1.getOwner()))
               {
                  extractedWorklist = subWorklist1;
                  break;
               }
            }
         }
      }
      return extractedWorklist;
   }

}
