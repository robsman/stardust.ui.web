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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.validator.ValidatorException;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.ErrorCase;
import org.eclipse.stardust.common.error.IErrorMessageProvider;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalErrorClass;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalErrorMessageProvider;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.common.exceptions.I18NException;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;



/**
 * @author Subodh.Godbole
 * 
 */
public class ExceptionHandler
{
   public static final String BEAN_NAME = "ippExceptionHandler";
   public static final String CLIENT_ID_NONE = "ClientIdNone";

   public static enum MessageDisplayMode
   {
      CUSTOM_AND_EXCEPTION_MSG, // Localized custom message and exception message to be displayed along with stack trace 
      ONLY_CUSTOM_MSG, // Only localized custom message to be displayed along with stack trace
      CUSTOM_MSG_OPTIONAL // Localized custom message to be displayed if standard message provider is not available along with stack trace
   }

   private final List<IErrorMessageProvider.Factory> translators;

   private Object SUMMARY_CONTEXT[] = {null, PortalErrorMessageProvider.SUMMARY_CONTEXT};
   private Object DETAIL_CONTEXT[] = {null, PortalErrorMessageProvider.DETAIL_CONTEXT};

   /**
    * 
    */
   public ExceptionHandler()
   {
      translators = new ArrayList<IErrorMessageProvider.Factory>(ExtensionProviderUtils
            .getExtensionProviders(IErrorMessageProvider.Factory.class));
   }

   private static ExceptionHandler getInstance()
   {
      return (ExceptionHandler) org.eclipse.stardust.ui.web.common.util.FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * @param exception
    */
   public static void handleException(Exception exception)
   {
      handleException(exception, null);
   }

   /**
    * @param exception
    * @param customMsg
    */
   public static void handleException(Exception exception, String customMsg)
   {
      handleException(exception, customMsg, MessageDisplayMode.CUSTOM_MSG_OPTIONAL, FacesUtils.getLocaleFromView());
   }

   /**
    * @param exception
    * @param customMsg
    * @param displayMode
    */
   public static void handleException(Exception exception, String customMsg, MessageDisplayMode displayMode)
   {
      handleException(exception, customMsg, displayMode, FacesUtils.getLocaleFromView());
   }

   /**
    * @param exception
    * @param customMsg
    * @param displayMode
    * @param locale
    */
   public static void handleException(Exception exception, String customMsg, MessageDisplayMode displayMode, Locale locale)
   {
      handleException(null, exception, customMsg, displayMode, locale);
   }

   /**
    * @param clientId
    * @param customMsg
    *           - should be localized message
    */
   public static void handleException(String clientId, String customMsg)
   {
      handleException(clientId, null, customMsg, MessageDisplayMode.ONLY_CUSTOM_MSG);
   }

   /**
    * @param clientId
    * @param exception
    */
   public static void handleException(String clientId, Exception exception)
   {
      handleException(clientId, exception, null, MessageDisplayMode.CUSTOM_MSG_OPTIONAL);
   }

   /**
    * @param clientId
    * @param exception
    * @param customMsg
    * @param displayMode
    */
   public static void handleException(String clientId, Exception exception, String customMsg,
         MessageDisplayMode displayMode)
   {
      handleException(clientId, exception, customMsg, displayMode, FacesUtils.getLocaleFromView());
   }

   /**
    * @param clientId
    * @param exception
    * @param customMsg
    * @param displayMode
    * @param locale
    */
   public static void handleException(String clientId, Exception exception, String customMsg,
         MessageDisplayMode displayMode, Locale locale)
   {
      FacesMessage facesMsg = getInstance().getFacesMessage(exception, customMsg, displayMode, locale);

      if (null != facesMsg)
      {
         if (null == clientId) // Client id null. Show Dialog
         {
            String messageToShow = "";
   
            String summary = facesMsg.getSummary();
            String details = facesMsg.getDetail();
            if (StringUtils.isNotEmpty(summary))
            {
               messageToShow = messageToShow + "\n" + summary;
            }
            if (StringUtils.isNotEmpty(details) && !messageToShow.contains(details))
            {
               messageToShow = messageToShow + "\n" + details;
            }
            
            if (FacesMessage.SEVERITY_ERROR == facesMsg.getSeverity())
            {
               if (exception instanceof I18NException || exception instanceof ResourceNotFoundException)
               {
                  MessageDialog.addErrorMessage(messageToShow);
               }
               else
               {
                  MessageDialog.addErrorMessage(messageToShow, exception);
               }
            }
            else if (FacesMessage.SEVERITY_INFO == facesMsg.getSeverity())
            {
               MessageDialog.addInfoMessage(messageToShow);
            }
            else if (FacesMessage.SEVERITY_WARN == facesMsg.getSeverity())
            {
               MessageDialog.addWarningMessage(messageToShow);
            }
         }
         else
         {
            if (StringUtils.isEmpty(clientId) || clientId.equals(CLIENT_ID_NONE))
            {
               clientId = null;
            }
            else if (StringUtils.isNotEmpty(clientId) && !clientId.contains(":")) // Fully qualified client Id
            {
               clientId = FacesUtils.getClientId(clientId);
            }
           
            FacesUtils.showFacesMessage(clientId, facesMsg);
         }
      }
   }

   /**
    * @param exception
    * @return
    * NOTE: instead of getting exception as string try using handleException() which
    *             will also display error on UI
    */
   public static String getExceptionMessage(Exception exception)
   {
      FacesMessage facesMessage = getInstance().getFacesMessage(exception, null, null, FacesUtils.getLocaleFromView());
      String message = facesMessage.getSummary();
      if (StringUtils.isEmpty(message))
      {
         message = facesMessage.getDetail();
      }

      return message;
   }

   /**
    * @param exception
    * @return
    */
   public static FacesMessage getFacesMessage(Exception exception)
   {
      return getInstance().getFacesMessage(exception, null, null, FacesUtils.getLocaleFromView());
   }

   /**
    * @param exception
    * @param customMsg
    * @param displayMode
    * @param locale
    * @return
    */
   protected FacesMessage getFacesMessage(Exception exception, String customMsg, MessageDisplayMode displayMode,
         Locale locale)
   {
      FacesMessage facesMessage = null;
      if (MessageDisplayMode.ONLY_CUSTOM_MSG.equals(displayMode))
      {
         facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, customMsg, null);
      }
      else
      {
         if (exception instanceof PortalException || exception instanceof ApplicationException)
         {
            ErrorCase errorCase = null;
            if (exception instanceof PortalException)
            {
               PortalException pe = (PortalException) exception;
               errorCase = pe.getErrorClass();
            }
            else if (exception instanceof ApplicationException)
            {
               errorCase = ((ApplicationException) exception).getError();
            }
            facesMessage = getMessageFromProvider(errorCase, exception, locale);
         }
         else if (exception instanceof ValidatorException)
         {
            facesMessage = ((ValidatorException) exception).getFacesMessage();
         }
         else if (exception instanceof I18NException)
         {
            String message = ((I18NException) exception).getMessage();
            facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null);
         }
         else if (exception instanceof ResourceNotFoundException)
         {
            String message = (MessagesViewsCommonBean.getInstance()
                  .getString("views.documentView.documentNotFoundError"));
            facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null);
         }

