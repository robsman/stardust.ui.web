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

package org.eclipse.stardust.ui.web.rules_manager.service.rest;

import java.util.Enumeration;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.rules_manager.common.LanguageUtil;
import org.eclipse.stardust.ui.web.rules_manager.service.RulesManagementService;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpRequest;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Path("/rules/{randomPostFix}")
public class RulesManagementResource
{
   @Context
   private ServletContext servletContext;
   
   @Context
   private HttpServletRequest httpRequest;

   /**
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("rule-sets")
   public Response getAllRuleSets()
   {
      try
      {
         String result = getRulesManagementService().getAllRuleSets().toString();
         
         return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   /**
    * @param ruleSets
    * @return
    */
   @POST
   @Path("save")
   public Response saveRuleSets(String ruleSets)
   {
      if (StringUtils.isEmpty(ruleSets))
      {
         return Response.status(Status.BAD_REQUEST).build();
      }
      try
      {
         String result = getRulesManagementService().saveRuleSets(ruleSets);         
         return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }   

   @GET
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   @Path("ruleSet/{ruleSetId}/download")
   public Response downloadRuleSet(@PathParam("ruleSetId") String ruleSetId)
   {
      byte[] ruleSetNameAndContent = getRulesManagementService().getRuleSet(ruleSetId);

      String fileName = ruleSetId;
      if (!fileName.endsWith(".json"))
      {
         fileName = fileName + ".json";
      }
      
      return Response.ok(ruleSetNameAndContent, MediaType.APPLICATION_OCTET_STREAM)
            .header("content-disposition",
                  "attachment; filename = \"" + fileName  + "\"")
            .build();
   }
   
   /**
    * @param bundleName
    * @param locale
    * @return
    */
   @GET
   @Path("/{bundleName}/{locale}")
   public Response getRetrieve(@PathParam("bundleName") String bundleName,
         @PathParam("locale") String locale)
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
               bundleData.append(key)
                     .append("=")
                     .append(bundle.getString(key))
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
    * @return
    */
   private RulesManagementService getRulesManagementService()
   {
      ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);

      return (RulesManagementService) context.getBean("rulesManagementService");
   }
}


