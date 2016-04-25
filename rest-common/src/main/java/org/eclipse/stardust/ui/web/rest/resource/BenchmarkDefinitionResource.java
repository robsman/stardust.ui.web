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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.component.service.BenchmarkDefinitionService;
import org.eclipse.stardust.ui.web.rest.documentation.DTODescription;
import org.eclipse.stardust.ui.web.rest.documentation.RequestDescription;
import org.eclipse.stardust.ui.web.rest.documentation.ResponseDescription;
import org.eclipse.stardust.ui.web.rest.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.dto.BenchmarkCategoryDTO;
import org.eclipse.stardust.ui.web.rest.dto.BenchmarkDefinitionDTO;
import org.eclipse.stardust.ui.web.rest.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.util.JsonMarshaller;
import org.eclipse.stardust.ui.web.viewscommon.common.exceptions.I18NException;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
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
   @ResponseDescription("Return list of design-time Benchmark Definitions\r\n"
         + "``` javascript\r\n"
         + "\n" + 
         "{\n" + 
         "  \"benchmarkDefinitions\": [\n" + 
         "    {\n" + 
         "      \"metadata\": {\n" + 
         "        \"author\": \"motu\",\n" + 
         "        \"lastModifiedDate\": 1433132165\n" + 
         "      },\n" + 
         "      \"content\": {\n" + 
         "        \"id\": \"13bd0f8e-45d0-4a7b-9d03-ed4d288bbdd4\",\n" + 
         "        \"name\": \"Benchmark Definition 1\",\n" + 
         "        \"description\": \"Benchmark Definition 1 Description\",\n" + 
         "        \"categories\": [\n" + 
         "           \n" + 
         "        ],\n" + 
         "        \"defaults\": {\n" + 
         "           \n" + 
         "        },\n" + 
         "        \"models\": [\n" + 
         "           \n" + 
         "        ]\n" + 
         "      }\n" + 
         "    },\n" + 
         "    {\n" + 
         "      \"metadata\": {\n" + 
         "        \"author\": \"motu\",\n" + 
         "        \"lastModifiedDate\": 1433132165\n" + 
         "      },\n" + 
         "      \"content\": {\n" + 
         "        \"id\": \"38724575-46ec-4bd6-93b5-cf38bb6bd308\",\n" + 
         "        \"name\": \"Benchmark Definition 2\",\n" + 
         "        \"description\": \"Benchmark Definition 2 Description\",\n" + 
         "        \"categories\": [\n" + 
         "           \n" + 
         "        ],\n" + 
         "        \"defaults\": {\n" + 
         "           \n" + 
         "        },\n" + 
         "        \"models\": [\n" + 
         "           \n" + 
         "        ]\n" + 
         "      }\n" + 
         "    },\n" + 
         "     \n" + 
         "  ]\n" + 
         "}\r\n"
         + "```")
   public Response getBenchmarkDefinitions()
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
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/run-time")
   @ResponseDescription("Return list of run-time Benchmark\r\n"
         + "``` javascript\r\n"
         + "{\n" + 
         "  \"benchmarkDefinitions\": [\n" + 
         "    {\n" + 
         "      \"metadata\": {\n" + 
         "        \"runtimeOid\": 11\n" + 
         "        \"modifiedBy\": \"\",\n" + 
         "        \"lastModified\": 0\n" + 
         "      },\n" + 
         "      \"content\": {\n" + 
         "        \"id\": \"13bd0f8e-45d0-4a7b-9d03-ed4d288bbdd4\",\n" + 
         "        \"name\": \"Benchmark Definition 1\",\n" + 
         "        \"description\": \"Benchmark Definition 1 Description\",\n" + 
         "        \"categories\": [\n" + 
         "           \n" + 
         "        ],\n" + 
         "        \"defaults\": {\n" + 
         "           \n" + 
         "        },\n" + 
         "        \"models\": [\n" + 
         "           \n" + 
         "        ]\n" + 
         "      }\n" + 
         "    },\n" + 
         "    {\n" + 
         "      \"metadata\": {\n" + 
         "        \"author\": \"motu\",\n" + 
         "        \"lastModifiedDate\": 1433132165\n" + 
         "      },\n" + 
         "      \"content\": {\n" + 
         "        \"id\": \"38724575-46ec-4bd6-93b5-cf38bb6bd308\",\n" + 
         "        \"name\": \"Benchmark Definition 2\",\n" + 
         "        \"description\": \"Benchmark Definition 2 Description\",\n" + 
         "        \"categories\": [\n" + 
         "           \n" + 
         "        ],\n" + 
         "        \"defaults\": {\n" + 
         "           \n" + 
         "        },\n" + 
         "        \"models\": [\n" + 
         "           \n" + 
         "        ]\n" + 
         "      }\n" + 
         "    },\n" + 
         "     \n" + 
         "  ]\n" + 
         "}\r\n" + 
         "```")
   public Response getRuntimeBenchmarkDefinitions()
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

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/run-time/{runtimeOid}")
   public Response getRuntimeBenchmarkDefinition(@PathParam("runtimeOid") long runtimeOid)
   {
         BenchmarkDefinitionDTO benchmarkDefinitionDTO = benchmarkDefinitionService.getRuntimeBenchmarkDefinition(runtimeOid);
         JsonObject benchmark = createBenchmarkJSON(benchmarkDefinitionDTO);
         return Response.ok(benchmark.toString()).build();   
   }
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/run-time/categories")
   @ResponseDescription("The response will contain list of BenchmarkCategoryDTO")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.BenchmarkCategoryDTO")
   public Response getRuntimeBenchmarkCategories(@QueryParam("oids") String benchmarkOids)
   {
         Set<BenchmarkCategoryDTO> benchmarkDefs = benchmarkDefinitionService
               .getRuntimeBenchmarkCategories(benchmarkOids);
            return Response.ok(AbstractDTO.toJson(new ArrayList<BenchmarkCategoryDTO>(benchmarkDefs)), MediaType.APPLICATION_JSON).build();            
   }
   
   @DELETE
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/run-time/{runtimeOid}")
   public Response deletePublishedBenchmarkDefinition(@PathParam("runtimeOid") long runtimeOid)
   {
         benchmarkDefinitionService.deletePublishedBenchmarkDefinition(runtimeOid);
         return Response.ok("Run-time Benchmark " + runtimeOid + " deleted.", MediaType.TEXT_PLAIN).build();
   }
   
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/run-time")
   @RequestDescription("The post data should be in below JSON format\r\n"
         + "``` javascript\r\n"
         + "{ \n" + 
         "      \"id\": \"13bd0f8e-45d0-4a7b-9d03-ed4d288bbdd4\",\n" + 
         "      \"validFrom\": 1433132950\n" + 
         "}\r\n"
         + "```")
   public Response publishBenchmarkDefinition(String postedData)
   {
         JsonObject benchmarkJSON = JsonDTO.getJsonObject(postedData);
         String benchmarkId = GsonUtils.extractString(benchmarkJSON, "id");
         benchmarkDefinitionService.publishBenchmarkDefinition(benchmarkId);
         return Response.ok(" ", MediaType.APPLICATION_JSON).build();
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/design-time")
   public Response createBenchmarkDefinition(String postedData)
   {
         JsonObject benchmarkData = jsonIo.readJsonObject(postedData);
         BenchmarkDefinitionDTO benchmarkDefinition = benchmarkDefinitionService
               .createBenchmarkDefinition(benchmarkData);

         return Response.ok(createBenchmarkJSON(benchmarkDefinition).toString(), MediaType.APPLICATION_JSON).build();
   }
   
   @POST
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/design-time/files")
	public Response upload(MultipartBody body) {
	   try {
		   
		    Attachment attachment = body.getAttachment("file");
			
			DataHandler dataHandler = attachment.getDataHandler();
			InputStream inputStream = dataHandler.getInputStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			
			byte[] tmp = new byte[4096];
			int ret = 0;
	
			while ((ret = inputStream.read(tmp)) > 0) {
				bos.write(tmp, 0, ret);
			}
			
			String content = bos.toString();
			
			//Required as IE will place a BOM (Byte Order Mark) at the head of the file.
			content = content.substring(content.indexOf("{"));
			
			JsonObject benchmarkData = jsonIo.readJsonObject(content);
	        BenchmarkDefinitionDTO benchmarkDefinition = benchmarkDefinitionService.createBenchmarkDefinition(benchmarkData);
	        
	        return Response.ok(createBenchmarkJSON(benchmarkDefinition).toString(), MediaType.APPLICATION_JSON).build();
	        

		} catch (Exception e) {
			trace.error(e, e);
			String error = createBenchmarkUploadErrorJSON(e).toString();
			return Response.serverError().entity(error).status(500).build();

		}
	}
   
   @PUT
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/design-time/{id}")
   public Response updateBenchmarkDefinition(@PathParam("id") String benchmarkId, String postedData)
   {
         JsonObject benchmarkData = jsonIo.readJsonObject(postedData);
         BenchmarkDefinitionDTO benchmarkDefinition = benchmarkDefinitionService.updateBenchmarkDefinition(benchmarkId,
               benchmarkData);

         return Response.ok(createBenchmarkJSON(benchmarkDefinition).toString(), MediaType.APPLICATION_JSON).build();
   }

   @DELETE
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/design-time/{id}")
   public Response deleteBenchmarkDefinition(@PathParam("id") String benchmarkId)
   {
         benchmarkDefinitionService.deleteBenchmarkDefinition(benchmarkId);
         return Response.ok("Benchmark " + benchmarkId + " deleted.", MediaType.TEXT_PLAIN).build();

   }
   
   private JsonObject createBenchmarkUploadErrorJSON(Exception ex){
	   
	   Gson gson = new Gson();
	   JsonObject errorObj = new JsonObject();
	   JsonObject envelope = new JsonObject();
	   
	   String errorMessage = ex.getMessage();
	   
	   if(errorMessage == null)
	   {
		   errorMessage = "";
	   }
	   
	   JsonArray failures = new JsonArray();
	   envelope.addProperty("message", errorMessage);
	   failures.add(envelope);
	   errorObj.add("failures", failures);
	   return errorObj;
   }
   
   /**
    * 
    * @param benchmarkDefinition
    * @return
    * @throws Exception
    */
   private JsonObject createBenchmarkJSON(BenchmarkDefinitionDTO benchmarkDefinition)
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
         throw new I18NException(e.getLocalizedMessage());
      }
   }


}
