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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
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
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.component.service.UserService;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.UserCountsDTO;
import org.eclipse.stardust.ui.web.rest.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.dto.UserPermissionsDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.ParticipantDTO;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * 
 * @author Nikhil.Gahlot
 * @author Johnson.Quadras
 *
 */
@Path("/user")
public class UserResource
{

   public static final Logger trace = LogManager.getLogger(UserResource.class);

   @Autowired
   private UserService userService;

   
   /**
    * @author Yogesh.Manware
    * @param processOid
    * @return
    * @throws Exception 
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/login")
   public Response login(String postedData) throws Exception
   {
      JsonObject postedJson = GsonUtils.readJsonObject(postedData);
      String userId = GsonUtils.extractString(postedJson, "userId");
      String password = GsonUtils.extractString(postedJson, "password");
      String domain = GsonUtils.extractString(postedJson, "domain");
      String partition = GsonUtils.extractString(postedJson, "partition");
      String realm = GsonUtils.extractString(postedJson, "realm");

      Map<String, String> properties = CollectionUtils.newHashMap();
      if (!StringUtils.isEmpty(domain))
      {
         properties.put(SecurityProperties.DOMAIN, domain);
      }
      if (!StringUtils.isEmpty(partition))
      {
         properties.put(SecurityProperties.PARTITION, partition);
      }
      if (!StringUtils.isEmpty(realm))
      {
         properties.put(SecurityProperties.REALM, realm);
      }

      SessionContext sessionCtx = SessionContext.findSessionContext();

      HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
            .getSession(false);

      if (!sessionCtx.isSessionInitialized())
      {
         sessionCtx.login(userId, password, properties, session);
      }
      
      JsonObject response = new JsonObject();
      response.addProperty("status", "OK");
      
      return Response.ok(response.toString(), MediaType.APPLICATION_JSON).build();
   }
   
   /**
    * @param serviceName
    * @param searchValue
    * @return
    */
   private String fetchUsers(String searchValue, boolean onlyActive, int maxMatches)
   {
      List<User> users = UserUtils.searchUsers(searchValue, onlyActive, maxMatches);
      List<ParticipantDTO> userWrappers = new ArrayList<ParticipantDTO>();
      for (User user : users)
      {
         userWrappers.add(new ParticipantDTO(user));
      }

      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = userWrappers;
      resultDTO.totalCount = userWrappers.size();

      Gson gson = new Gson();
      return gson.toJson(resultDTO);
   }

   @GET
   @Path("/search/{searchValue}")
   public Response searchUsers(@PathParam("searchValue") String searchValue, @QueryParam("active") Boolean active,
         @QueryParam("max") Integer max)
   {
      if (StringUtils.isNotEmpty(searchValue))
      {
         try
         {
            if (active == null)
            {
               active = false;
            }
            if (max == null)
            {
               max = 20;
            }
            String result = fetchUsers(searchValue, active, max);
            return Response.ok(result, MediaType.TEXT_PLAIN_TYPE).build();
         }
         catch (MissingResourceException mre)
         {
            return Response.status(Status.NOT_FOUND).build();
         }
         catch (Exception e)
         {
            return Response.status(Status.BAD_REQUEST).build();
         }
      }
      else
      {
         return Response.status(Status.FORBIDDEN).build();
      }
   }

   /**
    * This method returns the logged In user
    * 
    * @return
    */
   @GET
   @Path("/whoAmI")
   public Response getLoggedInUser()
   {
      try
      {
         UserDTO loggedInUser = userService.getLoggedInUser();
         return Response.ok(loggedInUser.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }

   }

   /**
    * This method returns runtime permissions for logged in user.
    * 
    * @return
    */
   @GET
   @Path("/whoAmI/runtime-permissions")
   public Response getPermissionsForLoggedInUser()
   {
      try
      {
         UserPermissionsDTO permissionsDTO = userService.getPermissionsForLoggedInUser();
         return Response.ok(permissionsDTO.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }

   }

   /**
    * This method returns the logged In user
    * 
    * @return
    */
   @GET
   @Path("/allCounts")
   public Response getAllCounts()
   {
      try
      {
         UserCountsDTO userCountsDTO = userService.getAllCounts();
         return Response.ok(userCountsDTO.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }

   }

   /**
    * This method returns the logged In user
    * 
    * @return
    */
   @GET
   @Path("/loadUserDetails/{userOID}")
   public Response getUserDetails(@PathParam("userOID") Long userOID)
   {
      try
      {
         UserDTO userDTO = userService.getUserDetails(userOID);
         return Response.ok(userDTO.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }

   }
}
