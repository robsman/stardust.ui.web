package org.eclipse.stardust.ui.web.viewscommon.common.controller;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class UriEncodingUtils
{
   public static String decodeURIComponent(String s)
   {
      String decodedString = s;
      if (!isEmpty(s))
      {
         try
         {
            decodedString = URLDecoder.decode(s, "UTF-8");
         }
         catch (UnsupportedEncodingException e)
         {
            // will never happen as UTF-8 is granted to be there
         }
      }

      return decodedString;
   }

   public static String encodeURIComponent(String s)
   {
      String encodedString = s;
      try
      {
         encodedString = URLEncoder.encode(s, "UTF-8") //
               .replaceAll("\\+", "%20") //
               .replaceAll("\\%21", "!") //
               .replaceAll("\\%27", "'") //
               .replaceAll("\\%28", "(") //
               .replaceAll("\\%29", ")") //
               .replaceAll("\\%7E", "~");
      }
      catch (UnsupportedEncodingException e)
      {
         // will never happen as UTF-8 is granted to be there
      }

      return encodedString;
   }

}
