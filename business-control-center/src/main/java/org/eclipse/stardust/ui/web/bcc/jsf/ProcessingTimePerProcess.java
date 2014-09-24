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

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ProcessDefinitionDetails;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.core.query.statistics.api.DateRange;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics.Contribution;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics.ContributionInInterval;
import org.eclipse.stardust.ui.web.bcc.views.CustomColumnUtils;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;



public class ProcessingTimePerProcess
{
   protected final static Logger trace = LogManager.getLogger(ProcessingTimePerProcess.class);
   
   private ProcessDefinition processDefinition;
   
   private double waitingTimeToday;
   private double waitingTimeLastWeek;
   private double waitingTimeLastMonth;
   
   private double timeToday;
   private double timeLastWeek;
   private double timeLastMonth;
   
   private long totalPICountToday;
   private long totalPICountLastWeek;
   private long totalPICountLastMonth;
   
   private int thresholdStateToday;
   private int thresholdStateLastWeek;
   private int thresholdStateLastMonth;
   private Map<String, Object> customColumns;
   private Map<String, Object> customColumnPisCount;
   
   public final static int UNDEFINED_THRESHOLD_STATE = -1;
   public final static int EXCEEDED_THRESHOLD_STATE = 1;
   public final static int CRITICAL_THRESHOLD_STATE = 2;
   public final static int NORMAL_THRESHOLD_STATE = 3;

