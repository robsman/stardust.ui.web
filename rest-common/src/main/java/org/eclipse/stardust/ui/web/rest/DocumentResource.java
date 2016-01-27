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
import java.util.HashMap;
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
import org.eclipse.stardust.ui.web.rest.service.dto.request.DocumentContentRequestDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.FileUploadUtils;
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
   @RequestDescription("Multiple documents can uploaded at the same time. \r\n" + 
         "\r\n" + 
         "File and its attributes has to be supplied in sequence. \r\n" + 
         "\r\n" + 
         "Supported Attributes are \r\n" + 
         "``` javascript\r\n" + 
         "parentFolderPath //where the document will created/updated)\r\n" + 
         "description \r\n" + 
         "comments\r\n" + 
         "createVersion //'true'(default) to indicate user wants to create a version if the document already exist with the same name)\r\n" + 
         "createNewVersion //'true' (default) to indicate user wants to create new version with update, 'false' to overwrite\r\n" + 
         "\r\n" + 
         "modelId //required when documentTypeId is also supplied\r\n" + 
         "documentTypeId //modelId must also be supplied before this attibute) \r\n" + 
         "\r\n" + 
         "```\r\n" + 
         "Note that all other attributes key-values would be assumed to be document *Properties*\r\n" + 
         "\r\n" + 
         "example of how client can provide the file attributes is \r\n" + 
         "``` javascript\r\n" + 
         "formData.append(\"file1\", files[1]);\r\n" + 
         "formData.append(\"description\", \"Description for file1\") \r\n" + 
         "formData.append(\"file2\", files[2]);\r\n" + 
         "formData.append(\"description\", \"Description for file2\") \r\n" + 
         "``` javascript")
   @ResponseDescription("Returns the result something like below\r\n" + "\r\n" + "```javascript\r\n" + "{\r\n"
         + "  \"failures\": [] //NotificationDTOs\r\n" + "  \"documents\": [] ////DocumentDTOs\r\n" + "}\r\n" + "```")
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
         + "`org.eclipse.stardust.ui.web.rest.service.dto.request.DocumentContentRequestDTO and few attributes from DocumentDTO`")
   @ResponseDescription("Returns the result something like below\r\n" + "\r\n" + "```javascript\r\n" + "{\r\n"
         + "  \"failures\": [] //NotificationDTOs\r\n" + "  \"documents\": [] ////DocumentDTOs\r\n" + "}\r\n" + "```")
   public Response createDocument(String postedData) throws Exception
   {
      ////TODO removed post testing - start 
      ////test with request
      /*{ name : "t15.txt",
         content : "Yogesh Manware",
         description : "Blah",
         parentFolderPath : "/Y",
         properties : {id: "1", name: "Yogesh M."}}*/
      ////TODO removed post testing - end 
      
      //custom token approach does not work here!
      Map<String, Object> data = JsonDTO.getAsMap(postedData);
      HashMap<String, Object> properties = null; 
      if (data.get("properties") != null)
      {
         properties = (HashMap<String, Object>) data.get("properties");
      }
      
      DocumentContentRequestDTO documentInfoDTO = DTOBuilder.buildFromJSON2(postedData,
            DocumentContentRequestDTO.class, null);

      documentInfoDTO.properties = properties; 
      
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
   @RequestDescription("Request must contain DocumentContentRequestDTO like json and few attributes from DocumentDTO")
   @ResponseDescription("if the document is updated successfully, it returns *Operation completed successfully*.")
   public Response updateDocument(@PathParam("documentId") String documentId, String postedData)
         throws Exception
   {
      //custom token approach does not work here!
      Map<String, Object> data = JsonDTO.getAsMap(postedData);
      HashMap<String, Object> properties = null; 
      if (data.get("properties") != null)
      {
         properties = (HashMap<String, Object>) data.get("properties");
      }

      DocumentContentRequestDTO documentInfoDTO = DTOBuilder.buildFromJSON2(postedData,
            DocumentContentRequestDTO.class, null);
      documentInfoDTO.properties = properties;
      DocumentDTO documentDTO = repositoryService.updateDocument(documentId, documentInfoDTO);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(documentDTO)).build();
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
   @ResponseDescription("if the document deleted succussfully, *Operation completed successfully* is sent back.")
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
   @RequestDescription("The document (id provided in URL), will be copied to the *targetFolderPath* supplied in body. \r\n"
         + "\r\n" + "If the document already exist, error message will be returned. \r\n" + "")
   @ResponseDescription("Returns DocumentDTO json object")
   public Response copy(@PathParam("documentId") String documentId, String postedData) throws Exception
   {
      Map<String, Object> data = JsonDTO.getAsMap(postedData);
      String targetFolderPath = (String) data.get("targetFolderPath");
      DocumentDTO documentDTO = repositoryService.copyDocument(documentId, targetFolderPath, false);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(documentDTO)).build();
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
   @Path("{documentId: .*}/copy")
   @RequestDescription("The document (id provided in URL), will be copied to the *targetFolderPath* supplied in body. \r\n"
         + "\r\n" + "If the document already exist, it will be updated with additional version. ")
   @ResponseDescription("Returns DocumentDTO json object")
   public Response copyAndUpdate(@PathParam("documentId") String documentId, String postedData) throws Exception
   {
      Map<String, Object> data = JsonDTO.getAsMap(postedData);
      String targetFolderPath = (String) data.get("targetFolderPath");
      DocumentDTO documentDTO = repositoryService.copyDocument(documentId, targetFolderPath, true);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(documentDTO)).build();
   }
   
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/revert{documentId: .*}")
   @RequestDescription("This endpoint expects the documentId to be reverted in url and in the request body following attributes are supported \r\n"
         + "\r\n"
         + "```javascript \r\n"
         + "uuid //target revision id\r\n"
         + "description\r\n"
         + "comment\r\n"
         + "createNewRevision // true' to indicate user wants to create new version with update, 'false' to overwrite\r\n"
         + "\r\n" + "```")
   @ResponseDescription("Returns the modified version of document as DocumentDTO")
   public Response revertToPreviousVersion(@PathParam("documentId") String documentId, String postedData)
  throws Exception
   {
      DocumentContentRequestDTO documentInfoDTO = DTOBuilder
            .buildFromJSON2(postedData, DocumentContentRequestDTO.class, null);
      DocumentDTO documentDTO = repositoryService.revertDocument(documentId, documentInfoDTO);
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
   @ResponseDescription("Returns all basic document information in the form of DocumentDTO, to retrieve content use */content/documentId*")
   public Response getDocument(@PathParam("documentId") String documentId) throws Exception
   {
      DocumentDTO documentDTO = repositoryService.getDocument(documentId); 
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
   @RequestDescription("Accepts list of ResourcePolicyDTOs\r\n" + 
         "\r\n" + 
         "**Note:** *Participant object can be replaced with simple key value pair of  \"participantQualifiedId\"* ")
   @ResponseDescription("If the document policies are updated successfully, returns *Operation completed successfully.*")
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
