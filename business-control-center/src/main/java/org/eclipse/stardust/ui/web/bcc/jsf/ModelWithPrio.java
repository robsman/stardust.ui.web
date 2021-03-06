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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.core.query.statistics.api.ProcessStatistics.IProcessStatistics;
import org.eclipse.stardust.ui.web.bcc.views.BusinessProcessManagerBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelElementLocalizerKey;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;



public class ModelWithPrio implements PriorityOverviewEntry, Serializable
{  
  
   private static final long serialVersionUID = 1L;
   private List<ProcessDefinitionWithPrio> children;
   private List<ProcessDefinitionWithPrio> allChildProcesses;
   private IProcessInstancesPrioritySearchHandler searchHandler;

   private Priorities priorities;  
   private Priorities criticalPriorities;
   
   String modelName;
   String description;
   private long interruptedCount;
   
   boolean filterAuxiliaryProcesses = true;
   public ModelWithPrio()
   {
      
   }
   public ModelWithPrio(List<ProcessDefinitionWithPrio> childProcesses, IProcessInstancesPrioritySearchHandler  detailSearchHandler,int modelOID, boolean filterAuxiliaryProcesses)
   {
      this.filterAuxiliaryProcesses = filterAuxiliaryProcesses;
      allChildProcesses = childProcesses;
      initialzeChildren();
      Iterator<PriorityOverviewEntry> childIter = childProcesses != null ? childProcesses.iterator() : 
           Collections.EMPTY_LIST.iterator();
      priorities = new Priorities();
      criticalPriorities = new Priorities();
      while (childIter.hasNext())
      {
         Object rowData = childIter.next();
         if(rowData instanceof ProcessDefinitionWithPrio)
         {
            ProcessDefinitionWithPrio pd = (ProcessDefinitionWithPrio) rowData;
            priorities.setHighPriority(priorities.getHighPriority() +
                  pd.getPriorities().getHighPriority());
            priorities.setNormalPriority(priorities.getNormalPriority() +
                  pd.getPriorities().getNormalPriority());
            priorities.setLowPriority(priorities.getLowPriority() +
                  pd.getPriorities().getLowPriority());
            criticalPriorities.setHighPriority(criticalPriorities.getHighPriority() +
                  pd.getCriticalPriorities().getHighPriority());
            criticalPriorities.setNormalPriority(criticalPriorities.getNormalPriority() +
                  pd.getCriticalPriorities().getNormalPriority());
            criticalPriorities.setLowPriority(criticalPriorities.getLowPriority() +
                  pd.getCriticalPriorities().getLowPriority());
            interruptedCount += pd.getInterruptedCount();
         }
      }
      Model model = ModelCache.findModelCache().getModel(modelOID);
      modelName = model != null ? I18nUtils.getLabel(model, model.getName())  : null;
      description = model != null ? I18nUtils.getDescriptionAsHtml(model, model.getDescription()) : null;
      this.searchHandler = detailSearchHandler;
   }

   /**
    * 
    */
   private void initialzeChildren()
   {
      children = new ArrayList<ProcessDefinitionWithPrio>();
      for (ProcessDefinitionWithPrio pp : allChildProcesses)
      {
         if (!(filterAuxiliaryProcesses && ProcessDefinitionUtils.isAuxiliaryProcess(pp.getProcessDefinition())))
         {
            children.add(pp);
         }
      }
   }

   public String getName()
   {
      return modelName;
   }
   
   public String getDescription()
   {
      return description;
   }
   
   public List<ProcessDefinitionWithPrio> getChildren()
   {
      return children;
   }
   
   public Priorities getPriorities()
   {
      return priorities;
   }
   
   public Priorities getCriticalPriorities()
   {
      return criticalPriorities;
   }
   
   public String getRowStyleClass()
   {
      return null;
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
      return IThresholdProvider.UNDEFINED_THRESHOLD_STATE;
   }
   
   public long getInterruptedCount()
   {
     return interruptedCount;
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
         Set<Long> oids = null;
         Set<ProcessDefinition> pds = CollectionUtils.newHashSet();
         BusinessProcessManagerBean businessProcessManagerBean = (BusinessProcessManagerBean) ManagedBeanUtils
               .getManagedBean(BusinessProcessManagerBean.BEAN_ID);

         if (isCriticalPriority() || showInterruptedAI())
         {
            oids = new HashSet<Long>();
            if (null != allChildProcesses)
            {
               for (Object rowData : allChildProcesses)
               {
                  if (rowData instanceof ProcessDefinitionWithPrio)
                  {
                     ProcessDefinitionWithPrio pd = (ProcessDefinitionWithPrio) rowData;
                     IProcessStatistics ps = pd.getProcessStatistics();
                     if (ps != null)
                     {
                        if(isCriticalPriority())
                        {
                           oids.addAll(priority == null ? ps.getTotalCriticalInstances() : ps
                                 .getCriticalInstances(priority.intValue()));   
                        }
                        else
                        {
                           oids.addAll(ps.getInterruptedInstances());
                        }
                     }
                  }
               }
            }
            searchHandler.setQueryData(null, priority, oids);
         }
         else
         {
               for (Object rowData : allChildProcesses)
               {
                  if (rowData instanceof ProcessDefinitionWithPrio)
                  {
                     ProcessDefinitionWithPrio pd = (ProcessDefinitionWithPrio) rowData;                  
                     pds.add(pd.getProcessDefinition());                     
                  }
               }
            searchHandler.setQueryData(pds, priority);
         }

         if (null != businessProcessManagerBean)
         {
            businessProcessManagerBean.setDetailViewProperties(this, searchHandler);
         }
      }
   }

   public String getType()
   {
      return null;
   }

}