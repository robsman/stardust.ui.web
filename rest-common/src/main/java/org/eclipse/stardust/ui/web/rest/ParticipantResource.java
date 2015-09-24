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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.service.ParticipantSearchComponent;
import org.eclipse.stardust.ui.web.rest.service.ParticipantService;
import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ModelDTO;
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

   @Resource
   private ParticipantSearchComponent participantSearchComponent;

   // Modify Participant
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("participants")
   public Response modifyParticipant(String postedData, @QueryParam("lazyLoad") @DefaultValue("false") Boolean lazyLoad)
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

      HashSet<String> participants = new HashSet<String>((Collection< ? extends String>) grantsMap.get("participants"));
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

      Map<String, List<ParticipantDTO>> participantDTOs = participantService.modifyParticipant(participants,
            usersToBeAdded, usersToBeRemoved, lazyLoad);

      return Response.ok(GsonUtils.toJsonHTMLSafeString(participantDTOs), MediaType.APPLICATION_JSON).build();
   }

   // get sub-participants for give participant Id
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("participants/tree")
   public Response getParticipantTree(@QueryParam("lazyLoad") @DefaultValue("false") Boolean lazyLoad)
         throws UnsupportedEncodingException
   {
      List<ModelDTO> modelParticipants = participantService.getParticipantTree(lazyLoad);
      return Response.ok(AbstractDTO.toJson(modelParticipants), MediaType.APPLICATION_JSON).build();
   }
   
   // get sub-participants for give participant Id
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("participants/{participantId}")
   public Response getSubParticipants(@PathParam("participantId") String participantId)
         throws UnsupportedEncodingException
   {
      participantId = URLDecoder.decode(participantId, "UTF-8");
      List<ParticipantDTO> participants = participantService.getParticipant(participantId);
      return Response.ok(AbstractDTO.toJson(participants), MediaType.APPLICATION_JSON).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("participants")
   public Response searchParticipants(@QueryParam("searchText") @DefaultValue("") String searchText,
         @QueryParam("maxMatches") @DefaultValue("8") Integer maxMatches,
         @QueryParam("searchType") @DefaultValue("3") Integer searchType,
         @QueryParam("filterPredefinedModel") @DefaultValue("false") Boolean filterPredefinedModel)
   {
      // search all unscoped participants including predefined ones
      return Response
            .ok(participantSearchComponent.searchAllParticipants(searchText, maxMatches, searchType,
                  filterPredefinedModel), MediaType.APPLICATION_JSON).build();
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("department")
   public Response createModifyDepartment(String postData) throws Exception
   {
      DepartmentDTO departmentDTO = DTOBuilder.buildFromJSON(postData, DepartmentDTO.class);
      List<ParticipantDTO> participants = participantService.createModifyDepartment(departmentDTO);

      Map<String, List<ParticipantDTO>> result = new HashMap<String, List<ParticipantDTO>>();
      result.put("participants", participants);

      return Response.ok(GsonUtils.toJsonHTMLSafeString(result), MediaType.APPLICATION_JSON).build();
   }

   @DELETE
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("department/{departmentId}")
   public Response deleteDepartment(@PathParam("departmentId") String departmentId) throws UnsupportedEncodingException
   {
      departmentId = URLDecoder.decode(departmentId, "UTF-8");
      participantService.deleteDepartment(departmentId);
      return Response.ok("deleted", MediaType.APPLICATION_JSON).build();
   }
}
