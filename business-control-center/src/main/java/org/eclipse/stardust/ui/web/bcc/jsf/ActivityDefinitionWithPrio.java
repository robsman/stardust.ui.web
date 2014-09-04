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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.engine.api.dto.ModelParticipantDetails;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.engine.core.query.statistics.api.ActivityStatistics.IActivityStatistics;
import org.eclipse.stardust.ui.web.bcc.views.BusinessProcessManagerBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelElementLocalizerKey;



public class ActivityDefinitionWithPrio implements PriorityOverviewEntry, Serializable
{
   
   private ProcessDefinition contextDefinition;

   private ProcessDefinition processDefinition;

   private Activity activity;
   
   private Priorities priorities;  
   private Priorities criticalPriorities;

   private int thresholdState;
   private long interruptedCount;
   
   private IActivitySearchHandler searchHandler;

   private IActivityStatistics as;

   public ActivityDefinitionWithPrio(Activity activity,
         ProcessDefinition processDefinition, ProcessDefinition context,
         IActivityStatistics as,
         IActivitySearchHandler searchHandler)
   {
      this.activity = activity;
      this.processDefinition = processDefinition;
      this.contextDefinition = context;
      this.as = as;
      thresholdState = IThresholdProvider.UNDEFINED_THRESHOLD_STATE;
      this.searchHandler = searchHandler;
      initVars();
   }

   private void initVars()
   {
      this.priorities = new Priorities();
      criticalPriorities = new Priorities();
      if(as != null)
      {
         priorities.setHighPriority(as.getInstancesCount(
               ProcessInstancePriority.HIGH));
         priorities.setNormalPriority(as.getInstancesCount(
               ProcessInstancePriority.NORMAL));
         priorities.setLowPriority(as.getInstancesCount(
               ProcessInstancePriority.LOW));
         criticalPriorities.setHighPriority(as.getCriticalInstancesCount(
               ProcessInstancePriority.HIGH));
         criticalPriorities.setNormalPriority(as.getCriticalInstancesCount(
               ProcessInstancePriority.NORMAL));
         criticalPriorities.setLowPriority(as.getCriticalInstancesCount(
               ProcessInstancePriority.LOW));
         interruptedCount = as.getInterruptedInstancesCount();
      }
      thresholdState = BusinessControlCenterConstants
         .getThresholdProvider().getActivityThreshold(this);
   }

   public Activity getActivity()
   {
      return activity;
   }
   
   public String getName()
   {
      return I18nUtils.getActivityName(activity);
   }
   
   public String getDescription() {
      return null;
   }
   
   public Priorities getPriorities()
   {
      return priorities;
   }
   
   public Priorities getCriticalPriorities()
   {
      return criticalPriorities;
   }

   public String getDefaultPerformerName()
   {
      ModelParticipant performer = activity.getDefaultPerformer();
      return I18nUtils.getLabel(performer, null, ModelElementLocalizerKey.KEY_NAME);
   }
   
   public String getDefaultPerformerDesc()
   {
      ModelParticipant performer = activity.getDefaultPerformer();
      String defaultDesc = performer instanceof ModelParticipantDetails
            ? ((ModelParticipantDetails) performer).getDescription()
            : null;
      return I18nUtils.getParticipantDescription(performer, defaultDesc);
   }

   public ProcessDefinition getProcessDefinition()
   {
      return processDefinition;
   }

   public ProcessDefinition getContext()
   {
      return contextDefinition;
   }

   public List getChildren()
   {
      return null;
   }

   public int getThresholdState()
   {
      return thresholdState;
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
   
   private boolean showInterruptedAI()
   {
      Map param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
      Object obj = param.get("showInterruptedAI");
      Boolean showInterrupted = null;
      if(obj != null)
      {
         showInterrupted = new Boolean(obj.toString());
      }
      return showInterrupted != null ? showInterrupted.booleanValue() : false;
   }
   
   public void doPriorityAction(ActionEvent event)
   {
      Integer priority = getSelectedPriority();
      if(searchHandler != null)
      {
         Set oids = null;
         if(isCriticalPriority())
         {
            if(as != null)
            {
               oids = priority == null ? as.getTotalCriticalInstances() : 
                  as.getCriticalInstances(priority.intValue());
            }
            else
            {
               oids = Collections.EMPTY_SET;
            }
         }
         else if (showInterruptedAI())
         {
            if (as != null)
            {
               oids = as.getInterruptedInstances();

            }
            else
            {
               oids = Collections.EMPTY_SET;
            }
         }
         searchHandler.setQueryData(processDefinition, activity, priority, oids);
         BusinessProcessManagerBean bean = (BusinessProcessManagerBean) 
            ManagedBeanUtils.getManagedBean(BusinessProcessManagerBean.BEAN_ID);
         if(bean != null)
         {
            bean.setDetailViewProperties(this, searchHandler);
         }
      }
   }

   public String getType()
   {
      return null;
   }

   public long getInterruptedCount()
   {
      return interruptedCount;
   }
}
