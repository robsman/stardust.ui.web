/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
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
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.stardust.common.CollectionUtils;

public class ResourceBundleCache
{

   private static Map<ClassLoader, ConcurrentMap<String, Object>> CACHE = new WeakHashMap<ClassLoader, ConcurrentMap<String, Object>>();

   public static PortalResourceBundle getBundle(String basename, Locale locale,
         ClassLoader classLoader)
   {
      // Get from cache
      ConcurrentMap<String, Object> bundleCache = getCache(classLoader);

      if (bundleCache != null)
      {
         Object object = bundleCache.get(getKey(basename, locale));
         if (object instanceof PortalResourceBundle)
         {
            // Cache hit
            return (PortalResourceBundle) object;
         }
         else if (object instanceof String)
         {
            // Cache miss
            return null;
         }
      }

      PortalResourceBundle portalBundle = null;
      try
      {
         // Fetch ResourceBundle
         ResourceBundle bundle = ResourceBundle.getBundle(basename, locale, classLoader);
         if (bundle != null)
         {
            portalBundle = new PortalResourceBundle(bundle);
         }
      }
      catch (MissingResourceException e)
      {
         portalBundle = null;
      }

      Object cacheValue = portalBundle == null ? "" : portalBundle;

      // Fill cache with empty string for miss or PortalResourceBundle for hit.
      bundleCache.put(getKey(basename, locale), cacheValue);

      return portalBundle;
   }

   private static synchronized ConcurrentMap<String, Object> getCache(ClassLoader classLoader)
   {
      ConcurrentMap<String, Object> bundleCache = CACHE.get(classLoader);
      // lazy init
      if (bundleCache == null)
      {
         bundleCache = CollectionUtils.newConcurrentHashMap();
         CACHE.put(classLoader, bundleCache);
      }
      return bundleCache;
   }

   private static String getKey(String basename, Locale locale)
   {
      StringBuilder sb = new StringBuilder();
      String localeString = locale != null ? locale.toString() : "";
      sb.append(basename).append(":").append(localeString);
      return sb.toString();
   }

}
