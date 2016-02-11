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
package org.eclipse.stardust.ui.web.rest.component.service;

import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.ui.web.rest.component.util.ParticipantManagementUtils;
import org.eclipse.stardust.ui.web.rest.dto.DataTableOptionsDTO;
import org.eclipse.stardust.ui.web.rest.dto.InvalidateUserStatusDTO;
import org.eclipse.stardust.ui.web.rest.dto.NotificationMessageDTO;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.dto.UserProfileStatusDTO;
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
   public QueryResultDTO getAllUsers(Boolean hideInvalidatedUsers, DataTableOptionsDTO options)
   {
      return participantManagementUtils.getAllUsers(hideInvalidatedUsers, options);
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

   /**
    * 
    * @param userOids
    * @return
    */
   public InvalidateUserStatusDTO invalidateUser(List<Long> userOids)
   {
      return participantManagementUtils.invalidateUser(userOids);
   }

   /**
    * 
    * @param activityInstanceOids
    * @param userOids
    * @return
    */
   public NotificationMessageDTO delegateToDefaultPerformer(List<Long> activityInstanceOids, List<Long> userOids)
   {
      return participantManagementUtils.delegateToDefaultPerformer(activityInstanceOids, userOids);

   }
}
