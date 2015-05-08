/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.Date;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.rest.service.utils.ProcessActivityUtils;

/**
 * @author Aditya.Gaikwad
 * @version $Revision: $
 */
public class ActivityFilterAttributesDTO
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
   private int priority = ProcessActivityUtils.ALL_PRIORITIES;

   public ActivityInstanceQuery buildQuery()
   {

      ActivityInstanceQuery query;

      if (state == ProcessActivityUtils.ACTIVITY_INSTANCE_STATE_ALIVE)
      {
         query = ActivityInstanceQuery.findAlive();
      }
      else if (state == ProcessActivityUtils.ACTIVITY_INSTANCE_STATE_COMPLETED)
      {
         query = ActivityInstanceQuery.findInState(ActivityInstanceState.Completed);
      }
      else if (state == ProcessActivityUtils.ACTIVITY_INSTANCE_STATE_ABORTED)
      {
         query = ActivityInstanceQuery.findInState(ActivityInstanceState.Aborted);
      }
      else if (state == ProcessActivityUtils.ACTIVITY_INSTANCE_STATE_SUSPENDED)
      {
         query = ActivityInstanceQuery.findInState(ActivityInstanceState.Suspended);
      }
      else if (state == ProcessActivityUtils.ACTIVITY_INSTANCE_STATE_HIBERNATED)
      {
         query = ActivityInstanceQuery.findInState(ActivityInstanceState.Hibernated);
      }
      else if (state == ProcessActivityUtils.ACTIVITY_INSTANCE_STATE_ABORTING)
      {
         query = ActivityInstanceQuery.findInState(ActivityInstanceState.Aborting);
      }
      else if (state == ProcessActivityUtils.ACTIVITY_INSTANCE_STATE_CREATED)
      {
         query = ActivityInstanceQuery.findInState(ActivityInstanceState.Created);
      }
      else if (state == ProcessActivityUtils.ACTIVITY_INSTANCE_STATE_APPLICATION)
      {
         query = ActivityInstanceQuery.findInState(ActivityInstanceState.Application);
      }
      else if (state == ProcessActivityUtils.ACTIVITY_INSTANCE_STATE_INTERRUPTED)
      {
         query = ActivityInstanceQuery.findInState(ActivityInstanceState.Interrupted);
      }
      else
      {
         query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
               ActivityInstanceState.Interrupted, ActivityInstanceState.Aborting, ActivityInstanceState.Suspended,
               ActivityInstanceState.Completed, ActivityInstanceState.Created, ActivityInstanceState.Hibernated,
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
      if (ProcessActivityUtils.ALL_PRIORITIES != priority)
      {
         filter.add(ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY.isEqual(priority));
      }
      if (user != null)
      {
         filter.add(new PerformingUserFilter(user.getOID()));
      }
      
      //TODO
/*      if (StringUtils.isNotEmpty(criticality)
            && !criticality.equals("ALL"))
      {
         CriticalityCategory cCat = CriticalityConfigurationUtil.getCriticalityForLabel(criticality);
         if (null != cCat)
         {
            filter.and(ActivityInstanceQuery.CRITICALITY.between(
                  CriticalityConfigurationUtil.getEngineCriticality(cCat.getRangeFrom()),
                  CriticalityConfigurationUtil.getEngineCriticality(cCat.getRangeTo())));
         }
      }*/
      query.where(filter);
      return query;
   }

   /**
    * 
    */
   public ActivityFilterAttributesDTO()
   {
      state = ProcessActivityUtils.ACTIVITY_INSTANCE_STATE_ALIVE;
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
