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
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.component.service.DeputyManagementService;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.util.JsonMarshaller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Path("/deputyManagement")
public class DeputyManagementResource
{
   private static final Logger trace = LogManager.getLogger(DeputyManagementResource.class);

   @Resource
   private DeputyManagementService deputyManagementService;

   /**
    * 
    * @return
    */
   @GET
   @Path("/users")
   public Response loadUsers()
   {
      try
      {
         QueryResultDTO result = deputyManagementService.loadUsers();
         return Response.ok(result.toJson(), MediaType.TEXT_PLAIN_TYPE).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   @GET
   @Path("/deputiesForUser/{userOID}")
   public Response loadDeputiesForUser(@PathParam("userOID") String userOID)
   {
      try
      {
         QueryResultDTO result = deputyManagementService.loadDeputiesForUser(Long.parseLong(userOID));
         return Response.ok(result.toJson(), MediaType.TEXT_PLAIN_TYPE).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   @GET
   @Path("/deputiesForUser/{userOID}/{searchValue}/{searchMode}")
   public Response getDeputyUsersData(@PathParam("userOID") String userOID,
         @PathParam("searchValue") String searchValue, @PathParam("searchMode") String searchMode)
   {
      try
      {
         QueryResultDTO result = deputyManagementService.getDeputyUsersData(Long.parseLong(userOID), searchValue,
               searchMode);
         return Response.ok(result.toJson(), MediaType.TEXT_PLAIN_TYPE).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }

   }

   @GET
   @Path("/authorizations/{userOID}")
   public Response getAuthorizations(@PathParam("userOID") String userOID)
   {
      try
      {
         QueryResultDTO result = deputyManagementService.getAuthorizations(Long.parseLong(userOID));
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
   @Path("/addOrModifyDeputy")
   public Response addOrModifyDeputy(String postData)
   {

      try
      {
         JsonMarshaller jsonIo = new JsonMarshaller();
         JsonObject postJSON = jsonIo.readJsonObject(postData);
         long userOID = postJSON.getAsJsonPrimitive("userOID").getAsLong();
         long deputyOID = postJSON.getAsJsonPrimitive("deputyOID").getAsLong();
         Date validFrom = null;
         if (postJSON.getAsJsonPrimitive("validFrom") != null)
         {
            validFrom = new Date(postJSON.getAsJsonPrimitive("validFrom").getAsLong());
         }
         Date validTo = null;
         if (postJSON.getAsJsonPrimitive("validTo") != null)
         {
            validTo = new Date(postJSON.getAsJsonPrimitive("validTo").getAsLong());
         }
         String mode = postJSON.getAsJsonPrimitive("mode").getAsString();
         List<String> modelParticipantIds = populateModelParticipantIds(postData);

         deputyManagementService.addOrModifyDeputy(userOID, deputyOID, validFrom, validTo, modelParticipantIds, mode);
         return Response.ok().build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   @POST
   @Path("/removeUserDeputy")
   public Response removeUserDeputy(String postData)
   {

      try
      {
         JsonMarshaller jsonIo = new JsonMarshaller();
         JsonObject postJSON = jsonIo.readJsonObject(postData);
         long userOID = postJSON.getAsJsonPrimitive("userOID").getAsLong();
         long deputyOID = postJSON.getAsJsonPrimitive("deputyOID").getAsLong();
         deputyManagementService.removeUserDeputy(userOID, deputyOID);
         return Response.ok().build();
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
   private List<String> populateModelParticipantIds(String postData)
   {
      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject postJSON = jsonIo.readJsonObject(postData);

      JsonArray userIdsArray = postJSON.getAsJsonArray("modelParticipantIds");
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
