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

package org.eclipse.stardust.ui.web.rules_manager.common;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Shrikant.Gangal
 *
 */
public class LanguageUtil
{
   // Map that holds accept-language header vs locale strings
   // Language entry only needs to be added when these are different
   // e.g. "zh" vs "zh_CN"
   private static Map<String, String> LANG_POST_FIX_MAP = new HashMap<String, String>();;

   // TODO - find a better way of initialiazing the map
   static {
      LANG_POST_FIX_MAP.put("zh", "zh_CN");
   }

   /**
    * @param langHeaderString
    * @return
    */
   public static String getLocale(String langHeaderString)
   {
      String langPostFix = LANG_POST_FIX_MAP.get(langHeaderString.substring(0, 2));
      if (null == langPostFix)
      {
         langPostFix = langHeaderString.substring(0, 2);
      }

      return langPostFix;
   }


   /**
    * @param locale
    * @return
    */
   public static Locale getLocaleObject(String locale)
   {
      String[] localeParts = locale.split("_");
      if (2 < localeParts.length)
      {
         return new Locale(localeParts[0], localeParts[1], localeParts[2]);
      }
      else if (1 < localeParts.length)
      {
         return new Locale(localeParts[0], localeParts[1]);
      }
      else if (0 < localeParts.length)
      {
         return new Locale(localeParts[0]);
      }
      else
      {
         return new Locale("en");
      }
   }
}

