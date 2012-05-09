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
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalityStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalityStatistics.IActivityEntry;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalityStatisticsQuery;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.ISearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelElementLocalizerKey;



/**
 * @author Shrikant.Gangal
 *
 */
public class ActivityDefCriticalityMgrTableEntry implements ICriticalityMgrTableEntry
{
   private Map<String, CriticalityStatistics> criticaliyStatisticsMap;
   private Map<String, CriticalityDetails> criticalityDetailsMap;
   private ProcessDefinition processDefinition;
   private Activity activity;
   private CriticalityCategory selectedCriticalityCategory;
   private String type;

   /**
    * @param activity
    * @param processDefinition
    * @param criticaliyStatisticsMap
    */
   public ActivityDefCriticalityMgrTableEntry(Activity activity, ProcessDefinition processDefinition,
         Map<String, CriticalityStatistics> criticaliyStatisticsMap)
   {
      this.activity = activity;
      this.processDefinition = processDefinition;
      this.criticaliyStatisticsMap = criticaliyStatisticsMap;
      if (ActivityInstanceUtils.isAuxiliaryActivity(activity))
      {
         type = "auxiliaryActivity";
      }
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
         IActivityEntry ae = cs
               .getStatisiticsForActivity(processDefinition.getQualifiedId(), activity.getQualifiedId());
         cc.setCount(null != ae ? ae.getInstancesCount() : 0);
         cc.setCriticalityLabel(label);
         criticalityDetailsMap.put(cc.getCriticalityLabel(), cc);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.bcc.views.criticalityManager.ICriticalityMgrTableEntry#initialize()
    */
   public void initialize()
   {
      initializeSelf();
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.bcc.views.criticalityManager.ICriticalityMgrTableEntry#getDefaultPerformerName()
    */
   public String getDefaultPerformerName()
   {
      ModelParticipant performer = activity.getDefaultPerformer();
      return I18nUtils.getLabel(performer, null, ModelElementLocalizerKey.KEY_NAME);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.bcc.views.criticalityManager.ICriticalityMgrTableEntry#getChildren()
    */
   public List<ICriticalityMgrTableEntry> getChildren()
   {
      return null;
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
         ActivityEntryCriticalitySearchhandler searchHandler = new ActivityEntryCriticalitySearchhandler();
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

   public String getType()
   {
      return type;
   }

   public String getName()
   {
      return I18nUtils.getActivityName(activity);
   }

   public Activity getActivity()
   {
      return activity;
   }

   public ProcessDefinition getProcessDefinition()
   {
      return processDefinition;
   }

   /**
    * @author Shrikant.Gangal
    *
    */
   public class ActivityEntryCriticalitySearchhandler implements ISearchHandler
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
               IActivityEntry ae = selectedCriticalityStats.getStatisiticsForActivity(
                     processDefinition.getQualifiedId(), activity.getQualifiedId());
               if (null != ae && ae.getInstances().iterator().hasNext())
               {
                  FilterTerm orTerm = filter.addOrTerm();
                  for (Iterator<Long> iter = ae.getInstances().iterator(); iter.hasNext();)
                  {
                     orTerm.add(ActivityInstanceQuery.OID.isEqual(iter.next().longValue()));
                  }
               }
               else
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
