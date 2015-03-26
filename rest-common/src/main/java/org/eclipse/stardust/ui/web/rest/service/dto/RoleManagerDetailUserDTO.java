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

import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

/**
 * @author Abhay.Thappan
 * @version $Revision: $
 */

@DTOClass
public class RoleManagerDetailUserDTO extends AbstractDTO
{
   public String userName;

   public String userOid;

   public String directItemCount;

   public String inDirectItemCount;

   public String loggedIn;

   public String roleCount;

   public UserItem userItem;

   public String totalItemCount;

   public boolean select;

   /**
    * @param directItemCount
    * @param inDirectItemCount
    * @param loggedIn
    * @param roleCount
    * @param userItem
    */
   public RoleManagerDetailUserDTO(String userName, String userOid,
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
         this.loggedIn = "Yes";
      }
      else
      {
         this.loggedIn = "No";
      }
      this.roleCount = roleCount;
      this.userItem = userItem;
   }
}
