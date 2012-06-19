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

import java.util.List;

import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;



public class CompletedActivityUserObject extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;

   private UserItem userItem;

   private String userId;

   private String userOid;

   private List<CompletedActivityDynamicUserObject> completedActivitiesList;
   
   private String name;

   /**
    * @param userItem
    * @param completedActivitiesList
    */
   public CompletedActivityUserObject(UserItem userItem, String userId, String userOid,
         List<CompletedActivityDynamicUserObject> completedActivitiesList)
   {
      super();
      this.userId = userId;
      this.userOid = userOid;
      this.userItem = userItem;
      this.completedActivitiesList = completedActivitiesList;
      this.name = I18nUtils.getUserLabel(userItem.getUser());
   }

   public UserItem getUserItem()
   {
      return userItem;
   }

   public void setUserItem(UserItem userItem)
   {
      this.userItem = userItem;
   }

   public List<CompletedActivityDynamicUserObject> getCompletedActivitiesList()
   {
      return completedActivitiesList;
   }

   public void setCompletedActivitiesList(
         List<CompletedActivityDynamicUserObject> completedActivitiesList)
   {
      this.completedActivitiesList = completedActivitiesList;
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

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

}
