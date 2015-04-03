package org.eclipse.stardust.ui.web.rest.service.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.ParticipantAssociationFilter;
import org.eclipse.stardust.engine.api.query.PerformingParticipantFilter;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.RoleItem;
import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.RoleManagerDetailUserDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.RoleManagerDetailsDTO;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.stereotype.Component;

@Component
public class RoleManagerDetailUtils
{

   /**
    * 
    * @param roleId
    * @param departmentOid
    * @return RoleManagerDetailsDTO
    */
   public RoleManagerDetailsDTO getRoleManagerDetails(String roleId, String departmentOid)
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      RoleItem roleItem = getRoleItem(roleId, departmentOid);

      RoleManagerDetailsDTO roleManagerDetailsDTO = new RoleManagerDetailsDTO();

      // Code to get the assigned user list
      List<UserItem> assignedUserList = getAssignedUsersAsUserItems(facade, roleItem);
      List<RoleManagerDetailUserDTO> userAssignedList = new ArrayList<RoleManagerDetailUserDTO>();

      for (UserItem userItem : assignedUserList)
      {

         userAssignedList.add(new RoleManagerDetailUserDTO(userItem.getUserName(), Long.toString(userItem.getUser()
               .getOID()), Long.toString(userItem.getDirectItemCount()),
               Long.toString(userItem.getIndirectItemCount()), Long.toString(userItem.getIndirectItemCount()
                     + userItem.getDirectItemCount()), userItem.isLoggedIn(), Long.toString(userItem.getRoleCount())));
      }
      roleManagerDetailsDTO.assignedUserList = userAssignedList;

      // code to get Assignable User list

      List<UserItem> assignableUserList = getAssignableUsersAsUserItems(facade, assignedUserList);
      List<RoleManagerDetailUserDTO> userAssignableList = new ArrayList<RoleManagerDetailUserDTO>();

      for (UserItem userItem : assignableUserList)
      {
         userAssignableList.add(new RoleManagerDetailUserDTO(userItem.getUserName(), Long.toString(userItem.getUser()
               .getOID()), Long.toString(userItem.getDirectItemCount()),
               Long.toString(userItem.getIndirectItemCount()), Long.toString(userItem.getIndirectItemCount()
                     + userItem.getDirectItemCount()), userItem.isLoggedIn(), Long.toString(userItem.getRoleCount())));
      }
      roleManagerDetailsDTO.assignableUserList = userAssignableList;

      // code to set the role manager details such as roleName, roleId etc.
      roleManagerDetailsDTO.roleName = (!roleItem.getRoleName().toString().isEmpty()) ? roleItem.getRoleName()
            .toString() : null;
      roleManagerDetailsDTO.roleId = (!roleItem.getRole().getId().isEmpty()) ? roleItem.getRole().getId() : roleId;
      roleManagerDetailsDTO.items = (!Long.toString(roleItem.getWorklistCount()).isEmpty()) ? Long.toString(roleItem
            .getWorklistCount()) : null;
      roleManagerDetailsDTO.account = (!Long.toString(roleItem.getUserCount()).isEmpty()) ? Long.toString(roleItem
            .getUserCount()) : null;
      roleManagerDetailsDTO.itemsPerUser = Long.toString(roleItem.getEntriesPerUser()) != null ? Long.toString(roleItem
            .getEntriesPerUser()) : null;
      roleManagerDetailsDTO.roleModifiable = canUserModifyRole(roleItem);

