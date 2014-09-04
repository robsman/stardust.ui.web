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

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.DocumentService;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentTypeDTO;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component
@Path("/portal/documents")
public class DocumentResource
{
   private static final Logger trace = LogManager
         .getLogger(ActivityInstanceResource.class);

   @Autowired
   private DocumentService documentService;

   private final JsonMarshaller jsonIo = new JsonMarshaller();

   @GET
   @Produces("image/png")
   @Path("{documentId}/{pageNumber: \\d+}")
   public Response getDocumentImage(@PathParam("documentId")
   String documentId, @PathParam("pageNumber")
   int pageNumber)
   {
      try
      {
         final byte[] image = getDocumentService().getDocumentImage(documentId,
               pageNumber);

         return Response.ok().entity(new StreamingOutput()
         {
            @Override
            public void write(OutputStream output) throws IOException,
                  WebApplicationException
            {
               output.write(image);
               output.flush();
            }
         }).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{documentId}/document-type")
   public Response getDocumentType(@PathParam("documentId")
   String documentId)
   {
      try
      {
         DocumentTypeDTO documentTypeDTO = getDocumentService().getDocumentType(
               documentId);

         Gson gson = new Gson();
         String jsonOutput = gson.toJson(documentTypeDTO);

         return Response.ok(jsonOutput.toString(), MediaType.APPLICATION_JSON).build();
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
   @Path("{documentId}/document-type")
   public Response setDocumentType(@PathParam("documentId")
   String documentId, String postedData)
   {
      try
      {
         JsonObject json = jsonIo.readJsonObject(postedData);

         Gson gson = new Gson();
         DocumentTypeDTO documentTypeDTO = gson.fromJson(json, DocumentTypeDTO.class);

         DocumentDTO updatedDocumentDTO = getDocumentService().setDocumentType(
               documentId, documentTypeDTO);

         String jsonOutput = gson.toJson(updatedDocumentDTO);

         return Response.ok(jsonOutput.toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   /**
    * @return
    */
   public DocumentService getDocumentService()
   {
      return documentService;
   }

   /**
    * @param documentService
    */
   public void setDocumentService(DocumentService documentService)
   {
      this.documentService = documentService;
   }
}
