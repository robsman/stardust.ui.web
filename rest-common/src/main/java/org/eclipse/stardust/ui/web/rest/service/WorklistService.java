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

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityTableUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityTableUtils.MODE;
import org.eclipse.stardust.ui.web.rest.service.utils.WorklistUtils;
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
    * @return
    */
   public QueryResultDTO getWorklistForParticipant(String participantQId, String context, Options options)
   {
      QueryResult< ? > queryResult = worklistUtils.getWorklistForParticipant(participantQId, options);
      return ActivityTableUtils.buildTableResult(queryResult, MODE.WORKLIST);
   }

   /**
    * @param userId
    * @return
    */
   public QueryResultDTO getWorklistForUser(String userId, String context, Options options)
   {
      QueryResult< ? > queryResult = worklistUtils.getWorklistForUser(userId, options);
      return ActivityTableUtils.buildTableResult(queryResult, MODE.WORKLIST);
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
      return ActivityTableUtils.buildTableResult(queryResult, MODE.WORKLIST);
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
      return ActivityTableUtils.buildTableResult(queryResult, MODE.WORKLIST);
   }

   /**
    * 
    * @param userId
    * @param options
    * @return
    */
   public QueryResultDTO getItemtWorkingFromDate(String dateId, Options options)
   {
      QueryResult< ? > queryResult = worklistUtils.getItemtWorkingFromDate(dateId, options);
      return ActivityTableUtils.buildTableResult(queryResult, MODE.WORKLIST);
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
      return ActivityTableUtils.buildTableResult(queryResult, MODE.WORKLIST);
   }

   /***
    * 
    * @param options
    * @return
    */
   public QueryResultDTO getWorklistForResubmissionActivities(Options options)
   {
      QueryResult< ? > queryResult = worklistUtils.getWorklistForResubmissionActivities(options);
      return ActivityTableUtils.buildTableResult(queryResult, MODE.WORKLIST);
   }

   /***
    * 
    * @param options
    * @return
    */
   public QueryResultDTO getWorklistForLoggedInUser(Options options)
   {
      QueryResult< ? > queryResult = worklistUtils.getWorklistForLoggedInUser(options);
      return ActivityTableUtils.buildTableResult(queryResult, MODE.WORKLIST);
   }

   /***
    * 
    * @param options
    * @return
    */
   public QueryResultDTO getAllActivable(Options options)
   {
      QueryResult< ? > queryResult = worklistUtils.getAllActivable(options);
      return ActivityTableUtils.buildTableResult(queryResult, MODE.WORKLIST);
   }
}
