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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.ProcessDefinitionDetails;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.CasePolicy;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceHierarchyFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.RawQueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.table.ISearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DataMappingWrapper;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;




/**
 * @author Giridhara.G
 * @version
 */
public class ProcessSearchProvider implements Serializable
{
   private static final long serialVersionUID = 1L;

   public static final String SELECTED_PROCESS_INSTANCES = "/selectedProcessInstances";

   public final static int PROCESS_INSTANCE_STATE_ALIVE = 1;

   public final static int PROCESS_INSTANCE_STATE_COMPLETED = 2;

   public final static int PROCESS_INSTANCE_STATE_ABORTED = 3;

   public final static int PROCESS_INSTANCE_STATE_INTERRUPTED = 4;

   public final static int PROCESS_INSTANCE_STATE_ALL = 5;
   
   public static final int ALL_PRIORITIES = -9999;

   private FilterAttributes filterAttributes;

   private List selectedProcesses = Collections.EMPTY_LIST;

   private List<DataMappingWrapper> descriptorItems = new ArrayList<DataMappingWrapper>();

   private DataPath[] commonDescriptors;

   /**
    * @param portalId
    */
   public ProcessSearchProvider()
   {
      filterAttributes = getFilterAttributes();
   }

   /**
    * @return FilterAttributes
    */
   public FilterAttributes getFilterAttributes()
   {
      if (filterAttributes == null)
      {
         filterAttributes = new FilterAttributes();
      }
      return filterAttributes;
   }   

   public void setFilterAttributes(FilterAttributes filterAttributes)
   {
      this.filterAttributes = filterAttributes;
   }

   /**
    * @return
    */
   public ISearchHandler<ProcessInstance> getSearchHandler()
   {
      return new ProcessSearchHandler();
   }

   public static class FilterAttributes implements Serializable
   {
      private static final long serialVersionUID = 1L;

      private int state;

      private Long oid;

      private Long rootOid;
      
      private Date startedFrom;
      private Date startedTo;
      private Date endTimeFrom;
      private Date endTimeTo;
      
      private boolean includeCase;
      private boolean includeRootProcess;
      private User user;
      private int priority;

      /**
       * 
       */
      public FilterAttributes()
      {
         state = PROCESS_INSTANCE_STATE_ALIVE;
         priority = ALL_PRIORITIES;
         //startedTo = new Date();
         //endTimeTo = new Date();
      }

