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
package org.eclipse.stardust.ui.web.rest.resource;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.component.service.ActivitySearchService;
import org.eclipse.stardust.ui.web.rest.documentation.DTODescription;
import org.eclipse.stardust.ui.web.rest.documentation.ResponseDescription;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.ActivitySearchDTO;

/**
 * @author Abhay.Thappan
 */
@Path("/activity-search")
public class ActivitySearchResource
{
   private static final Logger trace = LogManager.getLogger(ActivitySearchResource.class);

   @Resource
   private ActivitySearchService activitySearchService;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/allResubmissionActivityInstances")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.response.ActivitySearchDTO")
   public Response getAllResubmissionActivityInstances()
   {
      ActivitySearchDTO asDTO = activitySearchService.getAllResubmissionActivityInstances();
      return Response.ok(asDTO.toJson(), MediaType.APPLICATION_JSON).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/allActivityInstances")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.response.ActivitySearchDTO")
   public Response getAllActivityInstances()
   {
      ActivitySearchDTO asDTO = activitySearchService.getAllActivityInstances();
      return Response.ok(asDTO.toJson(), MediaType.APPLICATION_JSON).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/worklistForUser/{userOID}")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.response.ActivitySearchDTO")
   public Response getWorklistForUser(@PathParam("userOID") long userOID)
   {
      ActivitySearchDTO asDTO = activitySearchService.getWorklistForUser(userOID);
      return Response.ok(asDTO.toJson(), MediaType.APPLICATION_JSON).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/users")
   @ResponseDescription("The response will contain list of UserDTO")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.UserDTO")
   public Response getUsersByCriteria(@QueryParam("firstName") @DefaultValue("") String firstName, @QueryParam("lastName") @DefaultValue("") String lastName)
   {
      List<UserDTO> listOfUsers = activitySearchService.getUsers_anyLike(firstName, lastName);
      QueryResultDTO result = new QueryResultDTO();
      result.list = listOfUsers;
      return Response.ok(result.toJson(), MediaType.APPLICATION_JSON).build();
   }
}
