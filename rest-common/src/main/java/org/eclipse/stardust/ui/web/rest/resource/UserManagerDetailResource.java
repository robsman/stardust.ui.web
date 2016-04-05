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
package org.eclipse.stardust.ui.web.rest.resource;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.component.service.ProcessDefinitionService;
import org.eclipse.stardust.ui.web.rest.component.service.UserManagerDetailService;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityTableUtils;
import org.eclipse.stardust.ui.web.rest.dto.DataTableOptionsDTO;
import org.eclipse.stardust.ui.web.rest.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.UserAuthorizationStatusDTO;
import org.eclipse.stardust.ui.web.rest.dto.UserManagerDetailRoleDTO;
import org.eclipse.stardust.ui.web.rest.dto.UserManagerDetailsDTO;
import org.eclipse.stardust.ui.web.rest.util.JsonMarshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Path("/userManagerDetails")
public class UserManagerDetailResource
{
   private static final Logger trace = LogManager.getLogger(UserManagerDetailResource.class);

   @Resource
   private UserManagerDetailService userManagerDetailService;

   @Autowired
   ProcessDefinitionService processDefService;

   /**
    * 
    * @return
    */
   @GET
   @Path("/{userOid}")
   public Response getUserManagerDetails(@PathParam("userOid") String userOid)
   {

      try
      {
         UserManagerDetailsDTO result = userManagerDetailService.getUserManagerDetails(userOid);
         return Response.ok(result.toJson(), MediaType.TEXT_PLAIN_TYPE).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   /**
    * 
    * @param userOid
    * @param postData
    * @return
    */
   @POST
   @Path("/removeRoleFromUser/{userOid}")
   public Response removeRoleFromUser(@PathParam("userOid") String userOid, String postData)
   {

      try
      {
         List<UserManagerDetailRoleDTO> roles = populateRoles(postData);

         UserAuthorizationStatusDTO userAuthorizationStatus = userManagerDetailService.removeRoleFromUser(roles,
               userOid);
         return Response.ok(userAuthorizationStatus.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   /**
    * 
    * @param userOid
    * @param postData
    * @return
    */
   @POST
   @Path("/addRoleToUser/{userOid}")
   public Response addUserToRole(@PathParam("userOid") String userOid, String postData)
   {

      try
      {
         List<UserManagerDetailRoleDTO> roles = populateRoles(postData);

         UserAuthorizationStatusDTO userAuthorizationStatus = userManagerDetailService.addRoleToUser(roles, userOid);
         return Response.ok(userAuthorizationStatus.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   /**
    * Populate the options with the post data.
    * 
    * @param options
    * @param postData
    * @return
    */

   private List<UserManagerDetailRoleDTO> populateRoles(String postData)
   {
      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject postJSON = jsonIo.readJsonObject(postData);

      JsonArray rolesArray = postJSON.getAsJsonArray("roles");
      Type type = new TypeToken<List<UserManagerDetailRoleDTO>>()
      {
      }.getType();
      List<UserManagerDetailRoleDTO> roles = new ArrayList<UserManagerDetailRoleDTO>();
      if (null != rolesArray)
      {
         roles = new Gson().fromJson(rolesArray.toString(), type);

      }
      return roles;
   }

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
   @Path("/allActivities")
   public Response getAllActivitiesForRole(@QueryParam("skip") @DefaultValue("0") Integer skip,
         @QueryParam("pageSize") @DefaultValue("8") Integer pageSize,
         @QueryParam("orderBy") @DefaultValue("oid") String orderBy,
         @QueryParam("orderByDir") @DefaultValue("asc") String orderByDir, String postData)
   {
      try
      {
         DataTableOptionsDTO options = new DataTableOptionsDTO(pageSize, skip, orderBy, "asc".equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);

         JsonMarshaller jsonIo = new JsonMarshaller();
         JsonObject postJSON = jsonIo.readJsonObject(postData);

         String userOid = postJSON.get("userOid").getAsString();

         QueryResultDTO resultDTO = userManagerDetailService.getAllActivitiesForUser(userOid, options);

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
    * 
    * @param options
    * @param postData
    */

   private void populatePostData(DataTableOptionsDTO options, String postData)
   {
      List<DescriptorColumnDTO> availableDescriptors = processDefService.getDescriptorColumns(true);
      ActivityTableUtils.populatePostData(options, postData, availableDescriptors);
   }

}
