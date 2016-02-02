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
package org.eclipse.stardust.ui.web.rest.component.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.ParticipantAssociationFilter;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.Deputy;
import org.eclipse.stardust.engine.api.runtime.DeputyOptions;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.ui.web.rest.dto.DeputyMemberDetailDTO;
import org.eclipse.stardust.ui.web.rest.dto.SelectItemDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.ParticipantDTO;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.stereotype.Component;

@Component
public class DeputyManagementUtils
{
   private static int maxMatches = 10;

   public static String SRCH_MODE_ALL_USERS = "ALL_USERS";

   /**
    * 
    * @return
    */
   public List<DeputyMemberDetailDTO> loadUsers()
   {
      User currentUser = ServiceFactoryUtils.getServiceFactory().getUserService().getUser();
      List<DeputyMemberDetailDTO> usersList = new ArrayList<DeputyMemberDetailDTO>();
      UserQuery query = null;
      UserService userService = ServiceFactoryUtils.getUserService();
      if (AuthorizationUtils.canManageDeputies())
      {
         query = UserQuery.findActive();
         query.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Full));
         query.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(UserQuery.ACCOUNT);

         Users memberUsers = ServiceFactoryUtils.getQueryService().getAllUsers(query);
         for (User memberUser : memberUsers)
         {
            usersList.add(new DeputyMemberDetailDTO(memberUser, hasDeputies(userService, memberUser)));
         }
      }
      else
      {
         usersList.add(new DeputyMemberDetailDTO(currentUser, hasDeputies(userService, currentUser)));
      }

      return usersList;
   }

   /**
    * @param userService
    * @param user
    * @return
    */
   private boolean hasDeputies(UserService userService, User user)
   {
      boolean hasDeputies = false;
      if (CollectionUtils.isNotEmpty(userService.getDeputies(user)))
      {
         hasDeputies = true;
      }
      return hasDeputies;

   }

   /**
    * 
    * @param forUser
    * @return
    */
   public List<DeputyMemberDetailDTO> loadDeputiesForUser(long userOID)
   {
      UserService userService = ServiceFactoryUtils.getUserService();
      User forUser = userService.getUser(userOID);
      User deputyUser;
      List<DeputyMemberDetailDTO> deputyList = new ArrayList<DeputyMemberDetailDTO>();
      for (Deputy deputy : userService.getDeputies(forUser))
      {
         deputyUser = ServiceFactoryUtils.getUserService().getUser(deputy.getDeputyUser().getId());
         List<SelectItemDTO> participants = new ArrayList<SelectItemDTO>();

         for (ModelParticipantInfo participant : deputy.getParticipints())
         {
            participants.add(new SelectItemDTO(participant.getId(), ModelHelper.getParticipantName(participant)));
         }
         deputyList
               .add(new DeputyMemberDetailDTO(deputyUser, deputy.getFromDate(), deputy.getUntilDate(), participants));
      }
      return deputyList;

   }
   /**
    * 
    * @param userOID
    * @param searchValue
    * @param searchMode
    * @return
    */
   public List<ParticipantDTO> getDeputyUsersData(long userOID, String searchValue, String searchMode)
   {
      UserService userService = ServiceFactoryUtils.getUserService();
      User user = userService.getUser(userOID);
      List<User> users;

      if (SRCH_MODE_ALL_USERS.equals(searchMode))
      {
         // Filter User for whom deputy is being added, also filter already added
         // Deputies
         users = UserUtils.searchUsers(searchValue + "%", true, maxMatches);
      }
      else
      {
         UserQuery userQuery = getUsersWithSimilarGrants(user);
         userQuery.setPolicy(new SubsetPolicy(maxMatches, false));
         applyFilters(userQuery, searchValue + "%");
         users = ServiceFactoryUtils.getQueryService().getAllUsers(userQuery);
      }

      // This would filter the user for whom deputy is being added
      // and also filter already added Deputies
      List<String> selData = new ArrayList<String>();
      selData.add(user.getAccount());

      return buildSearchResult(users, selData, searchValue);
   }

   /**
    * @param users
    * @param selectedData
    * @param searchValue
    * @return
    */
   public static List<ParticipantDTO> buildSearchResult(List<User> users, List<String> selectedData,
         String searchValue)
   {
      List<ParticipantDTO> userItems = new ArrayList<ParticipantDTO>(users.size());

      if (CollectionUtils.isNotEmpty(users))
      {

         if (null == selectedData)
         {
            selectedData = new ArrayList<String>();
         }

         for (User user : users)
         {
            if (!selectedData.contains(user.getAccount()))
            {
               userItems.add(new ParticipantDTO((Participant) user));
            }
         }
      }

      return userItems;
   }

   /**
    * @param userQuery
    * @param searchValue
    */
   private void applyFilters(UserQuery userQuery, String searchValue)
   {
      String nameFirstLetterCaseChanged = UserUtils.alternateFirstLetter(searchValue);
      userQuery.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Core));

      FilterAndTerm andFilter = userQuery.getFilter().addAndTerm();
      FilterOrTerm filter = andFilter.addOrTerm();
      filter.or(UserQuery.FIRST_NAME.like(searchValue));
      filter.or(UserQuery.FIRST_NAME.like(nameFirstLetterCaseChanged));
      filter.or(UserQuery.LAST_NAME.like(searchValue));
      filter.or(UserQuery.LAST_NAME.like(nameFirstLetterCaseChanged));
      filter.or(UserQuery.ACCOUNT.like(searchValue));
      filter.or(UserQuery.ACCOUNT.like(nameFirstLetterCaseChanged));
      userQuery.where(filter);

      userQuery.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(UserQuery.ACCOUNT);
   }

   /**
    * @param excludeThisUser
    * @return
    */
   private UserQuery getUsersWithSimilarGrants(User user)
   {
      UserQuery query = UserQuery.findActive();
      if (user != null)
      {
         FilterTerm filter = query.getFilter().addOrTerm();

         ModelParticipantInfo modelParticipantInfo;
         Model model;
         Role role;
         Department department;

         List<Grant> grants = user.getAllGrants();
         for (Grant grant : grants)
         {
            if (!grant.isOrganization())
            {
               model = ModelCache.findModelCache().getActiveModel(grant);
               role = model.getRole(grant.getId());
               department = grant.getDepartment();

               if (department != null)
               {
                  modelParticipantInfo = department.getScopedParticipant(role);
               }
               else
               {
                  modelParticipantInfo = role;
               }
               filter.add(ParticipantAssociationFilter.forParticipant(((RoleInfo) modelParticipantInfo)));
            }
         }
      }
      else
      {
         query.where(UserQuery.OID.isEqual(0));
      }
      return query;
   }

   /**
    * 
    * @param userOID
    * @return
    */
   public List<SelectItemDTO> getAuthorizations(long userOID)
   {
      UserService userService = ServiceFactoryUtils.getUserService();
      User user = userService.getUser(userOID);
      List<SelectItemDTO> modelParticipantInfoList = new ArrayList<SelectItemDTO>();

      if (user != null)
      {
         ModelParticipantInfo modelParticipantInfo;
         Model model;
         Role role;
         Department department;

         List<Grant> grants = user.getAllGrants();
         for (Grant grant : grants)
         {
            if (!grant.isOrganization())
            {
               model = ModelCache.findModelCache().getActiveModel(grant);
               role = model.getRole(grant.getId());
               department = grant.getDepartment();

               if (department != null)
               {
                  modelParticipantInfo = department.getScopedParticipant(role);
               }
               else
               {
                  modelParticipantInfo = role;
               }
               modelParticipantInfoList.add(new SelectItemDTO(modelParticipantInfo.getId(), ModelHelper
                     .getParticipantName(modelParticipantInfo)));

            }
         }
      }
      return modelParticipantInfoList;
   }

   /**
    * 
    * @param userOID
    * @param deputyOID
    * @param validFrom
    * @param validTo
    * @param modelParticipantIds
    * @param mode
    */
   public void addOrModifyDeputy(long userOID, long deputyOID, Date validFrom, Date validTo,
         List<String> modelParticipantIds, String mode)
   {
      UserService userService = ServiceFactoryUtils.getUserService();
      User user = userService.getUser(userOID);
      User deputy = userService.getUser(deputyOID);
      Set<ModelParticipantInfo> participants = new HashSet<ModelParticipantInfo>();
      for (String modelParticipantId : modelParticipantIds)
      {
         ModelParticipantInfo participant = (ModelParticipantInfo) ModelCache.findModelCache().getParticipant(
               modelParticipantId, null);
         participants.add(participant);
      }

      DeputyOptions deputyOptions = new DeputyOptions(validFrom, validTo, participants);
      if (mode.equals("EDIT"))
      {
         userService.modifyDeputy(user, deputy, deputyOptions);
      }
      else
      {
         userService.addDeputy(user, deputy, deputyOptions);
      }

   }

   /**
    * 
    * @param userOID
    * @param deputyOID
    */
   public void removeUserDeputy(long userOID, long deputyOID)
   {
      UserService userService = ServiceFactoryUtils.getUserService();
      User user = userService.getUser(userOID);
      User deputy = userService.getUser(deputyOID);
      userService.removeDeputy(user, deputy);
   }

}
