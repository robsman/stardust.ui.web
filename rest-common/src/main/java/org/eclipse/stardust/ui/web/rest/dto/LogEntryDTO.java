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
/**
 * @author Abhay.Thappan
 */
package org.eclipse.stardust.ui.web.rest.dto;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.LogEntryDetails;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.rest.dto.core.DTOClass;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;

@DTOClass
public class LogEntryDTO extends AbstractDTO
{
   public Long timeStamp;

   public String subject;

   public String type;

   public String code;

   public Long userOID;

   public String context;

   public String account;

   public boolean linkDisabled;

   public LogEntryDTO(LogEntryDetails logEntry)
   {
      timeStamp = logEntry.getTimeStamp().getTime();
      subject = logEntry.getSubject();
      type = logEntry.getType().getName();
      code = logEntry.getCode().getName();
      context = logEntry.getContext();
      userOID = logEntry.getUserOID();
      initUser(logEntry.getUser());
   }

   /**
    * set user
    * 
    * @param user
    */
   private void initUser(User user)
   {
      String accountName = "";
      if (userOID == 0 || null == user)
      {
         linkDisabled = true;
      }
      User loggedInUser = SessionContext.findSessionContext().getUser();
      if ((null != user) && !user.getAccount().equals(loggedInUser.getAccount()))
      {
         UserUtils.loadDisplayPreferenceForUser(user);
         accountName = UserUtils.getUserDisplayLabel(user);
      }
      else
      {
         accountName = UserUtils.getUserDisplayLabel(loggedInUser);
      }

      if (!linkDisabled && StringUtils.isNotEmpty(accountName))
      {
         int charIndex = accountName.indexOf(":");
         accountName = accountName.substring(charIndex + 1, accountName.length());
      }
      account = accountName;
   }
}