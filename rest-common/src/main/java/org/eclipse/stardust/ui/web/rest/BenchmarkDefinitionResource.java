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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.BenchmarkDefinitionService;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkDefinitionDTO;
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
         for (BenchmarkDefinitionDTO dto : benchmarkDefs)
         {
            JsonObject benchmark = new JsonObject();
            benchmark.add("metadata", new Gson().toJsonTree(dto.metadata));
            benchmark.add("contents", dto.contents);
            jsonArray.add(benchmark);
         }
         benchmarkDefinitions.add("benchmark-definitions", jsonArray);
         
         return Response.ok(benchmarkDefinitions.toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }
}
