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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.PermissionState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserGroup;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.query.statistics.api.UserLoginStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserLoginStatisticsQuery;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.Permissions;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;



public class UserUtils
{
   public static final String USER_NAME_DISPLAY_FORMAT_0 = "{1}, {0} ({2})";
   public static final String USER_NAME_DISPLAY_FORMAT_1 = "{0} {1} ({2})";
   public static final String USER_NAME_DISPLAY_FORMAT_2 = "{1} {0} ({2})";
   public static final String USER_NAME_DISPLAY_FORMAT_PREF_ID = "ipp-admin-portal.userNameDisplayFormat.prefs.displayFormat";  
   private static final String PREFERENCES_ID = "preference";
   
   public static UserService getUserService()
   {
      SessionContext ctx = SessionContext.findSessionContext();
      ServiceFactory serviceFactory = ctx != null ? ctx.getServiceFactory() : null;
      return serviceFactory != null ? serviceFactory.getUserService() : null;
   }
   
   public static void updateServiceFactory(String account, String password)
   {
      SessionContext ctx = SessionContext.findSessionContext();
      Map credentials = new HashMap(2);
      credentials.put(SecurityProperties.CRED_USER, account);
      credentials.put(SecurityProperties.CRED_PASSWORD, password);
      ctx.getServiceFactory().setCredentials(credentials);
   }
   
   public static void updateLoggedInUser()
   {
      SessionContext ctx = SessionContext.findSessionContext();
      if(ctx != null)
      {
         ctx.resetUser();
      }
   }

   public static boolean isUserAdmin(User user)
   {
      return user.isAdministrator();
   }
   
