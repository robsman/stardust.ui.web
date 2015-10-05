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
 * @author Johnson.Quadras
 */
package org.eclipse.stardust.ui.web.rest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.TrafficLightViewService;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkCategoryDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkProcessActivitiesTLVStatisticsResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkTLVStatisticsByBOResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessDefinitionDTO;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Component
@Path("/trafficLightView")
public class TrafficLightViewResource
{
   public static final Logger trace = LogManager.getLogger(TrafficLightViewResource.class);

   @Resource
   private TrafficLightViewService trafficLightViewService;

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/stats")
   public Response getTrafficLightViewStatastic(String postData)
   {
      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject postJSON = jsonIo.readJsonObject(postData);
      Boolean isAllBenchmarks = postJSON.getAsJsonPrimitive("isAllBenchmarks").getAsBoolean();
      Boolean isAllProcessess = postJSON.getAsJsonPrimitive("isAllProcessess").getAsBoolean();

      JsonArray bOidsArray = postJSON.getAsJsonArray("bOids");
      Type type = new TypeToken<List<Long>>()
      {
      }.getType();
      List<Long> bOids = new ArrayList<Long>();
      if (null != bOidsArray)
      {
         bOids = new Gson().fromJson(bOidsArray.toString(), type);

      }

      JsonArray processesArray = postJSON.getAsJsonArray("processes");
      Type typeForProcessess = new TypeToken<List<ProcessDefinitionDTO>>()
      {
      }.getType();
      List<ProcessDefinitionDTO> processes = new ArrayList<ProcessDefinitionDTO>();
      if (null != processesArray)
      {
         processes = new Gson().fromJson(processesArray.toString(), typeForProcessess);

      }

      String dateType = postJSON.getAsJsonPrimitive("dateType").getAsString();

      Integer dayOffset = postJSON.getAsJsonPrimitive("dayOffset").getAsInt();

      JsonArray categories = postJSON.getAsJsonArray("categories");

      Type typeBenchmarkCategories = new TypeToken<List<BenchmarkCategoryDTO>>()
      {
      }.getType();
      List<BenchmarkCategoryDTO> benchmarkCategories = new ArrayList<BenchmarkCategoryDTO>();
      if (null != categories)
      {
         benchmarkCategories = new Gson().fromJson(categories.toString(), typeBenchmarkCategories);

      }

      JsonObject processActivityMap = postJSON.getAsJsonObject("processActivityMap");
      Set<Entry<String, JsonElement>> processActivityMapEntrySet = processActivityMap.entrySet();

      Map<String, List<String>> processActivitiesMap = new HashMap<String, List<String>>();
      for (Entry<String, JsonElement> entry : processActivityMapEntrySet)
      {
         JsonArray processActivityArray = entry.getValue().getAsJsonArray();

         Type processActivityType = new TypeToken<List<String>>()
         {
         }.getType();
         List<String> activities = new ArrayList<String>();
         if (null != processActivityArray)
         {
            activities = new Gson().fromJson(processActivityArray.toString(), processActivityType);

         }
         processActivitiesMap.put(entry.getKey(), activities);
      }
      BenchmarkProcessActivitiesTLVStatisticsResultDTO result = trafficLightViewService.getTrafficLightViewStatastic(
            isAllBenchmarks, isAllProcessess, bOids, processes, dateType, dayOffset, benchmarkCategories,
            processActivitiesMap);
      return Response.ok(result.toJson(), MediaType.APPLICATION_JSON).build();
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/statsByBO")
   public Response getTrafficLightViewStatasticByBO(String postData)
   {
      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject postJSON = jsonIo.readJsonObject(postData);
      Boolean isAllBenchmarks = postJSON.getAsJsonPrimitive("isAllBenchmarks").getAsBoolean();
      Boolean isAllProcessess = postJSON.getAsJsonPrimitive("isAllProcessess").getAsBoolean();

      JsonArray bOidsArray = postJSON.getAsJsonArray("bOids");
      Type type = new TypeToken<List<Long>>()
      {
      }.getType();
      List<Long> bOids = new ArrayList<Long>();
      if (null != bOidsArray)
      {
         bOids = new Gson().fromJson(bOidsArray.toString(), type);

      }

      JsonArray processesArray = postJSON.getAsJsonArray("processes");
      Type typeForProcessess = new TypeToken<List<ProcessDefinitionDTO>>()
      {
      }.getType();
      List<ProcessDefinitionDTO> processes = new ArrayList<ProcessDefinitionDTO>();
      if (null != processesArray)
      {
         processes = new Gson().fromJson(processesArray.toString(), typeForProcessess);

      }

      String dateType = postJSON.getAsJsonPrimitive("dateType").getAsString();

      Integer dayOffset = postJSON.getAsJsonPrimitive("dayOffset").getAsInt();

      JsonArray categories = postJSON.getAsJsonArray("categories");

      Type typeBenchmarkCategories = new TypeToken<List<BenchmarkCategoryDTO>>()
      {
      }.getType();
      List<BenchmarkCategoryDTO> benchmarkCategories = new ArrayList<BenchmarkCategoryDTO>();
      if (null != categories)
      {
         benchmarkCategories = new Gson().fromJson(categories.toString(), typeBenchmarkCategories);

      }

      String businessObjectQualifiedId = postJSON.getAsJsonPrimitive("businessObjectQualifiedId").getAsString();
      String businessObjectType = postJSON.getAsJsonPrimitive("businessObjectType").getAsString();

      JsonArray selectedBusinessObjectInstances = postJSON.getAsJsonArray("selectedBusinessObjectInstances");

      Type boInstances = new TypeToken<Set<String>>()
      {
      }.getType();
      Set<String> selBOInstances = new HashSet<String>();
      if (null != selectedBusinessObjectInstances)
      {
         selBOInstances = new Gson().fromJson(selectedBusinessObjectInstances.toString(), boInstances);

      }

      String groupBybusinessQualifiedId = null;
      Set<String> selGroupByBOInstances = null;
      if (postJSON.getAsJsonPrimitive("groupBybusinessQualifiedId") != null)
      {
         groupBybusinessQualifiedId = postJSON.getAsJsonPrimitive("groupBybusinessQualifiedId").getAsString();
         String groupBybusinessObjectType = postJSON.getAsJsonPrimitive("groupBybusinessObjectType").getAsString();

         JsonArray selectedRelatedBusinessObjectInstances = postJSON
               .getAsJsonArray("selectedRelatedBusinessObjectInstances");

         Type groupbyBOInstances = new TypeToken<Set<String>>()
         {
         }.getType();
         selGroupByBOInstances = new HashSet<String>();
         if (null != selectedRelatedBusinessObjectInstances)
         {
            selGroupByBOInstances = new Gson().fromJson(selectedRelatedBusinessObjectInstances.toString(),
                  groupbyBOInstances);
         }
      }

      BenchmarkTLVStatisticsByBOResultDTO result = trafficLightViewService.getTrafficLightViewStatasticByBO(
            isAllBenchmarks, isAllProcessess, bOids, processes, dateType, dayOffset, benchmarkCategories,
            businessObjectQualifiedId, selBOInstances, groupBybusinessQualifiedId, selGroupByBOInstances);
      return Response.ok(result.toJson(), MediaType.APPLICATION_JSON).build();
   }
}
