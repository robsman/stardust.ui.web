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
package org.eclipse.stardust.ui.web.rest.dto;

import org.eclipse.stardust.ui.web.rest.dto.core.DTOClass;

/**
 * @author Abhay.Thappan
 * @version
 */
@DTOClass
public class ProcessResourceMgmtRoleDTO extends AbstractDTO
{
   public String roleId;

   public long departmentOid;

   public String name;

   public Long items;

   public String account;

   public Long itemsPerUser;

   /**
    * @param roleId
    * @param name
    * @param items
    * @param userCount
    * @param itemsPerUser
    * @param loggedInUserCount
    */
   public ProcessResourceMgmtRoleDTO(String roleId, long departmentOid, String name,
         Long items, Long loggedInUserCount, Long userCount, Long itemsPerUser)
   {
      super();
      this.roleId = roleId;
      this.departmentOid = departmentOid;
      this.name = name;
      this.items = items;
      this.account = loggedInUserCount.toString() + "(" + userCount.toString() + ")";
      this.itemsPerUser = itemsPerUser;
   }

}
