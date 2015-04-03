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
/**
 * @author Abhay.Thappan
 */
package org.eclipse.stardust.ui.web.rest.service.utils;

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.RoleItem;
import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.UserManagerDetailRoleDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserManagerDetailsDTO;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.stereotype.Component;

@Component
public class UserManagerDetailUtils
{

   /**
    * 
    * @param roleId
    * @param departmentOid
    * @return RoleManagerDetailsDTO
    */
   public UserManagerDetailsDTO getUserManagerDetails(String userOid)
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      UserManagerDetailsDTO userManagerDetailsDTO = new UserManagerDetailsDTO();

      UserItem userItem = facade.getUserItem(Long.parseLong(userOid));

      userManagerDetailsDTO.userOid = Long.parseLong(userOid);
      userManagerDetailsDTO.userFullName = I18nUtils.getUserLabel(userItem.getUser());
      userManagerDetailsDTO.userAccount = userItem.getUser().getAccount();
      userManagerDetailsDTO.userEmailId = userItem.getUser().getEMail();
      userManagerDetailsDTO.directCountItem = Long.toString(userItem.getDirectItemCount());
      userManagerDetailsDTO.inDirectCountItem = Long.toString(userItem.getIndirectItemCount());
      userManagerDetailsDTO.roleCount = StringUtils.isEmpty(userManagerDetailsDTO.roleCount) ? Long.toString(userItem
            .getRoleCount()) : userManagerDetailsDTO.roleCount;
      userManagerDetailsDTO.manageAuthorization = AuthorizationUtils.canManageAuthorization();

      initUserItem(facade, userItem);

      // // code to get assigned roles list
      List<RoleItem> rolesAssigned = getAssignedRoles(facade, userItem);
      List<UserManagerDetailRoleDTO> roleAssignedList = getRoleItemsAsUserManagerDetailRoleList(rolesAssigned);
      userManagerDetailsDTO.assignedRoleList = roleAssignedList;

      // code to get assignable roles list
      List<RoleItem> rolesAssignable = getAssignableRoles(facade, userItem);
      List<UserManagerDetailRoleDTO> roleAssignableList = getRoleItemsAsUserManagerDetailRoleList(rolesAssignable);
      userManagerDetailsDTO.assignableRoleList = roleAssignableList;

