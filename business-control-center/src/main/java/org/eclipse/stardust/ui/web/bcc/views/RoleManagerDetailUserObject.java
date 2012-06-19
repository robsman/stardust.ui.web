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
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;



/**
 * @author Giridhara.G
 * @version $Revision: $
 */

public class RoleManagerDetailUserObject extends DefaultRowModel
{
   private String userName;

   private String userOid;

   private String directItemCount;

   private String inDirectItemCount;

   private String loggedIn;

   private String roleCount;

   private UserItem userItem;

   private String totalItemCount;

   private boolean select;

   /**
    * @param directItemCount
    * @param inDirectItemCount
    * @param loggedIn
    * @param roleCount
    * @param userItem
    */
   public RoleManagerDetailUserObject(String userName, String userOid,
         String directItemCount, String inDirectItemCount, String totalItemCount,
         boolean loggedIn, String roleCount, UserItem userItem, boolean select)
   {
      super();
      this.userName = userName;
      this.userOid = userOid;
      this.select = select;
      this.directItemCount = directItemCount;
      this.inDirectItemCount = inDirectItemCount;
      this.totalItemCount = totalItemCount;
      if (loggedIn)
      {
         this.loggedIn = MessagesBCCBean.getInstance().getString("common.button.yes");
      }
      else
      {
         this.loggedIn = MessagesBCCBean.getInstance().getString("common.button.no");
      }
      // this.loggedIn = loggedIn;
      this.roleCount = roleCount;
      this.userItem = userItem;
   }

   public String getDirectItemCount()
   {
      return directItemCount;
   }

   public void setDirectItemCount(String directItemCount)
   {
      this.directItemCount = directItemCount;
   }

   public String getInDirectItemCount()
   {
      return inDirectItemCount;
   }

   public void setInDirectItemCount(String inDirectItemCount)
   {
      this.inDirectItemCount = inDirectItemCount;
   }

   public String getRoleCount()
   {
      return roleCount;
   }

   public void setRoleCount(String roleCount)
   {
      this.roleCount = roleCount;
   }

   public UserItem getUserItem()
   {
      return userItem;
   }

   public void setUserItem(UserItem userItem)
   {
      this.userItem = userItem;
   }

   public boolean isSelect()
   {
      return select;
   }

   public void setSelect(boolean select)
   {
      this.select = select;
   }

   public String getUserName()
   {
      return userName;
   }

   public void setUserName(String userName)
   {
      this.userName = userName;
   }

   public String getTotalItemCount()
   {
      return totalItemCount;
   }

   public void setTotalItemCount(String totalItemCount)
   {
      this.totalItemCount = totalItemCount;
   }

   public String getLoggedIn()
   {
      return loggedIn;
   }

   public void setLoggedIn(String loggedIn)
   {
      this.loggedIn = loggedIn;
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
