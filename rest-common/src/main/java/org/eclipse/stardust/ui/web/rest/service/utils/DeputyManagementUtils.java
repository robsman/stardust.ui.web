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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.Deputy;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.ui.web.rest.service.dto.DeputyMemberDetailDTO;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.springframework.stereotype.Component;

@Component
public class DeputyManagementUtils
{
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
         deputyList.add(new DeputyMemberDetailDTO(deputyUser, deputy.getFromDate(), deputy.getUntilDate(), deputy
               .getParticipints()));
      }
      return deputyList;

   }

}
