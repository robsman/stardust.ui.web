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
package org.eclipse.stardust.ui.web.viewscommon.dialogs;

import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;


/**
 * This class is originally created because at few places IceFaces panel confirmation does
 * not work. Try using IceFaces panelConfirmation tag before using this class
 * 
 * @author Yogesh.Manware
 * 
 */
public class PanelConfirmation extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;
   private String message;
   private String acceptLabel;
   private String cancelLabel;
   private CallbackHandler callbackHandler;
 
   /**
    * pass reInitialize=true if reset required
    * 
    * @param reInitialize
    * @return
    */
   public static PanelConfirmation getInstance(Boolean reInitialize)
   {
      PanelConfirmation panelConfirmation = (PanelConfirmation) FacesUtils.getBeanFromContext("panelConfirmation");
      if (reInitialize)
      {
         panelConfirmation.reset();
      }

      return panelConfirmation;
   }

   @Override
   public void reset()
   {
      super.reset();
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      setTitle(propsBean.getString("common.confirm"));
      message = propsBean.getString("common.panelConfirmation.message");
      acceptLabel = propsBean.getString("common.yes");
      cancelLabel = propsBean.getString("common.no");
     
   }

   @Override
   public void apply()
   {
      super.apply();
      if (null != callbackHandler)
      {
         callbackHandler.handleEvent(EventType.APPLY);
      }
      closePopup();
   }

   /**
    * @return the message
    */
   public String getMessage()
   {
      return message;
   }

   /**
    * @param message
    *           the message to set
    */
   public void setMessage(String message)
   {
      this.message = message;
   }

   /**
    * @return the acceptLabel
    */
   public String getAcceptLabel()
   {
      return acceptLabel;
   }

   /**
    * @param acceptLabel
    *           the acceptLabel to set
    */
   public void setAcceptLabel(String acceptLabel)
   {
      this.acceptLabel = acceptLabel;
   }

   /**
    * @return the cancelLabel
    */
   public String getCancelLabel()
   {
      return cancelLabel;
   }

   /**
    * @param cancelLabel
    *           the cancelLabel to set
    */
   public void setCancelLabel(String cancelLabel)
   {
      this.cancelLabel = cancelLabel;
   }

   /**
    * @param callbackHandler
    *           the callbackHandler to set
    */
   public void setCallbackHandler(CallbackHandler callbackHandler)
   {
      this.callbackHandler = callbackHandler;
   }

   @Override
   public void initialize()
   {}
}
