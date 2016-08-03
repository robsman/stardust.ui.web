/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceHierarchyFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.bcc.ProcessSearchProvider;
import org.eclipse.stardust.ui.web.rest.component.util.ProcessActivityUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;

/**
 * @author Aditya.Gaikwad
 * @version $Revision: $
 */
public class FilterAttributesDTO implements Serializable
{
   private static final long serialVersionUID = 1L;

   private int state;

   private Long oid;

   private Long rootOid;

   private Date startedFrom;
   private Date startedTo;
   private Date endTimeFrom;
   private Date endTimeTo;

   private boolean caseOnlySearch;
   private boolean includeRootProcess;
   private User user;
   private int priority;
   private boolean includeCaseSearch;

   /**
    * 
    */
   public FilterAttributesDTO()
   {
      state = ProcessActivityUtils.PROCESS_INSTANCE_STATE_ALIVE;
      priority = ProcessActivityUtils.ALL_PRIORITIES;
   }

   /**
    * @return ProcessInstanceQuery
    */
   protected Query buildQuery()
   {

      Query query;

      // Case search by ActivityInstanceQuery
      if (caseOnlySearch && null != user)
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

      // else create ProcessInstanceQuery

      else if (state == ProcessActivityUtils.PROCESS_INSTANCE_STATE_ALIVE)
      {
         query = ProcessInstanceQuery.findInState(new ProcessInstanceState[] {
               ProcessInstanceState.Active, ProcessInstanceState.Interrupted, ProcessInstanceState.Aborting});
      }
      else if (state == ProcessActivityUtils.PROCESS_INSTANCE_STATE_COMPLETED)
      {
         query = ProcessInstanceQuery.findInState(ProcessInstanceState.Completed);
      }
      else if (state == ProcessActivityUtils.PROCESS_INSTANCE_STATE_ABORTED)
      {
         query = ProcessInstanceQuery.findInState(ProcessInstanceState.Aborted);
      }
      else if (state == ProcessActivityUtils.PROCESS_INSTANCE_STATE_INTERRUPTED)
      {
         query = ProcessInstanceQuery.findInState(ProcessInstanceState.Interrupted);
      }
      else if (state == ProcessActivityUtils.PROCESS_INSTANCE_STATE_ABORTING)
      {
         query = ProcessInstanceQuery.findInState(ProcessInstanceState.Aborting);
      }
      else if (state == ProcessActivityUtils.PROCESS_INSTANCE_STATE_HALTING)
      {
         query = ProcessInstanceQuery.findInState(ProcessInstanceState.Halting);
      }
      else
      {
         query = ProcessInstanceQuery.findInState(new ProcessInstanceState[] {
               ProcessInstanceState.Active, ProcessInstanceState.Completed, ProcessInstanceState.Interrupted,
               ProcessInstanceState.Aborted, ProcessInstanceState.Aborting,  ProcessInstanceState.Aborting,  ProcessInstanceState.Halting});
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
         filter.and(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.isEqual(rootOid.longValue()));
      }
      if (ProcessActivityUtils.ALL_PRIORITIES != priority)
      {
         filter.and(ProcessInstanceQuery.PRIORITY.isEqual(priority));
      }

      if (!includeCaseSearch)
      {
         FilterTerm orFilter = filter.addOrTerm();
         ProcessDefinition caseProcessDefination = ModelCache.findModelCache().getCaseProcessDefination();
         orFilter
               .add(ProcessInstanceQuery.PROCESS_DEFINITION_OID.notEqual(caseProcessDefination.getRuntimeElementOID()));
      }

      if (includeRootProcess)
      {
         filter.and(ProcessInstanceHierarchyFilter.ROOT_PROCESS);
      }
      if (caseOnlySearch)
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

   public boolean isCaseOnlySearch()
   {
      return caseOnlySearch;
   }

   public void setCaseOnlySearch(boolean caseOnlySearch)
   {
      this.caseOnlySearch = caseOnlySearch;
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

   public void setIncludeCaseSearch(boolean includeCaseSearch)
   {
      this.includeCaseSearch = includeCaseSearch;
   }

   public boolean isIncludeCaseSearch()
   {
      return includeCaseSearch;
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
      case ProcessActivityUtils.PROCESS_INSTANCE_STATE_ALIVE:
         query = ActivityInstanceQuery.findAlive();
         break;
      case ProcessActivityUtils.PROCESS_INSTANCE_STATE_COMPLETED:
         query = ActivityInstanceQuery.findInState(ActivityInstanceState.Completed);
         break;
      case ProcessActivityUtils.PROCESS_INSTANCE_STATE_ABORTED:
         query = ActivityInstanceQuery.findInState(ActivityInstanceState.Aborted);
         break;
      case ProcessActivityUtils.PROCESS_INSTANCE_STATE_INTERRUPTED:
         query = ActivityInstanceQuery.findInState(ActivityInstanceState.Interrupted);
         break;
      default:
         query = ActivityInstanceQuery.findAll();
         break;

      }
      return query;
   }
}
