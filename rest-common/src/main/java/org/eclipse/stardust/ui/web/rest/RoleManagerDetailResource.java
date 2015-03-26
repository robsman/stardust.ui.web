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
import org.eclipse.stardust.ui.web.rest.service.ProcessDefinitionService;
import org.eclipse.stardust.ui.web.rest.service.RoleManagerDetailService;
import org.eclipse.stardust.ui.web.rest.service.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.RoleManagerDetailsDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserAuthorizationStatusDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityTableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Component
@Path("/roleManagerDetails")
public class RoleManagerDetailResource
{
   private static final Logger trace = LogManager
         .getLogger(RoleManagerDetailResource.class);

   @Resource
   private RoleManagerDetailService roleManagerDetailService;
   
   @Autowired
   ProcessDefinitionService processDefService;

   /**
    * 
    * @return
    */
   @GET
   @Path("/{roleId}/{departmentOid}")
   public Response getRoleManagerDetails(@PathParam("roleId") String roleId,
         @PathParam("departmentOid") String departmentOid)
   {

      try
      {
         RoleManagerDetailsDTO result = roleManagerDetailService.getRoleManagerDetails(
               roleId, departmentOid);
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
    * @param userIds
    * @param roleId
    * @param departmentOid
    * @return
    */

   @POST
   @Path("/removeUserFromRole/{roleId}/{departmentOid}")
   public Response removeUserFromRole(@PathParam("roleId") String roleId,
         @PathParam("departmentOid") String departmentOid, String postData)
   {

      try
      {
      List<String> userIds = populateUserIds(postData);

      UserAuthorizationStatusDTO userAuthorizationStatus = roleManagerDetailService.removeUserFromRole(
            userIds, roleId, departmentOid);
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
    * @param userIds
    * @param roleId
    * @param departmentOid
    * @return
    */

   @POST
   @Path("/addUserToRole/{roleId}/{departmentOid}")
   public Response addUserToRole(@PathParam("roleId") String roleId,
         @PathParam("departmentOid") String departmentOid, String postData)
   {

      try
      {
      List<String> userIds = populateUserIds(postData);

      UserAuthorizationStatusDTO userAuthorizationStatus = roleManagerDetailService.addUserToRole(
            userIds, roleId, departmentOid);
      return Response.ok(userAuthorizationStatus.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }
   
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/allActivities")
   public Response getAllActivitiesForRole(
         @QueryParam("skip") @DefaultValue("0") Integer skip,
         @QueryParam("pageSize") @DefaultValue("8") Integer pageSize,
         @QueryParam("orderBy") @DefaultValue("oid") String orderBy,
         @QueryParam("orderByDir") @DefaultValue("asc") String orderByDir, String postData)
   {
      try
      {
         Options options = new Options(pageSize, skip, orderBy,
               "asc".equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);
         
         JsonMarshaller jsonIo = new JsonMarshaller();
         JsonObject postJSON = jsonIo.readJsonObject(postData);

         String roleId = postJSON.get("roleId").getAsString();                  
         String departmentOid = postJSON.get("departmentOid").getAsString();
         
         QueryResultDTO resultDTO = roleManagerDetailService.getAllActivitiesForRole(roleId.toString(), departmentOid.toString(), options);
         
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
   private void populatePostData(Options options, String postData)
   {
      List<DescriptorColumnDTO> availableDescriptors = processDefService.getDescriptorColumns(true);
      ActivityTableUtils.populatePostData(options, postData, availableDescriptors);
   }
   
   /**
    * Populate the options with the post data.
    * 
    * @param options
    * @param postData
    * @return
    */
   private List<String> populateUserIds(String postData)
   {
      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject postJSON = jsonIo.readJsonObject(postData);

      JsonArray userIdsArray = postJSON.getAsJsonArray("userIds");
      Type type = new TypeToken<List<String>>()
      {
      }.getType();
      List<String> userIds = new ArrayList<String>();
      if (null != userIdsArray)
      {
         userIds = new Gson().fromJson(userIdsArray.toString(), type);

      }
      return userIds;
   }
}
