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

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;


/**
 * @author ankita.Patel
 * @version $Revision: $
 */
public class RoleAssignmentTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;

   private String name;

   private String userId;

   private String userOid;

   private List<GrantsAssignmentTableEntry> grants;

   /**
    * 
    */
   public RoleAssignmentTableEntry()
   {}

   /**
    * @param userItem
    * @param grants
    */
   public RoleAssignmentTableEntry(User user, List<GrantsAssignmentTableEntry> grants)
   {
      super();
      this.userId = user.getId();
      this.userOid = String.valueOf(user.getOID());
      this.grants = grants;
      this.name = UserUtils.getUserDisplayLabel(user);
   }

   public List<GrantsAssignmentTableEntry> getGrants()
   {
      return grants;
   }

   public void setGrants(List<GrantsAssignmentTableEntry> grants)
   {
      this.grants = grants;
   }

   @Override
   public String toString()
   {
      return name + ":" + grants;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
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
