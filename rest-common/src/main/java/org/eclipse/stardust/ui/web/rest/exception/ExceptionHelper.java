/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.exception;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.ErrorCase;
import org.eclipse.stardust.common.error.IErrorMessageProvider;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.rest.service.dto.response.ErrorMessageDTO;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalErrorMessageProvider;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.springframework.stereotype.Component;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 * 
 */
@Component
public class ExceptionHelper
{

   private transient List<IErrorMessageProvider.Factory> translators;
   private static final Object SUMMARY_CONTEXT[] = {null, PortalErrorMessageProvider.SUMMARY_CONTEXT};
   private static final Object DETAIL_CONTEXT[] = {null, PortalErrorMessageProvider.DETAIL_CONTEXT};

   public ExceptionHelper()
   {
      translators = new ArrayList<IErrorMessageProvider.Factory>(
            ExtensionProviderUtils.getExtensionProviders(IErrorMessageProvider.Factory.class));
   }

   /**
    * @param exception
    * @param locale
    * @return
    */
   public ErrorMessageDTO getMessageFromProvider(Throwable exception, Locale locale)
   {
      return getMessageFromProvider(exception, locale,
            MessagePropertiesBean.getInstance().getString("common.unknownError"));
   }

   /**
    * @param exception
    * @param locale
    * @return
    */
   public ErrorMessageDTO getMessageFromProvider(Throwable exception, Locale locale, String defaultMessage)
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

         return getMessageFromProvider(errorCase, exception, locale);
      }

      return new ErrorMessageDTO(defaultMessage);
   }

   /**
    * @param errorCase
    * @param causedBy
    * @param locale
    * @return
    */
   public ErrorMessageDTO getMessageFromProvider(ErrorCase errorCase, Throwable causedBy, Locale locale)
   {
      if (errorCase != null)
      {
         Iterator<IErrorMessageProvider.Factory> tIter = translators.iterator();
         boolean usePortalContext = errorCase instanceof org.eclipse.stardust.ui.web.viewscommon.common.PortalErrorClass;
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
                  ErrorMessageDTO errorMessage = new ErrorMessageDTO(msg);
                  if (usePortalContext)
                  {
                     errorMessage.setDetailedMessage(msgProvider.getErrorMessage(errorCase, DETAIL_CONTEXT, locale));
                  }
                  return errorMessage;
               }
            }
         }
      }
      return null;
   }
}
