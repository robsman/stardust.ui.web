/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils;

import com.google.gson.JsonObject;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class TestRest
{
   public static void main(String[] args)
   {
      try
      {
         // login and jsession id
         String jSessionId = login();

         // get folders
         checkLoggedInUser(jSessionId);
      }
      catch (MalformedURLException e)
      {
         e.printStackTrace();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * @param jSessionId
    * @throws IOException
    */
   private static void checkLoggedInUser(String jSessionId) throws IOException
   {
      URL url = new URL("http://localhost:8080/idemo/services/rest/portal/user/whoAmI");

      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.addRequestProperty("Cookie", "JSESSIONID=" + URLEncoder.encode(jSessionId, "UTF-8")); // note
                                                                                                 // this
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestMethod("GET");

      if (conn.getResponseCode() != 200)
      {
         throw new RuntimeException("HTTP Error Code : " + conn.getResponseCode());
      }

      BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
      StringBuilder sb = new StringBuilder();

      String line;
      while ((line = br.readLine()) != null)
      {
         sb.append(line);
      }
      System.out.println(sb);
   }

   /**
    * @return
    * @throws IOException
    */
   private static String login() throws IOException
   {
      URL url = new URL("http://localhost:8080/idemo/services/rest/portal/user/login");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setDoInput(true);
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestMethod("POST");

      // Send post request
      conn.setDoOutput(true);
      JsonObject cred = new JsonObject();
      cred.addProperty("userId", "motu");
      cred.addProperty("password", "motu");
      // Other params such as domain, partition and realm can be also be sent

      OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
      wr.write(cred.toString());
      wr.flush();

      if (conn.getResponseCode() != 200)
      {
         throw new RuntimeException("HTTP Error Code : " + conn.getResponseCode());
      }

      String cookies = conn.getHeaderField("Set-Cookie");
      String jSessionId = StringUtils.substringAfterLast(cookies, "JSESSIONID=");
      jSessionId = jSessionId.substring(0, jSessionId.indexOf(";"));
      conn.disconnect();

      return jSessionId;
   }
}
