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
/**
 * @author Abhay.Thappan
 */
package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

@DTOClass
public class UserManagerDetailRoleDTO extends AbstractDTO
{
   public String roleId;

   public String name;

   public String items;

   public String account;

   public String itemsPerUser;


   public UserManagerDetailRoleDTO(String roleName, String roleId, long itemsCount, long roleAccount,
         long itemsPerUser)
   {
      super();
      this.name = roleName;
      this.roleId = roleId;
      this.items = Long.toString(itemsCount);
      this.account = Long.toString(roleAccount);
      this.itemsPerUser = Long.toString(itemsPerUser);
   }
}
