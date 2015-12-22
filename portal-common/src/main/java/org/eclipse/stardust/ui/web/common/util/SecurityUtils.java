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

package org.eclipse.stardust.ui.web.common.util;

/**
 * 
 * @author Johnson.Quadras
 *
 */
public class SecurityUtils
{

   private static final String RESTRICTED_URL_SYMBOL = "..";
   
   public static final String BAD_REQUEST_MESSAGE = "Bad Request.Requested path contains restricted characters.";

   /**
	   * 
	   */
   public static boolean containsRestrictedSymbols(String url)
   {
      if (url.contains(RESTRICTED_URL_SYMBOL))
      {
         return true;
      }
      return false;
   }

   /**
	   * 
	   */
   public static String sanitizeValue(String value)
   {
      value = stripCRLF(value);
      return value;
   }

   /**
	   * 
	   */
   private static String stripCRLF(String value)
   {
      if (value != null)
      {
         value = value.replaceAll("\n", "").replaceAll("\r", "");
      }
      return value;
   }

}
