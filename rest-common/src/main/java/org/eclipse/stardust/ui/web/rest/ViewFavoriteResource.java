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
import org.eclipse.stardust.ui.web.rest.misc.RequestDescription;
import org.eclipse.stardust.ui.web.rest.misc.ResponseDescription;
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
   @ResponseDescription("#### Sample Response:\r\n" + 
         "\r\n" + 
         "    [  \r\n" + 
         "      {\r\n" + 
         "        \"scope\":\"USER\",\r\n" + 
         "        \"moduleId\":\"FAVORITE\",\r\n" + 
         "        \"preferenceId\":\"trafficLightViewNew\",\r\n" + 
         "        \"preferenceName\":\"TLV1\",\r\n" + 
         "        \"preferenceValue\":\"{\r\n" + 
         "                    \"selectedProcesses\":[\r\n" + 
         "                      {\r\n" + 
         "                        \"id\":\"{CustomerManagement}CustomerOnboarding\",\r\n" + 
         "                        \"name\":\"Customer Onboarding\"\r\n" + 
         "                       }\r\n" + 
         "                       ],\r\n" + 
         "                    \"selectedBenchmarks\":[1],\r\n" + 
         "                    \"selectedDrillDown\":\"PROCESS_WORKITEM\",\r\n" + 
         "                    \"selectedDateType\":\"BUSINESS_DATE\",\r\n" + 
         "                    \"dayOffset\":-8\r\n" + 
         "                    }\",\r\n" + 
         "       \"isPasswordType\":false,\r\n" + 
         "       \"partitionId\":\"default\"\r\n" + 
         "       },\r\n" + 
         "       {\r\n" + 
         "        \"scope\":\"USER\",\r\n" + 
         "        \"moduleId\":\"FAVORITE\",\r\n" + 
         "        \"preferenceId\":\"trafficLightViewNew\",\r\n" + 
         "        \"preferenceName\":\"TLV2\",\r\n" + 
         "        \"preferenceValue\":\"{\r\n" + 
         "                      \"selectedProcesses\":[\r\n" + 
         "                      {\r\n" + 
         "                      \"id\":\"{Model12}ConsumerPepperProcess\",\r\n" + 
         "                      \"name\":\"ConsumerPepperProcess\"\r\n" + 
         "                      },\r\n" + 
         "                      {\"id\":\"{CustomerManagement}CustomerOnboarding\",\r\n" + 
         "                      \"name\":\"Customer Onboarding\"\r\n" + 
         "                      } ],\r\n" + 
         "                      \"selectedBenchmarks\":[1],\r\n" + 
         "                      \"selectedDrillDown\":\"PROCESS_WORKITEM\",\r\n" + 
         "                      \"selectedDateType\":\"BUSINESS_DATE\",\r\n" + 
         "                      \"dayOffset\":-8\r\n" + 
         "                      }\",\r\n" + 
         "        \"isPasswordType\":false,\r\n" + 
         "        \"partitionId\":\"default\"\r\n" + 
         "        }\r\n" + 
         "    ]")
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
   @ResponseDescription("#### Sample Response:\r\n" + 
         "    { \r\n" + 
         "     \"scope\":\"USER\",    \r\n" + 
         "     \"moduleId\":\"FAVORITE\",  \r\n" + 
         "     \"preferenceId\":\"trafficLightViewNew\",  \r\n" + 
         "     \"preferenceName\":\"TLV1\",   \r\n" + 
         "     \"preferenceValue\":\"{ \r\n" + 
         "                        \"selectedProcesses\": [{\"id\":\"ALL_PROCESSES\",  \r\n" + 
         "                        \"name\":\"AllProcesses\"}],  \r\n" + 
         "                        \"selectedBenchmarks\":[\"ALL_BENCHMARKS\"],\r\n" + 
         "                        \"selectedDrillDown\":\"PROCESS_WORKITEM\",  \r\n" + 
         "                        \"selectedDateType\":\"BUSINESS_DATE\",  \r\n" + 
         "                        \"dayOffset\":0\r\n" + 
         "                       }\",\r\n" + 
         "    \"isPasswordType\":false,  \r\n" + 
         "    \"partitionId\":\"default\"\r\n" + 
         "    }")
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
   @RequestDescription("#### Sample Request:\r\n" + 
         "    { \r\n" + 
         "       \"dayOffset\": -7,\r\n" + 
         "       \"selectedBenchmarks\": [1],\r\n" + 
         "       \"selectedDateType\": \"BUSINESS_DATE\",\r\n" + 
         "       \"selectedDrillDown\": \"PROCESS_WORKITEM\",\r\n" + 
         "       \"selectedProcesses\": [\r\n" + 
         "                             { \"id\": \"{CustomerManagement}CustomerOnboarding\", \r\n" + 
         "                               \"name\": \"Customer Onboarding\"\r\n" + 
         "                             }\r\n" + 
         "                            ],\r\n" + 
         "        \"showBusinessObjects\": false,\r\n" + 
         "        \"showGroupByObjects\": false\r\n" + 
         "    }")
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