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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.ClientErrorException;
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

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.rest.service.ParticipantService;
import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.request.DepartmentDTO;
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
   public Response modifyParticipant(String postedData)
   {
      Map grantsMap = null;
      if (StringUtils.isNotEmpty(postedData))
      {
         grantsMap = JsonDTO.getAsMap(postedData);
      }
      else
      {
         throw new ClientErrorException(Response.Status.BAD_REQUEST);
      }

      HashSet<String> participant = new HashSet<String>((Collection< ? extends String>) grantsMap.get("participants"));
      HashSet<String> usersToBeAdded = null;
      if (grantsMap.get("add") != null)
      {
         usersToBeAdded = new HashSet<String>((Collection< ? extends String>) grantsMap.get("add"));
      }

      HashSet<String> usersToBeRemoved = null;
      if (grantsMap.get("remove") != null)
      {
         usersToBeRemoved = new HashSet<String>((Collection< ? extends String>) grantsMap.get("remove"));
      }

      List<ParticipantDTO> participants = participantService.modifyParticipant(participant, usersToBeAdded,
            usersToBeRemoved);

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
   public Response createDepartment(String postData) throws Exception
   {
      DepartmentDTO departmentDTO = DTOBuilder.buildFromJSON(postData, DepartmentDTO.class);
      ParticipantDTO department = participantService.createDepartment(departmentDTO);
      return Response.ok(department.toJson(), MediaType.APPLICATION_JSON).build();
   }

   @DELETE
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("department/{departmentId}")
   public Response deleteDepartment(@PathParam("departmentId") String departmentId)
   {
      participantService.deleteDepartment(departmentId);
      return Response.ok("deleted", MediaType.APPLICATION_JSON).build();
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("department")
   public Response modifyDepartment(String postData) throws Exception
   {
      DepartmentDTO departmentDTO = DTOBuilder.buildFromJSON(postData, DepartmentDTO.class);
      ParticipantDTO department = participantService.modifyDepartment(departmentDTO);
      return Response.ok(department.toJson(), MediaType.APPLICATION_JSON).build();
   }

}
