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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.ui.web.common.app.PortalUiController;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.PopupDialog;
import org.eclipse.stardust.ui.web.common.util.StringUtils;


/**
 * @author Subodh.Godbole
 *
 */
public class MessageDialog extends PopupDialog
{
   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager.getLogger(MessageDialog.class);
   public static enum MessageType
   {
      INFO,
      WARNING,
      ERROR
   }

   private MessageType messageType;
   private String details;
   private List<String> detailsLines;
   private Throwable exception;

   private PortalUiController portalUiController;
   private MessageDialogHandler callbackHandler;

   /**
    * 
    */
   public MessageDialog()
   {
      super(""); // Title not known at this stage
      firePerspectiveEvents = true;
      modal = true;
   }
   
   public void openPopup()
   {
	   String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
	   if (StringUtils.isNotEmpty(viewId))
	   {
		   if (viewId.endsWith("/portalSingleViewLaunchPanelsOnly.xhtml"))
		   {
			   fromlaunchPanels = true;
		   }
	   }
	   
	   super.openPopup();
   }

   /**
    * @return
    */
   public static MessageDialog getInstance()
   {
      return (MessageDialog) FacesUtils.getBeanFromContext("ippMessageDialog");
   }

   public PortalUiController getPortalUiController()
   {
      return portalUiController;
   }

   public void setPortalUiController(PortalUiController portalUiController)
   {
      this.portalUiController = portalUiController;
   }

   /**
    * @param messageType
    * @param title
    * @param details
    */
   public static void addMessage(MessageType messageType, String title, String details)
   {
      MessageDialog msgbean = getInstance();
      msgbean.title = title;
      msgbean.messageType = messageType;
      msgbean.details = details;
      msgbean.setDetailsLines();
      msgbean.openPopup();
   }
   
   /**
    * @param messageType
    * @param title
    * @param exception
    */
   public static void addMessage(MessageType messageType, String title, Throwable exception)
   {
      MessageDialog msgbean = getInstance();
      msgbean.title = title;
      msgbean.messageType = messageType;
      msgbean.exception = exception;
      msgbean.logException();
      msgbean.openPopup();
   }
   
   /**
    * @param messageType
    * @param title
    * @param details
    * @param exception
    */
   public static void addMessage(MessageType messageType, String title, String details, Throwable exception)
   {
      MessageDialog msgbean = getInstance();
      msgbean.title = title;
      msgbean.details = details;
      msgbean.setDetailsLines();
      msgbean.messageType = messageType;
      msgbean.exception = exception;
      msgbean.logException();
      msgbean.openPopup();
   }
   
   private void logException()
   {
      if (null != this.exception)
      {
         switch (messageType)
         {
         case ERROR:
            trace.error(this.exception);
            break;

         case WARNING:
            trace.warn(this.exception);
            break;

         case INFO:
            trace.info(this.exception);
            break;

         default:
            break;
         }
      }
   }
   
   /**
    * For backwards compatibility
    * @param exception
    * @deprecated use addErrorMessage(...)
    */
   public static void addMessage(Throwable exception)
   {
      addMessage(MessageType.ERROR, getMessage("common.error"), exception);
   }
   
   /**
    * @param exception
    * @deprecated use ExceptionHandler.handleException(...)
    */
   public static void addErrorMessage(Throwable exception)
   {
      addMessage(MessageType.ERROR, getMessage("common.error"), exception);
   }

   /**
    * @param details
    */
   public static void addErrorMessage(String details)
   {
      addMessage(MessageType.ERROR, getMessage("common.error"), details);
   }
   
   /**
    * @param details
    * @param exception
    */
   public static void addErrorMessage(String details, Throwable exception)
   {
      addMessage(MessageType.ERROR, getMessage("common.error"), details, exception);
   }

   /**
    * @param details
    */
   public static void addInfoMessage(String details)
   {
      addMessage(MessageType.INFO, getMessage("common.info"), details);
   }


   /**
    * @param details
    */
   public static void addWarningMessage(String details, Exception e)
   {
      addMessage(MessageType.WARNING, getMessage("common.warning"), details, e);
   }
   
   /**
    * @param details
    */
   public static void addWarningMessage(String details)
   {
      addMessage(MessageType.WARNING, getMessage("common.warning"), details);
   }

   /**
    * 
    */
   public static void clearMessages()
   {
      MessageDialog msgbean = getInstance();
      msgbean.title = null;
      msgbean.messageType = null;
      msgbean.details = null;
      msgbean.exception = null;
   }
   
   @Override
   public void closePopup()
   {
      clearMessages();
      if (null != callbackHandler)
      {
         callbackHandler.accept();
      }
      super.closePopup();
   }
   
   @Override
   public void apply()
   {
   }

   @Override
   public void reset()
   {
   }

   public String getStackTrace()
   {
      StringBuffer sb = new StringBuffer();
      
      if(exception != null)
      {
         StringWriter sw = new StringWriter();
         exception.printStackTrace(new PrintWriter(sw));
         sb.append(sw);
      }
      
      return sb.toString();
   }

   /**
    * @return
    */
   private void setDetailsLines()
   {
      detailsLines = new ArrayList<String>();
      if(StringUtils.isNotEmpty(details))
      {
         StringTokenizer st = new StringTokenizer(details, "\n");
         while(st.hasMoreTokens())
         {
            detailsLines.add(st.nextToken());
         }
      }
   }
   
   private static String getMessage(String key)
   {
      MessagePropertiesBean messageBean = MessagePropertiesBean.getInstance();
      return messageBean.getString(key);
   }
   
   public MessageType getMessageType()
   {
      return messageType;
   }

   public String getDetails()
   {
      return details;
   }

   public List<String> getDetailsLines()
   {
      return detailsLines;
   }
   
   public Throwable getException()
   {
      return exception;
   }

   public MessageDialogHandler getCallbackHandler()
   {
      return callbackHandler;
   }

   public void setCallbackHandler(MessageDialogHandler callbackHandler)
   {
      this.callbackHandler = callbackHandler;
   }
   
   
   
}
