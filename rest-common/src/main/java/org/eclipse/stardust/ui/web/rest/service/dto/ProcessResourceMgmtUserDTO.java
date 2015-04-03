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

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

/**
 * @author Abhay.Thappan
 * @version
 */
@DTOClass
public class ProcessResourceMgmtUserDTO extends AbstractDTO
{

   public String userName;

   public String userOid;

   public Long roleCount;

   public Long directItemCount;

   public Long indirectItemCount;

   public Long itemCount;

   public String loggedIn;

   public String userId;

   /**
    * @param userName
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
   public ProcessResourceMgmtUserDTO(String userName, Long userOid, String userId, Long roleCount, Long directItemCount, Long indirectItemCount,
         Long itemCount, boolean loggedIn)
   {
      super();
      this.userName = userName;
      this.userOid = userOid.toString();
      this.userId = userId;
      this.roleCount = roleCount;
      this.directItemCount = directItemCount;
      this.indirectItemCount = indirectItemCount;
      this.itemCount = itemCount;
      if (loggedIn)
      {
         this.loggedIn = "Yes";
      }
      else
      {
         this.loggedIn = "No";
      }
   }
}
