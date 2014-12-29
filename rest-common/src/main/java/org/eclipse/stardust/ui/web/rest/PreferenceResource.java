/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.ui.web.rest.service.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
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

         Preferences preferences = adminService.getPreferences(pScope, moduleId, preferenceId);
         
         Map<String, Serializable> values = preferences.getPreferences();
         Gson gson = new Gson();
         return Response.ok(gson.toJson(values), MediaType.APPLICATION_JSON).build();
      }

      return null;
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

      return Response.ok("", MediaType.APPLICATION_JSON).build();
   }
}
