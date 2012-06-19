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
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelElementLocalizerKey;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.ISearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;



/**
 * @author Shrikant.Gangal
 * 
 */
public class ModelDefCriticalityMgrTableEntry implements ICriticalityMgrTableEntry
{
   private Map<String, CriticalityDetails> criticalityDetailsMap;
   private List<ProcessDefCriticalityMgrTableEntry> processEntries;
   private String modelName;
   private String description;
   private DeployedModel model;
   private CriticalityCategory selectedCriticalityCategory;
   private boolean filterAuxiliaryProcesses;

   public ModelDefCriticalityMgrTableEntry()
   {}

   public ModelDefCriticalityMgrTableEntry(List<ProcessDefCriticalityMgrTableEntry> processEntries, DeployedModel model, boolean filterAuxiliaryProcesses)
   {
      this.processEntries = processEntries;
      this.model = model;
      this.filterAuxiliaryProcesses = filterAuxiliaryProcesses;
      modelName = model != null ? I18nUtils.getLabel(model, model.getName()) : null;
      description = model != null ? I18nUtils.getDescriptionAsHtml(model, model.getDescription()) : null;
   }

   public void initializeSelf()
   {
      criticalityDetailsMap = new LinkedHashMap<String, CriticalityDetails>();
      for (ProcessDefCriticalityMgrTableEntry pe : processEntries)
      {
         Map<String, CriticalityDetails> cdm = pe.getCriticalityDetailsMap();
         Set<String> keys = cdm.keySet();
         for (String key : keys)
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
      List<ICriticalityMgrTableEntry> children = getChildren();
      for (ICriticalityMgrTableEntry child : children)
      {
         child.initialize();
      }
      initializeSelf();
   }

   public String getDefaultPerformerName()
   {
      return null;
   }

   public List getChildren()
   {
      return processEntries;
   }

   public String getType()
   {
      // TODO Auto-generated method stub
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

   public class ModelEntryCriticalitySearchhandler implements ISearchHandler
   {
      public Query createQuery()
      {
         if (null != selectedCriticalityCategory)
         {
            ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
            FilterTerm filter = query.getFilter();
            FilterTerm orTerm = filter.addOrTerm();
            List<ProcessDefinition> procDefs = ProcessDefinitionUtils.getAllProcessDefinitions(model, filterAuxiliaryProcesses);
            
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

      public QueryResult<ActivityInstance> performSearch(Query query)
      {
         return WorkflowFacade.getWorkflowFacade().getAllActivityInstances((ActivityInstanceQuery) query);
      }
   }
}
