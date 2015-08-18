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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.service.ProcessDefinitionService;
import org.eclipse.stardust.ui.web.rest.service.ProcessInstanceService;
import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.InstanceCountsDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessTableFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessTableFilterDTO.DescriptorFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component
@Path("/process-instances")
public class ProcessInstanceResource
{
   private static final Logger trace = LogManager
         .getLogger(ProcessInstanceResource.class);

   @Autowired
   private ProcessInstanceService processInstanceService;
   
   @Autowired
   ProcessDefinitionService processDefService;

   private final JsonMarshaller jsonIo = new JsonMarshaller();

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{processInstanceOid: \\d+}/documents/{documentId}/split")
   public Response splitDocument(@PathParam("processInstanceOid")
   long processInstanceOid, @PathParam("documentId")
   String documentId, String postedData)
   {
      try
      {
         JsonObject json = jsonIo.readJsonObject(postedData);

         return Response.ok(
               getProcessInstanceService().splitDocument(processInstanceOid, documentId,
                     json).toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{processInstanceOid: \\d+}/documents/{dataPathId}")
   public Response addDocument(@PathParam("processInstanceOid")
   String processInstanceOid, @PathParam("dataPathId")
   String dataPathId, String postedData)
   {
      try
      {
         JsonObject json = jsonIo.readJsonObject(postedData);

         return Response.ok(
               getProcessInstanceService().addProcessInstanceDocument(
                     Long.parseLong(processInstanceOid), dataPathId, json).toString(),
               MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @DELETE
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{processInstanceOid: \\d+}/documents/{dataPathId}{documentId: (/documentId)?}")
   public Response removeDocument(@PathParam("processInstanceOid")
   String processInstanceOid, @PathParam("dataPathId")
   String dataPathId, @PathParam("documentId")
   String documentId)
   {
      try
      {
         return Response.ok(
               getProcessInstanceService().removeProcessInstanceDocument(
                     Long.parseLong(processInstanceOid), dataPathId, documentId)
                     .toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("documentRendezvous.json")
   public Response getPendingProcesses(String postedData)
   {
      try
      {
         JsonObject json = jsonIo.readJsonObject(postedData);

         return Response.ok(
               getProcessInstanceService().getPendingProcesses(json).toString(),
               MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("processes.json")
   public Response startProcess(String postedData)
   {
      try
      {
         JsonObject json = jsonIo.readJsonObject(postedData);

         return Response.ok(getProcessInstanceService().startProcess(json).toString(),
               MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }
   
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/updatePriorities")
   public Response updatePriorities( String postedData)
   {
      try
      {
         Map<String, Integer> oidPriorityMap = (Map)GsonUtils.extractMap(JsonDTO.getJsonObject(postedData), "priorities");
         String jsonOutput = getProcessInstanceService().updatePriorities( oidPriorityMap);
         return Response.ok(jsonOutput, MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/allCounts")
   public Response getAllCounts()
   {
      try
      {
         InstanceCountsDTO processInstanceCountDTO = getProcessInstanceService().getAllCounts();
         return Response.ok(GsonUtils.toJsonHTMLSafeString(processInstanceCountDTO), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/allProcessStates")
   public Response getAllProcessStates()
   {
      try
      {
         return Response.ok(AbstractDTO.toJson(getProcessInstanceService().getAllProcessStates()), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }
   
   /**
    * @param postedData
    * @return
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/abort")
   public Response abortProcesses(String postedData)
   {
      // postedData = "{scope: 'root', processes : [11]}";
      return Response.ok(getProcessInstanceService().abortProcesses(postedData), MediaType.APPLICATION_JSON).build();
   }
   
   /**
    * @param postedData
    * @return
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/recover")
   public Response recoverProcesses(String postedData)
   {
      return Response.ok(getProcessInstanceService().recoverProcesses(postedData), MediaType.APPLICATION_JSON).build();
   }
   
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/attachToCase")
   public Response attachToCase(String request)
   {
      return Response
            .ok(processInstanceService.attachToCase(request), MediaType.APPLICATION_JSON).build();
   }
   
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/createCase")
   public Response createCase(String request)
   {
      return Response
            .ok(processInstanceService.createCase(request), MediaType.APPLICATION_JSON).build();
   }
   
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/search")
   public Response search(
         @PathParam("participantQId") String participantQId,
         @QueryParam("skip") @DefaultValue("0") Integer skip,
         @QueryParam("pageSize") @DefaultValue("8") Integer pageSize,
         @QueryParam("orderBy") @DefaultValue("oid") String orderBy,
         @QueryParam("orderByDir") @DefaultValue("asc") String orderByDir, String postData)
   {
      try
      {
         Options options = new Options(pageSize, skip, orderBy,
               "asc".equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);
         
         return Response.ok(GsonUtils.toJsonHTMLSafeString(processInstanceService.getProcessInstances(options)), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }
   
   /**
    * @author Nikhil.Gahlot
    * @param processInstanceOID
    * @return
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/spawnableProcesses")
  public Response spawnableProcesses(String postedData, @QueryParam("type") String type)
  {
      return Response
            .ok(processInstanceService.spawnableProcesses(postedData, type), MediaType.APPLICATION_JSON).build();
  }
   
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/checkIfProcessesAbortable")
  public Response checkIfProcessesAbortable(String postedData, @QueryParam("type") String type)
  {
     return Response
           .ok(processInstanceService.checkIfProcessesAbortable(postedData, type), MediaType.APPLICATION_JSON).build();
  }
   
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/switchProcess")
  public Response switchProcess(String postedData)
  {
      return Response
            .ok(processInstanceService.switchProcess(postedData), MediaType.APPLICATION_JSON).build();
  }
   
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/abortAndJoinProcess")
   public Response abortAndJoinProcess(String postedData)
   {
      return Response
            .ok(processInstanceService.abortAndJoinProcess(postedData), MediaType.APPLICATION_JSON).build();
   }
   
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/getRelatedProcesses")
   public Response getRelatedProcesses(String postedData, @QueryParam("matchAny") String matchAnyStr, @QueryParam("searchCases") String searchCasesStr)
   {
      boolean matchAny = "true".equals(matchAnyStr);
      boolean searchCases = "true".equals(searchCasesStr);
      
      return Response
            .ok(processInstanceService.getRelatedProcesses(postedData, matchAny, searchCases), MediaType.APPLICATION_JSON).build();
   }
   
   /**
    * Populate the options with the post data.
    * @param options
    * @param postData
    * @return
    */
   private Options populatePostData(Options options, String postData)
   {
      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject postJSON = jsonIo.readJsonObject(postData);

      // For filter
      JsonObject filters = postJSON.getAsJsonObject("filters");
      if (null != filters)
      {
         options.filter = getFilters(filters.toString());
      }


      JsonArray visbleColumns = postJSON.getAsJsonObject("descriptors").get("visbleColumns").getAsJsonArray();
      List<String> columnsList = new ArrayList<String>();
      for (JsonElement jsonElement : visbleColumns)
      {
         columnsList.add(StringUtils.substringAfter(jsonElement.getAsString(), "descriptorValues."));
      }
       options.visibleDescriptorColumns = columnsList;
       options.allDescriptorsVisible = postJSON.getAsJsonObject("descriptors").get("fetchAll").getAsBoolean();

      return options;
   }
   
   /**
    * Get the filters from the JSON string
    * @param jsonFilterString
    * @return
    */
   private ProcessTableFilterDTO getFilters(String jsonFilterString)
   {
      ProcessTableFilterDTO processListFilterDTO = null;
      if (StringUtils.isNotEmpty(jsonFilterString))
      {
         try
         {
            JsonMarshaller jsonIo = new JsonMarshaller();
            JsonObject json = jsonIo.readJsonObject(jsonFilterString);
            processListFilterDTO = DTOBuilder.buildFromJSON(json, ProcessTableFilterDTO.class,
                  ProcessTableFilterDTO.getCustomTokens());
            if (StringUtils.contains(jsonFilterString, "descriptorValues"))
            {
               populateDescriptorFilters(processListFilterDTO, json);
            }
         }
         catch (Exception e)
         {
            trace.error("Error in Deserializing filter JSON", e);
         }
      }

      return processListFilterDTO;
   }
   
   /**
    * Populates the descriptor filter values.
    * @param worklistFilter
    * @param descriptorColumnsFilterJson
    */
   private void populateDescriptorFilters(ProcessTableFilterDTO processListFilterDTO,
         JsonObject descriptorColumnsFilterJson)
   {

      List<DescriptorColumnDTO> descriptorColumns = processDefService
            .getDescriptorColumns(true);
      Map<String, DescriptorFilterDTO> descriptorColumnMap = new HashMap<String, DescriptorFilterDTO>();

      for (DescriptorColumnDTO descriptorColumnDTO : descriptorColumns)
      {
         Object filterDTO = null;
         if (null != descriptorColumnsFilterJson.get(descriptorColumnDTO.id))
         {
            // String TYPE
            if (ColumnDataType.STRING.toString().equals(descriptorColumnDTO.type))
            {
               filterDTO = new Gson().fromJson(
                     descriptorColumnsFilterJson.get(descriptorColumnDTO.id),
                     ProcessTableFilterDTO.TextSearchDTO.class);

            }
            else if (ColumnDataType.DATE.toString().equals(descriptorColumnDTO.type)
                  || ColumnDataType.NUMBER.toString().equals(descriptorColumnDTO.type))
            {
               filterDTO = new Gson().fromJson(
                     descriptorColumnsFilterJson.get(descriptorColumnDTO.id),
                     ProcessTableFilterDTO.RangeDTO.class);
            }
            else if (ColumnDataType.BOOLEAN.toString().equals(descriptorColumnDTO.type))
            {
               filterDTO = new Gson().fromJson(
                     descriptorColumnsFilterJson.get(descriptorColumnDTO.id),
                     ProcessTableFilterDTO.BooleanDTO.class);
            }
            descriptorColumnMap.put(descriptorColumnDTO.id, new DescriptorFilterDTO(
                  descriptorColumnDTO.type, filterDTO));
         }
      }

      processListFilterDTO.descriptorFilterMap = descriptorColumnMap;
   }

   /**
    * @return
    */
   public ProcessInstanceService getProcessInstanceService()
   {
      return processInstanceService;
   }

   /**
    * @param ProcessInstanceService
    */
   public void setProcessInstanceService(ProcessInstanceService processInstanceService)
   {
      this.processInstanceService = processInstanceService;
   }
}
