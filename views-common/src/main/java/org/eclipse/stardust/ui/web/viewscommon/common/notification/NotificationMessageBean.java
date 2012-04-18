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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;

/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class NotificationMessageBean extends PopupUIComponentBean
{
   public static enum ButtonType {
      YES_NO, OK_CANCEL, OK, CLOSE
   }
   
   private static final long serialVersionUID = 1L;
   List<NotificationMessage> notifications;
   private ButtonType buttonType = ButtonType.CLOSE;
   private ICallbackHandler callbackHandler ;

   /**
    * 
    */
   public NotificationMessageBean()
   {
      notifications = new ArrayList<NotificationMessage>();
   }

   
   /**
    * @param successNotifications
    * @param successTitle
    * @param failureNotifications
    * @param failureTitle
    * @param itemTitle
    * @param itemStatusTitle
    * @return
    */
   public static boolean showNotifications(List<NotificationItem> successNotifications, String successTitle,
         List<NotificationItem> failureNotifications, String failureTitle, String itemTitle, String itemStatusTitle, ICallbackHandler callbackHandler)
   {
      if ((CollectionUtils.isNotEmpty(successNotifications)) || (CollectionUtils.isNotEmpty(failureNotifications)))
      {
         NotificationMessageBean notificationMB = NotificationMessageBean.getCurrent();
         notificationMB.setCallbackHandler(callbackHandler);
         // Aborted processes
         NotificationMessage notificationMessage = new NotificationMessage();
         if (CollectionUtils.isNotEmpty(successNotifications))
         {
            notificationMessage.setMessage(successTitle);
            notificationMessage.setKeyTitle(itemTitle);
            notificationMessage.setValueTitle(itemStatusTitle);
            notificationMessage.setNotificationItem(successNotifications);
            notificationMB.add(notificationMessage);
         }

         // Skipped Processes
         notificationMessage = new NotificationMessage();
         if (CollectionUtils.isNotEmpty(failureNotifications))
         {
            notificationMessage.setMessage(failureTitle);
            notificationMessage.setKeyTitle(itemTitle);
            notificationMessage.setValueTitle(itemStatusTitle);
            notificationMessage.setNotificationItem(failureNotifications);
            notificationMB.add(notificationMessage);
         }
         if (!notificationMB.getNotifications().isEmpty())
         {
            notificationMB.openPopup();
            return true;
         }
      }
      return false;
   }
   
   /**
    * @param successNotifications
    * @param successTitle
    * @param failureNotifications
    * @param failureTitle
    */
   public static boolean showNotifications(List<NotificationItem> successNotifications, String successTitle,
         List<NotificationItem> failureNotifications, String failureTitle)
   {
      return showNotifications(successNotifications, successTitle, failureNotifications, failureTitle, null, null, null);
   }
   
  /**
   * 
   * @param successNotifications
   * @param successTitle
   * @param failureNotifications
   * @param failureTitle
   * @param callbackHandler
   * @return
   */
   public static boolean showNotifications(List<NotificationItem> successNotifications, String successTitle,
         List<NotificationItem> failureNotifications, String failureTitle, ICallbackHandler callbackHandler)
   {
      return showNotifications(successNotifications, successTitle, failureNotifications, failureTitle, null, null,callbackHandler);
   }
   
   /**
    * Closes popup and clears list
    */
   public void closePopup()
   {
      notifications.clear();
      super.closePopup();

      // Fire Event
      if (null != callbackHandler)
      {
         callbackHandler.handleEvent(EventType.APPLY);
      }
      callbackHandler = null;
   }
   
   public void apply()
   {
      closePopup();
      if(null != callbackHandler)
      {        
         callbackHandler.handleEvent(EventType.APPLY);
      }
      
   }

   /**
    * @return
    */
   public static NotificationMessageBean getCurrent()
   {
      return (NotificationMessageBean) FacesUtils.getBeanFromContext("notificationBean");
   }

   public void add(NotificationMessage notificationMessage)
   {
      notifications.add(notificationMessage);
   }

   public List<NotificationMessage> getNotifications()
   {
      return notifications;
   }

   public void setNotifications(List<NotificationMessage> notifications)
   {
      this.notifications = notifications;
   }

   @Override
   public void initialize()
   {     

   }

   public ButtonType getButtonType()
   {
      return buttonType;
   }

   public void setButtonType(ButtonType buttonType)
   {
      this.buttonType = buttonType;
   }   
   

   public ICallbackHandler getCallbackHandler()
   {
      return callbackHandler;
   }

   public void setCallbackHandler(ICallbackHandler callbackHandler)
   {
      this.callbackHandler = callbackHandler;
   }
}
