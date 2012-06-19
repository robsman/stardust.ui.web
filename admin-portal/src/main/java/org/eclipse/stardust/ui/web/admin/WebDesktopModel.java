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
package org.eclipse.stardust.ui.web.admin;

import java.io.Serializable;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.UserGroupQuery;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;



public class WebDesktopModel implements Serializable
{
   private final static long serialVersionUID = 1l;
   
   private ServiceFactory serviceFactory;
   
   private Long totalUsersCount;
   private Long activeUsersCount;
   private Long totalUserGroupsCount;
   private Long activeUserGroupsCount;

   private Long totalProcessInstancesCount;
   private Long activeProcessInstancesCount;
   private Long pendingProcessInstancesCount;
   private Long interruptedProcessInstancesCount;
   private Long completedProcessInstancesCount;
   private Long abortedProcessInstancesCount;

   private Long totalActivityInstancesCount;
   private Long activeActivityInstancesCount;
   private Long pendingActivityInstancesCount;
   private Long completedActivityInstancesCount;
   private Long interruptedActivityInstancesCount;
   private Long suspendedActivityInstancesCount;
   private Long hibernatedActivityInstancesCount;
   private Long abortedActivityInstancesCount;

   public WebDesktopModel(ServiceFactory serviceFactory)
   {
      this.serviceFactory = serviceFactory;
   }

   public ServiceFactory getServiceFactory()
   {
      return serviceFactory;
   }

   public long getTotalUsersCount()
   {
      totalUsersCount = getUsersCount(totalUsersCount, UserQuery.findAll());

      return totalUsersCount.longValue();
   }

   public long getActiveUsersCount()
   {
      activeUsersCount = getUsersCount(activeUsersCount, UserQuery.findActive());

      return activeUsersCount.longValue();
   }

   public long getTotalUserGroupsCount()
   {
      totalUserGroupsCount = getUserGroupsCount(totalUserGroupsCount, UserGroupQuery.findAll());

      return totalUserGroupsCount.longValue();
   }

   public long getActiveUserGroupsCount()
   {
      activeUserGroupsCount = getUserGroupsCount(activeUserGroupsCount, UserGroupQuery.findActive());

      return activeUserGroupsCount.longValue();
   }

   public long getTotalProcessInstancesCount()
   {
      totalProcessInstancesCount = getProcessInstancesCount(totalProcessInstancesCount,
            ProcessInstanceQuery.findAll());

      return totalProcessInstancesCount.longValue();
   }

   public long getActiveProcessInstancesCount()
   {
      activeProcessInstancesCount = getProcessInstancesCount(activeProcessInstancesCount,
            ProcessInstanceQuery.findActive());

      return activeProcessInstancesCount.longValue();
   }

   public long getPendingProcessInstancesCount()
   {
      pendingProcessInstancesCount = getProcessInstancesCount(pendingProcessInstancesCount,
            ProcessInstanceQuery.findInterrupted());

      return pendingProcessInstancesCount.longValue();
   }

   public long getInterruptedProcessInstancesCount()
   {
      interruptedProcessInstancesCount = getProcessInstancesCount(interruptedProcessInstancesCount,
            ProcessInstanceQuery.findInterrupted());

      return interruptedProcessInstancesCount.longValue();
   }

   public long getCompletedProcessInstancesCount()
   {
      completedProcessInstancesCount = getProcessInstancesCount(completedProcessInstancesCount,
            ProcessInstanceQuery.findCompleted());

      return completedProcessInstancesCount.longValue();
   }

   public long getAbortedProcessInstancesCount()
   {
      abortedProcessInstancesCount = getProcessInstancesCount(abortedProcessInstancesCount,
            ProcessInstanceQuery.findInState(ProcessInstanceState.Aborted));

      return abortedProcessInstancesCount.longValue();
   }

   public long getTotalActivityInstancesCount()
   {
      totalActivityInstancesCount = getActivityInstancesCount(totalActivityInstancesCount,
            ActivityInstanceQuery.findAll());

      return totalActivityInstancesCount.longValue();
   }

