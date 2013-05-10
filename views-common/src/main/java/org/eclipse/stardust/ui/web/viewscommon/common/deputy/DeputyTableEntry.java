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
package org.eclipse.stardust.ui.web.viewscommon.common.deputy;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.table.IRowModel;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MyPicturePreferenceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;

/**
 * @author Subodh.Godbole
 *
 */
public class DeputyTableEntry implements IRowModel
{
   private User user;
   private String userDisplayName;
   private String avatarImageURI;

   private Date validFrom;
   private Date validTo;

   private boolean hasDeputies;
   private String hasDeputiesLabel;

   private boolean selected;
   private Set<ModelParticipantInfo> participants;

   /**
    * @param user
    * @param validFrom
    * @param validTo
    * @param participants
    */
   public DeputyTableEntry(User user, Date validFrom, Date validTo, Set<ModelParticipantInfo> participants)
   {
      this.user = user;
      this.validFrom = validFrom;
      this.validTo = validTo;
      setParticipants(participants);
   }

   /**
    * @param user
    * @param hasDeputies
    */
   public DeputyTableEntry(User user, boolean hasDeputies)
   {
      this.user = user;
      setHasDeputies(hasDeputies);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.table.IRowModel#getStyleClass()
    */
   public String getStyleClass()
   {
      return null;
   }

   public String getUserDisplayName()
   {
      if (null != user && StringUtils.isEmpty(userDisplayName))
      {
         userDisplayName = UserUtils.getUserDisplayLabel(user);
      }

      return userDisplayName;
   }

   /**
    * @return
    */
   public DeputyTableEntry getClone()
   {
      DeputyTableEntry clone = new DeputyTableEntry(user, validFrom, validTo, participants);
      return clone;
   }

   /**
    * @return
    */
   public String getAvatarImageURI()
   {
      if (StringUtils.isEmpty(avatarImageURI))
      {
         avatarImageURI = MyPicturePreferenceUtils.getUsersImageURI(user);
      }
      return avatarImageURI;
   }

   /**
    * @param hasDeputies
    */
   public void setHasDeputies(boolean hasDeputies)
   {
      this.hasDeputies = hasDeputies;
      this.hasDeputiesLabel = hasDeputies
            ? MessagePropertiesBean.getInstance().getString("common.yes")
            : MessagePropertiesBean.getInstance().getString("common.no");
   }

   @Override
   public boolean equals(Object obj)
   {
      if (null != this.getUser() && obj instanceof DeputyTableEntry)
      {
         return this.getUser().getAccount().equals(((DeputyTableEntry) obj).getUser().getAccount());
      }

      return false;
   }

   public User getUser()
   {
      return user;
   }

   public void setUser(User user)
   {
      this.user = user;
   }

   public Date getValidFrom()
   {
      return validFrom;
   }

   public void setValidFrom(Date validFrom)
   {
      this.validFrom = validFrom;
   }

   public Date getValidTo()
   {
      return validTo;
   }

   public void setValidTo(Date validTo)
   {
      this.validTo = validTo;
   }

   public Set<ModelParticipantInfo> getParticipants()
   {
      return participants;
   }

   public void setParticipants(Set<ModelParticipantInfo> participants)
   {
      this.participants = (null != participants) ? participants : new HashSet<ModelParticipantInfo>();
   }

   public boolean isHasDeputies()
   {
      return hasDeputies;
   }

   public String getHasDeputiesLabel()
   {
      return hasDeputiesLabel;
   }

   public boolean isSelected()
   {
      return selected;
   }

   public void setSelected(boolean selected)
   {
      this.selected = selected;
   }
}