         if (null == facesMessage)
         {
            String msg = null;

            if (StringUtils.isNotEmpty(customMsg)
                  && (MessageDisplayMode.CUSTOM_MSG_OPTIONAL.equals(displayMode) || MessageDisplayMode.CUSTOM_AND_EXCEPTION_MSG
                        .equals(displayMode)))
            {
               msg = customMsg;
            }
            else
            {
               msg = MessagePropertiesBean.getInstance().getString("common.unknownError");
            }

            facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
         }
         else if (MessageDisplayMode.CUSTOM_AND_EXCEPTION_MSG.equals(displayMode))
         {
            facesMessage.setSummary(customMsg + "\n" + facesMessage.getSummary());
         }
      }
      
      return facesMessage;
   }

   /**
    * @param errorCase
    * @param causedBy
    * @param locale
    * @return
    */
   protected FacesMessage getMessageFromProvider(ErrorCase errorCase, Exception causedBy, Locale locale)
   {
      if (errorCase != null)
      {
         Iterator<IErrorMessageProvider.Factory> tIter = translators.iterator();
         boolean usePortalContext = errorCase instanceof PortalErrorClass;
         Object context[] = usePortalContext ? SUMMARY_CONTEXT : new Object[1];
         context[0] = causedBy;

         while (tIter.hasNext())
         {
            IErrorMessageProvider.Factory msgFactory = (IErrorMessageProvider.Factory) tIter.next();
            IErrorMessageProvider msgProvider = msgFactory.getProvider(errorCase);
            if (msgProvider != null)
            {
               String msg = msgProvider.getErrorMessage(errorCase, context, locale);
               if (null != errorCase)
               {
                  msg = errorCase.getId() + " - " + msg;
               }
               if (!StringUtils.isEmpty(msg))
               {
                  FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
                  if (usePortalContext)
                  {
                     facesMsg.setDetail(msgProvider.getErrorMessage(errorCase, DETAIL_CONTEXT, locale));
                  }
                  return facesMsg;
               }
            }
         }
      }
      return null;
   }
}