      return roleManagerDetailsDTO;
   }

   /**
    * 
    * @param facade
    * @param assignedUserList
    * @return
    */
   private List<UserItem> getAssignableUsersAsUserItems(WorkflowFacade facade, List<UserItem> assignedUserList)
   {
      UserQuery queryForAssignableUser = UserQuery.findActive();
      FilterAndTerm filter = queryForAssignableUser.getFilter();

      if (CollectionUtils.isNotEmpty(assignedUserList))
      {
         Iterator<UserItem> euIter = assignedUserList.iterator();
         while (euIter.hasNext())
         {
            UserItem userItem = euIter.next();
            filter.add(UserQuery.OID.notEqual(userItem.getUser().getOID()));
         }
      }
      UserDetailsPolicy userPolicyForAssignableUser = new UserDetailsPolicy(UserDetailsLevel.Core);
      userPolicyForAssignableUser.setPreferenceModules(UserPreferencesEntries.M_ADMIN_PORTAL);
      queryForAssignableUser.setPolicy(userPolicyForAssignableUser);

      List<UserItem> assignableUserList = facade.getAllUsersAsUserItems(queryForAssignableUser);
      return assignableUserList;
   }

   /**
    * 
    * @param facade
    * @param roleItem
    * @return
    */
   private List<UserItem> getAssignedUsersAsUserItems(WorkflowFacade facade, RoleItem roleItem)
   {
      UserQuery query = UserQuery.findAll();
      query.getFilter().add(ParticipantAssociationFilter.forParticipant(roleItem.getRole(), false));
      UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Core);
      userPolicy.setPreferenceModules(UserPreferencesEntries.M_ADMIN_PORTAL);
      query.setPolicy(userPolicy);
      List<UserItem> assignedUserList = facade.getAllUsersAsUserItems(query);
      return assignedUserList;
   }

   /**
    * @param roleId
    * @param departmentOid
    * @return
    */
   private RoleItem getRoleItem(String roleId, String departmentOid)
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      QualifiedModelParticipantInfo modelParticipantInfo = getModelParticipantInfo(roleId, departmentOid, facade);
      RoleItem roleItem = facade.getRoleItem(modelParticipantInfo);
      return roleItem;
   }

   /**
    * @param roleId
    * @param departmentOid
    * @param facade
    * @return
    */
   private QualifiedModelParticipantInfo getModelParticipantInfo(String roleId, String departmentOid,
         WorkflowFacade facade)
   {
      ModelParticipant participant = (ModelParticipant) ModelCache.findModelCache().getParticipant(roleId);
      Department department = facade.getAdministrationService().getDepartment(Long.parseLong(departmentOid));
      QualifiedModelParticipantInfo modelParticipantInfo = (QualifiedModelParticipantInfo) ((department == null)
            ? participant
            : department.getScopedParticipant(participant));
      return modelParticipantInfo;
   }

   /**
    * 
    * @param roleItem
    * @return
    */

   private boolean canUserModifyRole(RoleItem roleItem)
   {
      // Does logged-in user have "Manage Authorization" declarative security?
      if (AuthorizationUtils.canManageAuthorization())
      {
         // return UserUtils.hasNonTeamLeadGrant(facade.getLoginUser()) ? true :
         // (isParticipantPartofTeam() ? true : false);

         // If logged-in user has at least 1 non-team lead grant, then all participants
         // are modifiable
         WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
         if (UserUtils.hasNonTeamLeadGrant(facade.getLoginUser()))
         {
            return true;
         }
         else
         // Else logged-in user is a team lead
         {
            // Only "team participants" are modifiable
            if (isParticipantPartofTeam(roleItem))
            {
               return true;
            }
            else
            {
               return false;
            }
         }
      }
      // Logged-in user does not have "Manage Authorization" declarative security
      else
      {
         return false;
      }
   }

   /**
    * Returns true if Participant is part of Team
    * 
    * @param roleItem
    * @return
    */
   private boolean isParticipantPartofTeam(RoleItem roleItem)
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      List<QualifiedModelParticipantInfo> participantList = new ArrayList<QualifiedModelParticipantInfo>();
      participantList.add(roleItem.getRole());
      return (UserUtils.isParticipantPartofTeam(facade.getLoginUser(), participantList));
   }

   /**
    * 
    * @param userIds
    * @param roleId
    * @param departmentOid
    * @return
    */
   public boolean removeUserFromRole(List<String> userIds, String roleId, String departmentOid)
   {
      List<UserItem> users = CollectionUtils.newArrayList();
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      boolean showInfoDialog = false;
      boolean userAuthorizationChanged = false;

      UserItem userItem = checkForUserAuthChange(userIds, users, facade);

      if (userItem != null)
      {
         users.add(userItem);
         showInfoDialog = true;
      }
      RoleItem roleItem = getRoleItem(roleId, departmentOid);
      if (facade.removeUserFromRole(roleItem, users) > 0)
      {
         // items = Integer.toString(getAssignedUser().size()); not sure about this at
         // this moment
         // initialize();
         if (showInfoDialog && UserUtils.isLoggedInUser(userItem.getUser()))
         {
            userAuthorizationChanged = true;
         }
      }

      return userAuthorizationChanged;
   }

   /***
    * adds selected users to role
    * 
    * @param userIds
    * @param roleId
    * @param departmentOid
    * @return
    */
   public boolean addUserToRole(List<String> userIds, String roleId, String departmentOid)
   {

      boolean userAuthorizationChanged = false;
      List<UserItem> users = CollectionUtils.newArrayList();
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      boolean showInfoDialog = false;
      UserItem userItem = checkForUserAuthChange(userIds, users, facade);

      if (userItem != null)
      {
         users.add(userItem);
         showInfoDialog = true;
      }

      RoleItem roleItem = getRoleItem(roleId, departmentOid);
      if (facade.addUserToRole(roleItem, users) > 0)
      {
         // items = Integer.toString(getAssignedUser().size()); not sure about this at
         // this moment
         // initialize();
         if (showInfoDialog && UserUtils.isLoggedInUser(userItem.getUser()))
         {
            userAuthorizationChanged = true;
         }
      }

      return userAuthorizationChanged;
   }

   /**
    * @param userIds
    * @param users
    * @param facade
    * @return
    */
   private UserItem checkForUserAuthChange(List<String> userIds, List<UserItem> users, WorkflowFacade facade)
   {
      UserItem userItem = null;
      for (String userId : userIds)
      {

         if (facade.getUserItem(Long.parseLong(userId)).getUser().equals(facade.getLoginUser()))
         {
            userItem = facade.getUserItem(facade.getLoginUser());
         }
         else
         {
            users.add(facade.getUserItem(Long.parseLong(userId)));
         }

      }
      return userItem;
   }

   /**
    * This method will get all activities for role
    * 
    * @param roleId
    * @param departmentOid
    * @param options
    * @return
    */
   public QueryResult<ActivityInstance> getAllActivitiesForRole(String roleId, String departmentOid, Options options)
   {
      Query query = createQuery(roleId, departmentOid, options);
      QueryResult<ActivityInstance> result = performSearch(query);
      return result;
   }

   /**
    * 
    * @param roleId
    * @param departmentOid
    * @param options
    * @return
    */
   private Query createQuery(String roleId, String departmentOid, Options options)
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      QualifiedModelParticipantInfo modelParticipantInfo = getModelParticipantInfo(roleId, departmentOid, facade);
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Application, ActivityInstanceState.Created, ActivityInstanceState.Hibernated,
            ActivityInstanceState.Interrupted, ActivityInstanceState.Suspended});
      query.getFilter().add(PerformingParticipantFilter.forParticipant(modelParticipantInfo, true));

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
