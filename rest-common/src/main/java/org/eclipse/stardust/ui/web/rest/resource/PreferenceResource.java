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

import static org.eclipse.stardust.ui.web.common.util.StringUtils.splitUnique;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import org.eclipse.stardust.ui.web.rest.component.service.AuthorizationManagerService;
import org.eclipse.stardust.ui.web.rest.component.service.PreferenceService;
import org.eclipse.stardust.ui.web.rest.component.util.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.rest.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.dto.PreferenceDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.PermissionDTO;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

/**
 * @author Subodh.Godbole
 * @author Yogesh.Manware
 *
 */
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
   public Response getAllPermissions()
   {
      Map<String, Set<PermissionDTO>> permissions = authorizationManagerService.fetchPermissions();
      return Response.ok(GsonUtils.toJsonHTMLSafeString(permissions), MediaType.APPLICATION_JSON).build();
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/permissions/grants")
   public Response updatePermissions(String postedData)
   {
      Map grantsMap = null;
      if (StringUtils.isNotEmpty(postedData))
      {
         grantsMap = JsonDTO.getAsMap(postedData);
      }
      
      HashSet<String> participant = new HashSet<String>((Collection< ? extends String>) grantsMap.get("participants"));
      HashSet<String> allow = null;
      if (grantsMap.get("allow") != null)
      {
         allow = new HashSet<String>((Collection< ? extends String>) grantsMap.get("allow"));
      }
      
      HashSet<String> deny = null;
      if (grantsMap.get("deny") != null)
      {
         deny = new HashSet<String>((Collection< ? extends String>) grantsMap.get("deny"));
      }      
      boolean overwrite = false;
      if (grantsMap.containsKey("overwrite"))
      {
         overwrite = (Boolean) grantsMap.get("overwrite");
      }
      
      Set<PermissionDTO> permissions = authorizationManagerService.updatePermissions(allow, deny, participant,
            overwrite);
      Map<String, Set<PermissionDTO>> result = new HashMap<String, Set<PermissionDTO>>();
      result.put("permissions", permissions);
      
      return Response.ok(GsonUtils.toJsonHTMLSafeString(result), MediaType.APPLICATION_JSON).build();
   }

 /*  @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/permissions/grants/restore/{permisionId}")
   public Response restoreGrants(@PathParam("permisionId") String permissionId)
   {
      Map<String, Set<PermissionDTO>> permissions = authorizationManagerService.restoreGrants(permissionId);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(permissions), MediaType.APPLICATION_JSON).build();
   }*/
   
  /* //To restore multiple permissions
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/permissions/grants/restore")
   public Response restoreGrants2(@QueryParam("permissionIds") String permissionIds)
   {
      Map<String, Set<PermissionDTO>> permissions = authorizationManagerService.restoreGrants(permissionIds);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(permissions), MediaType.APPLICATION_JSON).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/participants")
   public Response getPermissionsForParticipants(@QueryParam("participantIds") String participantIds)
   {
      Set<String> participantQualifiedIds = splitUnique(participantIds, ",");
      Collection<ParticipantDTO> participants = authorizationManagerService
            .fetchPermissionsForParticipants(participantQualifiedIds);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(participants), MediaType.APPLICATION_JSON).build();
   }*/

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/participants/clone")
   public Response cloneParticipant(@QueryParam("sourceParticipantIds") String sourceParticipantIds,
         @QueryParam("targetParticipantIds") String targetParticipantIds)
   {
      Set<String> sourceParticipantQualifiedIds = splitUnique(sourceParticipantIds, ",");
      Set<String> targetParticipantQualifiedIds = splitUnique(targetParticipantIds, ",");
      Map<String, Set<PermissionDTO>> participants = authorizationManagerService.cloneParticipant(
            sourceParticipantQualifiedIds, targetParticipantQualifiedIds);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(participants), MediaType.APPLICATION_JSON).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/participants/restore")
   public Response restoreParticipants(@QueryParam("participantIds") String participantIds)
   {
      Set<String> participantQualifiedIds = splitUnique(participantIds, ",");
      Map<String, Set<PermissionDTO>> participants = authorizationManagerService
            .restoreParticipants(participantQualifiedIds);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(participants), MediaType.APPLICATION_JSON).build();
   }
}