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

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.ParticipantManagementService;
import org.eclipse.stardust.ui.web.rest.service.dto.InvalidateUserStatusDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMessageDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserProfileStatusDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

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
    * 
    * @param mode
    * @param oid
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/openCreateCopyModifyUser/{mode}/{oid}")
   public Response getCreateCopyModifyUserData(@PathParam("mode") String mode, @PathParam("oid") long oid)
   {
      try
      {
         UserDTO userDTO = participantManagementService.getCreateCopyModifyUserData(mode, oid);

         return Response.ok(userDTO.toJson(), MediaType.APPLICATION_JSON).build();
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
    * @param mode
    * @param postData
    * @return
    */
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/createCopyModifyUser/{mode}")
   public Response createCopyModifyUser(@PathParam("mode") String mode, String postData)
   {
      try
      {
         UserDTO userDTO = populateUserDTO(postData);
         UserProfileStatusDTO resultDTO = participantManagementService.createCopyModifyUser(userDTO, mode);

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
    * @param postData
    * @return
    */
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/invalidateUsers")
   public Response invalidateUsers(String postData)
   {
      try
      {
         List<Long> userOids = populateOids(postData, "userOids");
         InvalidateUserStatusDTO resultDTO = participantManagementService.invalidateUser(userOids);

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
    * @param postData
    * @return
    */
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/delegateToDefaultPerformer")
   public Response delegateToDefaultPerformer(String postData)
   {
      try
      {
         List<Long> userOids = populateOids(postData, "userOids");
         List<Long> activityInstanceOids = populateOids(postData, "activityInstanceOids");
         NotificationMessageDTO resultDTO = participantManagementService.delegateToDefaultPerformer(
               activityInstanceOids, userOids);

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
   private List<Long> populateOids(String postData, String collectionName)
   {
      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject postJSON = jsonIo.readJsonObject(postData);

      JsonArray oidsArray = postJSON.getAsJsonArray(collectionName);
      Type type = new TypeToken<List<Long>>()
      {
      }.getType();
      List<Long> oids = new ArrayList<Long>();
      if (null != oidsArray)
      {
         oids = new Gson().fromJson(oidsArray.toString(), type);

      }
      return oids;
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

   /**
    * 
    * @param postData
    * @return
    * @throws Exception
    */
   private UserDTO populateUserDTO(String postData) throws Exception
   {
      UserDTO userDTO = null;

      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject postJSON = jsonIo.readJsonObject(postData);

      JsonObject userJson = postJSON.getAsJsonObject("user");

      String userJsonStr = userJson.toString();
      if (StringUtils.isNotEmpty(userJsonStr))
      {
         try
         {
            userDTO = DTOBuilder.buildFromJSON(userJsonStr, UserDTO.class);
         }
         catch (Exception e)
         {
            trace.error("Error in Deserializing filter JSON", e);
            throw e;
         }
      }

      return userDTO;
   }
}
