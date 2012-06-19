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



public class ModelWithPrio implements PriorityOverviewEntry, Serializable
{  
  
   private static final long serialVersionUID = 1L;
   private List<ProcessDefinitionWithPrio> children;
   private IProcessInstancesPrioritySearchHandler searchHandler;

   private Priorities priorities;  
   private Priorities criticalPriorities;
   
   String modelName;
   String description;
   public ModelWithPrio()
   {
      
   }
   public ModelWithPrio(List<ProcessDefinitionWithPrio> children, IProcessInstancesPrioritySearchHandler  detailSearchHandler,int modelOID)
   {
      this.children = children;
      Iterator<PriorityOverviewEntry> childIter = children != null ? children.iterator() : 
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
         }
      }
      Model model = ModelCache.findModelCache().getModel(modelOID);
      modelName = model != null ? I18nUtils.getLabel(model, model.getName())  : null;
      description = model != null ? I18nUtils.getDescriptionAsHtml(model, model.getDescription()) : null;
      this.searchHandler = detailSearchHandler;
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
      if(searchHandler != null)
      {
         Set<Long> oids = null;
         Set<ProcessDefinition> pds = CollectionUtils.newHashSet();
         BusinessProcessManagerBean businessProcessManagerBean = (BusinessProcessManagerBean) ManagedBeanUtils
               .getManagedBean(BusinessProcessManagerBean.BEAN_ID);

         if (isCriticalPriority())
         {
            oids = new HashSet<Long>();
            if (null != children)
            {
               for (Object rowData : children)
               {
                  if (rowData instanceof ProcessDefinitionWithPrio)
                  {
                     ProcessDefinitionWithPrio pd = (ProcessDefinitionWithPrio) rowData;
                     IProcessStatistics ps = pd.getProcessStatistics();
                     if (ps != null)
                     {
                        oids.addAll(priority == null ? ps.getTotalCriticalInstances() : ps
                              .getCriticalInstances(priority.intValue()));
                     }
                  }
               }
            }
            searchHandler.setQueryData(null, priority, oids);
         }
         else
         {
               for (Object rowData : children)
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