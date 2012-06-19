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

import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class LoginTimeTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;

   private String name;

   private String day;

   private String week;

   private String month;

   private UserItem userItem;

   private String userId;

   private String userOid;

   /**
    * @param userItem
    * @param day
    * @param week
    * @param month
    */
   public LoginTimeTableEntry(UserItem userItem, String userId, String userOid,
         String day, String week, String month)
   {
      super();
      this.userItem = userItem;
      this.userId = userId;
      this.userOid = userOid;
      this.name = UserUtils.getUserDisplayLabel(userItem.getUser());
      this.day = day;
      this.week = week;
      this.month = month;
   }

   /**
    * 
    */
   public LoginTimeTableEntry()
   {

   }

   public String getName()
   {
      return name;
   }

   public String getDay()
   {
      return day;
   }

   public String getWeek()
   {
      return week;
   }

   public String getMonth()
   {
      return month;
   }

   public UserItem getUserItem()
   {
      return userItem;
   }

   public void setUserItem(UserItem userItem)
   {
      this.userItem = userItem;
   }

   public String getUserId()
   {
      return userId;
   }

   public void setUserId(String userId)
   {
      this.userId = userId;
   }

   public String getUserOid()
   {
      return userOid;
   }

   public void setUserOid(String userOid)
   {
      this.userOid = userOid;
   }

}
