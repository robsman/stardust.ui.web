/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.stereotype.Component;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.rest.common.LanguageUtil;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component
@Path("/i18n")
public class I18nResource
{
   @Context
   private HttpServletRequest httpRequest;

   @GET
   @Produces(MediaType.TEXT_PLAIN)
   @Path("/language")
   public Response getLanguage()
   {
      StringTokenizer tok = new StringTokenizer(httpRequest.getHeader("Accept-language"),
            ",");
      if (tok.hasMoreTokens())
      {
         return Response.ok(LanguageUtil.getLocale(tok.nextToken()),
               MediaType.TEXT_PLAIN_TYPE).build();
      }
      return Response.ok("en", MediaType.TEXT_PLAIN_TYPE).build();
   }

   /**
    * @param bundleName
    * @param locale
    * @return
    */
   @GET
   @Path("/{bundleName}/{locale}")
   public Response getRetrieve(@PathParam("bundleName")
   String bundleName, @PathParam("locale")
   String locale)
   {
      final String POST_FIX = "client-messages";

      if (StringUtils.isNotEmpty(bundleName) && bundleName.endsWith(POST_FIX))
      {
         try
         {
            StringBuffer bundleData = new StringBuffer();
            ResourceBundle bundle = ResourceBundle.getBundle(bundleName,
                  LanguageUtil.getLocaleObject(locale));

            String key;
            Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements())
            {
               key = keys.nextElement();
               bundleData.append(key).append("=").append(bundle.getString(key))
                     .append("\n");
            }

            return Response.ok(bundleData.toString(), MediaType.TEXT_PLAIN_TYPE).build();
         }
         catch (MissingResourceException mre)
         {
            return Response.status(Status.NOT_FOUND).build();
         }
         catch (Exception e)
         {
            return Response.status(Status.BAD_REQUEST).build();
         }
      }
      else
      {
         return Response.status(Status.FORBIDDEN).build();
      }
   }
}
