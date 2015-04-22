/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.PasswordManagementService;
import org.eclipse.stardust.ui.web.rest.service.dto.PasswordRulesDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

/**
 * @author Johnson.Quadras
 * @version $Revision: $
 */
@Component
@Path("/passwordManagement")
public class PasswordManagementResource
{
   private static final Logger trace = LogManager.getLogger(PasswordManagementResource.class);
   @Autowired
   PasswordManagementService passwordManagementService;
   /**
    * 
    */
   @GET
   @Path("/passwordRules")
   @Produces(MediaType.APPLICATION_JSON)
   public Response getPasswordRules()
   {
      try
      {
         PasswordRulesDTO result = passwordManagementService.getPasswordRules();

         JsonObject resultJSON = new JsonObject();
         if (null != result)
         {
            resultJSON.add("rules", new JsonMarshaller().readJsonObject(result.toJson()));
         }
         return Response.ok(resultJSON.toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   /**
    * 
    * @param postedData
    * @return
    */
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/passwordRules")
   public Response savePasswordRules(String postedData)
   {
      try
      {
         PasswordRulesDTO passwordRulesDTO = DTOBuilder.buildFromJSON(postedData, PasswordRulesDTO.class);
         passwordManagementService.savePasswordRules(passwordRulesDTO);
         return Response.ok("SUCCESS", MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }
}