   public long getActiveActivityInstancesCount()
   {
      activeActivityInstancesCount = getActivityInstancesCount(activeActivityInstancesCount,
            ActivityInstanceQuery.findInState(ActivityInstanceState.Application));

      return activeActivityInstancesCount.longValue();
   }

   public long getPendingActivityInstancesCount()
   {
      ActivityInstanceQuery query = ActivityInstanceQuery
            .findInState(new ActivityInstanceState[] {
                  ActivityInstanceState.Interrupted, ActivityInstanceState.Suspended,
                  ActivityInstanceState.Hibernated});
      pendingActivityInstancesCount = getActivityInstancesCount(
            pendingActivityInstancesCount, query);

      return pendingActivityInstancesCount.longValue();
   }

   public long getCompletedActivityInstancesCount()
   {
      completedActivityInstancesCount = getActivityInstancesCount(completedActivityInstancesCount,
            ActivityInstanceQuery.findCompleted());

      return completedActivityInstancesCount.longValue();
   }

   public long getAbortedActivityInstancesCount()
   {
      abortedActivityInstancesCount = getActivityInstancesCount(abortedActivityInstancesCount,
            ActivityInstanceQuery.findInState(ActivityInstanceState.Aborted));
      
      return abortedActivityInstancesCount.longValue();
   }

   public long getInterruptedActivitiyInstancesCount()
   {
      interruptedActivityInstancesCount = getActivityInstancesCount(interruptedActivityInstancesCount,
            ActivityInstanceQuery.findInState(ActivityInstanceState.Interrupted));

      return interruptedActivityInstancesCount.longValue();
   }

   public long getSuspendedActivityInstancesCount()
   {
      suspendedActivityInstancesCount = getActivityInstancesCount(suspendedActivityInstancesCount,
            ActivityInstanceQuery.findInState(ActivityInstanceState.Suspended));

      return suspendedActivityInstancesCount.longValue();
   }

   public long getHibernatedActivityInstancesCount()
   {
      hibernatedActivityInstancesCount = getActivityInstancesCount(hibernatedActivityInstancesCount,
            ActivityInstanceQuery.findInState(ActivityInstanceState.Hibernated));

      return hibernatedActivityInstancesCount.longValue();
   }

   public void reset()
   {
      totalUsersCount = null;
      activeUsersCount = null;
      totalUserGroupsCount = null;
      activeUserGroupsCount = null;

      totalProcessInstancesCount = null;
      activeProcessInstancesCount = null;
      pendingProcessInstancesCount = null;
      interruptedProcessInstancesCount = null;
      completedProcessInstancesCount = null;
      abortedProcessInstancesCount = null;

      totalActivityInstancesCount = null;
      activeActivityInstancesCount = null;
      pendingActivityInstancesCount = null;
      completedActivityInstancesCount = null;
      interruptedActivityInstancesCount = null;
      suspendedActivityInstancesCount = null;
      hibernatedActivityInstancesCount = null;
      abortedActivityInstancesCount = null;
   }

   private Long getUsersCount(Long count, UserQuery query)
   {
      if (null == count)
      {
         QueryService service = serviceFactory.getQueryService();
         count = new Long(service.getUsersCount(query));
      }
      return count;
   }

   private Long getUserGroupsCount(Long count, UserGroupQuery query)
   {
      if (null == count)
      {
         QueryService service = serviceFactory.getQueryService();
         count = new Long(service.getUserGroupsCount(query));
      }
      return count;
   }

   private Long getProcessInstancesCount(Long count, ProcessInstanceQuery query)
   {
      if (null == count)
      {
         QueryService service = serviceFactory.getQueryService();
         count = new Long(service.getProcessInstancesCount(query));
      }
      return count;
   }

   private Long getActivityInstancesCount(Long count, ActivityInstanceQuery query)
   {
      if (null == count)
      {
         QueryService service = serviceFactory.getQueryService();

         count = new Long(service.getActivityInstancesCount(query));
      }
      return count;
   }
}
