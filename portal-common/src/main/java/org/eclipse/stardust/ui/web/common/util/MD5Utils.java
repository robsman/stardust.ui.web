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

import java.security.MessageDigest;

/**
 * @author Shrikant.Gangal
 *
 */
public class MD5Utils
{
   private static final String BYTE_ENCODING = "CP1252";
   private static final String MSG_DIGEST_ALG = "MD5";
   /**
    * @param message
    * @return MD5hex
    */
   public static String computeMD5Hex(String message)
   {
      try
      {
         MessageDigest md = MessageDigest.getInstance(MSG_DIGEST_ALG);
         return computeHEX(md.digest(message.getBytes(BYTE_ENCODING)));
      }
      catch (Exception e)
      {
         /* 
          * Ignore exception as there is not much that can be done here.
          * A null response from this method will cause a default Avatar to be displayed
          * to the user.
          */
      }
      return null;
   }
   
   /**
    * @param array
    * @return a hex value of the given byte array.
    */
   public static String computeHEX(byte[] array)
   {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < array.length; ++i)
      {
         sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
      }
      return sb.toString();
   }
}
