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
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;

public class CostsPerProcess
{
   protected final static Logger trace = LogManager.getLogger(CostsPerProcess.class);
   
   private final static String CUSTOM_COL_COST_SUFFIX = "Costs";
   private final static String CUSTOM_COL_STATUS_SUFFIX = "Status";
   
   private ProcessDefinition processDefinition;
   
   private double costsToday;
   private double costsLastWeek;
   private double costsLastMonth;
   
   private long totalCountPIToday;
   private long totalCountPILastWeek;
   private long totalCountPILastMonth;
   
   private int thresholdStateToday;
   private int thresholdStateLastWeek;
   private int thresholdStateLastMonth;
   
   private Map<String, Object> customColumns;
   // Map for keeping track of totalPICount to fetch average Count
   private Map<String, Object> customColumnPisCount;
   
   public final static int UNDEFINED_THRESHOLD_STATE = -1;
   public final static int EXCEEDED_THRESHOLD_STATE = 1;
   public final static int CRITICAL_THRESHOLD_STATE = 2;
   public final static int NORMAL_THRESHOLD_STATE = 3;

   public CostsPerProcess(ProcessDefinition pd, Map<String, Object> columnDefinitionMap)
   {
      this.processDefinition = pd;
      totalCountPIToday = 0l;
      totalCountPILastWeek = 0l;
      totalCountPILastMonth = 0l;
      costsToday = 0f;
      costsLastWeek = 0f;
      costsLastMonth = 0f;
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
            
            customColumns.put(key+CUSTOM_COL_COST_SUFFIX, 0f);
            customColumnPisCount.put(key+CUSTOM_COL_COST_SUFFIX, 0l);
            customColumns.put(key+CUSTOM_COL_STATUS_SUFFIX, NORMAL_THRESHOLD_STATE);
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

   public double getAverageCostsLastMonth()
   {
      return totalCountPILastMonth > 0 ? (costsLastMonth / totalCountPILastMonth) : 0;
   }

   public double getAverageCostsLastWeek()
   {
      return totalCountPILastWeek > 0 ? (costsLastWeek / totalCountPILastWeek) : 0;
   }

   public double getAverageCostsToday()
   {
      return totalCountPIToday > 0 ? (costsToday / totalCountPIToday) : 0;
   }
   
   public void addContribution(Contribution con, Map<String, DateRange> customColDateRange)
   {
      if(con != null)
      {
         ContributionInInterval ciiToday = con.getOrCreateContributionInInterval(DateRange.TODAY);
         ContributionInInterval ciiLastWeek = con.getOrCreateContributionInInterval(DateRange.LAST_WEEK);
         ContributionInInterval ciiLastMonth = con.getOrCreateContributionInInterval(DateRange.LAST_MONTH);
       
         if((ciiToday.getnPis() > 0) && ciiToday.getCost() > 0)
         {
            totalCountPIToday += ciiToday.getnPis();
            costsToday += ciiToday.getCost();
            if(ciiToday.getCriticalByExecutionCost().getRedInstancesCount() > 0)
            {
               thresholdStateToday = EXCEEDED_THRESHOLD_STATE;
            }
            else if(thresholdStateToday != EXCEEDED_THRESHOLD_STATE &&
                  ciiToday.getCriticalByExecutionCost().getYellowInstancesCount() > 0)
            {
               thresholdStateToday = CRITICAL_THRESHOLD_STATE;
            }
         }
         else
         {
            if(ciiToday.getnPis() > 0 && ciiToday.getCost() < 0)
            {
               trace.error("Invalid costs of today: " + ciiToday.getCost() + 
                     " by process with id: " + processDefinition.getId());
            }
         }
         if(ciiLastWeek.getnPis() > 0 && ciiLastWeek.getCost() > 0)
         {
            totalCountPILastWeek  += ciiLastWeek.getnPis();
            costsLastWeek += ciiLastWeek.getCost();
            if(ciiLastWeek.getCriticalByExecutionCost().getRedInstancesCount() > 0)
            {
               thresholdStateLastWeek = EXCEEDED_THRESHOLD_STATE;
            }
            else if(thresholdStateLastWeek != EXCEEDED_THRESHOLD_STATE &&
                  ciiLastWeek.getCriticalByExecutionCost().getYellowInstancesCount() > 0)
            {
               thresholdStateLastWeek = CRITICAL_THRESHOLD_STATE;
            }
         }
         else
         {
            if(ciiLastWeek.getnPis() > 0 && ciiLastWeek.getCost() < 0)
            {
               trace.error("Invalid costs of last week: " + ciiLastWeek.getCost()  + 
                     " by process with id: " + processDefinition.getId());
            }
         }
         if(ciiLastMonth.getnPis() > 0 && ciiLastMonth.getCost() > 0)
         {
            totalCountPILastMonth += ciiLastMonth.getnPis();
            costsLastMonth += ciiLastMonth.getCost();
            if(ciiLastMonth.getCriticalByExecutionCost().getRedInstancesCount() > 0)
            {
               thresholdStateLastMonth = EXCEEDED_THRESHOLD_STATE;
            }
            else if(thresholdStateLastMonth != EXCEEDED_THRESHOLD_STATE &&
                  ciiLastMonth.getCriticalByExecutionCost().getYellowInstancesCount() > 0)
            {
               thresholdStateLastMonth = CRITICAL_THRESHOLD_STATE;
            }
         }
         else
         {
            if(ciiLastMonth.getnPis() > 0 && ciiLastMonth.getCost() < 0)
            {
               trace.error("Invalid costs of last month: " + ciiLastMonth.getCost() + 
                     " by process with id: " + processDefinition.getId());
            }
         }
         
         for(Map.Entry<String, DateRange> custCols : customColDateRange.entrySet())
         {
            String key = custCols.getKey();
            DateRange dateRange = custCols.getValue();
            ContributionInInterval ciiCustomCol = con.getOrCreateContributionInInterval(dateRange);
            
            if((ciiCustomCol.getnPis() > 0) && ciiCustomCol.getCost() > 0)
            {
               try{
                  Long nPisCount = (Long) customColumnPisCount.get(key+CUSTOM_COL_COST_SUFFIX);
                  double costValue = Double.valueOf(customColumns.get(key+CUSTOM_COL_COST_SUFFIX).toString());
                  Integer thresholdValue = (Integer) customColumns.get(key+CUSTOM_COL_STATUS_SUFFIX);
                  nPisCount += ciiCustomCol.getnPis();
                  costValue += ciiCustomCol.getCost();
                  if(ciiCustomCol.getCriticalByExecutionCost().getRedInstancesCount() > 0)
                  {
                     thresholdValue = EXCEEDED_THRESHOLD_STATE;
                  }
                  else if(thresholdStateToday != EXCEEDED_THRESHOLD_STATE &&
                        ciiToday.getCriticalByExecutionCost().getYellowInstancesCount() > 0)
                  {
                     thresholdValue = CRITICAL_THRESHOLD_STATE;
                  }
                  if (nPisCount > 0)
                  {
                     costValue = (costValue / nPisCount);
                  }
                  customColumns.put(key+CUSTOM_COL_COST_SUFFIX, costValue);
                  customColumns.put(key+CUSTOM_COL_STATUS_SUFFIX, thresholdValue);
                  customColumnPisCount.put(key+CUSTOM_COL_COST_SUFFIX, nPisCount);
               }catch (Exception e) {
                     e.printStackTrace();
               }
               
            }
            else
            {
               customColumns.put(key+CUSTOM_COL_COST_SUFFIX, 0);
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
   
}