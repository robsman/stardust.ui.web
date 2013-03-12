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
package org.eclipse.stardust.ui.web.admin.views;

import java.util.Date;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.api.runtime.LogType;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;

/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class LogTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;
   private static final String TYPE_PREFIX = "views.overview.logTable.type.";
   private static final String CODE_PREFIX = "views.overview.logTable.code.";

   private Date timeStamp;

   private String type;

   private String code;

   private String context;

   private String subject;

   private String user;

   private Long userOID;

   private String toolTipSubject;
   
   private boolean linkDisabled;

   /**
    * 
    */
   public LogTableEntry()
   {}

   /**
    * @param timeStamp
    * @param type
    * @param code
    * @param context
    * @param subject
    * @param user
    */
   public LogTableEntry(Date timeStamp, LogType type, LogCode code, String context,
         String subject, User user, Long userOID)
   {
      super();
      AdminMessagesPropertiesBean propsBean = AdminMessagesPropertiesBean.getInstance();
      this.timeStamp = timeStamp;
      this.type = propsBean.getString(TYPE_PREFIX + type.getValue());
      this.code = propsBean.getString(CODE_PREFIX + code.getValue());
      this.context = context;
      this.subject = subject;
      this.userOID = userOID;
      initUser(user);
      this.toolTipSubject = subject;
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
      if (!user.getAccount().equals(loggedInUser.getAccount()))
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
      this.user = accountName;
   }
   
   public Date getTimeStamp()
   {
      return timeStamp;
   }

   public String getType()
   {
      return type;
   }

   public String getCode()
   {
      return code;
   }

   public String getContext()
   {
      return context;
   }

   public String getSubject()
   {
      if (subject.length() > 50)
      {
         return subject.substring(0, 50).concat("...");
      }
      return subject;
   }

   public String getUser()
   {
      return user;
   }

   public Long getUserOID()
   {
      return userOID;
   }

   public String getToolTipSubject()
   {
      return toolTipSubject;
   }

   public boolean isLinkDisabled()
   {
      return linkDisabled;
   }
}