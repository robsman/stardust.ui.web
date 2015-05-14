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
package org.eclipse.stardust.ui.web.rest.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserProfileStatusDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.ParticipantManagementUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.stereotype.Component;

@Component
public class ParticipantManagementService
{
   @Resource
   private ParticipantManagementUtils participantManagementUtils;

   /**
    * 
    * @return
    */
   public QueryResultDTO getAllUsers(Boolean hideInvalidatedUsers, Options options)
   {
      QueryResult<User> users = participantManagementUtils.getAllUsers(hideInvalidatedUsers, options);
      return buildAllUsersResult(users);
   }

   /**
    * 
    * @param users
    * @return
    */
   private QueryResultDTO buildAllUsersResult(QueryResult<User> users)
   {
      List<UserDTO> userDTOList = new ArrayList<UserDTO>();

      for (User user : users)
      {
         UserDTO userDTO = DTOBuilder.build(user, UserDTO.class);
         userDTO.displayName = UserUtils.getUserDisplayLabel(user);
         userDTOList.add(userDTO);
      }

      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = userDTOList;
      resultDTO.totalCount = users.getTotalCount();

      return resultDTO;
   }

   /**
    * 
    * @param mode
    * @param oid
    * @return
    */
   public UserDTO getCreateCopyModifyUserData(String mode, long oid)
   {
      return participantManagementUtils.initializeView(mode, oid);

   }

   /**
    * 
    * @param userDTO
    * @param mode
    * @return
    */
   public UserProfileStatusDTO createCopyModifyUser(UserDTO userDTO, String mode)
   {
      return participantManagementUtils.createCopyModifyUser(userDTO, mode);
   }
}
