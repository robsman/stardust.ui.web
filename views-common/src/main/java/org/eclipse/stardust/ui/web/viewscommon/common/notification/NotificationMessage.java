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
package org.eclipse.stardust.ui.web.viewscommon.common.notification;

import java.util.List;

/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class NotificationMessage
{
   private String message;
   private String keyTitle;
   private String valueTitle;
   private List<NotificationItem> notificationItem;
   private boolean displayNoRecordsFound = true;
   private boolean showColonAtEnd = true;

   /**
    * 
    */
   public NotificationMessage()
   {

   }

   public String getMessage()
   {
      return message;
   }

   public void setMessage(String message)
   {
      this.message = message;
   }

   public List<NotificationItem> getNotificationItem()
   {
      return notificationItem;
   }

   public void setNotificationItem(List<NotificationItem> notificationItem)
   {
      this.notificationItem = notificationItem;
   }

   public String getKeyTitle()
   {
      return keyTitle;
   }

   public void setKeyTitle(String keyTitle)
   {
      this.keyTitle = keyTitle;
   }

   public String getValueTitle()
   {
      return valueTitle;
   }

   public void setValueTitle(String valueTitle)
   {
      this.valueTitle = valueTitle;
   }

   public boolean isDisplayNoRecordsFound()
   {
      return displayNoRecordsFound;
   }

   public void setDisplayNoRecordsFound(boolean displayNoRecordsFound)
   {
      this.displayNoRecordsFound = displayNoRecordsFound;
   }

   public boolean isShowColonAtEnd()
   {
      return showColonAtEnd;
   }

   public void setShowColonAtEnd(boolean showColonAtEnd)
   {
      this.showColonAtEnd = showColonAtEnd;
   }

}
