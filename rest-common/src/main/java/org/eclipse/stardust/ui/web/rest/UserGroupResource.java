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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.UserGroupService;
import org.eclipse.stardust.ui.web.rest.service.dto.UserGroupDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserGroupQueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Aditya.Gaikwad
 *
 */
@Component
@Path("/user-group")
public class UserGroupResource
{

   public static final Logger trace = LogManager.getLogger(UserGroupResource.class);

   @Autowired
   private UserGroupService userGroupService;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/all")
   public Response getAllUserGroups(@QueryParam("skip") @DefaultValue("0") Integer skip,
         @QueryParam("pageSize") @DefaultValue("8") Integer pageSize,
         @QueryParam("orderBy") @DefaultValue("oid") String orderBy,
         @QueryParam("orderByDir") @DefaultValue("asc") String orderByDir, String postData)
   {
      Options options = new Options(pageSize, skip, orderBy, "asc".equalsIgnoreCase(orderByDir));
      UserGroupQueryResultDTO allUserGroupsDTO = getUserGroupService().getAllUserGroups(options);
      return Response.ok(allUserGroupsDTO.toJson(), MediaType.APPLICATION_JSON).build();
   }

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/modify")
   public Response modifyUserGroup(String postData) throws Exception
   {
      UserGroupDTO userGroupDTO;
      UserGroupDTO modifiedUserGroup = null;
      userGroupDTO = DTOBuilder.buildFromJSON(postData, UserGroupDTO.class);
      modifiedUserGroup = getUserGroupService().modifyUserGroup(userGroupDTO);
      return Response.status(202).entity(modifiedUserGroup.toJson()).build();
   }

   @DELETE
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/delete/{id}")
   public Response deleteUserGroup(@PathParam("id") String id)
   {
      UserGroupDTO deleteUserGroup = getUserGroupService().deleteUserGroup(id);
      return Response.ok(deleteUserGroup.toJson(), MediaType.APPLICATION_JSON).build();
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/create")
   public Response createUserGroup(String postData) throws Exception
   {
      UserGroupDTO userGroupDTO;
      UserGroupDTO createUserGroup = null;
      userGroupDTO = DTOBuilder.buildFromJSON(postData, UserGroupDTO.class);
      createUserGroup = getUserGroupService().createUserGroup(userGroupDTO.getId(), userGroupDTO.getName(),
            userGroupDTO.getDescription(), userGroupDTO.getValidFrom(), userGroupDTO.getValidTo());
      return Response.status(201).entity(createUserGroup.toJson()).build();
   }

   public UserGroupService getUserGroupService()
   {
      return userGroupService;
   }

   public void setUserGroupService(UserGroupService userGroupService)
   {
      this.userGroupService = userGroupService;
   }

}