      /**
       * @return ProcessInstanceQuery
       */
      protected Query buildQuery()
      {

         Query query;
         
         // Case search by ActivityInstanceQuery
         if (includeCase && null != user)
         {
            query = getActivityQueryByProcessState(state);
            FilterAndTerm filter = query.getFilter().addAndTerm();
            if (null != oid)
            {
               filter.and(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(oid));
            }
            filter.add(new PerformingUserFilter(user.getOID()));

            filter.add(ActivityFilter.forProcess(PredefinedConstants.DEFAULT_CASE_ACTIVITY_ID,
                  PredefinedConstants.CASE_PROCESS_ID, false));

            if (ProcessSearchProvider.ALL_PRIORITIES != priority)
            {
               filter.add(ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY.isEqual(priority));
            }

            if (null != startedFrom && null != startedTo)
            {
               filter.and(ActivityInstanceQuery.START_TIME.between(startedFrom.getTime(), startedTo.getTime()));
            }
            else if (null != startedTo)
            {
               filter.and(ActivityInstanceQuery.START_TIME.lessOrEqual(startedTo.getTime()));
            }
            else if (null != startedFrom)
            {
               filter.and(ActivityInstanceQuery.START_TIME.greaterOrEqual(startedFrom.getTime()));
            }

            return query;
         }
         
         //else create ProcessInstanceQuery
         
         else if (state == PROCESS_INSTANCE_STATE_ALIVE)
         {
            query = ProcessInstanceQuery.findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Active, ProcessInstanceState.Interrupted,
                  ProcessInstanceState.Aborting});
         }
         else if (state == PROCESS_INSTANCE_STATE_COMPLETED)
         {
            query = ProcessInstanceQuery.findInState(ProcessInstanceState.Completed);
         }
         else if (state == PROCESS_INSTANCE_STATE_ABORTED)
         {
            query = ProcessInstanceQuery.findInState(ProcessInstanceState.Aborted);
         }
         else if (state == PROCESS_INSTANCE_STATE_INTERRUPTED)
         {
            query = ProcessInstanceQuery.findInState(ProcessInstanceState.Interrupted);
         }
         else
         {
            query = ProcessInstanceQuery.findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Active, ProcessInstanceState.Completed,
                  ProcessInstanceState.Interrupted, ProcessInstanceState.Aborted,
                  ProcessInstanceState.Aborting});
         }
         FilterAndTerm filter = query.getFilter().addAndTerm();
        
         if (null != startedFrom && null != startedTo)
         {
            filter.and(ProcessInstanceQuery.START_TIME.between(startedFrom.getTime(), startedTo.getTime()));
         }
         else if (startedTo != null)
         {
            filter.and(ProcessInstanceQuery.START_TIME.lessOrEqual(startedTo.getTime()));
         }
         else if (startedFrom != null)
         {
            filter.and(ProcessInstanceQuery.START_TIME.greaterOrEqual(startedFrom.getTime()));
         }

         if (null != endTimeFrom && null != endTimeTo)
         {
            filter.and(ProcessInstanceQuery.TERMINATION_TIME.between(endTimeFrom.getTime(), endTimeTo.getTime()));
         }
         else if (endTimeTo != null)
         {
            filter.and(ProcessInstanceQuery.TERMINATION_TIME.notEqual(0));
            filter.and(ProcessInstanceQuery.TERMINATION_TIME.lessOrEqual(endTimeTo.getTime()));
         }
         else if (endTimeFrom != null)
         {
            filter.and(ProcessInstanceQuery.TERMINATION_TIME.greaterOrEqual(endTimeFrom.getTime()));
         }
         
         if (oid != null)
         {
            filter.and(ProcessInstanceQuery.OID.isEqual(oid.longValue()));
         }
         if (rootOid != null)
         {
            filter.and(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.isEqual(rootOid
                  .longValue()));
         }
         if (ALL_PRIORITIES != priority)
         {
            filter.and(ProcessInstanceQuery.PRIORITY.isEqual(priority));
         }
         if (includeRootProcess)
         {
            FilterTerm orFilter = filter.addOrTerm();
            ProcessDefinition caseProcessDefination = ModelCache.findModelCache().getCaseProcessDefination();
            orFilter.add(ProcessInstanceQuery.PROCESS_DEFINITION_OID.notEqual(caseProcessDefination
                  .getRuntimeElementOID()));
            filter.and(ProcessInstanceHierarchyFilter.ROOT_PROCESS);
         }
         if (includeCase)
         {
            String qualifiedGroupId = "{" + PredefinedConstants.PREDEFINED_MODEL_ID + "}"
            + PredefinedConstants.CASE_PROCESS_ID;
            filter.add(new ProcessDefinitionFilter(qualifiedGroupId, false));
         }
         return query;
      } 
     

      public Long getOid()
      {
         return oid;
      }

      public void setOid(Long oid)
      {
         this.oid = oid;
      }

      public Long getRootOid()
      {
         return rootOid;
      }

      public void setRootOid(Long rootOid)
      {
         this.rootOid = rootOid;
      }

      public int getState()
      {
         return state;
      }

      public void setState(int state)
      {
         this.state = state;
      }

      protected boolean validParameters()
      {
         return true;
      }

      public TimeZone getTimeZone()
      {
         return java.util.TimeZone.getDefault();
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

      public Date getEndTimeFrom()
      {
         return endTimeFrom;
      }

      public void setEndTimeFrom(Date endTimeFrom)
      {
         this.endTimeFrom = endTimeFrom;
      }

      public Date getEndTimeTo()
      {
         return endTimeTo;
      }

      public void setEndTimeTo(Date endTimeTo)
      {
         this.endTimeTo = endTimeTo;
      }

      public boolean isIncludeCase()
      {
         return includeCase;
      }

      public void setIncludeCase(boolean includeCase)
      {
         this.includeCase = includeCase;
      }

      public boolean isIncludeRootProcess()
      {
         return includeRootProcess;
      }

      public void setIncludeRootProcess(boolean includeRootProcess)
      {
         this.includeRootProcess = includeRootProcess;
      }

      public User getUser()
      {
         return user;
      }

      public void setUser(User user)
      {
         this.user = user;
      }

      public int getPriority()
      {
         return priority;
      }

      public void setPriority(int priority)
      {
         this.priority = priority;
      }
      
      /**
       * 
       * @param state
       * @return
       */
      private ActivityInstanceQuery getActivityQueryByProcessState(int state)
      {
         ActivityInstanceQuery query = null;
         switch (state)
         {
         case PROCESS_INSTANCE_STATE_ALIVE:
            query = ActivityInstanceQuery.findAlive();
            break;
         case PROCESS_INSTANCE_STATE_COMPLETED:
            query = ActivityInstanceQuery.findInState(ActivityInstanceState.Completed);
            break;
         case PROCESS_INSTANCE_STATE_ABORTED:
            query = ActivityInstanceQuery.findInState(ActivityInstanceState.Aborted);
            break;
         case PROCESS_INSTANCE_STATE_INTERRUPTED:
            query = ActivityInstanceQuery.findInState(ActivityInstanceState.Interrupted);
            break;
         default:
            query = ActivityInstanceQuery.findAll();
            break;

         }
         return query;
      }
      
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public class ProcessSearchHandler extends IppSearchHandler<ProcessInstance>
   {
      private static final long serialVersionUID = 1L;

      @Override
      public Query createQuery()
      {
         
         Query query = filterAttributes.buildQuery();

         FilterAndTerm filter = query.getFilter().addAndTerm();
         
         if (filterAttributes.isIncludeCase())
         {            
            filter = DescriptorFilterUtils.createCaseDescriptors(descriptorItems, filter);
         }
         else
         {
            if (CollectionUtils.isNotEmpty(selectedProcesses))
            {
               FilterTerm or = filter.addOrTerm();
               ProcessDefinition pd;
               for (Iterator iterator = selectedProcesses.iterator(); iterator.hasNext();)
               {
                  pd = (ProcessDefinition) iterator.next();
                  or.add(new ProcessDefinitionFilter(pd.getQualifiedId(), false));
               }
               ProcessDefinitionDetails processDefDetails = (ProcessDefinitionDetails) selectedProcesses.get(0);
               if (PredefinedConstants.CASE_PROCESS_ID.equals(processDefDetails.getId())
                     & selectedProcesses.size() == 1)
               {
                  filter = DescriptorFilterUtils.createCaseDescriptors(descriptorItems, filter);
               }
               else
               {
                  DescriptorFilterUtils.evaluateAndApplyFilters(query, descriptorItems, commonDescriptors);
                  if (!filterAttributes.isIncludeRootProcess())
                  {
                     query.setPolicy(CasePolicy.INCLUDE_CASES);
                  }
               }
            }
            else
            {
               filter.and(ProcessInstanceQuery.OID.isEqual(0));
            }

         }
         query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
         return query;

      }

      /**
       * 
       */
      @Override
      public QueryResult<ProcessInstance> performSearch(Query query)
      {
         if (query instanceof ProcessInstanceQuery)
         {
            return getAllProcessInstances((ProcessInstanceQuery) query);
         }
         else
         {
            QueryResult<ActivityInstance> aiResult = allActivityInstances((ActivityInstanceQuery) query);
            List<ProcessInstance> result = new ArrayList<ProcessInstance>();
            for (ActivityInstance ai : aiResult)
            {
               result.add(ai.getProcessInstance());
            }
            return new RawQueryResult<ProcessInstance>(result, null, aiResult.hasMore(), Long.valueOf(aiResult.getTotalCount()));
         }
      }

      /**
       * @param query
       * @return QueryResult
       */
      private QueryResult<ProcessInstance> getAllProcessInstances(ProcessInstanceQuery query)
      {
         QueryResult<ProcessInstance> result = null;
         SessionContext sessionContext = SessionContext.findSessionContext();
         ServiceFactory serviceFactory = (null != sessionContext) ? sessionContext
               .getServiceFactory() : null;
         if (serviceFactory != null)
         {
            result = serviceFactory.getQueryService().getAllProcessInstances(query);
         }
         return result;
      }
      
   }

   /**
    * 
    * @param query
    * @return
    */
   private QueryResult<ActivityInstance> allActivityInstances(ActivityInstanceQuery query)
   {
      QueryResult<ActivityInstance> result = null;
      SessionContext sessionContext = SessionContext.findSessionContext();
      ServiceFactory serviceFactory = (null != sessionContext) ? sessionContext.getServiceFactory() : null;
      if (serviceFactory != null)
      {
         result = serviceFactory.getQueryService().getAllActivityInstances(query);
      }
      return result;
   }

   /**
    * set selected processes and corresponding descriptors
    * 
    * @param selectedProcess
    */

   public void setSelectedProcesses(List selectedProcess, List<DataMappingWrapper> descriptorItems,
         DataPath[] commonDescriptors)
   {
      this.selectedProcesses = selectedProcess;
      this.descriptorItems = descriptorItems;
      this.commonDescriptors = commonDescriptors;
   } 
   
  
}