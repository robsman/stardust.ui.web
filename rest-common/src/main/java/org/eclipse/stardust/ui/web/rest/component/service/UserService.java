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

package org.eclipse.stardust.ui.web.rest.component.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.spi.user.UserProvider;
import org.eclipse.stardust.ui.web.rest.component.util.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.rest.dto.UserCountsDTO;
import org.eclipse.stardust.ui.web.rest.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.dto.UserPermissionsDTO;
import org.eclipse.stardust.ui.web.rest.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.user.impl.IppUser;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.MyPicturePreferenceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.stereotype.Component;

/**
 * @author Johnson.Quadras
 *
 */
@Component
public class UserService
{

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   private static final Logger trace = LogManager.getLogger(UserService.class);

   @Resource
   private UserProvider userProvider;

   /**
    * Gets the logged In User
    * 
    * @return
    */
   public UserDTO getLoggedInUser()
   {
      User loggedInUser = SessionContext.findSessionContext().getUser();
      UserDTO userDTO = DTOBuilder.build(loggedInUser, UserDTO.class);
      userDTO.displayName = UserUtils.getUserDisplayLabel(loggedInUser);
      userDTO.myDocumentsFolderPath = DocumentMgmtUtility.getMyDocumentsPath();
      
      return userDTO;
   }

   /**
    * 
    * @return
    */
   public UserCountsDTO getAllCounts()
   {
      UserCountsDTO userCountsDTO = new UserCountsDTO();
      userCountsDTO.totalCount = getTotalUsersCount();
      userCountsDTO.activeCount = getActiveUsersCount();
      return userCountsDTO;

   }


   /**
    * 
    * @return
    */
   private Long getActiveUsersCount()
   {
      QueryService service = serviceFactoryUtils.getQueryService();
      Long count = new Long(service.getUsersCount(UserQuery.findActive()));
      return count;
   }

   /**
    * 
    * @return
    */
   private Long getTotalUsersCount()
   {
      QueryService service = serviceFactoryUtils.getQueryService();
      Long count = new Long(service.getUsersCount(UserQuery.findAll()));
      return count;
   }

   /**
    * 
    * @param userOID
    * @return
    */
   public UserDTO getUserDetails(Long userOID)
   {
      User user = UserUtils.getUser(userOID, UserDetailsLevel.WithProperties);
      UserDTO userDTO = DTOBuilder.build(user, UserDTO.class);
      if (user.getValidFrom() != null)
      {
         userDTO.validFrom = user.getValidFrom().getTime();
      }

      if (user.getValidTo() != null)
      {
         userDTO.validTo = user.getValidTo().getTime();
      }
      userDTO.displayName = UserUtils.getUserDisplayLabel(user);
      userDTO.userImageURI = MyPicturePreferenceUtils.getUsersImageURI(user);
      return userDTO;
   }

   /**
    * @param userOID
    * @param userDetailsLevelStr
    * @return TODO enhancement may be required to return all details of user based of
    *         detail level
    */
   public List<UserDTO> getUserDetails(Set<String> userIds, String userDetailsLevelStr)
   {
      Users users = UserUtils.getUsers(userIds, null, getUserDetailsLevel(userDetailsLevelStr));
      List<UserDTO> userDTOs = new ArrayList<UserDTO>();
      for (User user2 : users)
      {
         UserDTO userDTO = DTOBuilder.build(user2, UserDTO.class);
         userDTO.displayName = UserUtils.getUserDisplayLabel(user2);
         userDTO.userImageURI = MyPicturePreferenceUtils.getUsersImageURI(user2);
         userDTOs.add(userDTO);
      }
      return userDTOs;
   }
   
   /**
    * @param level
    * @return
    */
   private UserDetailsLevel getUserDetailsLevel(String level)
   {
      if (UserDetailsLevel.Minimal.getName().equalsIgnoreCase(level))
      {
         return UserDetailsLevel.Minimal;
      }
      else if (UserDetailsLevel.Core.getName().equalsIgnoreCase(level))
      {
         return UserDetailsLevel.Core;
      }
      else if (UserDetailsLevel.WithProperties.getName().equalsIgnoreCase(level))
      {
         return UserDetailsLevel.WithProperties;
      }
      else if (UserDetailsLevel.Full.getName().equalsIgnoreCase(level))
      {
         return UserDetailsLevel.Full;
      }

      return UserDetailsLevel.Minimal;
   }

   /**
    * 
    */
   public UserPermissionsDTO getPermissionsForLoggedInUser()
   {
      org.eclipse.stardust.ui.web.common.spi.user.User loggedInUser = userProvider.getUser();

      Set<String> allPermissionIds = serviceFactoryUtils.getAdministrationService().getGlobalPermissions()
            .getAllPermissionIds();

      List<String> availablePermissions = new ArrayList<String>();

      IppUser ippUser = (IppUser) loggedInUser;
      for (String permission : allPermissionIds)
      {
         if (ippUser.hasPermission(permission))
         {
            availablePermissions.add(permission);
         }
      }

      UserPermissionsDTO result = new UserPermissionsDTO(availablePermissions);
      return result;
   }
}
