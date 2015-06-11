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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
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
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.service.AuthorizationManagerService;
import org.eclipse.stardust.ui.web.rest.service.PreferenceService;
import org.eclipse.stardust.ui.web.rest.service.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.PreferenceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.PermissionDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

/**
 * @author Subodh.Godbole
 *
 */
@Component
@Path("/preference")
public class PreferenceResource
{
   private static final Logger trace = LogManager.getLogger(PreferenceResource.class);

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Autowired
   private PreferenceService PreferenceService;

   @Autowired
   private AuthorizationManagerService authorizationManagerService;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{scope}/{moduleId}/{preferenceId}")
   public Response getPreference(@PathParam("scope") String scope, @PathParam("moduleId") String moduleId,
         @PathParam("preferenceId") String preferenceId)
   {
      if (StringUtils.isNotEmpty(scope))
      {
         PreferenceScope pScope = scope.equalsIgnoreCase(PreferenceScope.USER.toString())
               ? PreferenceScope.USER
               : PreferenceScope.PARTITION;

         AdministrationService adminService = serviceFactoryUtils.getAdministrationService();

         Map<String, Map<String, Serializable>> allVals = new HashMap<String, Map<String, Serializable>>();

         Preferences preferences = adminService.getPreferences(pScope, moduleId, preferenceId);
         allVals.put(pScope.toString(), preferences.getPreferences());

         if (pScope == PreferenceScope.USER)
         {
            // Get for Partition Scope
            pScope = PreferenceScope.PARTITION;

            preferences = adminService.getPreferences(pScope, moduleId, preferenceId);
            allVals.put(pScope.toString(), preferences.getPreferences());
         }

         Gson gson = new Gson();
         return Response.ok(gson.toJson(allVals), MediaType.APPLICATION_JSON).build();
      }

      trace.error("Scope should not be empty.");
      return Response.status(Status.BAD_REQUEST).build();
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{scope}/{moduleId}/{preferenceId}")
   public Response savePreference(@PathParam("scope") String scope, @PathParam("moduleId") String moduleId,
         @PathParam("preferenceId") String preferenceId, String postedData)
   {
      if (StringUtils.isNotEmpty(scope))
      {
         PreferenceScope pScope = scope.equalsIgnoreCase(PreferenceScope.USER.toString())
               ? PreferenceScope.USER
               : PreferenceScope.PARTITION;

         AdministrationService adminService = serviceFactoryUtils.getAdministrationService();

         Map values = null;
         if (StringUtils.isNotEmpty(postedData))
         {
            values = JsonDTO.getAsMap(postedData);
         }

         Preferences preferences = new Preferences(pScope, moduleId, preferenceId, values);
         adminService.savePreferences(preferences);

         Gson gson = new Gson();
         return Response.ok(gson.toJson(""), MediaType.APPLICATION_JSON).build();
      }

      trace.error("Scope should not be empty.");
      return Response.status(Status.BAD_REQUEST).build();
   }

   /**
    * 
    */
   @GET
   @Path("/partition")
   @Produces(MediaType.APPLICATION_JSON)
   public Response getTenantPreferences()
   {
      try
      {
         List<PreferenceDTO> result = PreferenceService.fetchPartitionPreferences();
         return Response.ok(GsonUtils.toJsonHTMLSafeString(result), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error("Exception when fetching tenant preferences", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }

   }

   /**
    * 
    * @return
    */
   @GET
   @Path("/user")
   @Produces(MediaType.APPLICATION_JSON)
   public Response getUserPreferences(@QueryParam("realmId") String realmId, @QueryParam("userId") String userId)
   {
      try
      {
         List<PreferenceDTO> result = PreferenceService.fetchUserPreferences(userId, realmId);
         return Response.ok(GsonUtils.toJsonHTMLSafeString(result), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error("Exception when fetching Uuser preferences", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/permissions/grants")
   public Response getAllPermissionsAndGrants()
   {
      Map<String, Set<PermissionDTO>> permissions = authorizationManagerService.fetchPermissions();
      return Response.ok(GsonUtils.toJsonHTMLSafeString(permissions), MediaType.APPLICATION_JSON).build();
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/permissions/grants")
   public Response setGrants(String postedData)
   {
      Map grantsMap = null;
      if (StringUtils.isNotEmpty(postedData))
      {
         grantsMap = JsonDTO.getAsMap(postedData);
      }
      Map<String, Set<PermissionDTO>> permissions = authorizationManagerService.updateGrants(
            (List<String>) grantsMap.get("allow"), (List<String>) grantsMap.get("deny"),
            (List<String>) grantsMap.get("participants"));
      return Response.ok(GsonUtils.toJsonHTMLSafeString(permissions), MediaType.APPLICATION_JSON).build();
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/permissions/grants/restore/{permisionId}")
   public Response restoreGrants(@PathParam("permisionId") String permissionId)
   {
      Map<String, Set<PermissionDTO>> permissions = authorizationManagerService.restoreGrants(permissionId);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(permissions), MediaType.APPLICATION_JSON).build();
   }
   
   //To restore multiple permissions
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/permissions/grants/restore")
   public Response restoreGrants2(@QueryParam("permissionIds") String permissionIds)
   {
      Map<String, Set<PermissionDTO>> permissions = authorizationManagerService.restoreGrants(permissionIds);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(permissions), MediaType.APPLICATION_JSON).build();
   }
}