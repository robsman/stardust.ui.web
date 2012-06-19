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
package org.eclipse.stardust.ui.web.viewscommon.user;

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.utils.MyPicturePreferenceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;



/**
 * @author Subodh.Godbole
 * 
 */
public class UserWrapper extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;

   private User user;
   private String fullName;
   private String displayLabel;
   
   private boolean online;
   private String onlineIconLink;
   private String offlineIconLink;
   
   private boolean removeable;
   
   private String imageURL;
   
   private UserAutocompleteMultiSelector autocompleteUserSelector;
   
   private User tableOwner;

   /**
    * @param user
    */
   public UserWrapper(User user, boolean online)
   {
      this(user, "", online);
      setUserImage();
   }
   
   /**
    * @param user
    * @param tableOwner
    * @param online
    */
   public UserWrapper(User user, User tableOwner, boolean online)
   {
      this(user, "", online);
      setUserImage();
      this.tableOwner = tableOwner;
   }
   
   /**
    * @param user
    * @param displayLabel
    */
   public UserWrapper(User user, String displayLabel, boolean online)
   {
      this.user = user;
      this.displayLabel = displayLabel;
      this.fullName = user.getLastName() + ", " + user.getFirstName();

      this.online = online;
      this.onlineIconLink = ResourcePaths.I_USER_ONLINE;
      this.offlineIconLink = ResourcePaths.I_USER_OFFLINE;

      this.removeable = true;
      setUserImage();
   }
   
   /**
    * @param user
    * @param tableOwner
    * @param displayLabel
    * @param online
    */
   public UserWrapper(User user, User tableOwner, String displayLabel, boolean online)
   {
      this.user = user;
      this.displayLabel = displayLabel;
      this.fullName = user.getLastName() + ", " + user.getFirstName();

      this.online = online;
      this.onlineIconLink = ResourcePaths.I_USER_ONLINE;
      this.offlineIconLink = ResourcePaths.I_USER_OFFLINE;

      this.removeable = true;
      setUserImage();
      this.tableOwner = tableOwner;
   }

   public void removeUser()
   {
      autocompleteUserSelector.removeSelectedUser(this);
   }

   /**
    * @return
    */
   public String getIconLink()
   {
      return isOnline() ? onlineIconLink : offlineIconLink;
   }
   
   @Override
   public boolean equals(Object arg0)
   {
      if(arg0 instanceof UserWrapper)
      {
         UserWrapper userWrapper = (UserWrapper)arg0;
         return userWrapper.getUser().getAccount().equals(getUser().getAccount());
      }

      return super.equals(arg0);
   }
   
   public User getUser()
   {
      return user;
   }

   public String getFullName()
   {
      return fullName;
   }

   public String getDisplayLabel()
   {
      return UserUtils.getUserDisplayLabel(user);
   }

   public boolean isRemoveable()
   {
      return removeable && isUserTableOwner();
   }

   public void setRemoveable(boolean removeable)
   {
      this.removeable = removeable;
   }

   public boolean isOnline()
   {
      return online;
   }

   public void setOnline(boolean online)
   {
      this.online = online;
   }

   public UserAutocompleteMultiSelector getAutocompleteUserSelector()
   {
      return autocompleteUserSelector;
   }

   public void setAutocompleteUserSelector(UserAutocompleteMultiSelector autocompleteUserSelector)
   {
      this.autocompleteUserSelector = autocompleteUserSelector;
   }

   public String getOnlineIconLink()
   {
      return onlineIconLink;
   }

   public void setOnlineIcon(String onlineIconLink)
   {
      this.onlineIconLink = onlineIconLink;
   }

   public String getOfflineIconLink()
   {
      return offlineIconLink;
   }

   public void setOfflineIconLink(String offlineIconLink)
   {
      this.offlineIconLink = offlineIconLink;
   }
   
   public String getImageURL() {
      return imageURL;
   }
   
   private void setUserImage() {
      imageURL = MyPicturePreferenceUtils.getUsersImageURI(user);
   }
   
   private boolean isUserTableOwner()
   {
      if (tableOwner != null && SessionContext.findSessionContext() != null
            && SessionContext.findSessionContext().getUser() != null)
      {
         return tableOwner.getAccount().equals(SessionContext.findSessionContext().getUser().getAccount());
      }

      return true;
   }
}
