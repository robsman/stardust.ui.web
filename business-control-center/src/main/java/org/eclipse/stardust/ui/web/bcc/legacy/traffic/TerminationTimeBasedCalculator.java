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
package org.eclipse.stardust.ui.web.bcc.legacy.traffic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;


/**
 * Prototypical implementation to the ActivityTrafficLightCalculator.
 * 
 * @author mueller1
 * 
 */
public class TerminationTimeBasedCalculator implements ActivityTrafficLightCalculator
{

   private static Logger logger = LogManager
         .getLogger(TerminationTimeBasedCalculator.class);

   private Map/* <String, Integer> */originalStatus = null;

   private Map/* <String, String> */expectedTerminationTime = null;

   public TerminationTimeBasedCalculator()
   {
      this.originalStatus = new HashMap/* <String, Integer> */();
      this.expectedTerminationTime = new HashMap/* <String, String> */();

      this.expectedTerminationTime = TrafficLightViewPropertyProvider.getInstance()
            .getAllProcessingThresholds();
   }

   public int getColorStateForActivity(String processDefintionId, String activityId,
         String categoryId, String categroyValue, int numberOfNotCompletedActivities,
         int numberOfCompletedActivities)
   {

      logger.info("Calculate color coded status for cell : " + processDefintionId + ", "
            + activityId + ", " + categoryId + ", " + categroyValue);
 
      boolean instancesExists = (numberOfNotCompletedActivities + numberOfCompletedActivities) > 0;
      
      String expectedTerminationTime = (String) this.expectedTerminationTime
            .get(processDefintionId + "." + activityId);

      int hour = 0;
      int minutes = 0;

      if (expectedTerminationTime != null)
      {
         hour = new Integer(expectedTerminationTime.substring(0, 2)).intValue();
         minutes = new Integer(expectedTerminationTime.substring(2)).intValue();
      }

      Calendar deadline = Calendar.getInstance(PortalApplication.getInstance().getTimeZone());
      deadline.set(Calendar.HOUR_OF_DAY, hour);
      deadline.set(Calendar.MINUTE, minutes);

      int originalStatus = loadOriginalStatus(processDefintionId, activityId, categoryId,
            categroyValue);

      if (originalStatus == -1)
      {
         numberOfNotCompletedActivities = getOriginalNumberOfNotCompleteActivities(
               processDefintionId, activityId, categoryId, categroyValue);

         originalStatus = checkCurrentStatus(processDefintionId, activityId, categoryId,
               categroyValue, deadline.getTimeInMillis(), instancesExists);
         this.saveOriginalStatus(processDefintionId, activityId, categoryId,
               categroyValue, originalStatus);
      }

      return originalStatus;
   }

   private Calendar getStartDate()
   {
      Calendar startTime = Calendar.getInstance(PortalApplication.getInstance().getTimeZone());
      startTime.set(Calendar.HOUR_OF_DAY, 0);
      startTime.set(Calendar.MINUTE, 0);
      startTime.set(Calendar.SECOND, 0);
      return startTime;
   }

   private int getOriginalNumberOfNotCompleteActivities(String processDefintionId,
         String activityId, String categoryId, String categoryValue)
   {
      Calendar startTime = getStartDate();
      ActivityInstanceQuery query = ActivityInstanceQuery.findAlive();
      query.getFilter().add(ActivityFilter.forProcess(activityId, processDefintionId));
      FilterOrTerm orTerm = query.getFilter().addOrTerm();
      orTerm.add(ActivityInstanceQuery.START_TIME
            .greaterThan(startTime.getTimeInMillis()));
      orTerm.add(ActivityInstanceQuery.LAST_MODIFICATION_TIME.greaterOrEqual(startTime
            .getTimeInMillis()));

      SessionContext sessionCtx = SessionContext.findSessionContext();
      ServiceFactory sFactory = sessionCtx.getServiceFactory();
      ActivityInstances aInstances = sFactory.getQueryService().getAllActivityInstances(
            query);

      int numOfNotCompletedActivities = 0;
      if (categoryId != null)
      {
         for (int i = 0; i < aInstances.size(); i++)
         {
            ActivityInstance aInstance = (ActivityInstance) aInstances.get(i);
            String value = null;
            if(aInstance.getDescriptorValue(categoryId) != null)
            	value = ((Object) aInstance.getDescriptorValue(categoryId)).toString();
            if ((value != null && categoryValue.equals(value))
                  || "total".equals(categoryValue)
                  || (value == null && categoryId != null && "undefined"
                        .equals(categoryValue)))
            {
               numOfNotCompletedActivities++;
            }
         }
      }

      return numOfNotCompletedActivities;
   }