      return userManagerDetailsDTO;

   }

   /**
    * 
    * @param facade
    * @param userItem
    */
   private void initUserItem(WorkflowFacade facade, UserItem userItem)
   {
      if (userItem != null && !UserDetailsLevel.Full.equals(userItem.getUser().getDetailsLevel()))
      {
         UserQuery query = UserQuery.findAll();
         UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Full);
         userPolicy.setPreferenceModules(UserPreferencesEntries.M_ADMIN_PORTAL);
         query.setPolicy(userPolicy);
         query.getFilter().add(UserQuery.OID.isEqual(userItem.getUser().getOID()));

         List<User> users = facade.getAllUsers(query);
         User u = CollectionUtils.isNotEmpty(users) ? users.get(0) : null;
         if (u != null)
         {
            userItem.setUser(u);
         }

      }
   }

   /**
    * @param roles
    * @return
    */
   private List<UserManagerDetailRoleDTO> getRoleItemsAsUserManagerDetailRoleList(List<RoleItem> roles)
   {
      List<UserManagerDetailRoleDTO> roleList = CollectionUtils.newArrayList();
      if (roles != null)
      {
         for (RoleItem roleItem : roles)
         {
            roleList.add(new UserManagerDetailRoleDTO(roleItem.getRoleName(), roleItem.getRole().getId(), roleItem
                  .getWorklistCount(), roleItem.getUserCount(), roleItem.getEntriesPerUser()));
         }
      }
      return roleList;
   }

   /**
    * @param facade
    * @param userItem
    * @return
    */
   private List<RoleItem> getAssignedRoles(WorkflowFacade facade, UserItem userItem)
   {
      List<RoleItem> rolesAssigned = facade.getAllRolesExceptCasePerformer();
      List<Grant> grants = userItem.getUser().getAllGrants();
      rolesAssigned.retainAll(grants);
      return rolesAssigned;
   }

   /**
    * 
    * @param facade
    * @param userItem
    * @return
    */
   public List<RoleItem> getAssignableRoles(WorkflowFacade facade, UserItem userItem)
   {
      List<RoleItem> roles = facade.getAllRolesExceptCasePerformer();
      List<Grant> grants = userItem.getUser().getAllGrants();
      roles.removeAll(grants);
      return roles;

   }

   /**
    * Adds selected roles to User
    */
   public boolean addRoleToUser(List<String> roleIds, String userOid)
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      boolean userAuthorizationChanged = false;
      List<RoleItem> roles = CollectionUtils.newArrayList();
      UserItem user = facade.getUserItem(Long.parseLong(userOid));

      // List<UserManagerRoleAssignmentUserObject> rolesList =
      // roleAssignableTable.getList();
      for (String roleId : roleIds)
      {
         roles.add(getRoleItem(roleId, facade));
      }

      // If logged-in user has at least 1 non-team lead grant, then all participants
      // are modifiable

      if (UserUtils.hasNonTeamLeadGrant(facade.getLoginUser()))
      {
         if (CollectionUtils.isNotEmpty(roles) && (facade.addRolesToUser(user, roles) > 0))
         {
            if (UserUtils.isLoggedInUser(user.getUser()))
            {
               userAuthorizationChanged = true;
            }
         }
      }
      else
      // Else logged-in user is a team lead
      {
         // Only "team participants" are modifiable
         List<RoleItem> rolesToAdd = getTeamsRoles(roles, facade);
         if (CollectionUtils.isNotEmpty(rolesToAdd) && (facade.addRolesToUser(user, rolesToAdd) > 0))
         {
            // roleCount = Integer.toString(getAssignedRoles(user).size());
            // initialize();
            if (UserUtils.isLoggedInUser(user.getUser()))
            {
               userAuthorizationChanged = true;
            }
         }
      }
      return userAuthorizationChanged;

   }

   /**
    * Returns team's role from selected roles
    * 
    * @param roles
    * @param facade
    * @return
    */
   private List<RoleItem> getTeamsRoles(List<RoleItem> roles, WorkflowFacade facade)
   {
      List<RoleItem> rolesToAdd = CollectionUtils.newArrayList();

      List<QualifiedModelParticipantInfo> tempList = null;
      for (RoleItem roleItem : roles)
      {
         QualifiedModelParticipantInfo modelParticipantInfo = roleItem.getRole();
         tempList = CollectionUtils.newArrayList();
         tempList.add(modelParticipantInfo);
         if (UserUtils.isParticipantPartofTeam(facade.getLoginUser(), tempList))
         {
            rolesToAdd.add(facade.getRoleItem(modelParticipantInfo));
         }
      }

      return rolesToAdd;
   }

   /**
    * @param roleId
    * @param facade
    * @param departmentOid
    * @return
    */
   private RoleItem getRoleItem(String roleId, WorkflowFacade facade)
   {
      ModelParticipant participant = (ModelParticipant) ModelCache.findModelCache().getParticipant(roleId);
      RoleItem roleItem = facade.getRoleItem(participant);
      return roleItem;
   }

   /**
    * 
    * @param roleIds
    * @param userOid
    * @return
    */
   public boolean removeRoleFromUser(List<String> roleIds, String userOid)
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      boolean userAuthorizationChanged = false;
      List<RoleItem> roles = CollectionUtils.newArrayList();
      UserItem user = facade.getUserItem(Long.parseLong(userOid));

      for (String roleId : roleIds)
      {
         roles.add(getRoleItem(roleId, facade));

      }

      // If logged-in user has at least 1 non-team lead grant, then all participants
      // are modifiable
      if (UserUtils.hasNonTeamLeadGrant(facade.getLoginUser()))
      {
         if (CollectionUtils.isNotEmpty(roles) && (facade.removeRolesFromUser(user, roles) > 0))
         {
            // roleCount = Integer.toString(getAssignedRoles(user).size());
            // initialize();
            if (UserUtils.isLoggedInUser(user.getUser()))
            {
               userAuthorizationChanged = true;
            }
         }
      }
      else
      // Else logged-in user is a team lead
      {
         // Only "team participants" are modifiable
         List<RoleItem> rolesToremove = getTeamsRoles(roles, facade);
         if (CollectionUtils.isNotEmpty(rolesToremove) && (facade.removeRolesFromUser(user, rolesToremove) > 0))
         {
            // roleCount = Integer.toString(getAssignedRoles(user).size());
            // initialize();
            if (UserUtils.isLoggedInUser(user.getUser()))
            {
               userAuthorizationChanged = true;
            }
         }
      }
      return userAuthorizationChanged;
   }

   /**
    * This method will get all activities for user
    * 
    * @param userOid
    * @param options
    * @return
    */

   public QueryResult<ActivityInstance> getAllActivitiesForUser(String userOid, Options options)
   {
      Query query = createQuery(userOid, options);
      QueryResult<ActivityInstance> result = performSearch(query);
      return result;
   }

   /**
    * 
    * @param userOid
    * @param options
    * @return
    */

   private Query createQuery(String userOid, Options options)
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Application, ActivityInstanceState.Created, ActivityInstanceState.Hibernated,
            ActivityInstanceState.Interrupted, ActivityInstanceState.Suspended});
      query.getFilter().add(new PerformingUserFilter(Long.parseLong(userOid)));

      ActivityTableUtils.addDescriptorPolicy(options, query);

      ActivityTableUtils.addSortCriteria(query, options);

      ActivityTableUtils.addFilterCriteria(query, options);

      SubsetPolicy subsetPolicy = new SubsetPolicy(options.pageSize, options.skip, true);
      query.setPolicy(subsetPolicy);

      return query;
   }

   /**
    * 
    * @param query
    * @return
    */

   private QueryResult<ActivityInstance> performSearch(Query query)
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      QueryResult<ActivityInstance> result = facade.getAllActivityInstances((ActivityInstanceQuery) query);
      return result;
   }

}