   /**
    * @param searchValue
    * @param onlyActive
    * @param maxMatches
    * @return
    */
   public static List<User> searchUsers(String searchValue, boolean onlyActive, int maxMatches)
   {
      UserQuery userQuery = onlyActive ? UserQuery.findActive() : UserQuery.findAll();

      userQuery.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Core));
      userQuery.setPolicy(new SubsetPolicy(maxMatches, false));

      FilterOrTerm filter = userQuery.getFilter().addOrTerm();
      filter.or(UserQuery.FIRST_NAME.like(searchValue));
      filter.or(UserQuery.LAST_NAME.like(searchValue));
      filter.or(UserQuery.ACCOUNT.like(searchValue));
      userQuery.where(filter);

      userQuery.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(
            UserQuery.ACCOUNT);

      Users users = SessionContext.findSessionContext().getServiceFactory().getQueryService()
            .getAllUsers(userQuery);
      
      return users;
   }
   
   /**
    * @param users
    * @return
    */
   public static UserLoginStatistics getUserLoginStatistics(List<User> users)
   {
      if (CollectionUtils.isNotEmpty(users))
      {
         QueryService queryService = SessionContext.findSessionContext().getServiceFactory().getQueryService();
         // TODO: In future try for only getting data given users 
         UserLoginStatistics userLoginStatistics = (UserLoginStatistics) queryService.getAllUsers(UserLoginStatisticsQuery
               .forAllUsers());
         
         return userLoginStatistics;
      }

      return null;
   }
   
   /**
    * Returns true if Participant is part of Team
    * @param user
    * @param participantList
    * @return
    */
   public static boolean isParticipantPartofTeam(User user, List<QualifiedModelParticipantInfo> participantList)
   {
      boolean isRolePartofTeam = false;
      ModelCache modelCache = ModelCache.findModelCache();
      
      List<Organization> orgList = null;
      List<Participant> teamParticipants = new ArrayList<Participant>();
      Set<Grant> grants = new HashSet<Grant>(user.getAllGrants());
      for (Grant grant : grants)
      {
         Model model = modelCache.getActiveModel(grant);
         Participant p = model != null ? model.getParticipant(grant.getId()) : null;
         Role teamleaderRole = null;
         if (p instanceof Role)
         {
            teamleaderRole = (Role) p;
            if (CollectionUtils.isNotEmpty(teamleaderRole.getTeams()))
            {
               orgList = teamleaderRole.getTeams();
            }
         }
      }
      
      if(orgList != null)
      {
         for (Organization org : orgList)
         {
            teamParticipants.add(org);
            retrieveParticipants(teamParticipants, org);
         }
      }
      
      for (Participant p : teamParticipants)
      {
         for (QualifiedModelParticipantInfo mp : participantList)
         {
            if(p.getQualifiedId().equals(mp.getQualifiedId()))
            {
               isRolePartofTeam = true;
               break;
            }
         }
      }
      return isRolePartofTeam;
   }
   
   /**
    * Returns true if User is team lead
    * @param user
    * @return
    */
   public static boolean isTeamLead(User user)
   {
      boolean isTeamLead = false;
      ModelCache modelCache = ModelCache.findModelCache();
      
      Set<Grant> grants = new HashSet<Grant>(user.getAllGrants());
      for (Grant grant : grants)
      {
         Model model = modelCache.getActiveModel(grant);
         Participant p = model != null ? model.getParticipant(grant.getId()) : null;
         Role teamleaderRole = null;
         if (p instanceof Role)
         {
            teamleaderRole = (Role) p;
            if (CollectionUtils.isNotEmpty(teamleaderRole.getTeams()))
            {
               isTeamLead = true;
               break;
            }
         }
      }
      return isTeamLead;
   }
   
   /**
    * @param user
    * @return true if User has at least 1 non-team lead grant
    */
   public static boolean hasNonTeamLeadGrant(User user)
   {
      boolean hasNonTeamLeadGrant = false;
      ModelCache modelCache = ModelCache.findModelCache();
      
      Set<Grant> grants = new HashSet<Grant>(user.getAllGrants());
      for (Grant grant : grants)
      {
         Model model = modelCache.getActiveModel(grant);
         Participant p = model != null ? model.getParticipant(grant.getId()) : null;
         if (p instanceof Organization)
         {
            hasNonTeamLeadGrant = true;
            break;
         }
         else if (p instanceof Role)
         {
            Role role = null;
            role = (Role) p;
            if (CollectionUtils.isEmpty(role.getTeams()))
            {
               hasNonTeamLeadGrant = true;
               break;
            }
         }
      }
      return hasNonTeamLeadGrant;
   }
   
   public static String getUserDisplayLabel(User user)
   {
      String label = "";
      
      if (null != user)
      {
         label = formatUserName(user, (String) user.getProperty(USER_NAME_DISPLAY_FORMAT_PREF_ID));
      }

      if (StringUtils.isEmpty(label))
      {
         label = "<Unknown User>";
      }

      return label;
   }
   
   /**
    * @param user
    * @param format
    * @return
    */
   public static String formatUserName(User user, String format)
   {
      return formatUserName(user.getFirstName(), user.getLastName(), user.getAccount(), format);      
   }
   
   /**
    * @param firstName
    * @param lastName
    * @param account
    * @param format
    * @return
    */
   public static String formatUserName(String firstName, String lastName, String account, String format)
   {
      if (StringUtils.isNotEmpty(format))
      {
         return MessageFormat.format(format, firstName, lastName, account);
      }
      
      return MessageFormat.format(getDefaultUserNameDisplayFormat(), firstName, lastName, account);
   }   
   
   /**
    * 
    */
   public static void saveDefaultUserNameDisplayFormat(String defaultDisplayFormat)
   {
      AdministrationService adminService = SessionContext.findSessionContext().getServiceFactory()
            .getAdministrationService();
      Preferences retrievedPrefs = adminService.getPreferences(PreferenceScope.PARTITION,
            UserPreferencesEntries.M_ADMIN_PORTAL, PREFERENCES_ID);

      if (null == retrievedPrefs)
      {
         Map<String, Serializable> prefMap = new HashMap<String, Serializable>();
         retrievedPrefs = new Preferences(PreferenceScope.PARTITION, UserPreferencesEntries.M_ADMIN_PORTAL,
               PREFERENCES_ID, prefMap);
      }
      retrievedPrefs.getPreferences().put(getDefaultUserNameFormatPrefKey(),
            defaultDisplayFormat);

      adminService.savePreferences(retrievedPrefs);
   }

   
   /**
    * @return
    */
   public static String getDefaultUserNameDisplayFormat()
   {
      String defaultdispFormat = USER_NAME_DISPLAY_FORMAT_0;
      AdministrationService adminService = SessionContext.findSessionContext().getServiceFactory().getAdministrationService();
      Preferences retrievedPrefs = adminService.getPreferences(PreferenceScope.PARTITION,
            UserPreferencesEntries.M_ADMIN_PORTAL,
            PREFERENCES_ID);
      if (null != retrievedPrefs && !CollectionUtils.isEmpty(retrievedPrefs.getPreferences()))
      {
         defaultdispFormat = (String) retrievedPrefs.getPreferences().get(getDefaultUserNameFormatPrefKey());
         defaultdispFormat = StringUtils.isEmpty(defaultdispFormat) ? USER_NAME_DISPLAY_FORMAT_0 : defaultdispFormat;
      }
      
      return defaultdispFormat;
   }
   
   /**
    * @return
    */
   private static String getDefaultUserNameFormatPrefKey()
   {
      return UserPreferencesEntries.M_ADMIN_PORTAL + "." + UserPreferencesEntries.V_USER_NAME_DISPALY_FORMAT + "."
            + UserPreferencesEntries.F_USER_NAME_DISPLAY_FORMAT_DEFAULT;
   }
   
   /**
    * Retrieves participants for sub organization
    * 
    * @param participantList
    * @param o
    */
   private static void retrieveParticipants(List<Participant> participantList, Organization o)
   {
      List<Role> tempList = o.getAllSubRoles();
      for (Role participant : tempList)
      {
         participantList.add(participant);
      }
      
      List<Organization> orgList = o.getAllSubOrganizations();
      for (Organization org : orgList)
      {
         participantList.add(org);
         retrieveParticipants(participantList, org);
      }
   }

   /**
    * Copies grants and usergroups from one user to other
    * 
    * @param referenceUser
    * @param targetUser
    */
   public static User copyGrantsAndUserGroups(User referenceUser, User targetUser)
   {
      List<Grant> grants = referenceUser.getAllGrants();
      List<QualifiedModelParticipantInfo> allParticipants = ParticipantUtils.fetchAllParticipants();
      ModelParticipantInfo modelParticipantInfo;
      for (Grant grant : grants)
      {
         modelParticipantInfo = ParticipantUtils.getParticipantModelInfo(allParticipants, grant);
         if (null != modelParticipantInfo)
         {
            targetUser.addGrant(modelParticipantInfo);
         }
      }
      // copy user groups
      List<UserGroup> userGroups = referenceUser.getAllGroups();
      for (UserGroup userGroup : userGroups)
      {
         targetUser.joinGroup(userGroup.getId());
      }
      return getUserService().modifyUser(targetUser);
   }

   /**
    * Returns the path consisting of {@link ParticipantItem} elements to the requested
    * department in the model participant.
    * 
    * @param qualifiedParticipantId
    * @param department
    * @return
    */
   public static List<ParticipantItem> getParticipantPath(String qualifiedParticipantId, Department department)
   {
      List<ModelParticipant> modelParticipantPath = ModelParticipantTree.getModelParticipantPath(qualifiedParticipantId);
      
      Department dept = department;
      Map<String, Department> organizationDepartmentMap = CollectionUtils.newHashMap();
      while (null != dept)
      {
         organizationDepartmentMap.put(dept.getOrganization().getQualifiedId(), dept);
         dept = dept.getParentDepartment();
      }
      
      List<ParticipantItem> participantItemPath = CollectionUtils.newList();
      Department prevDpt = null;
      QualifiedModelParticipantInfo scopedParticipant = null;
      for (ModelParticipant modelParticipant : modelParticipantPath)
      {
         scopedParticipant = ParticipantUtils.getScopedParticipant(modelParticipant, prevDpt);
         participantItemPath.add(new ParticipantItem(scopedParticipant));

         if (modelParticipant instanceof OrganizationInfo)
         {
            Department dpt = organizationDepartmentMap.get(modelParticipant.getQualifiedId());
            if (dpt != null)
            {
               participantItemPath.add(new ParticipantItem(dpt));
               prevDpt = dpt;
            }
            else
            {
               if (modelParticipant.isDepartmentScoped())
               {
                  participantItemPath.add(new ParticipantItem(scopedParticipant));
               }
            }
         }
      }

      return participantItemPath;
   }
   
   /**
    * @return
    */
   public static String getPartitionID()
   {
      return SessionContext.findSessionContext().getUser().getPartitionId();
   }

   /**
    * @return
    */
   public static String getRealmId()
   {
      return SessionContext.findSessionContext().getUser().getRealm().getId();
   }
}
