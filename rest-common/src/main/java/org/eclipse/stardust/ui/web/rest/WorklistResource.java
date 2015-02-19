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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.rest.service.ProcessDefinitionService;
import org.eclipse.stardust.ui.web.rest.service.WorklistService;
import org.eclipse.stardust.ui.web.rest.service.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.WorklistFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.WorklistFilterDTO.DescriptorFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;

/**
 * @author Subodh.Godbole
 * @author Johnson.Quadras
 * @version $Revision: $
 */
@Component
@Path("/worklist")
public class WorklistResource
{
   private static final Logger trace = LogManager.getLogger(WorklistResource.class);

   @Autowired
   private WorklistService worklistService;

   @Autowired
   ProcessDefinitionService processDefService;

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/participant/{participantQId}")
   public Response getWorklistForParticipant1(
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

         QueryResultDTO resultDTO = getWorklistService().getWorklistForParticipant(
               participantQId, "default", options);

         return Response.ok(resultDTO.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (ObjectNotFoundException onfe)
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/user/{userId}")
   public Response getWorklistForUser(@PathParam("userId") String userId,
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
         QueryResultDTO resultDTO = getWorklistService().getWorklistForUser(userId,
               "default", options);
         return Response.ok(resultDTO.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (ObjectNotFoundException onfe)
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }
   }

   public WorklistService getWorklistService()
   {
      return worklistService;
   }

   public void setWorklistService(WorklistService worklistService)
   {
      this.worklistService = worklistService;
   }

   /**
    * Get the filters from the JSON string
    * @param jsonFilterString
    * @return
    */
   private WorklistFilterDTO getFilters(String jsonFilterString)
   {
      WorklistFilterDTO worklistFilter = null;
      if (StringUtils.isNotEmpty(jsonFilterString))
      {
         try
         {
            JsonMarshaller jsonIo = new JsonMarshaller();
            JsonObject json = jsonIo.readJsonObject(jsonFilterString);
            worklistFilter = DTOBuilder.buildFromJSON(json, WorklistFilterDTO.class,
                  WorklistFilterDTO.getCustomTokens());
            if (StringUtils.contains(jsonFilterString, "descriptorValues"))
            {
               populateDescriptorFilters(worklistFilter, json);
            }
         }
         catch (Exception e)
         {
            trace.error("Error in Deserializing filter JSON", e);
         }
      }

      return worklistFilter;
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
      return options;
   }
   
   /**
    * Populates the descriptor filter values.
    * @param worklistFilter
    * @param descriptorColumnsFilterJson
    */
   private void populateDescriptorFilters(WorklistFilterDTO worklistFilter,
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
                     WorklistFilterDTO.TextSearchDTO.class);

            }
            else if (ColumnDataType.DATE.toString().equals(descriptorColumnDTO.type)
                  || ColumnDataType.NUMBER.toString().equals(descriptorColumnDTO.type))
            {
               filterDTO = new Gson().fromJson(
                     descriptorColumnsFilterJson.get(descriptorColumnDTO.id),
                     WorklistFilterDTO.RangeDTO.class);
            }
            else if (ColumnDataType.BOOLEAN.toString().equals(descriptorColumnDTO.type))
            {
               filterDTO = new Gson().fromJson(
                     descriptorColumnsFilterJson.get(descriptorColumnDTO.id),
                     WorklistFilterDTO.BooleanDTO.class);
            }
            descriptorColumnMap.put(descriptorColumnDTO.id, new DescriptorFilterDTO(
                  descriptorColumnDTO.type, filterDTO));
         }
      }

      worklistFilter.descriptorFilterMap = descriptorColumnMap;
   }

}
