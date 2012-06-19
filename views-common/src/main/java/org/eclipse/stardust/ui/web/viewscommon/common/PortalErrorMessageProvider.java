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
package org.eclipse.stardust.ui.web.viewscommon.common;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.*;


public class PortalErrorMessageProvider implements IErrorMessageProvider
{
   private final static PortalErrorMessageProvider INSTANCE =
      new PortalErrorMessageProvider();
   
   public final static String SUMMARY_CONTEXT = "summary";
   public final static String DETAIL_CONTEXT = "detail";

   public static final String MSG_BUNDLE_NAME = "ipp-bpm-portal-errors";
   private Map bundles = new ConcurrentHashMap();
   
   public String getErrorMessage(ErrorCase errorCase, Object[] context, Locale locale)
   {
      ResourceBundle messages = ErrorMessageUtils.getErrorBundle(bundles, MSG_BUNDLE_NAME, locale);
      String msg = null;
      if(errorCase instanceof PortalErrorClass)
      {
         PortalErrorClass pec = (PortalErrorClass) errorCase;
         Context contextParam = new Context(context);
         if(context.length == 0 || contextParam.contains(SUMMARY_CONTEXT))
         {
            msg = ErrorMessageUtils.getErrorMessage(messages, pec);
            if(StringUtils.isEmpty(msg))
            {
               msg = pec.getLocalizedMessage(locale);
            }
         }
         else if(contextParam.contains(DETAIL_CONTEXT))
         {
            if(pec.isDetailAvailable())
            {
               PortalErrorClass detailPec = pec.getDetailErrorClass();
               msg = ErrorMessageUtils.getErrorMessage(messages, detailPec);
               if(StringUtils.isEmpty(msg))
               {
                  msg = detailPec.getLocalizedMessage(locale);
               }
            }
         }
      }
      return msg;
   }

   public String getErrorMessage(ApplicationException exception, Locale locale)
   {
      String msg = null;
      if(exception.getError() != null)
      {
         msg = getErrorMessage(exception.getError(), new Object[0], locale);
      }
      else if(exception instanceof AccessForbiddenException)
      {
         msg = Localizer.getString(LocalizerKey.ACCESS_FORBIDDEN);
         
      }
      return StringUtils.isEmpty(msg) ? exception.getLocalizedMessage() : msg;
   }

   private static class Context
   {
      private Object[] context;
      
      public Context(Object[] context)
      {
         this.context = context;
      }
      
      public boolean contains(Object contextKey)
      {
         for (int i = 0; i < context.length; i++)
         {
            if(contextKey.equals(context[i]))
            {
               return true;
            }
         }
         return false;
      }
   }
   
   public static class Factory implements IErrorMessageProvider.Factory
   {

      public IErrorMessageProvider getProvider(ErrorCase errorCase)
      {
         return (errorCase instanceof PortalErrorClass) ? INSTANCE : null;
      }
      
   }
}
