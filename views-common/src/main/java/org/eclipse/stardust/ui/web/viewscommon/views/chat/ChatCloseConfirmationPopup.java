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
package org.eclipse.stardust.ui.web.viewscommon.views.chat;

import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;

public class ChatCloseConfirmationPopup extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;

   private static final String BEAN_NAME = "chatCloseConfirmationPopup";

   private boolean confirmed = false;
   
   private boolean isViewCloseRequested=false;
   
   private View chatView;

   private String includePath;

   public ChatCloseConfirmationPopup()
   {
      this("");
   }

   public ChatCloseConfirmationPopup(String title)
   {
      super(title);
   }

   public static ChatCloseConfirmationPopup getCurrent()
   {
      return (ChatCloseConfirmationPopup) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   @Override
   public void apply()
   {
       confirmed = true;       
       if(isViewCloseRequested && null!=chatView)
       {
          PortalApplication.getInstance().closeView(chatView, true);
       }
       closePopup(); 
   }   

   @Override
   public void openPopup()
   {
      confirmed = false;
      super.openPopup();
   }  

   @Override
   public void closePopup()
   {
      isViewCloseRequested=false;
      chatView=null;
      super.closePopup();
   }

   public String getIncludePath()
   {
      return includePath;
   }

   public void setIncludePath(String includePath)
   {
      this.includePath = includePath;
   }

   public boolean isConfirmed()
   {
      return confirmed;
   }   

   
   public void setViewCloseRequested(boolean isViewCloseRequested)
   {
      this.isViewCloseRequested = isViewCloseRequested;
   }

   @Override
   public void initialize()
   {}

   public void setChatView(View chatView)
   {
      this.chatView = chatView;
   }
   
}
