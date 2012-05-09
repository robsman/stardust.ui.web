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
package org.eclipse.stardust.ui.web.bcc.views.criticalityManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalityStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalityStatistics.IActivityEntry;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalityStatisticsQuery;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.ISearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;



/**
 * @author Shrikant.Gangal
 * 
 */
public class ModelDefCriticalityMgrTableEntry implements ICriticalityMgrTableEntry
{
   private Map<String, CriticalityDetails> criticalityDetailsMap;
   private List<ICriticalityMgrTableEntry> processEntries;
   private List<ICriticalityMgrTableEntry> filteredProcessEntries;
   private String modelName;
   private String description;
   private DeployedModel model;
   private CriticalityCategory selectedCriticalityCategory;
   private boolean filterAuxiliaryProcesses;

   /**
    * 
    */
   public ModelDefCriticalityMgrTableEntry()
   {}

   /**
    * @param processEntries
    * @param model
    * @param filterAuxiliaryProcesses
    * @param filterAuxiliaryActivities
    */
   public ModelDefCriticalityMgrTableEntry(List<ICriticalityMgrTableEntry> processEntries, DeployedModel model, boolean filterAuxiliaryProcesses, boolean filterAuxiliaryActivities)
   {
      this.filterAuxiliaryProcesses = filterAuxiliaryProcesses;
      this.processEntries = processEntries;
      initiFilteredProcessEntries();
      this.model = model;
      modelName = model != null ? I18nUtils.getLabel(model, model.getName()) : null;
      description = model != null ? I18nUtils.getDescriptionAsHtml(model, model.getDescription()) : null;
   }

   /**
    * 
    */
   public void initializeSelf()
   {
      criticalityDetailsMap = new LinkedHashMap<String, CriticalityDetails>();
      for (ICriticalityMgrTableEntry pe : processEntries)
      {
         Map<String, CriticalityDetails> cdm = pe.getCriticalityDetailsMap();
         for (String key : cdm.keySet())
         {
            CriticalityDetails pcc = cdm.get(key);

            CriticalityDetails cc = criticalityDetailsMap.get(key);
            if (null == cc)
            {
               cc = new CriticalityDetails(this);
               cc.setCount(pcc.getCount());
               cc.setCriticalityLabel(pcc.getCriticalityLabel());
               criticalityDetailsMap.put(cc.getCriticalityLabel(), cc);
            }
            else
            {
               cc.setCount(cc.getCount() + pcc.getCount());
            }            
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.bcc.views.criticalityManager.ICriticalityMgrTableEntry#
    * reInitialize()
    */
   public void initialize()
   {
      List<ICriticalityMgrTableEntry> children = (List<ICriticalityMgrTableEntry>) processEntries;
      for (ICriticalityMgrTableEntry child : children)
      {
         child.initialize();
      }
      initializeSelf();
   }

   /**
    * 
    */
   private void initiFilteredProcessEntries()
   {
      filteredProcessEntries = new ArrayList<ICriticalityMgrTableEntry>();
      for (ICriticalityMgrTableEntry pe : processEntries)
      {
         if (!(filterAuxiliaryProcesses && ProcessDefinitionUtils
               .isAuxiliaryProcess(((ProcessDefCriticalityMgrTableEntry) pe).getProcessDefinition())))
         {
            filteredProcessEntries.add(pe);
         }
      }
   }

   public void doCriticalityAction(ActionEvent event)
   {
      Map param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
      String selectedCriticalityCategoryLabel = (String) param.get("selectedCriticalityCategory");
      selectedCriticalityCategory = CriticalityConfigurationUtil
            .getCriticalityForLabel(selectedCriticalityCategoryLabel);
      if (null != selectedCriticalityCategory)
      {
         ModelEntryCriticalitySearchhandler searchHandler = new ModelEntryCriticalitySearchhandler();
         ActivityCriticalityManagerBean bean = (ActivityCriticalityManagerBean) ManagedBeanUtils
               .getManagedBean(ActivityCriticalityManagerBean.BEAN_ID);
         if (bean != null)
         {
            bean.setDetailViewProperties(searchHandler);
         }
      }
   }

   public Map<String, CriticalityDetails> getCriticalityDetailsMap()
   {
      return criticalityDetailsMap;
   }

   public String getDefaultPerformerName()
   {
      return null;
   }

   public List<ICriticalityMgrTableEntry> getChildren()
   {
      return filteredProcessEntries;
   }

   public String getType()
   {
      return null;
   }

   public String getName()
   {
      return modelName;
   }
   
   public String getDescription()
   {
      return description;
   }

   /**
    * @author Shrikant.Gangal
    *
    */
   public class ModelEntryCriticalitySearchhandler implements ISearchHandler
   {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.viewscommon.common.ISearchHandler#createQuery()
       */
      public Query createQuery()
      {
         if (null != selectedCriticalityCategory)
         {
            ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
            FilterTerm filter = query.getFilter();
            FilterTerm orTerm = filter.addOrTerm();

            /*
             * Fetch all process definitions irrespective of the filterAuxiliaryProcesses
             * flag as we need to show consider all processes for showing row counts.
             */
            List<ProcessDefinition> procDefs = ProcessDefinitionUtils.getAllProcessDefinitions(model, false);

            boolean hasAtLeastOneActivity = false;
            for (ProcessDefinition processDefinition : procDefs)
            {
               CriticalityStatisticsQuery cQuery = CriticalityStatisticsQuery.forProcesses(processDefinition);
               cQuery.where(ActivityInstanceQuery.CRITICALITY.between(
                     CriticalityConfigurationUtil.getEngineCriticality(selectedCriticalityCategory.getRangeFrom()),
                     CriticalityConfigurationUtil.getEngineCriticality(selectedCriticalityCategory.getRangeTo())));
               QueryService queryService = SessionContext.findSessionContext().getServiceFactory().getQueryService();
               CriticalityStatistics selectedCriticalityStats = (CriticalityStatistics) queryService
                     .getAllActivityInstances(cQuery);
               if (null != selectedCriticalityStats)
               {
                  @SuppressWarnings("unchecked")
                  List<Activity> activities = processDefinition.getAllActivities();
                  for (Activity act : activities)
                  {
                     IActivityEntry ae = selectedCriticalityStats.getStatisiticsForActivity(
                           processDefinition.getQualifiedId(), act.getQualifiedId());
                     if (null != ae)
                     {
                        Set<Long> actOids = ae.getInstances();
                        for (Iterator<Long> iter = actOids.iterator(); iter.hasNext();)
                        {
                           orTerm.add(ActivityInstanceQuery.OID.isEqual(iter.next().longValue()));
                           hasAtLeastOneActivity = true;
                        }
                     }
                  }
               }
               else
               {
                  filter.add(ActivityInstanceQuery.OID.isNull());
               }
            }
            if (!hasAtLeastOneActivity)
            {
               filter.add(ActivityInstanceQuery.OID.isNull());
            }

            return query;
         }

         return null;
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.viewscommon.common.ISearchHandler#performSearch(org.eclipse.stardust.engine.api.query.Query)
       */
      public QueryResult<ActivityInstance> performSearch(Query query)
      {
         return WorkflowFacade.getWorkflowFacade().getAllActivityInstances((ActivityInstanceQuery) query);
      }
   }
}
