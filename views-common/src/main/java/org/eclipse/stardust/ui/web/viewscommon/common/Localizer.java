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

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.html5.ManagedBeanUtils;

public class Localizer
{
   protected static final Logger trace = LogManager.getLogger(Localizer.class);

   public static String getString(LocalizerKey key)
   {
      return getString(ManagedBeanUtils.getLocale(), key);
   }

   public static String getString(Locale locale, LocalizerKey key)
   {
      String text = null;
      String basename = key != null ? key.getBundleName() : null;
      if(basename != null)
      {
         PortalResourceBundle bundle = getMessageBundle(locale, basename);
         text = getMessageText(bundle, key.getKey(), key.isMandatory());
         if(text == null)
         {
            LocalizerKey tmpKey = new LocalizerKey("dummy");
            basename = tmpKey.getBundleName();
            if(basename != null)
            {
               bundle = getMessageBundle(locale, basename);
               text = getMessageText(bundle, key.getKey(), false);
            }
         }
      }

      return text;
   }

   private static String getMessageText(PortalResourceBundle bundle, String key, boolean traceAsError)
   {
      String text = null;
      String failureMsg = null;
      try
      {
         if(bundle != null)
         {
            text = bundle.getString(key);
            if (text == null)
            {
               failureMsg = bundle.getErrorMsg(key);
            }
         }
      }
      finally
      {
         if(failureMsg != null)
         {
            if(traceAsError)
            {
               trace.error(failureMsg);
            }
            else if(!traceAsError && trace.isDebugEnabled())
            {
               trace.debug(failureMsg);
            }
         }
      }
      return text;
   }

   public static String getString(LocalizerKey key, String replacePattern, String replaceBy)
   {
      return getString(ManagedBeanUtils.getLocale(), key, replacePattern,
            replaceBy);
   }

   public static String getString(Locale locale, LocalizerKey key, String replacePattern, String replaceBy)
   {
      String text = getString(key);
      if(text != null)
      {
         int index = text.indexOf(replacePattern);
         if(index >= 0)
         {
            StringBuffer buffer = new StringBuffer(text.length() + replaceBy.length());
            buffer.append(text.substring(0, index));
            buffer.append(replaceBy);
            buffer.append(text.substring(index + replacePattern.length()));
            text = buffer.toString();
         }
      }
      return text;
   }

   private static PortalResourceBundle getMessageBundle(Locale locale, String basename)
   {

      return ResourceBundleCache.getBundle(basename, locale, getCurrentClassLoader());

   }

   protected static ClassLoader getCurrentClassLoader()
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();

      if (null == loader)
      {
         loader = Localizer.class.getClassLoader();
      }

      return loader;
   }
}
