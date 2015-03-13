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
 * @author Abhay.Thappan
 */
package org.eclipse.stardust.ui.web.rest;

import java.util.MissingResourceException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.DocumentSearchServiceBean;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentSearchCriteriaDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentSearchFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.InfoDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Component
@Path("/documentSearch")
public class DocumentSearchResource
{
   private static final Logger trace = LogManager.getLogger(DocumentSearchResource.class);

   @Resource
   private DocumentSearchServiceBean documentSearchService;

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Context
   private HttpServletRequest httpRequest;

   /**
    * 
    * @return
    */
   @GET
   @Path("/searchAttributes")
   public Response searchAttributes()
   {

      try
      {
         String result = documentSearchService.createDocumentSearchFilterAttributes();
         return Response.ok(result, MediaType.TEXT_PLAIN_TYPE).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   /**
    * 
    * @param skip
    * @param pageSize
    * @param orderBy
    * @param orderByDir
    * @param postData
    * @return
    */
   @POST
   @Path("/searchByCriteria")
   public Response searchByCritera(@QueryParam("skip") @DefaultValue("0") Integer skip,
         @QueryParam("pageSize") @DefaultValue("8") Integer pageSize,
         @QueryParam("orderBy") @DefaultValue("documentName") String orderBy,
         @QueryParam("orderByDir") @DefaultValue("desc") String orderByDir,
         String postData)
   {

      try
      {
         Options options = new Options(pageSize, skip, orderBy,
               "desc".equalsIgnoreCase(orderByDir));
         populateFilters(options, postData);

         DocumentSearchCriteriaDTO documentSearchAttributes = getDocumentSearchCriteria(postData);

         QueryResultDTO result = documentSearchService.performSearch(options,
               documentSearchAttributes);
         Gson gson = new Gson();
         return Response.ok(gson.toJson(result), MediaType.TEXT_PLAIN_TYPE).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   /**
    * 
    * @param documentId
    * @return
    */
   @GET
   @Path("/loadProcessByDocument/{documentId}")
   public Response loadProcessByDocument(@PathParam("documentId") String documentId)
   {

      try
      {
         QueryResultDTO resultDTO = documentSearchService
               .getProcessInstancesFromDocument(documentId);

         Gson gson = new Gson();
         return Response.ok(gson.toJson(resultDTO), MediaType.TEXT_PLAIN_TYPE).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }

   }

   /**
    * 
    * @param documentOwner
    * @return
    */
   @GET
   @Path("/loadUserDetails/{documentOwner}")
   public Response getUserDetails(@PathParam("documentOwner") String documentOwner)
   {
      try
      {
         UserDTO user = documentSearchService.getUserDetails(documentOwner);

         Gson gson = new Gson();
         return Response.ok(gson.toJson(user), MediaType.TEXT_PLAIN_TYPE).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   /**
    * 
    * @param documentId
    * @return
    */
   @GET
   @Path("/loadDocumentVersions/{documentId}")
   public Response getDocumentVersions(@PathParam("documentId") String documentId)
   {

      QueryResultDTO resultDTO;
      try
      {
         resultDTO = documentSearchService.getDocumentVersions(documentId);
         Gson gson = new Gson();
         return Response.ok(gson.toJson(resultDTO), MediaType.TEXT_PLAIN_TYPE).build();
      }
      catch (ResourceNotFoundException e)
      {
         trace.error(e, e);
         return Response.status(Status.NOT_FOUND).build();
      }

   }

   /**
    * 
    * @return
    */
   @GET
   @Path("/loadAvailableProcessDefinitions")
   public Response loadAvailableProcessDefinitions()
   {
      try
      {

         QueryResultDTO resultDTO = documentSearchService
               .loadAvailableProcessDefinitions();
         Gson gson = new Gson();
         return Response.ok(gson.toJson(resultDTO), MediaType.TEXT_PLAIN_TYPE).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.status(Status.BAD_REQUEST).build();
      }
   }

   /**
    * 
    * @param processOID
    * @param documentId
    * @return
    */
   @GET
   @Path("/attachDocumentsToProcess/{processOID}/{documentId}")
   public Response attachDocumentsToProcess(@PathParam("processOID") String processOID,
         @PathParam("documentId") String documentId)
   {
      try
      {

         InfoDTO result = documentSearchService.attachDocumentsToProcess(
               Long.parseLong(processOID), documentId);
         Gson gson = new Gson();
         return Response.ok(gson.toJson(result), MediaType.TEXT_PLAIN_TYPE).build();
      }
      catch (MissingResourceException mre)
      {
         trace.error(mre, mre);
         return Response.status(Status.NOT_FOUND).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.status(Status.BAD_REQUEST).build();
      }
   }

   /**
    * Populate the options with the post data.
    * 
    * @param options
    * @param postData
    * @return
    */
   private Options populateFilters(Options options, String postData)
   {
      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject postJSON = jsonIo.readJsonObject(postData);

      // For filter
      JsonObject filters = postJSON.getAsJsonObject("filters");
      if (null != filters)
      {
         DocumentSearchFilterDTO docSearchFilterDTO = new Gson().fromJson(
               postJSON.get("filters"), DocumentSearchFilterDTO.class);

         options.filter = docSearchFilterDTO;
      }
      return options;
   }

   /**
    * 
    * @param postData
    * @return
    * @throws Exception
    */
   private DocumentSearchCriteriaDTO getDocumentSearchCriteria(String postData)
         throws Exception
   {
      DocumentSearchCriteriaDTO documentSearchCriteria = null;

      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject postJSON = jsonIo.readJsonObject(postData);

      JsonObject documentSearchCriteriaJson = postJSON
            .getAsJsonObject("documentSearchCriteria");

      String documentSearchCriteriaJsonStr = documentSearchCriteriaJson.toString();
      if (StringUtils.isNotEmpty(documentSearchCriteriaJsonStr))
      {
         try
         {
            documentSearchCriteria = DTOBuilder.buildFromJSON(
                  documentSearchCriteriaJsonStr, DocumentSearchCriteriaDTO.class);
         }
         catch (Exception e)
         {
            trace.error("Error in Deserializing filter JSON", e);
            throw e;
         }
      }

      return documentSearchCriteria;
   }

}
