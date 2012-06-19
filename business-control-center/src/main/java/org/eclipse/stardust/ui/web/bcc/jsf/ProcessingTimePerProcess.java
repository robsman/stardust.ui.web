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

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ProcessDefinitionDetails;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics.Contribution;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;



public class ProcessingTimePerProcess
{
   protected final static Logger trace = LogManager.getLogger(ProcessingTimePerProcess.class);
         
   private ProcessDefinition processDefinition;
   
   private double timeToday;
   private double timeLastWeek;
   private double timeLastMonth;
   
   private long totalPICountToday;
   private long totalPICountLastWeek;
   private long totalPICountLastMonth;
   
   private int thresholdStateToday;
   private int thresholdStateLastWeek;
   private int thresholdStateLastMonth;
   
   public final static int UNDEFINED_THRESHOLD_STATE = -1;
   public final static int EXCEEDED_THRESHOLD_STATE = 1;
   public final static int CRITICAL_THRESHOLD_STATE = 2;
   public final static int NORMAL_THRESHOLD_STATE = 3;

   public ProcessingTimePerProcess(ProcessDefinition pd)
   {
      this.processDefinition = pd;
      totalPICountToday = 0l;
      totalPICountLastWeek = 0l;
      totalPICountLastMonth = 0l;
      timeToday = 0f;
      timeLastWeek = 0f;
      timeLastMonth = 0f;
      thresholdStateToday = NORMAL_THRESHOLD_STATE;
      thresholdStateLastWeek = NORMAL_THRESHOLD_STATE;
      thresholdStateLastMonth = NORMAL_THRESHOLD_STATE;
   }
   
   public ProcessDefinition getProcessDefinition()
   {
      return processDefinition;
   }
   
   public String getProcessDefinitionDescription()
   {
      String defaultDesc = processDefinition instanceof ProcessDefinitionDetails
            ? ((ProcessDefinitionDetails) processDefinition).getDescription()
            : null;
      return I18nUtils.getProcessDescription(processDefinition, defaultDesc);
   }

   public String getAverageTimeLastMonth()
   {
//		return totalPICountLastMonth > 0 ? DateUtils
//				.formatDurationInHumanReadableFormat((long) timeLastMonth
//						/ totalPICountLastMonth) : "-";
		return DateUtils.formatDurationInHumanReadableFormat((long) timeLastMonth);
   }

   public String getAverageTimeLastWeek()
   {
//        return totalPICountLastWeek > 0 ? DateUtils
//				.formatDurationInHumanReadableFormat((long) timeLastWeek
//						/ totalPICountLastWeek) : "-";
	   return DateUtils.formatDurationInHumanReadableFormat((long) timeLastWeek);
   }

   public String getAverageTimeToday()
   {
//		return totalPICountToday > 0 ? DateUtils
//				.formatDurationInHumanReadableFormat((long) timeToday
//						/ totalPICountToday) : "-";
	   return DateUtils.formatDurationInHumanReadableFormat((long) timeToday);
	}
   
   public void addContribution(Contribution con)
   {
      if(con != null)
      {
         if(con.contributionToday.nPis > 0 && con.contributionToday.timeSpent.getTime() > 0)
         {
            totalPICountToday += con.contributionToday.nPis;
            timeToday += con.contributionToday.timeSpent.getTime();
            if(con.contributionToday.criticalByProcessingTime.getRedInstancesCount() > 0)
            {
               thresholdStateToday = EXCEEDED_THRESHOLD_STATE;
            }
            else if(thresholdStateToday != EXCEEDED_THRESHOLD_STATE &&
                  con.contributionToday.criticalByProcessingTime.getYellowInstancesCount() > 0)
            {
               thresholdStateToday = CRITICAL_THRESHOLD_STATE;
            }
         }
         else
         {
            if(con.contributionToday.nPis > 0 && con.contributionToday.timeSpent.getTime() < 0)
            {
               trace.error("Invalid time of today: " + con.contributionToday.timeSpent.getTime() + 
                     " by process with id: " + processDefinition.getId());
            }
         }

         if(con.contributionLastWeek.nPis > 0 && con.contributionLastWeek.timeSpent.getTime() > 0)
         {
            totalPICountLastWeek += con.contributionLastWeek.nPis;
            timeLastWeek += con.contributionLastWeek.timeSpent.getTime();
            if(con.contributionLastWeek.criticalByProcessingTime.getRedInstancesCount() > 0)
            {
               thresholdStateLastWeek = EXCEEDED_THRESHOLD_STATE;
            }
            else if(thresholdStateLastWeek != EXCEEDED_THRESHOLD_STATE &&
                  con.contributionLastWeek.criticalByProcessingTime.getYellowInstancesCount() > 0)
            {
               thresholdStateLastWeek = CRITICAL_THRESHOLD_STATE;
            }
         }
         else
         {
            if(con.contributionLastWeek.nPis > 0 && con.contributionLastWeek.timeSpent.getTime() < 0)
            {
               trace.error("Invalid time of last week: " + con.contributionLastWeek.timeSpent.getTime() + 
                     " by process with id: " + processDefinition.getId());
            }
         }
         
         if(con.contributionLastMonth.nPis > 0 && con.contributionLastMonth.timeSpent.getTime() > 0)
         {
            totalPICountLastMonth += con.contributionLastMonth.nPis;
            timeLastMonth += con.contributionLastMonth.timeSpent.getTime();
            if(con.contributionLastMonth.criticalByProcessingTime.getRedInstancesCount() > 0)
            {
               thresholdStateLastMonth = EXCEEDED_THRESHOLD_STATE;
            }
            else if(thresholdStateLastMonth != EXCEEDED_THRESHOLD_STATE &&
                  con.contributionLastMonth.criticalByProcessingTime.getYellowInstancesCount() > 0)
            {
               thresholdStateLastMonth = CRITICAL_THRESHOLD_STATE;
            }
         }
         else
         {
            if(con.contributionLastMonth.nPis > 0 && con.contributionLastMonth.timeSpent.getTime() < 0)
            {
               trace.error("Invalid time of last month: " + con.contributionLastMonth.timeSpent.getTime() + 
                     " by process with id: " + processDefinition.getId());
            }
         }
      }
   }
   
   public int getTodayState()
   {
      return thresholdStateToday;
   }
   
   public int getLastWeekState()
   {
      return thresholdStateLastWeek;
   }
   
   public int getLastMonthState()
   {
      return thresholdStateLastMonth;
   }
}