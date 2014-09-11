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
package org.eclipse.stardust.ui.web.bcc.jsf;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.engine.core.query.statistics.api.OpenActivitiesStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.OpenActivitiesStatistics.OpenActivities;
import org.eclipse.stardust.engine.core.query.statistics.api.OpenActivitiesStatistics.OpenActivitiesDetails;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;


/**
 * @author fuhrmann
 * @version $Revision: 16517 $
 */
public class OpenActivitiesCalculator
{
   public final static String OPEN_ACTIVITIES_TODAY = "openActivitiesToday";

   public final static String OPEN_ACTIVITIES_YESTERDAY = "openActivitiesYesterday";

   public final static String OPEN_ACTIVITIES_AVG = "openActivitiesAvg";
   
   public final static String OPEN_ACTIVITIES_TODAY_OIDS = "openActivitiesTodayOids";
   public final static String OPEN_ACTIVITIES_YESTERDAY_OIDS = "openActivitiesYesterdayOids";

   public final static String OPEN_ACTIVITY_HIBERNATED = "openActivityHibernated";
   public final static String OPEN_ACTIVITY_HIBERNATE_OIDS = "openActivityHibernateOids";

   private final Collection processDefinitions;

   private final OpenActivitiesStatistics openActivitiesStatistics;
   
   public OpenActivitiesCalculator(Collection processDefinitions,
         OpenActivitiesStatistics openActivitiesStatistics)
   {
      this.processDefinitions = processDefinitions;
      this.openActivitiesStatistics = openActivitiesStatistics;
   }
   
   /**
    * Returns all open activities of the participant from today, yesterday and the daily
    * average for the last month.
    * 
    * @param participantInfo
    * @return
    */
   public Map/* <String,Double> */getTotalOpenActivities(ParticipantInfo participantInfo)
   {
      Map totalOpenActivities = new HashMap();
      long pendingAis = 0;
      long pendingYesterdayAis = 0;
      double pendingAisAvg = 0;
      long hiberanteAis = 0;
      Set<Long> pendingAiOids = CollectionUtils.newHashSet();
      Set<Long> pendingYesterdayAiOids = CollectionUtils.newHashSet();
      Set<Long> hibernateAiOids = CollectionUtils.newHashSet();
      for (Iterator iterator = processDefinitions.iterator(); iterator.hasNext();)
      {
         ProcessDefinition pd = (ProcessDefinition) iterator.next();
         OpenActivities activities = openActivitiesStatistics.findOpenActivities(pd
               .getQualifiedId(), participantInfo);
         
         pendingAis += activities.lowPriority.pendingAis;
         pendingAis += activities.normalPriority.pendingAis;
         pendingAis += activities.highPriority.pendingAis;
         
         hiberanteAis += activities.lowPriority.hibernatedAis;
         hiberanteAis += activities.normalPriority.hibernatedAis;
         hiberanteAis += activities.highPriority.hibernatedAis;
         
         hibernateAiOids.addAll(activities.lowPriority.hibernatedAiInstances);
         hibernateAiOids.addAll(activities.normalPriority.hibernatedAiInstances);
         hibernateAiOids.addAll(activities.highPriority.hibernatedAiInstances);
         
         pendingAiOids.addAll(activities.lowPriority.pendingAiInstances);
         pendingAiOids.addAll(activities.normalPriority.pendingAiInstances);
         pendingAiOids.addAll(activities.highPriority.pendingAiInstances);
         
         int historyDays = openActivitiesStatistics.getNumberOfDaysHistory();

         long pendingAisHistory = 0;

         for (int i = 0; i < historyDays; i++)
         {
            pendingAisHistory += activities.lowPriority.pendingAisHistory[i];
            pendingAisHistory += activities.normalPriority.pendingAisHistory[i];
            pendingAisHistory += activities.highPriority.pendingAisHistory[i];
         }
         pendingAisAvg = pendingAisAvg + getAvg(pendingAisHistory, historyDays);

         pendingYesterdayAis += activities.lowPriority.pendingAisHistory[0];
         pendingYesterdayAis += activities.normalPriority.pendingAisHistory[0];
         pendingYesterdayAis += activities.highPriority.pendingAisHistory[0];
         
         pendingYesterdayAiOids.addAll(activities.lowPriority.getPendingAiInstancesHistory(0));
         pendingYesterdayAiOids.addAll(activities.normalPriority.getPendingAiInstancesHistory(0));
         pendingYesterdayAiOids.addAll(activities.highPriority.getPendingAiInstancesHistory(0));
         
      }
      totalOpenActivities.put(OPEN_ACTIVITIES_TODAY, new Double(pendingAis));
      totalOpenActivities.put(OPEN_ACTIVITIES_YESTERDAY, new Double(pendingYesterdayAis));

      pendingAisAvg = getAvg(pendingAisAvg, processDefinitions.size());
      totalOpenActivities.put(OPEN_ACTIVITIES_AVG, new Double(pendingAisAvg));
      
      totalOpenActivities.put(OPEN_ACTIVITY_HIBERNATED, hiberanteAis);
      totalOpenActivities.put(OPEN_ACTIVITIES_TODAY_OIDS, pendingAiOids);
      totalOpenActivities.put(OPEN_ACTIVITIES_YESTERDAY_OIDS, pendingYesterdayAiOids);
      totalOpenActivities.put(OPEN_ACTIVITY_HIBERNATE_OIDS, hibernateAiOids);
      
      return totalOpenActivities;
   }

