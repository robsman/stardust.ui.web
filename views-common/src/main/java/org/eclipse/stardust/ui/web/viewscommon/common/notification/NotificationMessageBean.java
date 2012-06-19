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
