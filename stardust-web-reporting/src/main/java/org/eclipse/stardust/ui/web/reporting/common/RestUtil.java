package org.eclipse.stardust.ui.web.reporting.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RestUtil
{
   public static String performRestCall(String restUri, String acceptMimeType) throws IOException
   {
      URL url = new URL(restUri);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      connection.setRequestMethod("GET");
      connection.setRequestProperty("Accept", acceptMimeType);
      if (connection.getResponseCode() != 200)
      {
         throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
      }

      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((connection.getInputStream())));

      String output;
      StringBuffer buffer = new StringBuffer();
      while ((output = bufferedReader.readLine()) != null)
      {
         buffer.append(output);
      }

      connection.disconnect();
      return buffer.toString();
   }

   public static String performRestJsonCall(String restUri) throws IOException
   {
      return performRestCall(restUri, "application/json");
   }
}
