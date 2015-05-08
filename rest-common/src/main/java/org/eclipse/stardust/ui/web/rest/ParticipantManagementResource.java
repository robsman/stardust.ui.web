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
/**
 * @author Abhay.Thappan
 */
package org.eclipse.stardust.ui.web.rest;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.ParticipantManagementService;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserFilterDTO;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Component
@Path("/participantManagement")
public class ParticipantManagementResource
{
   private static final Logger trace = LogManager.getLogger(ParticipantManagementResource.class);

   @Resource
   private ParticipantManagementService participantManagementService;

   /**
    * 
    * @param skip
    * @param pageSize
    * @param orderBy
    * @param orderByDir
    * @param postData
    * @return
    */
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/allUsers")
   public Response getAllUsers(@QueryParam("skip") @DefaultValue("0") Integer skip,
         @QueryParam("pageSize") @DefaultValue("8") Integer pageSize,
         @QueryParam("orderBy") @DefaultValue("oid") String orderBy,
         @QueryParam("orderByDir") @DefaultValue("asc") String orderByDir, String postData)
   {
      try
      {
         Options options = new Options(pageSize, skip, orderBy, "asc".equalsIgnoreCase(orderByDir));
         populateFilters(options, postData);
         
         JsonMarshaller jsonIo = new JsonMarshaller();
         JsonObject postJSON = jsonIo.readJsonObject(postData);
         boolean hideInvalidatedUsers = postJSON.getAsJsonPrimitive("hideInvalidatedUsers").getAsBoolean();

         QueryResultDTO resultDTO = participantManagementService.getAllUsers(hideInvalidatedUsers, options);

         return Response.ok(resultDTO.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (ObjectNotFoundException onfe)
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }
   }

   /**
    * Populate the options with the post data.
    * 
    * @param options
    * @param postData
    * @return
    */
   private Options populateFilters(Options options, String postData)
   {
      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject postJSON = jsonIo.readJsonObject(postData);

      // For filter
      JsonObject filters = postJSON.getAsJsonObject("filters");
      if (null != filters)
      {
         UserFilterDTO userFilterDTO = new Gson().fromJson(postJSON.get("filters"), UserFilterDTO.class);

         options.filter = userFilterDTO;
      }
      return options;
   }
}
