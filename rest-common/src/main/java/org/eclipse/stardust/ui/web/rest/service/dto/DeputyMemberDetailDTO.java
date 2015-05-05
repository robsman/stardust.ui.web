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

import java.util.Date;
import java.util.List;

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;
import org.eclipse.stardust.ui.web.viewscommon.utils.MyPicturePreferenceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;

/**
 * @author Abhay.Thappan
 *
 */
@DTOClass
public class DeputyMemberDetailDTO extends AbstractDTO
{
   public User user;

   public String userDisplayName;

   public long userOID;

   public String avatarImageURI;

   public Long validFrom = null;

   public Long validTo = null;

   public boolean hasDeputies;

   public String hasDeputiesLabel;

   public boolean selected;

   public List<SelectItemDTO> participants;

   /**
    * @param user
    * @param validFrom
    * @param validTo
    * @param participants
    */
   public DeputyMemberDetailDTO(User user, Date validFrom, Date validTo, List<SelectItemDTO> participants)
   {
      this.user = user;
      this.userDisplayName = getUserDisplayName();
      this.userOID = user.getOID();
      this.avatarImageURI = getAvatarImageURI();
      if (validFrom != null)
      {
         this.validFrom = validFrom.getTime();
      }
      if (validTo != null)
      {
         this.validTo = validTo.getTime();
      }
      this.participants = participants;
   }

   /**
    * @param user
    * @param hasDeputies
    */
   public DeputyMemberDetailDTO(User user, boolean hasDeputies)
   {
      this.user = user;
      this.userDisplayName = getUserDisplayName();
      this.userOID = user.getOID();
      this.avatarImageURI = getAvatarImageURI();
      setHasDeputies(hasDeputies);
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
      if (null != this.user && obj instanceof DeputyMemberDetailDTO)
      {
         return this.user.getAccount().equals(((DeputyMemberDetailDTO) obj).user.getAccount());
      }

      return false;
   }
}
