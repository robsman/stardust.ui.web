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
package org.eclipse.stardust.ui.web.bcc.views;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;

/**
 * @author Giridhara.G
 * @version
 */
public class ProcessResourceMgmtRoleTableEntry extends DefaultRowModel
{   
   private static final long serialVersionUID = 1L;

   private String roleId;

   private long departmentOid;

   private String name;

   private Long items;

   private String account;

   private Long itemsPerUser;

   /**
    * @param roleId
    * @param name
    * @param items
    * @param userCount
    * @param itemsPerUser
    * @param loggedInUserCount
    */
   public ProcessResourceMgmtRoleTableEntry(String roleId, long departmentOid, String name, Long items,
         Long loggedInUserCount, Long userCount, Long itemsPerUser)
   {
      super();
      this.roleId = roleId;
      this.departmentOid = departmentOid;
      this.name = name;
      this.items = items;
      this.account = loggedInUserCount.toString() + "(" + userCount.toString() + ")";
      this.itemsPerUser = itemsPerUser;
   }

   public String getRoleId()
   {
      return roleId;
   }

   public void setRoleId(String roleId)
   {
      this.roleId = roleId;
   }

   public long getDepartmentOid()
   {
      return departmentOid;
   }

   public void setDepartmentOid(long departmentOid)
   {
      this.departmentOid = departmentOid;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public Long getItems()
   {
      return items;
   }

   public void setItems(Long items)
   {
      this.items = items;
   }

   public String getAccount()
   {
      return account;
   }

   public void setAccount(String account)
   {
      this.account = account;
   }

   public Long getItemsPerUser()
   {
      return itemsPerUser;
   }

   public void setItemsPerUser(Long itemsPerUser)
   {
      this.itemsPerUser = itemsPerUser;
   }
}
