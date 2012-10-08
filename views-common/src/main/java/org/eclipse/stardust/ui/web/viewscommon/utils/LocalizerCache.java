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

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.DataPath;


public final class LocalizerCache
{
   private static Map<LocalizerCacheModel, DataPath> dataPathCache = CollectionUtils.newHashMap();

   private LocalizerCache()
   {

   }

   /**
    * reset cache data
    */
   public static void reset()
   {
      if (CollectionUtils.isNotEmpty(dataPathCache))
      {
         dataPathCache.clear();
      }
   }

   public static boolean contains(String id)
   {
      return dataPathCache.containsKey(new LocalizerCacheModel(id, org.eclipse.stardust.ui.web.common.util.FacesUtils
            .getLocaleFromRequest()));
   }

   public static DataPath get(String id)
   {
      return dataPathCache.get(new LocalizerCacheModel(id, org.eclipse.stardust.ui.web.common.util.FacesUtils
            .getLocaleFromRequest()));
   }

   public static void put(String id, DataPath path)
   {
      dataPathCache.put(
            new LocalizerCacheModel(id, org.eclipse.stardust.ui.web.common.util.FacesUtils.getLocaleFromRequest()),
            path);
   }
}
/**
 * 
 */
class LocalizerCacheModel implements Serializable
{
   private static final long serialVersionUID = 1L;
   private final Locale locale;
   private final String id;

   public LocalizerCacheModel(String id, Locale locale)
   {
      this.id = id;
      this.locale = locale;
   }

   public String getId()
   {
      return id;
   }

   public Locale getLocale()
   {
      return locale;
   }

   @Override
   public boolean equals(Object obj)
   {
      if ((null != obj) && obj instanceof LocalizerCacheModel)
      {
         LocalizerCacheModel other = (LocalizerCacheModel) obj;

         if (other.getId().equals(id) && (null != other.getLocale() && other.getLocale().equals(locale)))
         {
            return true;
         }
      }

      return false;
   }

   @Override
   public int hashCode()
   {
      int result = 17;
      result += ((37 * result) + getId().hashCode());
      if (null != getLocale())
      {
         result += ((37 * result) + getLocale().hashCode());
      }
      return result;
   }

}