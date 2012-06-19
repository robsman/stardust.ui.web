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

import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;


public class UserManagerRoleAssignmentUserObject extends DefaultRowModel
{   
   private static final long serialVersionUID = -4053939578953086072L;

   private String roleName;

   private String itemsCount;

   private String roleAccount;

   private String itemsPerUser;
   
   private ModelParticipantInfo modelParticipantInfo;
   
   private boolean select;
   
   public UserManagerRoleAssignmentUserObject(String roleName, ModelParticipantInfo modelParticipantInfo, long itemsCount,
         long roleAccount, long itemsPerUser, boolean select)
   {
      super();
      this.select = select;
      this.roleName = roleName;
      this.modelParticipantInfo = modelParticipantInfo;
      this.itemsCount = Long.toString(itemsCount);
      this.roleAccount = Long.toString(roleAccount);
      this.itemsPerUser = Long.toString(itemsPerUser);     
   }

   public String getRoleName()
   {
      return roleName;
   }

   public void setRoleName(String roleName)
   {
      this.roleName = roleName;
   }

   public String getItemsCount()
   {
      return itemsCount;
   }

   public void setItemsCount(String itemsCount)
   {
      this.itemsCount = itemsCount;
   }

   public String getRoleAccount()
   {
      return roleAccount;
   }

   public void setRoleAccount(String roleAccount)
   {
      this.roleAccount = roleAccount;
   }

   public String getItemsPerUser()
   {
      return itemsPerUser;
   }

   public void setItemsPerUser(String itemsPerUser)
   {
      this.itemsPerUser = itemsPerUser;
   }

   public ModelParticipantInfo getModelParticipantInfo()
   {
      return modelParticipantInfo;
   }

   public void setModelParticipantInfo(ModelParticipantInfo modelParticipantInfo)
   {
      this.modelParticipantInfo = modelParticipantInfo;
   }

   public boolean isSelect()
   {
      return select;
   }

   public void setSelect(boolean select)
   {
      this.select = select;
   }
}