   /**
    * Returns all critical open activities of the participant from today, yesterday and
    * the daily average for the last month.
    * 
    * @param participantInfo
    * @return
    */
   public Map /* <String, Double> */getCriticalOpenActivities(ParticipantInfo participantInfo)
   {
      Map criticalOpenActivities = new HashMap();
      long pendingAis = 0;
      long pendingYesterdayAis = 0;
      double pendingAisAvg = 0;
      long hiberanteAis = 0;
      Set<Long> pendingAiOids = CollectionUtils.newHashSet();
      Set<Long> pendingYesterdayAiOids = CollectionUtils.newHashSet();
      Set<Long> hibernateAiOids = CollectionUtils.newHashSet();
      
      for (Iterator iterator = processDefinitions.iterator(); iterator.hasNext();)
      {
         ProcessDefinition pd = (ProcessDefinition) iterator.next();
         OpenActivities activities = openActivitiesStatistics.findOpenActivities(pd
               .getQualifiedId(), participantInfo);
         
         pendingAis += activities.lowPriority.pendingCriticalAis;
         pendingAis += activities.normalPriority.pendingCriticalAis;
         pendingAis += activities.highPriority.pendingCriticalAis;
         
         pendingAiOids.addAll(activities.lowPriority.pendingCriticalAiInstances);
         pendingAiOids.addAll(activities.normalPriority.pendingCriticalAiInstances);
         pendingAiOids.addAll(activities.highPriority.pendingCriticalAiInstances);
         
         hiberanteAis += activities.lowPriority.hibernatedAis;
         hiberanteAis += activities.normalPriority.hibernatedAis;
         hiberanteAis += activities.highPriority.hibernatedAis;
         
         hibernateAiOids.addAll(activities.lowPriority.hibernatedAiInstances);
         hibernateAiOids.addAll(activities.normalPriority.hibernatedAiInstances);
         hibernateAiOids.addAll(activities.highPriority.hibernatedAiInstances);
         
         int historyDays = openActivitiesStatistics.getNumberOfDaysHistory();
         
         long pendingAisHistory = 0;

         for (int i = 0; i < historyDays; i++)
         {
            pendingAisHistory += activities.lowPriority.pendingCriticalAisHistory[i];
            pendingAisHistory += activities.normalPriority.pendingCriticalAisHistory[i];
            pendingAisHistory += activities.highPriority.pendingCriticalAisHistory[i];
         }

         pendingAisAvg = pendingAisAvg + getAvg(pendingAisHistory, historyDays);

         pendingYesterdayAis += activities.lowPriority.pendingCriticalAisHistory[0];
         pendingYesterdayAis += activities.normalPriority.pendingCriticalAisHistory[0];
         pendingYesterdayAis += activities.highPriority.pendingCriticalAisHistory[0];
         
         pendingYesterdayAiOids.addAll(activities.lowPriority.getPendingCriticalAiInstancesHistory(0));
         pendingYesterdayAiOids.addAll(activities.normalPriority.getPendingCriticalAiInstancesHistory(0));
         pendingYesterdayAiOids.addAll(activities.highPriority.getPendingCriticalAiInstancesHistory(0));
         
      }
      
      criticalOpenActivities.put(OPEN_ACTIVITIES_TODAY, new Double(pendingAis));
      criticalOpenActivities.put(OPEN_ACTIVITIES_YESTERDAY, new Double(
            pendingYesterdayAis));
      
      pendingAisAvg = getAvg(pendingAisAvg, processDefinitions.size());
      criticalOpenActivities.put(OPEN_ACTIVITIES_AVG, new Double(pendingAisAvg));
      
      criticalOpenActivities.put(OPEN_ACTIVITY_HIBERNATED, hiberanteAis);
      criticalOpenActivities.put(OPEN_ACTIVITIES_TODAY_OIDS, pendingAiOids);
      criticalOpenActivities.put(OPEN_ACTIVITIES_YESTERDAY_OIDS, pendingYesterdayAiOids);
      criticalOpenActivities.put(OPEN_ACTIVITY_HIBERNATE_OIDS, hibernateAiOids);
      
      return criticalOpenActivities;
   }

   private double getAvg(double sum, int value)
   {
      BigDecimal avg = new BigDecimal(0);
      if (sum != 0)
      {
         avg = new BigDecimal(sum).divide(new BigDecimal(value), 2,
               BigDecimal.ROUND_HALF_EVEN);
      }
      return avg.doubleValue();
   }

}
