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

import javax.annotation.Resource;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.rest.service.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
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

   /**
    * Gets the logged In User
    * @return
    */
   public UserDTO getLoggedInUser()
   {
      User loggedInUser = serviceFactoryUtils.getUserService().getUser();
      UserDTO userDTO = DTOBuilder.build(loggedInUser, UserDTO.class);
      userDTO.displayName = UserUtils.getUserDisplayLabel(loggedInUser);
      return userDTO;
   }

}
