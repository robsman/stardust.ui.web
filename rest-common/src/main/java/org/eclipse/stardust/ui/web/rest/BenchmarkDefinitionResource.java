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
import org.eclipse.stardust.ui.web.rest.service.BenchmarkDefinitionService;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkDefinitionDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.JsonDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
@Component
@Path("/benchmark-definitions")
public class BenchmarkDefinitionResource
{
   private static final Logger trace = LogManager.getLogger(BenchmarkDefinitionResource.class);

   @Autowired
   private BenchmarkDefinitionService benchmarkDefinitionService;

   private JsonMarshaller jsonIo = new JsonMarshaller();

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/design-time")
   public Response getBenchmarkDefinitions()
   {
      try
      {
         List<BenchmarkDefinitionDTO> benchmarkDefs = benchmarkDefinitionService.getBenchmarkDefinitions();
         JsonObject benchmarkDefinitions = new JsonObject();
         JsonArray jsonArray = new JsonArray();
         for (BenchmarkDefinitionDTO benchmarkDefinition : benchmarkDefs)
         {
            JsonObject benchmark = createBenchmarkJSON(benchmarkDefinition);
            jsonArray.add(benchmark);
         }
         benchmarkDefinitions.add("benchmarkDefinitions", jsonArray);

         return Response.ok(benchmarkDefinitions.toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error("Exception while fetching design time Benchmark Definitions", e);

         return Response.serverError().build();
      }
   }
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/run-time")
   public Response getRuntimeBenchmarkDefinitions()
   {
      try
      {
         List<BenchmarkDefinitionDTO> benchmarkDefs = benchmarkDefinitionService.getRuntimeBenchmarkDefinitions();
         JsonObject benchmarkDefinitions = new JsonObject();
         JsonArray jsonArray = new JsonArray();
         for (BenchmarkDefinitionDTO benchmarkDefinition : benchmarkDefs)
         {
            JsonObject benchmark = createBenchmarkJSON(benchmarkDefinition);
            jsonArray.add(benchmark);
         }
         benchmarkDefinitions.add("benchmarkDefinitions", jsonArray);

         return Response.ok(benchmarkDefinitions.toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error("Exception while fetching Runtime Benchmark Definitions", e);

         return Response.serverError().build();
      }

   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/run-time/{runtimeOid}")
   public Response getRuntimeBenchmarkDefinition(@PathParam("runtimeOid") long runtimeOid)
   {
      try
      {
         BenchmarkDefinitionDTO benchmarkDefinitionDTO = benchmarkDefinitionService.getRuntimeBenchmarkDefinition(runtimeOid);
         JsonObject benchmark = createBenchmarkJSON(benchmarkDefinitionDTO);
         return Response.ok(benchmark.toString()).build();   
      }
      catch (Exception e)
      {
         trace.error("Exception while fetching Runtime Benchmark Definition", e);

         return Response.serverError().build();
      }
      
   }
   
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/run-time")
   public Response publishBenchmarkDefinition(String postedData)
   {
      try
      {
         JsonObject benchmarkJSON = JsonDTO.getJsonObject(postedData);
         String benchmarkId = GsonUtils.extractString(benchmarkJSON, "id");
         benchmarkDefinitionService.publishBenchmarkDefinition(benchmarkId);
         return Response.ok(" ", MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error("Exception while publishing Benchmark Definitions", e);

         return Response.serverError().build();
      }
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/design-time")
   public Response createBenchmarkDefinition(String postedData)
   {
      try
      {
         JsonObject benchmarkData = jsonIo.readJsonObject(postedData);
         BenchmarkDefinitionDTO benchmarkDefinition = benchmarkDefinitionService
               .createBenchmarkDefinition(benchmarkData);

         return Response.ok(createBenchmarkJSON(benchmarkDefinition).toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @PUT
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/design-time/{id}")
   public Response updateBenchmarkDefinition(@PathParam("id") String benchmarkId, String postedData)
   {
      try
      {
         JsonObject benchmarkData = jsonIo.readJsonObject(postedData);
         BenchmarkDefinitionDTO benchmarkDefinition = benchmarkDefinitionService.updateBenchmarkDefinition(benchmarkId,
               benchmarkData);

         return Response.ok(createBenchmarkJSON(benchmarkDefinition).toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error("Exception while updating design time Benchmark Definitions", e);

         return Response.serverError().build();
      }
   }

   @DELETE
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/design-time/{id}")
   public Response deleteBenchmarkDefinition(@PathParam("id") String benchmarkId)
   {
      try
      {
         benchmarkDefinitionService.deleteBenchmarkDefinition(benchmarkId);
         return Response.ok("Benchmark " + benchmarkId + " deleted.", MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error("Exception while deleting design time Benchmark Definitions", e);

         return Response.serverError().build();
      }

   }

   /**
    * 
    * @param benchmarkDefinition
    * @return
    * @throws Exception
    */
   private JsonObject createBenchmarkJSON(BenchmarkDefinitionDTO benchmarkDefinition) throws Exception
   {
      JsonObject benchmarkDefinitionJson = new JsonObject();
      try
      {
         benchmarkDefinitionJson.add("metadata", new Gson().toJsonTree(benchmarkDefinition.metadata));
         benchmarkDefinitionJson.add("content", benchmarkDefinition.content);
         return benchmarkDefinitionJson;
      }
      catch (Exception e)
      {
         throw e;
      }
   }

}