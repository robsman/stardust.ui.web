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
package org.eclipse.stardust.ui.web.rest.resource;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.component.service.DocumentSearchService;
import org.eclipse.stardust.ui.web.rest.component.util.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.rest.documentation.DTODescription;
import org.eclipse.stardust.ui.web.rest.documentation.RequestDescription;
import org.eclipse.stardust.ui.web.rest.documentation.ResponseDescription;
import org.eclipse.stardust.ui.web.rest.dto.InfoDTO;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.util.JsonMarshaller;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Path("/documentSearch")
public class DocumentSearchResource
{
   private static final Logger trace = LogManager.getLogger(DocumentSearchResource.class);

   @Resource
   private DocumentSearchService documentSearchService;

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
   @ResponseDescription("Return JSON with below information\r\n"
         + "``` javascript\r\n" + 
         "{\n" + 
         "typicalFileTypes :[] //list of file types\n" + 
         "documentTypes : [] // list of document types\n" + 
         "repositories : []  // list of repositories\n" + 
         "allRegisteredMimeFileTypes : []  // list of all MIME file types\n" + 
         "}\r\n"
         + "```")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.DocumentSearchFilterAttributesDTO")
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
    * @param documentId
    * @return
    */
   @GET
   @Path("/loadProcessByDocument/{documentId}")
   @ResponseDescription("The response will contain list of ProcessInstanceDTO")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.ProcessInstanceDTO")
   public Response loadProcessByDocument(@PathParam("documentId") String documentId)
   {

      try
      {
         QueryResultDTO resultDTO = documentSearchService.getProcessInstancesFromDocument(documentId);

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
    * @param documentId
    * @return
    */
   @GET
   @Path("/loadDocumentVersions/{documentId}")
   @ResponseDescription("The response will contain list of DocumentVersionDTO")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.DocumentVersionDTO")
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
   @ResponseDescription("The response will contain list of SelectItemDTO having processInstanceOID and name")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.SelectItemDTO")
   public Response loadAvailableProcessDefinitions()
   {
      try
      {

         QueryResultDTO resultDTO = documentSearchService.loadAvailableProcessDefinitions();
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
    * @param postData
    * @return
    */
   @POST
   @Path("/attachDocumentsToProcess/{processOID}")
   @RequestDescription("PostData should have below JSON format\r\n"
         + "``` javascript\r\n"
         + "{\n"
         + " documentIds : []\r\n"
         + "}\r\n"
         + "```")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.InfoDTO")
   public Response attachDocumentsToProcess(@PathParam("processOID") String processOID, String postData)
   {
      try
      {
         List<String> documentIds = populateDocumentIds(postData);

         InfoDTO result = documentSearchService.attachDocumentsToProcess(Long.parseLong(processOID), documentIds);
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
    * @param postData
    * @return
    */

   private List<String> populateDocumentIds(String postData)
   {
      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject postJSON = jsonIo.readJsonObject(postData);

      JsonArray documentIdsArray = postJSON.getAsJsonArray("documentIds");
      Type type = new TypeToken<List<String>>()
      {
      }.getType();
      List<String> documentIds = new ArrayList<String>();
      if (null != documentIdsArray)
      {
         documentIds = new Gson().fromJson(documentIdsArray.toString(), type);

      }
      return documentIds;
   }
}
