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
package org.eclipse.stardust.ui.web.common.services.rest;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;

/**
 * @author Subodh.Godbole
 *
 */
@Path("properties/{bundleName}/{locale}")
public class ClientPropertiesRestlet
{
   private static final Logger trace = LogManager.getLogger(ClientPropertiesRestlet.class);

   /**
    * @param bundleName
    * @param locale
    * @return
    */
   @GET
   public Response getRetrieve(@PathParam("bundleName") String bundleName, @PathParam("locale") String locale)
   {
      // TODO have some naming convention for properties exposed over web
      try
      {
         StringBuffer bundleData = new StringBuffer();
         ResourceBundle bundle = ResourceBundle.getBundle(bundleName, getLocaleObject(locale));

         String key;
         Enumeration<String> keys = bundle.getKeys();
         while (keys.hasMoreElements())
         {
            key = keys.nextElement();
            bundleData.append(key).append("=").append(bundle.getString(key)).append("\n");
         }
   
         return Response.ok(bundleData.toString(), MediaType.TEXT_PLAIN_TYPE).build();
      }
      catch (Exception e)
      {
         trace.error("Unable to retrieve bundle", e);
         return Response.status(Status.BAD_REQUEST).build();
      }
   }
   
   /**
    * @param locale
    * @return
    */
   private Locale getLocaleObject(String locale)
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
