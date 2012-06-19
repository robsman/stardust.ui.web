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
 * @version
 */
public class ProcessResourceMgmtUserTableEntry extends DefaultRowModel
{

   private String userName;

   private UserItem userItem;

   private String userAccount;

   private String userFullName;

   private String userEmail;

   private String userOid;

   private Long roleCount;

   private Long directItemCount;

   private Long indirectItemCount;

   private Long itemCount;

   private String loggedIn;

   private String userId;

   /**
    * @param userName
    * @param userItem
    * @param userOid
    * @param userId
    * @param userFullName
    * @param userAccount
    * @param eMail
    * @param roleCount
    * @param directItemCount
    * @param indirectItemCount
    * @param itemCount
    * @param loggedIn
    */
   public ProcessResourceMgmtUserTableEntry(String userName, UserItem userItem,
         Long userOid, String userId, String userFullName, String userAccount,
         String eMail, Long roleCount, Long directItemCount, Long indirectItemCount,
         Long itemCount, boolean loggedIn)
   {
      super();
      this.userName = userName;
      this.userItem = userItem;
      this.userOid = userOid.toString();
      this.userId = userId;
      this.userFullName = userFullName;
      this.userAccount = userAccount;
      if (eMail != null)
      {
         this.userEmail = eMail;
      }
      else
      {
         this.userEmail = "";
      }

      this.roleCount = roleCount;
      this.directItemCount = directItemCount;
      this.indirectItemCount = indirectItemCount;
      this.itemCount = itemCount;
      if (loggedIn)
      {
         
         this.loggedIn = MessagesBCCBean.getInstance().getString("common.button.yes");
      }
      else
      {
         this.loggedIn = MessagesBCCBean.getInstance().getString("common.button.no");
      }
   }

   public String getUserName()
   {
      return userName;
   }

   public void setUserName(String userName)
   {
      this.userName = userName;
   }

   public UserItem getUserItem()
   {
      return userItem;
   }

   public void setUserItem(UserItem userItem)
   {
      this.userItem = userItem;
   }

   public String getUserAccount()
   {
      return userAccount;
   }

   public void setUserAccount(String userAccount)
   {
      this.userAccount = userAccount;
   }

   public String getUserFullName()
   {
      return userFullName;
   }

   public void setUserFullName(String userFullName)
   {
      this.userFullName = userFullName;
   }

   public String getUserEmail()
   {
      return userEmail;
   }

   public void setUserEmail(String userEmail)
   {
      this.userEmail = userEmail;
   }

   public String getUserOid()
   {
      return userOid;
   }

   public void setUserOid(String userOid)
   {
      this.userOid = userOid;
   }

   public Long getRoleCount()
   {
      return roleCount;
   }

   public void setRoleCount(Long roleCount)
   {
      this.roleCount = roleCount;
   }

   public Long getDirectItemCount()
   {
      return directItemCount;
   }

   public void setDirectItemCount(Long directItemCount)
   {
      this.directItemCount = directItemCount;
   }

   public Long getIndirectItemCount()
   {
      return indirectItemCount;
   }

   public void setIndirectItemCount(Long indirectItemCount)
   {
      this.indirectItemCount = indirectItemCount;
   }

   public Long getItemCount()
   {
      return itemCount;
   }

   public void setItemCount(Long itemCount)
   {
      this.itemCount = itemCount;
   }

   public String getLoggedIn()
   {
      return loggedIn;
   }

   public void setLoggedIn(String loggedIn)
   {
      this.loggedIn = loggedIn;
   }

   public String getUserId()
   {
      return userId;
   }

   public void setUserId(String userId)
   {
      this.userId = userId;
   }

}
