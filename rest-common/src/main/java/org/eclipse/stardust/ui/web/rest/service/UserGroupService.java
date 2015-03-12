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
package org.eclipse.stardust.ui.web.rest.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.UserGroups;
import org.eclipse.stardust.engine.api.runtime.UserGroup;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.UserGroupDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserGroupQueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.UserGroupUtils;
import org.springframework.stereotype.Component;

/**
 * @author Aditya.Gaikwad
 *
 */
@Component
public class UserGroupService
{

   public static final Logger trace = LogManager.getLogger(UserGroupService.class);

   @Resource
   private UserGroupUtils userGroupUtils;

   /**
    * @param options
    * @return
    */
   public UserGroupQueryResultDTO getAllUserGroups(Options options)
   {
      UserGroups userGroups = userGroupUtils.getAllUserGroups(options);
      List<UserGroupDTO> allUserGroupsDTO = DTOBuilder.buildList(userGroups, UserGroupDTO.class);
      UserGroupQueryResultDTO ugQueryResultDTO = new UserGroupQueryResultDTO();
      ugQueryResultDTO.list = allUserGroupsDTO;
      ugQueryResultDTO.totalCount = userGroups.getTotalCount();
      ugQueryResultDTO.activeCount = getActiveUserGroupCount();
      return ugQueryResultDTO;
   }

   /**
    * @param id
    * @return
    */
   public UserGroupDTO getUserGroup(String id)
   {
      UserGroup userGroup = userGroupUtils.getUserGroup(id);
      return DTOBuilder.build(userGroup, UserGroupDTO.class);
   }

   /**
    * @param userGroupDTO
    * @return
    */
   public UserGroupDTO modifyUserGroup(UserGroupDTO userGroupDTO)
   {
      UserGroup userGroup = userGroupUtils.getUserGroup(userGroupDTO.getId());
      userGroup.setDescription(userGroupDTO.getDescription());
      userGroup.setName(userGroupDTO.getName());
      if (userGroupDTO.getValidFrom() != null)
      {
         userGroup.setValidFrom(new Date(userGroupDTO.getValidFrom()));
      }
      if (userGroupDTO.getValidTo() != null)
      {
         userGroup.setValidTo(new Date(userGroupDTO.getValidTo()));
      }
      return DTOBuilder.build(userGroupUtils.modifyUserGroup(userGroup), UserGroupDTO.class);
   }

   /**
    * @param id
    * @return
    */
   public UserGroupDTO deleteUserGroup(String id)
   {
      UserGroup deleteUserGroup = userGroupUtils.deleteUserGroup(id);
      return DTOBuilder.build(deleteUserGroup, UserGroupDTO.class);
   }

   /**
    * @param id
    * @param name
    * @param description
    * @param validFrom
    * @param validTo
    * @return
    */
   public UserGroupDTO createUserGroup(String id, String name, String description, Long validFrom, Long validTo)
   {
      UserGroup createdUserGroup = userGroupUtils.createUserGroup(id, name, description, validFrom, validTo);
      return DTOBuilder.build(createdUserGroup, UserGroupDTO.class);
   }

   /**
    * @return
    */
   public long getActiveUserGroupCount()
   {
      return userGroupUtils.getActiveUserGroupCount();
   }

}
