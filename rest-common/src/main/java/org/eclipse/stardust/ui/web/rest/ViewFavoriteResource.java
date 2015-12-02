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

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.service.ViewFavoriteService;
import org.eclipse.stardust.ui.web.rest.service.dto.PreferenceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.SaveFavoriteStatusDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * @author Abhay.Thappan
 *
 */
@Component
@Path("/favorites")
public class ViewFavoriteResource
{
   private static final Logger trace = LogManager.getLogger(ViewFavoriteResource.class);

   @Autowired
   private ViewFavoriteService viewFavoriteService;

   @SuppressWarnings({"rawtypes", "unchecked"})
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{preferenceId}/{preferenceName}")
   public Response addFavorite(@PathParam("preferenceId") String preferenceId,
         @PathParam("preferenceName") String preferenceName, String postedData)
   {
      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject postJSON = jsonIo.readJsonObject(postedData);

      try
      {
         SaveFavoriteStatusDTO resultDTO = viewFavoriteService.addFavorite(preferenceId, preferenceName,
               postJSON.toString());
         Gson gson = new Gson();
         return Response.ok(gson.toJson(resultDTO), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Response getAllFavorite()
   {
      try
      {
         List<PreferenceDTO> prefList = viewFavoriteService.getAllFavorite(null);
         return Response.ok(GsonUtils.toJsonHTMLSafeString(prefList), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{preferenceId}")
   public Response getFavoriteByType(@PathParam("preferenceId") String preferenceId)
   {
      try
      {
         List<PreferenceDTO> prefList = viewFavoriteService.getAllFavorite(preferenceId);
         return Response.ok(GsonUtils.toJsonHTMLSafeString(prefList), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{preferenceId}/{preferenceName}")
   public Response getFavoriteByName(@PathParam("preferenceId") String preferenceId,
         @PathParam("preferenceName") String preferenceName)
   {
      try
      {
         List<PreferenceDTO> prefList = viewFavoriteService.getAllFavorite(preferenceId);
         PreferenceDTO preferenceDTO = null;
         for (PreferenceDTO pref : prefList)
         {
            if (pref.preferenceName.equals(preferenceName))
            {
               preferenceDTO = pref;
            }
         }
         return Response.ok(GsonUtils.toJsonHTMLSafeString(preferenceDTO), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   @PUT
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{preferenceId}/{preferenceName}")
   public Response updateFavorite(@PathParam("preferenceId") String preferenceId,
         @PathParam("preferenceName") String preferenceName, String postedData)
   {
      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject postJSON = jsonIo.readJsonObject(postedData);

      try
      {
         viewFavoriteService.updateFavorite(preferenceId, preferenceName, postJSON.toString());
         Gson gson = new Gson();
         return Response.ok(gson.toJson(""), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   @DELETE
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{preferenceId}/{preferenceName}")
   public Response deleteFavorite(@PathParam("preferenceId") String preferenceId,
         @PathParam("preferenceName") String preferenceName)
   {
      try
      {
         viewFavoriteService.deleteFavorite(preferenceId, preferenceName);
         Gson gson = new Gson();
         return Response.ok(gson.toJson(""), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }
}