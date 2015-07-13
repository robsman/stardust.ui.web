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
package org.eclipse.stardust.ui.web.rest;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.ui.web.rest.service.ParticipantService;
import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.ParticipantDTO;
import org.springframework.stereotype.Component;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
@Component
@Path("/")
public class ParticipantResource
{
   @Resource
   private ParticipantService participantService;

   // Modify Participant
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("participants")
   public Response modifyParticipant(@PathParam("participantId") String participantId,
         @PathParam("type") String participantType)
   {
      // TODO Implementation pending
      List<ParticipantDTO> participants = participantService.getParticipant(participantId);
      return Response.ok(AbstractDTO.toJson(participants), MediaType.APPLICATION_JSON).build();
   }

   // get sub-participants for give participant Id
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("participants/{participantId}")
   public Response getSubParticipants(@PathParam("participantId") String participantId,
         @PathParam("type") String participantType)
   {
      List<ParticipantDTO> participants = participantService.getParticipant(participantId);
      return Response.ok(AbstractDTO.toJson(participants), MediaType.APPLICATION_JSON).build();
   }

   //
   @PUT
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("department")
   public Response createDepartment(@PathParam("participantId") String participantId,
         @PathParam("type") String participantType)
   {
      // TODO Implementation pending
      return Response.ok("Department Modified", MediaType.APPLICATION_JSON).build();
   }

   @DELETE
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("department")
   public Response deleteDepartment(@PathParam("participantId") String participantId,
         @PathParam("type") String participantType)
   {
      // TODO Implementation pending
      return Response.ok("Department Deleted...", MediaType.APPLICATION_JSON).build();
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("department")
   public Response modifyDepartment(@PathParam("participantId") String participantId,
         @PathParam("type") String participantType)
   {
      // TODO Implementation pending
      return Response.ok("Department Created...", MediaType.APPLICATION_JSON).build();
   }
}
