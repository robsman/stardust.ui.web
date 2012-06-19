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
package org.eclipse.stardust.ui.web.common.message;

import java.io.Serializable;
import java.util.Date;

import org.eclipse.stardust.ui.web.common.spi.user.User;
import org.eclipse.stardust.ui.web.common.util.DateUtils;



/**
 * @author Subodh.Godbole
 *
 */
public class AlertEntry implements Serializable
{
   private static final long serialVersionUID = 1L;

   public static int MAX_TEXT_LENGTH = 30;
   
   private String iconUrl;
   private User senderUser; // TODO: This can be System
   private String text;
   private Date timeStamp;
   private AlertHandler alertHandler;
   private boolean shownToUser;
   private Object payload;

   /**
    * 
    */
   public AlertEntry()
   {
   }

   /**
    * @param iconUrl
    * @param senderUser
    * @param text
    * @param timeStamp
    * @param alertHandler
    */
   public AlertEntry(String iconUrl, User senderUser, String text, Date timeStamp,
         AlertHandler alertHandler)
   {
      this.iconUrl = iconUrl;
      this.senderUser = senderUser;
      this.text = text;
      this.timeStamp = timeStamp;
      this.alertHandler = alertHandler;
   }

   /**
    * @param iconUrl
    * @param senderUser
    * @param text
    * @param alertHandler
    */
   public AlertEntry(String iconUrl, User senderUser, String text,
         AlertHandler alertHandler)
   {
      this(iconUrl, senderUser, text, new Date(), alertHandler);
   }

   /**
    * @return
    */
   public String getHumanReadableTimeStamp()
   {
      return DateUtils.getHumanReadableTimeStamp(timeStamp);
   }

   /**
    * @return
    */
   public String getSenderFullName()
   {
      if(senderUser != null)
      {
         return senderUser.getDisplayName();
      }
      return "NA";
   }
   
   /**
    * @return
    */
   public String getDisplayText()
   {
      if(text.length() > MAX_TEXT_LENGTH)
      {
         String str = text.substring(0, MAX_TEXT_LENGTH);
         str += "...";
         return str;
      }
      
      return text;
   }
   
   /**
    * @return
    */
   public AlertEntry getClone()
   {
      AlertEntry clone = new AlertEntry(iconUrl, senderUser, text, timeStamp, alertHandler);
      clone.setPayload(getPayload());
      return clone;
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if(null != obj && obj instanceof AlertEntry)
      {
         AlertEntry otherAlert = ((AlertEntry)obj);

         if (this.getText() != null && this.getText().equals(otherAlert.getText())
               && this.getTimeStamp() != null && this.getTimeStamp().equals(otherAlert.getTimeStamp()))
         {
            return true;
         }
      }

      return false;
   }

   public String getIconUrl()
   {
      return iconUrl;
   }
   
   public void setIconUrl(String iconUrl)
   {
      this.iconUrl = iconUrl;
   }
   
   public User getSenderUser()
   {
      return senderUser;
   }
   
   public void setSenderUser(User senderUser)
   {
      this.senderUser = senderUser;
   }

   public String getText()
   {
      return text;
   }
   
   public void setText(String text)
   {
      this.text = text;
   }
   
   public Date getTimeStamp()
   {
      return timeStamp;
   }
   
   public void setTimeStamp(Date timeStamp)
   {
      this.timeStamp = timeStamp;
   }

   public AlertHandler getAlertHandler()
   {
      return alertHandler;
   }

   public void setAlertHandler(AlertHandler alertHandler)
   {
      this.alertHandler = alertHandler;
   }

   public Object getPayload()
   {
      return payload;
   }

   public void setPayload(Object payload)
   {
      this.payload = payload;
   }

   public boolean isShownToUser()
   {
      return shownToUser;
   }

   public void setShownToUser(boolean shownToUser)
   {
      this.shownToUser = shownToUser;
   }
}