   public ProcessingTimePerProcess(ProcessDefinition pd, Map<String, Object> columnDefinitionMap)
   {
      this.processDefinition = pd;
      totalPICountToday = 0l;
      totalPICountLastWeek = 0l;
      totalPICountLastMonth = 0l;
      timeToday = 0f;
      timeLastWeek = 0f;
      timeLastMonth = 0f;
      waitingTimeToday = 0f;
      waitingTimeLastWeek = 0f;
      waitingTimeLastMonth = 0f;
      thresholdStateToday = NORMAL_THRESHOLD_STATE;
      thresholdStateLastWeek = NORMAL_THRESHOLD_STATE;
      thresholdStateLastMonth = NORMAL_THRESHOLD_STATE;
      if(!CollectionUtils.isEmpty(columnDefinitionMap))
      {
         customColumns = CollectionUtils.newHashMap();
         customColumnPisCount = CollectionUtils.newHashMap();
         for(Entry<String, Object> colDef:columnDefinitionMap.entrySet())
         {
            String key = colDef.getKey();
            
            customColumns.put(key+CustomColumnUtils.CUSTOM_COL_TIME_SUFFIX, 0.0d);
            customColumns.put(key+CustomColumnUtils.CUSTOM_COL_WAIT_TIME_SUFFIX, 0.0d);
            customColumns.put(key+CustomColumnUtils.CUSTOM_COL_STATUS_SUFFIX, NORMAL_THRESHOLD_STATE);
            // Keey PI count in a map , required for calculation
            customColumnPisCount.put(key+CustomColumnUtils.CUSTOM_COL_TIME_SUFFIX, 0l);
         }   
      }
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

   public String getAverageTimeToday()
   {
//      return totalPICountToday > 0 ? DateUtils
//              .formatDurationInHumanReadableFormat((long) timeToday
//                      / totalPICountToday) : "-";
       return DateUtils.formatDurationInHumanReadableFormat((long) timeToday);
    }
   
   public String getAverageTimeLastWeek()
   {
//        return totalPICountLastWeek > 0 ? DateUtils
//				.formatDurationInHumanReadableFormat((long) timeLastWeek
//						/ totalPICountLastWeek) : "-";
	   return DateUtils.formatDurationInHumanReadableFormat((long) timeLastWeek);
   }

   public String getAverageWaitingTimeToday()
   {
	   return DateUtils.formatDurationInHumanReadableFormat((long) waitingTimeToday);
	}
   
   public String getAverageWaitingTimeLastMonth()
   {
        return DateUtils.formatDurationInHumanReadableFormat((long) waitingTimeLastMonth);
   }

   public String getAverageWaitingTimeLastWeek()
   {
       return DateUtils.formatDurationInHumanReadableFormat((long) waitingTimeLastWeek);
   }

   public void addContribution(Contribution con, Map<String, DateRange> customColDateRange)
   {
      if(con != null)
      {
         ContributionInInterval ciiToday = con.getOrCreateContributionInInterval(DateRange.TODAY);
         ContributionInInterval ciiLastWeek = con.getOrCreateContributionInInterval(DateRange.LAST_WEEK);
         ContributionInInterval ciiLastMonth = con.getOrCreateContributionInInterval(DateRange.LAST_MONTH);
         
         if(ciiToday.getnPis() > 0 && ciiToday.getTimeSpent().getTime() > 0)
         {
            totalPICountToday += ciiToday.getnPis();
            timeToday += ciiToday.getTimeSpent().getTime();
            if(ciiToday.getCriticalByProcessingTime().getRedInstancesCount() > 0)
            {
               thresholdStateToday = EXCEEDED_THRESHOLD_STATE;
            }
            else if(thresholdStateToday != EXCEEDED_THRESHOLD_STATE &&
                  ciiToday.getCriticalByProcessingTime().getYellowInstancesCount() > 0)
            {
               thresholdStateToday = CRITICAL_THRESHOLD_STATE;
            }
            if(ciiToday.getTimeWaiting().getTime() > 0)
            {
               waitingTimeToday +=ciiToday.getTimeWaiting().getTime();
            }
            else
            {
                  trace.error("Invalid wait time of today: " + ciiToday.getTimeWaiting().getTime() + 
                        " by process with id: " + processDefinition.getId());
            }
         }
         else
         {
            if(ciiToday.getnPis() > 0 && ciiToday.getTimeSpent().getTime() < 0)
            {
               trace.error("Invalid time of today: " + ciiToday.getTimeSpent().getTime() + 
                     " by process with id: " + processDefinition.getId());
            }
         }

         if(ciiLastWeek.getnPis() > 0 && ciiLastWeek.getTimeSpent().getTime() > 0)
         {
            totalPICountLastWeek += ciiLastWeek.getnPis();
            timeLastWeek += ciiLastWeek.getTimeSpent().getTime();
            if(ciiLastWeek.getCriticalByProcessingTime().getRedInstancesCount() > 0)
            {
               thresholdStateLastWeek = EXCEEDED_THRESHOLD_STATE;
            }
            else if(thresholdStateLastWeek != EXCEEDED_THRESHOLD_STATE &&
                  ciiLastWeek.getCriticalByProcessingTime().getYellowInstancesCount() > 0)
            {
               thresholdStateLastWeek = CRITICAL_THRESHOLD_STATE;
            }
            if(ciiLastWeek.getTimeWaiting().getTime() > 0)
            {
               waitingTimeLastWeek +=ciiLastWeek.getTimeWaiting().getTime();
            }
            else
            {
                  trace.error("Invalid wait time of last week: " + ciiLastWeek.getTimeWaiting().getTime() + 
                        " by process with id: " + processDefinition.getId());
            }
         }
         else
         {
            if(ciiLastWeek.getnPis() > 0 && ciiLastWeek.getTimeSpent().getTime() < 0)
            {
               trace.error("Invalid time of last week: " + ciiLastWeek.getTimeSpent().getTime() + 
                     " by process with id: " + processDefinition.getId());
            }
         }
         
         if(ciiLastMonth.getnPis() > 0 && ciiLastMonth.getTimeSpent().getTime() > 0)
         {
            totalPICountLastMonth += ciiLastMonth.getnPis();
            timeLastMonth += ciiLastMonth.getTimeSpent().getTime();
            if(ciiLastMonth.getCriticalByProcessingTime().getRedInstancesCount() > 0)
            {
               thresholdStateLastMonth = EXCEEDED_THRESHOLD_STATE;
            }
            else if(thresholdStateLastMonth != EXCEEDED_THRESHOLD_STATE &&
                  ciiLastMonth.getCriticalByProcessingTime().getYellowInstancesCount() > 0)
            {
               thresholdStateLastMonth = CRITICAL_THRESHOLD_STATE;
            }
            if(ciiLastMonth.getTimeWaiting().getTime() > 0)
            {
               waitingTimeLastMonth +=ciiLastMonth.getTimeWaiting().getTime();
            }
            else
            {
                  trace.error("Invalid wait time of last month: " + ciiLastMonth.getTimeWaiting().getTime() + 
                        " by process with id: " + processDefinition.getId());
            }
         }
         else
         {
            if(ciiLastMonth.getnPis() > 0 && ciiLastMonth.getTimeSpent().getTime() < 0)
            {
               trace.error("Invalid time of last month: " + ciiLastMonth.getTimeSpent().getTime() + 
                     " by process with id: " + processDefinition.getId());
            }
         }
         
         for (Map.Entry<String, DateRange> custCols : customColDateRange.entrySet())
         {
            String key = custCols.getKey();
            DateRange dateRange = custCols.getValue();
            ContributionInInterval ciiCustomCol = con.getOrCreateContributionInInterval(dateRange);

            if ((ciiCustomCol.getnPis() > 0) && ciiCustomCol.getTimeSpent().getTime() > 0)
            {
               Long nPisCount = (Long) customColumnPisCount.get(key + CustomColumnUtils.CUSTOM_COL_TIME_SUFFIX);
               double timeValue = Double.valueOf(customColumns.get(key + CustomColumnUtils.CUSTOM_COL_TIME_SUFFIX)
                     .toString());
               double waitTime = Double.valueOf(customColumns.get(key + CustomColumnUtils.CUSTOM_COL_WAIT_TIME_SUFFIX)
                     .toString());
               Integer thresholdValue = (Integer) customColumns.get(key + CustomColumnUtils.CUSTOM_COL_STATUS_SUFFIX);
               nPisCount += ciiCustomCol.getnPis();
               timeValue += ciiCustomCol.getTimeSpent().getTime();
               waitTime += ciiCustomCol.getTimeWaiting().getTime();
               if (ciiCustomCol.getCriticalByExecutionCost().getRedInstancesCount() > 0)
               {
                  thresholdValue = EXCEEDED_THRESHOLD_STATE;
               }
               else if (thresholdStateToday != EXCEEDED_THRESHOLD_STATE
                     && ciiToday.getCriticalByExecutionCost().getYellowInstancesCount() > 0)
               {
                  thresholdValue = CRITICAL_THRESHOLD_STATE;
               }
               
               customColumns.put(key + CustomColumnUtils.CUSTOM_COL_TIME_SUFFIX, timeValue);
               customColumns.put(key + CustomColumnUtils.CUSTOM_COL_WAIT_TIME_SUFFIX, waitTime);
               customColumns.put(key + CustomColumnUtils.CUSTOM_COL_STATUS_SUFFIX, thresholdValue);
               
               customColumnPisCount.put(key + CustomColumnUtils.CUSTOM_COL_TIME_SUFFIX, nPisCount);
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

   public Map<String, Object> getCustomColumns()
   {
      return customColumns;
   }

   public void setCustomColumns(Map<String, Object> customColumns)
   {
      this.customColumns = customColumns;
   }

   public Map<String, Object> getCustomColumnPisCount()
   {
      return customColumnPisCount;
   }

   public void setCustomColumnPisCount(Map<String, Object> customColumnPisCount)
   {
      this.customColumnPisCount = customColumnPisCount;
   }
}