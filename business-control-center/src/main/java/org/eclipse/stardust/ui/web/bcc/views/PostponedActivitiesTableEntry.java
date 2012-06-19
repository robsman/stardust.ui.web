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
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;



/**
 * @author ankita.Patel
 * @version $Revision: $
 */
public class PostponedActivitiesTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;

   private String name;

   private UserItem userItem;

   private String userId;

   private String userOid;

   private List<ParticipantsTableEntry> participantList;

   public PostponedActivitiesTableEntry(UserItem userItem, String userId, String userOid,
         List<ParticipantsTableEntry> participantList)
   {
      super();
      this.userId = userId;
      this.userOid = userOid;
      this.userItem = userItem;
      this.participantList = participantList;
      this.name = UserUtils.getUserDisplayLabel(userItem.getUser());
   }

   public List<ParticipantsTableEntry> getParticipantList()
   {
      return participantList;
   }

   public void setParticipantList(List<ParticipantsTableEntry> participantList)
   {
      this.participantList = participantList;
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

   public UserItem getUserItem()
   {
      return userItem;
   }

   public void setUserItem(UserItem userItem)
   {
      this.userItem = userItem;
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
