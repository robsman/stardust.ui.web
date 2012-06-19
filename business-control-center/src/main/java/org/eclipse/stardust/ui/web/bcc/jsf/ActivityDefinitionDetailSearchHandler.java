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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.core.query.statistics.api.ActivityStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.ActivityStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalExecutionTimePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.ActivityStatistics.IActivityStatistics;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;



public class ActivityDefinitionDetailSearchHandler implements IActivityStatisticsSearchHandler, Serializable
{   
   private static final long serialVersionUID = 1L;

   public final static String BEAN_ID = "bccActivityStatisticsSearchHandler";
   
   private IActivitySearchHandler searchHandler;
   
   public List<ActivityDefinitionWithPrio> getActivityStatistics(ProcessDefinition pd)
   {
      if (pd != null)
      {
         return getActivityStatistics(pd, true);
      }
      return null;
   }
   
   private List <ActivityDefinitionWithPrio> getActivityStatistics(
         ProcessDefinition pd, boolean hideNullStatistics)
   {
      SessionContext sessionCtx = SessionContext.findSessionContext();
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      List<ActivityDefinitionWithPrio>pdwp = null;
      if(sessionCtx.isSessionInitialized())
      {
         QueryService queryService = sessionCtx.getServiceFactory().getQueryService();
         ActivityStatisticsQuery query = ActivityStatisticsQuery.forProcesses(pd);
         query.setPolicy(CriticalExecutionTimePolicy.criticalityByDuration(
               Constants.getCriticalDurationThreshold(
                     ProcessInstancePriority.LOW, 1.0f),
               Constants.getCriticalDurationThreshold(
                     ProcessInstancePriority.NORMAL, 1.0f),
               Constants.getCriticalDurationThreshold(
                     ProcessInstancePriority.HIGH, 1.0f)));
         ActivityStatistics activityStatistics = (ActivityStatistics)queryService
            .getAllActivityInstances(query);
         pdwp = new ArrayList<ActivityDefinitionWithPrio>();
         Collection<Activity> activitis = facade.getAllActivities(pd);
         for(Activity activity :activitis)
         {           
            IActivityStatistics as = activityStatistics.getStatisticsForActivity(
                  pd.getQualifiedId(), activity.getQualifiedId());
            ActivityDefinitionWithPrio ad = new ActivityDefinitionWithPrio(
                  activity, pd, pd, as,
                  searchHandler);
            if (!(hideNullStatistics && 
                  (ad.getPriorities().getTotalPriority() == 0)))
            {
               pdwp.add(ad);
            }
         }
      }
      return pdwp;
   }

   public IActivitySearchHandler getActivityPrioritySearchHandler()
   {
      return searchHandler;
   }

   public void setActivityPrioritySearchHandler(IActivitySearchHandler searchHandler)
   {
      this.searchHandler = searchHandler;
   }

}
