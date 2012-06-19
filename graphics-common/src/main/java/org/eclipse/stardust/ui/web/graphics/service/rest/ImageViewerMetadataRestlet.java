/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.graphics.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.ui.web.graphics.service.GraphicsMetadataService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


@Path("documents/{documentId}/pages/{pageNo}/{randomPostfix}")
public class ImageViewerMetadataRestlet
{

   @Context
   private ServletContext servletContext;

   @Context
   private HttpServletRequest httpRequest;
   
   private long annotationId = System.currentTimeMillis();

   /*
    * This doesn't belong here. needs a util method at portal level that exposes spring
    * context
    */
   private GraphicsMetadataService getMetadataService()
   {
      ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);
      GraphicsMetadataService metadataService = (GraphicsMetadataService) context.getBean("graphicsMetadataService");
      metadataService.init(httpRequest);
      return metadataService;
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   public void createAnnotation(@PathParam("documentId") String documentId, @PathParam("pageNo") String pageNo, String postedData)
   {
      getMetadataService().createAnnotation(documentId, Integer.parseInt(pageNo), postedData, httpRequest);
   }

   @POST
   @Path ("/updateAnnotation")
   public void updateAnnotation(@PathParam("documentId") String documentId, @PathParam("pageNo") String pageNo, String postedData) {
      getMetadataService().updateAnnotation(documentId, Integer.parseInt(pageNo), postedData, httpRequest);
   }
   
   @POST
   @Path ("/deleteAnnotation")
   public void deleteAnnotation(@PathParam("documentId") String documentId, @PathParam("pageNo") String pageNo, String postedData) {
      getMetadataService().deleteAnnotation(documentId, Integer.parseInt(pageNo), postedData, httpRequest);
   }
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Response retrieveMetadata(@PathParam("documentId") String documentId, @PathParam("pageNo") String pageNo)
   {
      String key = documentId + ":" + pageNo;
      Response response = null;
      String result = getMetadataService().retrieve(documentId, Integer.parseInt(pageNo), httpRequest);
      response = Response.ok(result, APPLICATION_JSON_TYPE).build();
      return response;
   }
   
   @POST
   @Path ("/updateRotationFactor")
   public void updateRotationFactor(@PathParam("documentId") String documentId, @PathParam("pageNo") String pageNo, String postedData) {
      String key = documentId + ":" + pageNo;
      getMetadataService().updateRotationFactor(documentId, Integer.parseInt(pageNo), postedData, httpRequest);
   }
   
   @POST
   @Path ("/cleanCache")
   public void cleanCache(@PathParam("documentId") String documentId, String postedData) {
      getMetadataService().cleanCache(documentId, postedData, httpRequest);
   }
   
   @GET
   @Produces(MediaType.TEXT_PLAIN)
   @Path ("/uniqueId")
   public Response getUniqueId()
   {
      return Response.ok(getNextId(), MediaType.TEXT_PLAIN_TYPE).build();
   }
   
   @GET
   @Produces(MediaType.TEXT_PLAIN)
   @Path ("/language")
   public Response getLanguage()
   {
      StringTokenizer tok = new StringTokenizer(httpRequest.getHeader("Accept-language"), ",");
      if (tok.hasMoreTokens())
      {
         return Response.ok(getMetadataService().getLocale(tok.nextToken()), MediaType.TEXT_PLAIN_TYPE).build();
      }      
      return Response.ok("en", MediaType.TEXT_PLAIN_TYPE).build();
   }
   
   @POST
   @Path ("/viewerState")
   public void updateViewerState(@PathParam("documentId") String docId, String postedData)
   {
      getMetadataService().updateViewerState(docId, postedData);
   }
   
   @GET
   @Path ("/viewerState")
   @Produces(MediaType.APPLICATION_JSON)
   public Response getViewerState(@PathParam("documentId") String docId)
   {      
      String jsonState  = getMetadataService().getViewerState(docId);
      return Response.ok(jsonState, APPLICATION_JSON_TYPE).build();
   }   
   
   /**
    * @return
    */
   private synchronized String getNextId() {
      return "" + annotationId++;
   }
}
