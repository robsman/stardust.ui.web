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

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.ActivityInstanceService;
import org.eclipse.stardust.ui.web.rest.service.dto.ActivityInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessInstanceDTO;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component
@Path("/portal/activity-instances")
public class ActivityInstanceResource
{
   private static final Logger trace = LogManager
         .getLogger(ActivityInstanceResource.class);

   @Autowired
   private ActivityInstanceService activityInstanceService;

   private final JsonMarshaller jsonIo = new JsonMarshaller();

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{activityInstanceOid: \\d+}")
   public Response getActivityInstance(@PathParam("activityInstanceOid")
   long activityInstanceOid)
   {
      try
      {
         ActivityInstanceDTO aiDTO = getActivityInstanceService().getActivityInstance(
               activityInstanceOid);

         Gson gson = new Gson();
         String json = gson.toJson(aiDTO);

         return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{activityInstanceOid: \\d+}/attachments.json")
   public Response getProcessesAttachments(@PathParam("activityInstanceOid")
   long activityInstanceOid)
   {
      try
      {
         List<DocumentDTO> processAttachments = getActivityInstanceService()
               .getProcessAttachmentsForActivityInstance(activityInstanceOid);

         Gson gson = new Gson();
         String jsonOutput = gson.toJson(processAttachments);

         return Response.ok(jsonOutput.toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("completeRendezvous.json")
   public Response completeRendezvous(String postedData)
   {
      try
      {
         JsonObject json = jsonIo.readJsonObject(postedData);

         Gson gson = new Gson();
         ActivityInstanceDTO aiDTO = gson.fromJson(json.get("pendingActivityInstance"),
               ActivityInstanceDTO.class);
         DocumentDTO documentDTO = gson.fromJson(json.get("document").getAsJsonObject(),
               DocumentDTO.class);

         List<ProcessInstanceDTO> processInstances = getActivityInstanceService()
               .completeRendezvous(aiDTO.getOid(), documentDTO.getUuid());

         String jsonOutput = gson.toJson(processInstances);

         return Response.ok(jsonOutput.toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   /**
    * @return the activityInstanceService
    */
   public ActivityInstanceService getActivityInstanceService()
   {
      return activityInstanceService;
   }

   /**
    * @param activityInstanceService
    */
   public void setActivityInstanceService(ActivityInstanceService activityInstanceService)
   {
      this.activityInstanceService = activityInstanceService;
   }
}
