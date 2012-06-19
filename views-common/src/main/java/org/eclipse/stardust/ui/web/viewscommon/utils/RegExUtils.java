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

import org.eclipse.stardust.ui.web.common.util.StringUtils;

/**
 * @author Subodh.Godbole
 * 
 */
public class RegExUtils
{
   private static final String[] REG_EXP_ESCAPE_CHARS = {"[", "^", "$", ".", "|", "?", "+", "(", ")", "{", "}"};
   private static final String BACKSLASH = "\\";

   /**
    * 
    */
   private RegExUtils()
   {
      // Utiliity Class
   }

   /**
    * Escapes the input for RegEx
    * 
    * @param str
    * @return
    */
   public static String escape(String str)
   {
      if (StringUtils.isNotEmpty(str))
      {
         for (final String escapeChar : REG_EXP_ESCAPE_CHARS)
         {
            str = str.replace(escapeChar, BACKSLASH + escapeChar);
         }
      }

      return str;
   }
}
