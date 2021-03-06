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
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalityStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalityStatistics.IActivityEntry;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalityStatistics.IProcessEntry;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalityStatisticsQuery;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.ISearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;



/**
 * @author Shrikant.Gangal
 * 
 */
public class ProcessDefCriticalityMgrTableEntry implements ICriticalityMgrTableEntry
{

   private ProcessDefinition processDefinition;
   private List<ICriticalityMgrTableEntry> activityEntries;
   private List<ICriticalityMgrTableEntry> filteredActivityEntries;
   private Map<String, CriticalityStatistics> criticaliyStatisticsMap;
   private Map<String, CriticalityDetails> criticalityDetailsMap;
   private CriticalityCategory selectedCriticalityCategory;
   private String type;
   private boolean filterAuxiliaryActivities;

   /**
    * @param processDefinition
    * @param criticaliyStatisticsMap
    * @param activityEntries
    * @param filterAuxiliaryActivities
    */
   public ProcessDefCriticalityMgrTableEntry(ProcessDefinition processDefinition,
         Map<String, CriticalityStatistics> criticaliyStatisticsMap,
         List<ICriticalityMgrTableEntry> activityEntries, boolean filterAuxiliaryActivities)
   {
      this.processDefinition = processDefinition;
      this.activityEntries = activityEntries;
      this.criticaliyStatisticsMap = criticaliyStatisticsMap;
      this.filterAuxiliaryActivities = filterAuxiliaryActivities;
      if (ProcessDefinitionUtils.isAuxiliaryProcess(processDefinition))
      {
         type = "auxiliaryProcess";
      }
      initFilteredActivities();
   }

   /**
    * 
    */
   public void initializeSelf()
   {
      criticalityDetailsMap = new LinkedHashMap<String, CriticalityDetails>();
      Set<String> labels = criticaliyStatisticsMap.keySet();
      for (String label : labels)
      {
         CriticalityDetails cc = new CriticalityDetails(this);
         CriticalityStatistics cs = criticaliyStatisticsMap.get(label);
         IProcessEntry pe = cs.getStatisitcsForProcess(processDefinition.getQualifiedId());
         cc.setCount(null != pe ? pe.getCumulatedInstances() : 0);
         cc.setCriticalityLabel(label);
         criticalityDetailsMap.put(cc.getCriticalityLabel(), cc);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.bcc.views.criticalityManager.ICriticalityMgrTableEntry#initialize()
    */
   public void initialize()
   {
      List<ICriticalityMgrTableEntry> children = (List<ICriticalityMgrTableEntry>) activityEntries;
      for (ICriticalityMgrTableEntry child : children)
      {
         child.initialize();
      }
      initializeSelf();
   }

   /**
    * 
    */
   private void initFilteredActivities()
   {
      filteredActivityEntries = new ArrayList<ICriticalityMgrTableEntry>();
      for (ICriticalityMgrTableEntry ae : activityEntries)
      {
         if (!(filterAuxiliaryActivities && ActivityInstanceUtils
               .isAuxiliaryActivity(((ActivityDefCriticalityMgrTableEntry) ae).getActivity())))
         {
            filteredActivityEntries.add(ae);
         }
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.bcc.views.criticalityManager.ICriticalityMgrTableEntry#doCriticalityAction(javax.faces.event.ActionEvent)
    */
   public void doCriticalityAction(ActionEvent event)
   {
      Map param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
      String selectedCriticalityCategoryLabel = (String) param.get("selectedCriticalityCategory");
      selectedCriticalityCategory = CriticalityConfigurationUtil
            .getCriticalityForLabel(selectedCriticalityCategoryLabel);
      if (null != selectedCriticalityCategory)
      {
         ProcessEntryCriticalitySearchhandler searchHandler = new ProcessEntryCriticalitySearchhandler();
         ActivityCriticalityManagerBean bean = (ActivityCriticalityManagerBean) ManagedBeanUtils
               .getManagedBean(ActivityCriticalityManagerBean.BEAN_ID);
         if (bean != null)
         {
            bean.setDetailViewProperties(searchHandler);
         }
      }
   }

   public String getDefaultPerformerName()
   {
      return null;
   }

   public List<ICriticalityMgrTableEntry> getChildren()
   {
      return filteredActivityEntries;
   }

   public String getType()
   {
      return type;
   }

   public String getName()
   {
      return I18nUtils.getProcessName(processDefinition);
   }
   
   public String getDescription()
   {
      return I18nUtils.getDescriptionAsHtml(processDefinition, processDefinition.getDescription());
   }

   public Map<String, CriticalityDetails> getCriticalityDetailsMap()
   {
      return criticalityDetailsMap;
   }

   public ProcessDefinition getProcessDefinition()
   {
      return processDefinition;
   }

   /**
    * @author Shrikant.Gangal
    *
    */
   public class ProcessEntryCriticalitySearchhandler implements ISearchHandler
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
            CriticalityStatisticsQuery cQuery = CriticalityStatisticsQuery.forProcesses(processDefinition);
            cQuery.where(ActivityInstanceQuery.CRITICALITY.between(
                  CriticalityConfigurationUtil.getEngineCriticality(selectedCriticalityCategory.getRangeFrom()),
                  CriticalityConfigurationUtil.getEngineCriticality(selectedCriticalityCategory.getRangeTo())));
            QueryService queryService = SessionContext.findSessionContext().getServiceFactory().getQueryService();
            CriticalityStatistics selectedCriticalityStats = (CriticalityStatistics) queryService
                  .getAllActivityInstances(cQuery);
            ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
            FilterTerm filter = query.getFilter();
            if (null != selectedCriticalityStats)
            {
               @SuppressWarnings("unchecked")
               List<Activity> activities = processDefinition.getAllActivities();
               FilterTerm orTerm = filter.addOrTerm();
               boolean hasAtLeastOneActivity = false;
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
               if (!hasAtLeastOneActivity)
               {
                  filter.add(ActivityInstanceQuery.OID.isNull());
               }
            }
            else
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
