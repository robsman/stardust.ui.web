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
package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.List;

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;

/**
 * @author Abhay.Thappan
 */
@DTOClass
public class RoleAssignmentDTO extends AbstractDTO
{
   public String name;

   public String userId;

   public String userOid;

   public List<GrantsAssignmentDTO> grants;

   /**
    * 
    */
   public RoleAssignmentDTO()
   {}

   /**
    * 
    * @param user
    * @param grants
    */
   public RoleAssignmentDTO(User user, List<GrantsAssignmentDTO> grants)
   {
      super();
      this.userId = user.getId();
      this.userOid = String.valueOf(user.getOID());
      this.grants = grants;
      this.name = UserUtils.getUserDisplayLabel(user);
   }
}
