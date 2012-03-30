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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.engine.api.dto.ProcessDefinitionDetails;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.engine.core.query.statistics.api.ProcessStatistics.IProcessStatistics;
import org.eclipse.stardust.ui.web.bcc.views.BusinessProcessManagerBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;



public class ProcessDefinitionWithPrio implements PriorityOverviewEntry, Serializable
{
   
   private ProcessDefinition processDefinition;
   private List activityDataModel;
   private List children;
   private IProcessInstancesPrioritySearchHandler detailSearchHandler;
   private IActivityStatisticsSearchHandler activitySearchHandler;
   private boolean fetchChildren;
   private boolean alreadyFetchedChildren;
   
   private Priorities priorities;  
   private Priorities criticalPriorities;
   private IProcessStatistics ps;
   private String processType;
   
   private int thresholdState;

   private boolean filterAuxiliaryActivities;

   public ProcessDefinitionWithPrio(ProcessDefinition processDefinition, 
         IProcessStatistics ps,
         IActivityStatisticsSearchHandler activitySearchHandler,
         IProcessInstancesPrioritySearchHandler detailSearchHandler, boolean filterAuxiliaryActivities)
   {
      this.filterAuxiliaryActivities = filterAuxiliaryActivities;
      this.processDefinition = processDefinition;
      this.ps = ps;
      thresholdState = IThresholdProvider.UNDEFINED_THRESHOLD_STATE;
      this.activitySearchHandler = activitySearchHandler;
      this.detailSearchHandler = detailSearchHandler;
      fetchChildren = false;
      
      if (ProcessDefinitionUtils.isAuxiliaryProcess(processDefinition))
      {
         processType = "auxiliaryProcess";
      }

      initVars(ps);
      prepareActivityDataModel();
   }

   private void initVars(IProcessStatistics ps)
   {
      this.priorities = new Priorities();
      criticalPriorities = new Priorities();
      if(ps != null)
      {
         priorities.setHighPriority(ps.getInstancesCount(
               ProcessInstancePriority.HIGH));
         priorities.setNormalPriority(ps.getInstancesCount(
               ProcessInstancePriority.NORMAL));
         priorities.setLowPriority(ps.getInstancesCount(
               ProcessInstancePriority.LOW));
         
         criticalPriorities.setHighPriority(ps.getCriticalInstancesCount(
               ProcessInstancePriority.HIGH));
         criticalPriorities.setNormalPriority(ps.getCriticalInstancesCount(
               ProcessInstancePriority.NORMAL));
         criticalPriorities.setLowPriority(ps.getCriticalInstancesCount(
               ProcessInstancePriority.LOW));
      }
      thresholdState = BusinessControlCenterConstants
         .getThresholdProvider().getProcessThreshold(this);
   }

   private void prepareActivityDataModel()
   {
      activityDataModel = new ArrayList(1);
      if(priorities.getTotalPriority() > 0)
      {
         activityDataModel.add("dummy");
      }
      alreadyFetchedChildren = false;
   }

   public ProcessDefinition getProcessDefinition()
   {
      return processDefinition;
   }
   
   public String getDescription()
   {
      String defaultDesc = processDefinition instanceof ProcessDefinitionDetails
            ? ((ProcessDefinitionDetails) processDefinition).getDescription()
            : null;
      return I18nUtils.getProcessDescription(processDefinition, defaultDesc);
   }
   
   public String getName()
   {
      return I18nUtils.getProcessName(processDefinition);
   }

   public void setProcessDefinition(ProcessDefinition pi)
   {
      this.processDefinition = pi;
   }

   public Priorities getPriorities()
   {
      return priorities;
   }
   
   public Priorities getCriticalPriorities()
   {
      return criticalPriorities;
   }

   public void resetChildren()
   {
      prepareActivityDataModel();
      fetchChildren = true;
   }
   
   public List getChildren()
   {
      if(activitySearchHandler == null)
      {
         activitySearchHandler = (IActivityStatisticsSearchHandler) ManagedBeanUtils
            .getManagedBean(ActivityDefinitionDetailSearchHandler.BEAN_ID);
      }
      if(!alreadyFetchedChildren && fetchChildren)
      {
         activityDataModel = activitySearchHandler.getActivityStatistics(processDefinition);
         children = new ArrayList();
         for (int i = 0; i < activityDataModel.size(); i++)
         {
            if (!(filterAuxiliaryActivities && ActivityInstanceUtils
                  .isAuxiliaryActivity(((ActivityDefinitionWithPrio) activityDataModel.get(i)).getActivity())))
            {
               children.add(activityDataModel.get(i));
            }
         }
         alreadyFetchedChildren = true;
         fetchChildren = false;
      }
      return children;
   }
   
   public void activateChildrenFetch()
   {
      fetchChildren = true;;
   }
   
   public String getDefaultPerformerName()
   {
      return null;
   }
   
   public String getDefaultPerformerDesc()
   {
      return null;
   }

   public int getThresholdState()
   {
      return thresholdState;
   }
   
   protected IProcessStatistics getProcessStatistics()
   {
      return ps;
   }
   
   private Integer getSelectedPriority()
   {
      Map param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
      Object obj = param.get("selectedPriorityForDetails");
      Integer priority = null;
      if(obj != null)
      {
         try
         {
            priority = new Integer(obj.toString());
         }
         catch(NumberFormatException e)
         {
            // ignore
         }
      }
      return priority;
   }
   
   private boolean isCriticalPriority()
   {
      Map param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
      Object obj = param.get("selectedCriticalForDetails");
      Boolean critical = null;
      if(obj != null)
      {
         critical = new Boolean(obj.toString());
      }
      return critical != null ? critical.booleanValue() : false;
   }
   
   public void doPriorityAction(ActionEvent event)
   {
      Integer priority = getSelectedPriority();
      if( detailSearchHandler == null)
      {
         detailSearchHandler = new ProcessInstancesPrioritySearchHandler();
      }
      if(detailSearchHandler != null)
      {
         Set oids = null;
         if(isCriticalPriority())
         {
            if(ps != null)
            {
               oids = priority == null ? ps.getTotalCriticalInstances() :
                  ps.getCriticalInstances(priority.intValue());               
            }
            else
            {
               oids = Collections.EMPTY_SET;
            }
         }
         detailSearchHandler.setQueryData(processDefinition, priority, oids);
         BusinessProcessManagerBean bean = (BusinessProcessManagerBean) 
            ManagedBeanUtils.getManagedBean(BusinessProcessManagerBean.BEAN_ID);
         if(bean != null)
         {
            bean.setDetailViewProperties(this, detailSearchHandler);
         }
      }
   }

   public String getType()
   {
      return processType;
   }
}
