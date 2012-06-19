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
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelElementLocalizerKey;


public class CostsPerProcess
{
   protected final static Logger trace = LogManager.getLogger(CostsPerProcess.class);
   
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
   
   public final static int UNDEFINED_THRESHOLD_STATE = -1;
   public final static int EXCEEDED_THRESHOLD_STATE = 1;
   public final static int CRITICAL_THRESHOLD_STATE = 2;
   public final static int NORMAL_THRESHOLD_STATE = 3;

   public CostsPerProcess(ProcessDefinition pd)
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
   
   public void addContribution(Contribution con)
   {
      if(con != null)
      {
         if(con.contributionToday.nPis > 0 && con.contributionToday.cost > 0)
         {
            totalCountPIToday += con.contributionToday.nPis;
            costsToday += con.contributionToday.cost;
            if(con.contributionToday.criticalByExecutionCost.getRedInstancesCount() > 0)
            {
               thresholdStateToday = EXCEEDED_THRESHOLD_STATE;
            }
            else if(thresholdStateToday != EXCEEDED_THRESHOLD_STATE &&
                  con.contributionToday.criticalByExecutionCost.getYellowInstancesCount() > 0)
            {
               thresholdStateToday = CRITICAL_THRESHOLD_STATE;
            }
         }
         else
         {
            if(con.contributionToday.nPis > 0 && con.contributionToday.cost < 0)
            {
               trace.error("Invalid costs of today: " + con.contributionToday.cost + 
                     " by process with id: " + processDefinition.getId());
            }
         }
         if(con.contributionLastWeek.nPis > 0 && con.contributionLastWeek.cost > 0)
         {
            totalCountPILastWeek  += con.contributionLastWeek.nPis;
            costsLastWeek += con.contributionLastWeek.cost;
            if(con.contributionLastWeek.criticalByExecutionCost.getRedInstancesCount() > 0)
            {
               thresholdStateLastWeek = EXCEEDED_THRESHOLD_STATE;
            }
            else if(thresholdStateLastWeek != EXCEEDED_THRESHOLD_STATE &&
                  con.contributionLastWeek.criticalByExecutionCost.getYellowInstancesCount() > 0)
            {
               thresholdStateLastWeek = CRITICAL_THRESHOLD_STATE;
            }
         }
         else
         {
            if(con.contributionLastWeek.nPis > 0 && con.contributionLastWeek.cost < 0)
            {
               trace.error("Invalid costs of last week: " + con.contributionLastWeek.cost  + 
                     " by process with id: " + processDefinition.getId());
            }
         }
         if(con.contributionLastMonth.nPis > 0 && con.contributionLastMonth.cost > 0)
         {
            totalCountPILastMonth += con.contributionLastMonth.nPis;
            costsLastMonth += con.contributionLastMonth.cost;
            if(con.contributionLastMonth.criticalByExecutionCost.getRedInstancesCount() > 0)
            {
               thresholdStateLastMonth = EXCEEDED_THRESHOLD_STATE;
            }
            else if(thresholdStateLastMonth != EXCEEDED_THRESHOLD_STATE &&
                  con.contributionLastMonth.criticalByExecutionCost.getYellowInstancesCount() > 0)
            {
               thresholdStateLastMonth = CRITICAL_THRESHOLD_STATE;
            }
         }
         else
         {
            if(con.contributionLastMonth.nPis > 0 && con.contributionLastMonth.cost < 0)
            {
               trace.error("Invalid costs of last month: " + con.contributionLastMonth.cost + 
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