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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.exception.PortalErrorClass;
import org.eclipse.stardust.ui.web.rest.exception.PortalRestException;
import org.eclipse.stardust.ui.web.rest.service.DocumentService;
import org.eclipse.stardust.ui.web.rest.service.RepositoryService;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentTypeDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.request.DocumentInfoDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.FileUploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * @author Anoop.Nair
 * @author Abhay.Thappan
 * @version $Revision: $
 */
@Component
@Path("/documents")
public class DocumentResource
{
   private static final Logger trace = LogManager
         .getLogger(ActivityInstanceResource.class);

   @Autowired
   private DocumentService documentService;

   @Autowired
   private RepositoryService repositoryService;
   
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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/downloadDocument/{documentId}/{documentName}")
	public Response downloadReportDefinition(
			@PathParam("documentId") String documentId,
			@PathParam("documentName") String documentName) {
		try {

			return Response
					.ok(documentService
							.downloadDocumentDefinition(documentId),
							MediaType.APPLICATION_OCTET_STREAM)
					.header("content-disposition",
							"attachment; filename = \"" + documentName + "\"")
					.build();
		} catch (MissingResourceException mre) {
			return Response.status(Status.NOT_FOUND).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}

	}

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{documentId}/document-type")
   public Response getDocumentType(@PathParam("documentId")
   String documentId) throws PortalRestException
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
         throw new PortalRestException(PortalErrorClass.DOCUMENT_NOT_FOUND, e);
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
    * @author Yogesh.Manware
    * @param processOid
    * @return
    * @throws Exception 
    */
   @PUT
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("")
   public Response addDocument(List<Attachment> attachments) throws Exception
   {
      // parse attachments
      List<DocumentInfoDTO> uploadedDocuments = FileUploadUtils.parseAttachments(attachments);

      List<DocumentDTO> documents = new ArrayList<DocumentDTO>();
      for (DocumentInfoDTO documentInfoDTO : uploadedDocuments)
      {
         documents.add(repositoryService.createDocument(documentInfoDTO));
      }

      return Response.ok(GsonUtils.toJsonHTMLSafeString(documents)).build();
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
