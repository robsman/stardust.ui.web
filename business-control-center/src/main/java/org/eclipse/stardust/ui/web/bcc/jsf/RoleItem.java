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
/**
 * 
 */
package org.eclipse.stardust.ui.web.bcc.jsf;

import java.io.Serializable;

import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.PerformingParticipantFilter;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;



public class RoleItem implements Serializable
{
   private final static long serialVersionUID = 1l;
   
   private QualifiedModelParticipantInfo role;

   private long worklistCount;

   private long userCount;

   private long loggedInUserCount;

   private String roleName;
   
   public RoleItem(ModelParticipantInfo role)
   {
      this.role = (QualifiedModelParticipantInfo)role;
      // computeWorklistItemCount();
   }

   public void computeWorklistItemCount()
   {
      worklistCount = 0;
      ActivityInstanceQuery query = ActivityInstanceQuery
            .findInState(new ActivityInstanceState[] {
                  ActivityInstanceState.Application, ActivityInstanceState.Created,
                  ActivityInstanceState.Hibernated, ActivityInstanceState.Interrupted,
                  ActivityInstanceState.Suspended});
      query.getFilter()
            .and(PerformingParticipantFilter.forParticipant(role));
      try
      {
         WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
         worklistCount = facade.getActivityInstancesCount(query);
         /*
          * Collection<ProcessDefinition> pdList = facade.getAllProcessDefinitions();
          * for(ProcessDefinition pd : pdList) { List<ActivityInstanceWithPrio> ais =
          * facade.getAliveActivityInstances(pd); for(ActivityInstanceWithPrio ai : ais) {
          * Activity a = ai.getActivityInstance().getActivity(); String roleName =
          * ai.getActivityInstance().getParticipantPerformerID(); if
          * ((a.getImplementationType().equals(ImplementationType.Manual) ||
          * a.getImplementationType().equals(ImplementationType.Application)) ) {
          * ++worklistCount; } } }
          */
      }
      catch (InvalidServiceException e)
      {
         PageMessage.setMessage(e);
      }
   }

   public QualifiedModelParticipantInfo getRole()
   {
      return role;
   }

   public String getRoleName()
   {
      if(roleName == null)
      {
         roleName = ModelHelper.getParticipantName(role);
      }
      
      return roleName;
   }

   public String getRoleDescription()
   {
      // TODO: Fix this!
//      String defaultDesc = role instanceof ModelParticipantDetails
//            ? ((ModelParticipantDetails) role).getDescription()
//            : null;
//      return I18nUtils.getParticipantDescription((Participant) role, defaultDesc);
      return getRoleName();
   }

   public long getEntriesPerUser()
   {
      return getUserCount() > 0 ? getWorklistCount() / getUserCount() : 0;
   }

   public long getWorklistCount()
   {
      return worklistCount;
   }

   public void addWorklistEntry(long count)
   {
      worklistCount += count;
   }

   public void removeWorklistEntry(long count)
   {
      worklistCount -= count;
      worklistCount = worklistCount > 0 ? worklistCount : 0;
   }

   public long getUserCount()
   {
      return userCount;
   }

   public void addUser(long count)
   {
      userCount += count;
   }

   public void removeUser(long count)
   {
      userCount -= count;
      userCount = userCount < 0 ? 0 : userCount;
   }

   public long getLoggedInUserCount()
   {
      return loggedInUserCount;
   }

   public void addLoggedInUser(long count)
   {
      loggedInUserCount += count;
   }

   public void removeLoggedInUser(long count)
   {
      loggedInUserCount -= count;
      loggedInUserCount = loggedInUserCount < 0 ? 0 : loggedInUserCount;
   }

   /**
    * @return
    */
   public String getUniqueKey()
   {
      return ParticipantUtils.getParticipantUniqueKey(role);
   }

   // @Override
   public boolean equals(Object obj)
   {
      boolean isEqual = false;
      if (obj == this)
      {
         isEqual = true;
      }
      else if (obj instanceof Grant)
      {
         Grant grant = (Grant) obj;
         Department grantDepartment = grant.getDepartment();
         long grantDepartmentOID = (grantDepartment == null) ? 0 : (grantDepartment.getOID());
         
         DepartmentInfo departmentInfo = role.getDepartment();
         long departmentOID = (departmentInfo == null) ? 0 : (departmentInfo.getOID());
         
         isEqual = (role.getQualifiedId().compareTo(grant.getQualifiedId()) == 0) && (grantDepartmentOID == departmentOID);
      }
      else if (obj instanceof Role ||
            obj instanceof Organization)
      {
         isEqual = role.equals(obj);
      }
      else if (obj instanceof RoleItem)
      {
         isEqual = role.equals(((RoleItem) obj).getRole());
      }

      return isEqual;
   }
}