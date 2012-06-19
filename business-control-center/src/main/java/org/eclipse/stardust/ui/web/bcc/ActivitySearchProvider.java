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
package org.eclipse.stardust.ui.web.bcc;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.bcc.views.ProcessSearchBean;
import org.eclipse.stardust.ui.web.common.table.ISearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DataMappingWrapper;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;





/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class ActivitySearchProvider implements Serializable
{
   public final static int ACTIVITY_INSTANCE_STATE_ALIVE = 1;

   public final static int ACTIVITY_INSTANCE_STATE_COMPLETED = 2;

   public final static int ACTIVITY_INSTANCE_STATE_ABORTED = 3;

   public final static int ACTIVITY_INSTANCE_STATE_SUSPENDED = 4;

   public final static int ACTIVITY_INSTANCE_STATE_HIBERNATED = 5;

   public final static int ACTIVITY_INSTANCE_STATE_ABORTING = 6;

   public final static int ACTIVITY_INSTANCE_STATE_CREATED = 7;

   public final static int ACTIVITY_INSTANCE_STATE_APPLICATION = 8;

   public final static int ACTIVITY_INSTANCE_STATE_INTERRUPTED = 9;

   public final static int ACTIVITY_INSTANCE_STATE_ALL = 10;

   private ActivityFilterAttributes filterAttributes;

   private List<Activity> selectedActivities;

   private List<DataMappingWrapper> descriptorItems = CollectionUtils.newArrayList();
   
   private DataPath[] commonDescriptors;

   /**
    * 
    */
   public ActivitySearchProvider()
   {
   }

   public ActivityFilterAttributes getFilterAttributes()
   {
      if (filterAttributes == null)
      {
         filterAttributes = new ActivityFilterAttributes();
      }
      return filterAttributes;

   }

   public void setFilterAttributes(ActivityFilterAttributes filterAttributes)
   {
      this.filterAttributes = filterAttributes;
   }

   public List<Activity> getSelectedActivities()
   {
      return selectedActivities;
   }

   /**
    * @param selectedActivities
    * @param descriptorItems
    * @param commonDescriptors
    */
   public void setSelectedActivities(List<Activity> selectedActivities, List<DataMappingWrapper> descriptorItems,
         DataPath[] commonDescriptors)
   {
      this.selectedActivities = selectedActivities;
      this.descriptorItems = descriptorItems;
      this.commonDescriptors = commonDescriptors;
   }

   public ISearchHandler<ActivityInstance> getSearchHandler()
   {
      return new ActivitySearchHandler();
   }

   /**
    * @author Ankita.Patel
    * @version $Revision: $
    */
   public static class ActivityFilterAttributes implements Serializable
   {
      private static final long serialVersionUID = -8229492654602396887L;

      private int state;

      private Long activityOID;
      
      private User user;
      private Date startedFrom;
      private Date startedTo;
      private Date modifyTimeFrom;
      private Date modifyTimeTo;
      private String criticality;
      private int priority = ProcessSearchProvider.ALL_PRIORITIES;

      protected ActivityInstanceQuery buildQuery()
      {

         ActivityInstanceQuery query;
         
         if (state == ACTIVITY_INSTANCE_STATE_ALIVE)
         {
            query = ActivityInstanceQuery.findAlive();
         }
         else if (state == ACTIVITY_INSTANCE_STATE_COMPLETED)
         {
            query = ActivityInstanceQuery.findInState(ActivityInstanceState.Completed);
         }
         else if (state == ACTIVITY_INSTANCE_STATE_ABORTED)
         {
            query = ActivityInstanceQuery.findInState(ActivityInstanceState.Aborted);
         }
         else if (state == ACTIVITY_INSTANCE_STATE_SUSPENDED)
         {
            query = ActivityInstanceQuery.findInState(ActivityInstanceState.Suspended);
         }
         else if (state == ACTIVITY_INSTANCE_STATE_HIBERNATED)
         {
            query = ActivityInstanceQuery.findInState(ActivityInstanceState.Hibernated);
         }
         else if (state == ACTIVITY_INSTANCE_STATE_ABORTING)
         {
            query = ActivityInstanceQuery.findInState(ActivityInstanceState.Aborting);
         }
         else if (state == ACTIVITY_INSTANCE_STATE_CREATED)
         {
            query = ActivityInstanceQuery.findInState(ActivityInstanceState.Created);
         }
         else if (state == ACTIVITY_INSTANCE_STATE_APPLICATION)
         {
            query = ActivityInstanceQuery.findInState(ActivityInstanceState.Application);
         }
         else if (state == ACTIVITY_INSTANCE_STATE_INTERRUPTED)
         {
            query = ActivityInstanceQuery.findInState(ActivityInstanceState.Interrupted);
         }
         else
         {
            query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
                  ActivityInstanceState.Interrupted, ActivityInstanceState.Aborting,
                  ActivityInstanceState.Suspended, ActivityInstanceState.Completed,
                  ActivityInstanceState.Created, ActivityInstanceState.Hibernated,
                  ActivityInstanceState.Aborted, ActivityInstanceState.Application});
         }
         FilterAndTerm filter = query.getFilter().addAndTerm();

         if (null != startedFrom && null != startedTo)
         {
            filter.and(ActivityInstanceQuery.START_TIME.between(startedFrom.getTime(), startedTo.getTime()));
         }
         else if (startedTo != null)
         {
            filter.and(ActivityInstanceQuery.START_TIME.lessOrEqual(startedTo.getTime()));
         }
         else if (startedFrom != null)
         {
            filter.and(ActivityInstanceQuery.START_TIME.greaterOrEqual(startedFrom.getTime()));
         }
         if (null != modifyTimeFrom && null != modifyTimeTo)
         {
            filter.and(ActivityInstanceQuery.LAST_MODIFICATION_TIME.between(modifyTimeFrom.getTime(),
                  modifyTimeTo.getTime()));
         }
         else if (modifyTimeTo != null)
         {
            filter.and(ActivityInstanceQuery.LAST_MODIFICATION_TIME.lessOrEqual(modifyTimeTo.getTime()));
         }
         else if (modifyTimeFrom != null)
         {
            filter.and(ActivityInstanceQuery.LAST_MODIFICATION_TIME.greaterOrEqual(modifyTimeFrom.getTime()));
         }
         
         if (activityOID != null)
         {
            filter.and(ActivityInstanceQuery.OID.isEqual(activityOID.longValue()));
         }
         if (ProcessSearchProvider.ALL_PRIORITIES != priority)
         {
            filter.add(ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY.isEqual(priority));
         }
         if (user != null)
         {
            filter.add(new PerformingUserFilter(user.getOID()));
         }
         if (StringUtils.isNotEmpty(criticality)
               && !criticality.equals(ProcessSearchBean.getInstance().getMessages()
                     .getString("chooseProcess.options.all.label")))
         {
            CriticalityCategory cCat = CriticalityConfigurationUtil.getCriticalityForLabel(criticality);
            if (null != cCat)
            {
               filter.and(ActivityInstanceQuery.CRITICALITY.between(
                     CriticalityConfigurationUtil.getEngineCriticality(cCat.getRangeFrom()),
                     CriticalityConfigurationUtil.getEngineCriticality(cCat.getRangeTo())));
            }
         }
         query.where(filter);
         return query;
      }

      /**
       * 
       */
      public ActivityFilterAttributes()
      {
         state = ACTIVITY_INSTANCE_STATE_ALIVE;
//         startedTo = new Date();
//         modifyTimeTo = new Date();
      }

      public int getState()
      {
         return state;
      }

      public void setState(int state)
      {
         this.state = state;
      }

      public String getCriticality()
      {
         return criticality;
      }

      public void setCriticality(String criticality)
      {
         this.criticality = criticality;
      }

      public Long getActivityOID()
      {
         return activityOID;
      }

      public void setActivityOID(Long activityOID)
      {
         this.activityOID = activityOID;
      }

      public User getUser()
      {
         return user;
      }

      public void setUser(User user)
      {
         this.user = user;
      }

      public Date getStartedFrom()
      {
         return startedFrom;
      }

      public void setStartedFrom(Date startedFrom)
      {
         this.startedFrom = startedFrom;
      }

      public Date getStartedTo()
      {
         return startedTo;
      }

      public void setStartedTo(Date startedTo)
      {
         this.startedTo = startedTo;
      }

      public Date getModifyTimeFrom()
      {
         return modifyTimeFrom;
      }

      public void setModifyTimeFrom(Date modifyTimeFrom)
      {
         this.modifyTimeFrom = modifyTimeFrom;
      }

      public Date getModifyTimeTo()
      {
         return modifyTimeTo;
      }

      public void setModifyTimeTo(Date modifyTimeTo)
      {
         this.modifyTimeTo = modifyTimeTo;
      }

      public int getPriority()
      {
         return priority;
      }

      public void setPriority(int priority)
      {
         this.priority = priority;
      }     
      

   }

   /**
    * @param query
    */
   private void applyFilter(Query query)
   {
      List<Activity> activities = getSelectedActivities();
      List<ProcessDefinition> pdsList = CollectionUtils.newArrayList();
      Model model = null;
      boolean applyCaseFilter = false;

      if (activities != null && !CollectionUtils.isEmpty(activities))
      {
         for (Activity activity : activities)
         {
            model = ModelCache.findModelCache().getModel(activity.getModelOID());
            pdsList.add(model.getProcessDefinition(activity.getProcessDefinitionId()));
         }
      }
      if (pdsList != null && !CollectionUtils.isEmpty(pdsList))
      {
         //TODO: Review following code later
         ProcessSearchBean searchBean = ProcessSearchBean.getInstance();
         if (CollectionUtils.isNotEmpty(searchBean.getSelectedProcesses()))
         {
            int arraySize = searchBean.getSelectedProcesses().length;
            if (arraySize == 1)
            {
               String selectedProcess = searchBean.getSelectedProcesses()[0];
               ProcessDefinition pd = searchBean.getProcessDefinitions().get(selectedProcess);
               if (null != pd && PredefinedConstants.CASE_PROCESS_ID.equals(pd.getId()))
               {
                  FilterAndTerm filter = query.getFilter().addAndTerm();
                  applyCaseFilter = true;
                  filter = DescriptorFilterUtils.createCaseDescriptors(descriptorItems, filter);
               }
            }
         }
         if (!applyCaseFilter)
         {
            DataPath[] commonDescriptors = CommonDescriptorUtils.getCommonDescriptors(pdsList, true);
            DescriptorFilterUtils.evaluateAndApplyFilters(query, descriptorItems, commonDescriptors);
         }

      }
   }
   
   /**
    * @author Ankita.Patel
    * @version $Revision: $
    */
   public class ActivitySearchHandler extends IppSearchHandler<ActivityInstance>
   {
      private static final long serialVersionUID = 1L;

      @Override
      public Query createQuery()
      {
         ActivityInstanceQuery query;
         
         query = filterAttributes.buildQuery();
         
         FilterTerm filter = query.getFilter().addOrTerm();

         if (CollectionUtils.isNotEmpty(selectedActivities))
         {
            for (Activity activity : selectedActivities)
            {
               filter.add(ActivityInstanceQuery.ACTIVITY_OID.isEqual(activity.getRuntimeElementOID()));
            }
         }
         else
         {
            // For Case PI search, selectedActivities will be null
            ProcessSearchBean processSearch = ProcessSearchBean.getInstance();
            if (processSearch.getFilterAttributes().isIncludeCase())
            {
               filter.add(ActivityFilter.forProcess(PredefinedConstants.DEFAULT_CASE_ACTIVITY_ID,
                     PredefinedConstants.CASE_PROCESS_ID, false));
            }
            else
            {  
               //Adding a dummy filter which will guarantee return of no activities
               //As an activity id will never be -1
               filter.add(ActivityFilter.forAnyProcess("-1"));
            }
         }
         
         applyFilter(query);
         
         return query;
      }

      @Override
      public QueryResult<ActivityInstance> performSearch(Query query)
      {
         return allActivityInstances((ActivityInstanceQuery) query);
      }

      private QueryResult<ActivityInstance> allActivityInstances(ActivityInstanceQuery query)
      {
         QueryResult<ActivityInstance> result = null;
         SessionContext sessionContext = SessionContext.findSessionContext();
         ServiceFactory serviceFactory = (null != sessionContext) ? sessionContext
               .getServiceFactory() : null;
         if (serviceFactory != null)
         {
            result = serviceFactory.getQueryService().getAllActivityInstances(query);
         }
         return result;
      }
   }
}
