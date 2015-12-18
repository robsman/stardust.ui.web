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
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import org.eclipse.stardust.ui.web.rest.exception.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.misc.RequestDescription;
import org.eclipse.stardust.ui.web.rest.misc.ResponseDescription;
import org.eclipse.stardust.ui.web.rest.service.DocumentService;
import org.eclipse.stardust.ui.web.rest.service.RepositoryService;
import org.eclipse.stardust.ui.web.rest.service.ResourcePolicyService;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentTypeDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ResourcePolicyDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DocumentDTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.request.DocumentContentRequestDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.FileUploadUtils;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

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
   
   @Autowired
   private ResourcePolicyService resourcePolicyService; 
   
   @Resource
   private RestCommonClientMessages restCommonClientMessages;
   
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
    * @param attachments
    * @return
    * @throws Exception
    */
   @POST
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/upload")
   public Response uploadDocuments(List<Attachment> attachments) throws Exception
   {
      // parse attachments
      List<DocumentContentRequestDTO> uploadedDocuments = FileUploadUtils.parseAttachments(attachments);
      Map<String, Object> result = repositoryService.createDocuments(uploadedDocuments, null, false);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(result)).build();
   }
   
   /**
    * @author Yogesh.Manware
    * @param attachments
    * @return
    * @throws Exception
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("")
   @RequestDescription("Request must contain json representation of\r\n"
         + "`org.eclipse.stardust.ui.web.rest.service.dto.request.DocumentContentRequestDTO`")
   public Response createDocument(String postedData) throws Exception
   {
      DocumentContentRequestDTO documentInfoDTO = DTOBuilder
            .buildFromJSON2(postedData, DocumentContentRequestDTO.class, null);
      
      Map<String, Object> result = repositoryService.createDocument(documentInfoDTO, null, false);
      
      return Response.ok(GsonUtils.toJsonHTMLSafeString(result)).build();
   }
  
   /**
    *  @author Yogesh.Manware
    * @param documentId
    * @param postedData
    * @return
    * @throws Exception 
    */
   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{documentId: .*}")
   @RequestDescription("Request must contain DocumentContentRequestDTO like json")
   @ResponseDescription("if the document is updated successfully, it returns *Operation completed successfully*.")
   public Response updateDocument(@PathParam("documentId") String documentId, String postedData)
         throws Exception
   {
      DocumentContentRequestDTO documentInfoDTO = DTOBuilder.buildFromJSON(postedData, DocumentContentRequestDTO.class);
      repositoryService.updateDocument(documentId, documentInfoDTO);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(restCommonClientMessages.get("success.message"))).build();
   }
   
   /**
    *  @author Yogesh.Manware
    * @param documentId
    * @return
    * @throws Exception
    */
   @DELETE
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{documentId: .*}")
   @ResponseDescription("if the document deleted succussfully *Operation completed successfully* is sent back.")
   public Response deleteDocument(@PathParam("documentId") String documentId) throws Exception
   {
      repositoryService.deleteDocument(documentId);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(restCommonClientMessages.get("success.message"))).build();
   }
   
   /**
    *  @author Yogesh.Manware
    * @param documentId
    * @param postedData
    * @return
    * @throws Exception
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{documentId: .*}/copy")
   public Response copy(@PathParam("documentId") String documentId, String postedData) throws Exception
   {
      Map<String, Object> data = JsonDTO.getAsMap(postedData);
      String targetFolderPath = (String) data.get("targetFolderPath");
      boolean overWrite = false;
      if (data.get("overWrite") != null)
      {
         overWrite = (Boolean) data.get("overWrite");
      }
      documentId = DocumentMgmtUtility.checkAndGetCorrectResourceId(documentId);

      DocumentDTO documentDTO = DocumentDTOBuilder.build(DocumentMgmtUtility.copyDocumentTo(
            DocumentMgmtUtility.getDocument(documentId), targetFolderPath, overWrite));

      return Response.ok(GsonUtils.toJsonHTMLSafeString(documentDTO)).build();
   }

   /**
    *  @author Yogesh.Manware
    * @param documentId
    * @return
    * @throws Exception
    */
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{documentId: .*}")
   @ResponseDescription("Returns all basic document information, for content use */content/documentId*")
   public Response getDocument(@PathParam("documentId") String documentId) throws Exception
   {
      documentId = DocumentMgmtUtility.checkAndGetCorrectResourceId(documentId);
      DocumentDTO documentDTO = DocumentDTOBuilder.build(DocumentMgmtUtility.getDocument(documentId));
      return Response.ok(GsonUtils.toJsonHTMLSafeString(documentDTO)).build();
   }
   
   /**
    * 
    *  @author Yogesh.Manware
    * @param documentId
    * @return
    * @throws Exception
    */
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/content/{documentId: .*}")
   @ResponseDescription("Returns document content (bytes)")
   public Response getDocumentContent(@PathParam("documentId") String documentId) throws Exception
   {
      documentId = DocumentMgmtUtility.checkAndGetCorrectResourceId(documentId);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(repositoryService.getDocumentContent(documentId))).build();
   }

   /**
    *  @author Yogesh.Manware
    * @param documentId
    * @return
    * @throws Exception
    */
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/history/{documentId: .*}")
   @ResponseDescription("Returns all previous versions of document")
   public Response getDocumentVersions(@PathParam("documentId") String documentId) throws Exception
   {
      documentId = DocumentMgmtUtility.checkAndGetCorrectResourceId(documentId);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(repositoryService.getDocumentHistory(documentId))).build();
   }
   
   /**
    * @author Yogesh.Manware
    * @param documentId
    * @return
    * @throws Exception
    */
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/policy/{documentId: .*}")
   @ResponseDescription("Returns ResourcePolicyContainerDTO containing losts of own and inherited policies in the form of ResourcePolicyDTO")
   public Response getDocumentPolicies(@PathParam("documentId") String documentId) throws Exception
   {
      documentId = DocumentMgmtUtility.checkAndGetCorrectResourceId(documentId);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(resourcePolicyService.getPolicy(documentId, false))).build();
   }

   /**
    * @author Yogesh.Manware
    * @param documentId
    * @param postedData
    * @return
    * @throws Exception
    */
   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/policy/{documentId: .*}")
   @RequestDescription("accepts list of ResourcePolicyDTO"
         + "**Note:** *Participant object can be replaced with simple key value pair of  \"participantQualifiedId\"* ")
   @ResponseDescription("if the document policies are updated successfully, *Operation completed successfully.*")
   public Response updateDocumentPolicies(@PathParam("documentId") String documentId, String postedData)
         throws Exception
   {
      Type type = new TypeToken<List<ResourcePolicyDTO>>()
      {
      }.getType();

      List<ResourcePolicyDTO> resourcePolicies = (List<ResourcePolicyDTO>) GsonUtils.extractList(
            GsonUtils.readJsonArray(postedData), type);

      resourcePolicyService.savePolicy(documentId, resourcePolicies, false);

      return Response.ok(GsonUtils.toJsonHTMLSafeString(restCommonClientMessages.get("success.message"))).build();
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