   private void saveOriginalStatus(String processDefintionId, String activityId,
         String categoryId, String categroyValue, int status)
   {
      StringBuffer cacheKey = new StringBuffer();
      cacheKey.append(processDefintionId).append("_");
      cacheKey.append(activityId).append("_");
      cacheKey.append(categoryId).append("_");
      cacheKey.append(categroyValue);

      if (!this.originalStatus.containsKey(cacheKey.toString()))
      {
         this.originalStatus.put(cacheKey.toString(), new Integer(status));
      }
   }

   private int loadOriginalStatus(String processDefintionId, String activityId,
         String categoryId, String categroyValue)
   {
      StringBuffer cacheKey = new StringBuffer();
      cacheKey.append(processDefintionId).append("_");
      cacheKey.append(activityId).append("_");
      cacheKey.append(categoryId).append("_");
      cacheKey.append(categroyValue);

      Integer status = (Integer) this.originalStatus.get(cacheKey.toString());
      return status != null ? status.intValue() : -1;
   }

   private int checkCurrentStatus(String processDefintionId, String activityId,
         String categoryId, String categoryValue, long deadline, boolean instancesExists)
   {
      ActivityInstances aInstances = findAllActivityInstances(processDefintionId,
            activityId);
      List<ActivityInstance> completedAIs = new ArrayList<ActivityInstance>();
      List<ActivityInstance> notCompletedAIs = new ArrayList<ActivityInstance>();
      if (categoryId != null)
      {
         for (int i = 0; i < aInstances.size(); i++)
         {
            ActivityInstance aInstance = (ActivityInstance) aInstances.get(i);
            String value = null;
            if(aInstance.getDescriptorValue(categoryId) != null)
            	value = ((Object) aInstance.getDescriptorValue(categoryId)).toString();            	
            
            if ((value != null && categoryValue.equals(value))
                  || "total".equals(categoryValue)
                  || (value == null && categoryId != null && "undefined"
                        .equals(categoryValue)))
            {
               if (ActivityInstanceState.Completed.equals(aInstance.getState()))
               {
                  completedAIs.add(aInstance);
               }
               else
               {
                  notCompletedAIs.add(aInstance);
               }
            }
         }
      }
      if (!instancesExists)
      {
         return NEUTRAL;
      }
      if (notCompletedAIs.isEmpty() && !completedAIs.isEmpty())
      {
         for (ActivityInstance activity : completedAIs)
         {
            if (deadline < activity.getLastModificationTime().getTime())
            {
               return CRITICAL;
            }
         }
         return NORMAL;
      }
      if (deadline > System.currentTimeMillis())
      {
         return WARN;
      }
      return CRITICAL;
   }

   private ActivityInstances findAllActivityInstances(String processDefintionId,
         String activityId)
   {
      Calendar startTime = getStartDate();
      ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
      query.getFilter().add(ActivityFilter.forProcess(activityId, processDefintionId));
      FilterOrTerm orTerm = query.getFilter().addOrTerm();
      orTerm.add(ActivityInstanceQuery.START_TIME
            .greaterThan(startTime.getTimeInMillis()));
      orTerm.add(ActivityInstanceQuery.LAST_MODIFICATION_TIME.greaterOrEqual(startTime
            .getTimeInMillis()));

      SessionContext sessionCtx = SessionContext.findSessionContext();
      ServiceFactory sFactory = sessionCtx.getServiceFactory();
      ActivityInstances aInstances = sFactory.getQueryService().getAllActivityInstances(
            query);
      return aInstances;
   }

}
