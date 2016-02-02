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

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.rest.common.Options;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityTableUtils;
import org.eclipse.stardust.ui.web.rest.component.util.WorklistUtils;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityTableUtils.MODE;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.TrivialManualActivityDTO;
import org.springframework.stereotype.Component;

/**
 * @author Subodh.Godbole
 * @version $Revision: $
 */
@Component
public class WorklistService
{
   @Resource
   private WorklistUtils worklistUtils;

   @Resource
   private ActivityInstanceUtils activityInstanceUtils;

   /**
    * @param participantQId
    * @param string
    * @return
    */
   public QueryResultDTO getWorklistForParticipant(String participantQId, String userId, Options options)
   {
      QueryResult< ? > queryResult = worklistUtils.getWorklistForParticipant(participantQId, userId, options);
      return getTableResult(queryResult, options.fetchTrivialManualActivities);
   }

   /**
    * @param userId
    * @return
    */
   public QueryResultDTO getWorklistForUser(String userId, Options options, boolean fetchAllStates)
   {
      QueryResult< ? > queryResult = worklistUtils.getWorklistForUser(userId, options, fetchAllStates);
      return getTableResult(queryResult, options.fetchTrivialManualActivities);
   }

   /**
    * @param userId
    * @return
    */
   public QueryResultDTO getUnifiedWorklistForUser(String userId, String context, Options options)
   {
      QueryResult< ? > queryResult = worklistUtils.getUnifiedWorklistForUser(userId, options);
      return getTableResult(queryResult, options.fetchTrivialManualActivities);
   }

   /**
    * 
    * @param criticalityValue
    * @param context
    * @param options
    * @return
    */
   public QueryResultDTO getWorklistForHighCriticality(Options options)
   {
      QueryResult< ? > queryResult = worklistUtils.getWorklistForHighCriticality(options);
      return getTableResult(queryResult, options.fetchTrivialManualActivities);
   }

   /**
    * 
    * @param userId
    * @param options
    * @return
    */
   public QueryResultDTO getAllAssignedWorkItems(Options options)
   {
      QueryResult< ? > queryResult = worklistUtils.getAllAssignedWorkItems(options);
      return getTableResult(queryResult, options.fetchTrivialManualActivities);
   }

   /**
    * 
    * @param userId
    * @param options
    * @return
    */
   public QueryResultDTO getWorklistItemsFromDate(String dateId, Options options)
   {
      QueryResult< ? > queryResult = worklistUtils.getWorklistItemsFromDate(dateId, options);
      return getTableResult(queryResult, options.fetchTrivialManualActivities);
   }

   /**
    * 
    * @param processQId
    * @param options
    * @return
    */
   public QueryResultDTO getWorklistByProcess(String processQId, Options options)
   {
      QueryResult< ? > queryResult = worklistUtils.getWorklistByProcess(processQId, options);
      return getTableResult(queryResult, options.fetchTrivialManualActivities);
   }

   /***
    * 
    * @param options
    * @return
    */
   public QueryResultDTO getWorklistForResubmissionActivities(Options options)
   {
      QueryResult< ? > queryResult = worklistUtils.getWorklistForResubmissionActivities(options);
      QueryResultDTO resultDTO = null;
      if (options.fetchTrivialManualActivities)
      {
         Map<String, TrivialManualActivityDTO> trivialManualActivities = activityInstanceUtils
               .getTrivialManualActivities((List<ActivityInstance>) queryResult, "default");
         if(CollectionUtils.isNotEmpty(options.extraColumns))
         {
            resultDTO = ActivityTableUtils.buildTableResult(queryResult, MODE.WORKLIST, trivialManualActivities, options.extraColumns);
         }
         else
         {
            resultDTO = ActivityTableUtils.buildTableResult(queryResult, MODE.WORKLIST, trivialManualActivities);   
         }
      }
      else
      {
         if(CollectionUtils.isNotEmpty(options.extraColumns))
         {
            resultDTO = ActivityTableUtils.buildTableResult(queryResult, MODE.WORKLIST, null, options.extraColumns);
         }
         else
         {
            resultDTO = ActivityTableUtils.buildTableResult(queryResult, MODE.WORKLIST);   
         }
      }
      return resultDTO;
   }

   /***
    * 
    * @param options
    * @return
    */
   public QueryResultDTO getWorklistForLoggedInUser(Options options)
   {
      QueryResult< ? > queryResult = worklistUtils.getWorklistForLoggedInUser(options);
      return getTableResult(queryResult, options.fetchTrivialManualActivities);
   }

   /***
    * 
    * @param options
    * @return
    */
   public QueryResultDTO getAllActivable(Options options)
   {
      QueryResult< ? > queryResult = worklistUtils.getAllActivable(options);
      return getTableResult(queryResult, options.fetchTrivialManualActivities);
   }

   /***
    * 
    * @param options
    * @return
    */
   public QueryResultDTO getWorklistForProcessInstances(Options options, List<String> pInstanceOids)
   {
      QueryResult< ? > queryResult = worklistUtils.getWorklistForProcessInstances(options, pInstanceOids);
      return ActivityTableUtils.buildTableResult(queryResult, MODE.WORKLIST);
   }

   /**
    * 
    */
   private QueryResultDTO getTableResult(QueryResult< ? > queryResult, boolean fetchTrivialManualActivities)
   {
      QueryResultDTO resultDTO = null;
      if (fetchTrivialManualActivities)
      {
         Map<String, TrivialManualActivityDTO> trivialManualActivities = activityInstanceUtils
               .getTrivialManualActivities((List<ActivityInstance>) queryResult, "default");
         resultDTO = ActivityTableUtils.buildTableResult(queryResult, MODE.WORKLIST, trivialManualActivities);
      }
      else
      {
         resultDTO = ActivityTableUtils.buildTableResult(queryResult, MODE.WORKLIST);
      }
      return resultDTO;
   }

}
