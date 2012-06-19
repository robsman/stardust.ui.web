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

/**
 * @author Yogesh.Manware
 * 
 */
public class StringUtils
{

   /**
    * @param source
    * @param separator
    * @return
    */
   public static final String substringAfterLast(String source, String separator)
   {
      if (org.eclipse.stardust.common.StringUtils.isEmpty(source))
      {
         return source;
      }
      if (org.eclipse.stardust.common.StringUtils.isEmpty(separator))
      {
         return "";
      }
      int pos = source.lastIndexOf(separator);
      if (pos == -1 || pos == (source.length() - separator.length()))
      {
         return "";
      }
      return source.substring(pos + separator.length());
   }

   /**
    * @param source
    * @param separator
    * @return
    */
   public static String substringBeforeLast(String source, String separator)
   {
      if (org.eclipse.stardust.common.StringUtils.isEmpty(source) || org.eclipse.stardust.common.StringUtils.isEmpty(separator))
      {
         return source;
      }
      int pos = source.lastIndexOf(separator);
      if (pos == -1)
      {
         return source;
      }
      return source.substring(0, pos);
   }
}
