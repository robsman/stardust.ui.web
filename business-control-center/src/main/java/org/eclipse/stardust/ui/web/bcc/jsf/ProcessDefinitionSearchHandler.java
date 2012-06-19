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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalExecutionTimePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.ProcessStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.ProcessStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.ProcessStatistics.IProcessStatistics;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;



public class ProcessDefinitionSearchHandler implements IProcessDefinitionSearchHandler
{
   private IActivityStatisticsSearchHandler activityStatisticsSearchHandler;
   private IProcessInstancesPrioritySearchHandler detailSearchHandler;
   
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.bcc.jsf.IProcessDefinitionSearchHandler#getProcessDefinitions()
    */
   public List getProcessDefinitions(boolean filterAuxiliaryProcesses)
   {
      List<ProcessDefinition> allProcessDefinitions;
      if (filterAuxiliaryProcesses)
      {
         allProcessDefinitions = ProcessDefinitionUtils.getAllBusinessRelevantProcesses();
      }
      else
      {
         allProcessDefinitions = ProcessDefinitionUtils.getAllAccessibleProcessDefinitions();
      }
      return getProcessStatistics(allProcessDefinitions);
   }
   
   /**
    * Performs a search for business-relevant process definitions as defined in the
    * {@link BusinessControlCenterConstants#BUSINESS_RELEVANT_PROCESS_ID_LIST}. The result is
    * stored in the field {@link #searchResult}.
    */
   public List getProcessDefinitions(boolean filterAuxiliaryProcesses, org.eclipse.stardust.engine.api.model.Model model)
   {
      // TODO: Merge
      List<ProcessDefinition> processes = ProcessDefinitionUtils.getAllProcessDefinitions(model, filterAuxiliaryProcesses);     
      return getProcessStatistics(processes);
   }
   
   private List <ProcessDefinitionWithPrio>getProcessStatistics(
         List <ProcessDefinition> pdl)
   {
      SessionContext sessionCtx = SessionContext.findSessionContext();
      List <ProcessDefinitionWithPrio>pdwp = null;
      if(sessionCtx.isSessionInitialized())
      {
         QueryService queryService = sessionCtx.getServiceFactory().getQueryService();
         ProcessStatisticsQuery query = ProcessStatisticsQuery.forProcesses(new HashSet<ProcessDefinition>(pdl));
         query.setPolicy(CriticalExecutionTimePolicy.criticalityByDuration(
               Constants.getCriticalDurationThreshold(
                     ProcessInstancePriority.LOW, 1.0f),
               Constants.getCriticalDurationThreshold(
                     ProcessInstancePriority.NORMAL, 1.0f),
               Constants.getCriticalDurationThreshold(
                     ProcessInstancePriority.HIGH, 1.0f)));
         ProcessStatistics processStatistics = (ProcessStatistics)
            queryService.getAllProcessInstances(query);
         
         pdwp = new ArrayList<ProcessDefinitionWithPrio>();
         for ( ProcessDefinition processDefinition:pdl)
         {              
            IProcessStatistics ps = processStatistics.getStatisticsForProcess(processDefinition.getQualifiedId());
            
            ProcessDefinitionWithPrio pdWithPrio = new ProcessDefinitionWithPrio(
                  processDefinition, ps,
                  activityStatisticsSearchHandler, detailSearchHandler);
            pdwp.add(pdWithPrio);
         }
      }
      return pdwp;
   }
      
   public void setActivityStatisticsSearchHandler(IActivityStatisticsSearchHandler searchHandler)
   {
      this.activityStatisticsSearchHandler = searchHandler;
   }
   
   public IActivityStatisticsSearchHandler getActivityStatisticsSearchHandler()
   {
      return activityStatisticsSearchHandler;
   }
   
   public void setProcessInstancePrioritySearchHandler(
         IProcessInstancesPrioritySearchHandler searchHandler)
   {
      this.detailSearchHandler = searchHandler;
   }
   
   public IProcessInstancesPrioritySearchHandler getProcessInstancePrioritySearchHandler()
   {
      return this.detailSearchHandler;
   }
}
