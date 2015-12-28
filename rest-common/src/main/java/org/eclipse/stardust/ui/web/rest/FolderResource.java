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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
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

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.rest.exception.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.misc.RequestDescription;
import org.eclipse.stardust.ui.web.rest.misc.ResponseDescription;
import org.eclipse.stardust.ui.web.rest.service.MapAdapter;
import org.eclipse.stardust.ui.web.rest.service.RepositoryService;
import org.eclipse.stardust.ui.web.rest.service.ResourcePolicyService;
import org.eclipse.stardust.ui.web.rest.service.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ResourcePolicyDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.request.DocumentContentRequestDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.FolderDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.FileUploadUtils;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
@Component
@Path("/folders")
public class FolderResource
{
   @Autowired
   private RepositoryService repositoryService;

   @Resource
   private RestCommonClientMessages restCommonClientMessages;

   @Autowired
   private ResourcePolicyService resourcePolicyService;

   /**
    * @param folderId
    * @param levelOfDetail
    * @param createIfDoesNotExist
    * @return
    */
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{folderId : .*}")
   @ResponseDescription("Returns folderDTO json object")
   public Response getFolder(@PathParam("folderId") String folderId,
         @QueryParam("levelOfDetail") @DefaultValue("1") int levelOfDetail,
         @QueryParam("create") @DefaultValue("false") boolean createIfDoesNotExist)
   {
      folderId = DocumentMgmtUtility.checkAndGetCorrectResourceId(folderId);
      FolderDTO folderContents = repositoryService.getFolder(folderId, levelOfDetail, createIfDoesNotExist);
      // TODO move jsonHelper and MapAdapter to Portal-Common and then modify GsonUtils
      Gson gson = new GsonBuilder().registerTypeAdapter(Map.class, new MapAdapter()).disableHtmlEscaping().create();
      return Response.ok(gson.toJson(folderContents, FolderDTO.class), MediaType.APPLICATION_JSON).build();
   }

   /**
    * @param folderId
    * @return
    * @throws Exception
    */
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/policy/{folderId: .*}")
   @ResponseDescription("Returns ResourcePolicyContainerDTO containing losts of own and inherited policies in the form of ResourcePolicyDTO")
   public Response getFolderPolicies(@PathParam("folderId") String folderId) throws Exception
   {
      folderId = DocumentMgmtUtility.checkAndGetCorrectResourceId(folderId);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(resourcePolicyService.getPolicy(folderId, true))).build();
   }

   /**
    * @param folderId
    * @param postedData
    * @return
    * @throws Exception
    */
   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/policy/{folderId: .*}")
   @RequestDescription("accepts list of ResourcePolicyDTO"
         + "**Note:** *Participant object can be replaced with simple key value pair of  \"participantQualifiedId\"* ")
   @ResponseDescription("if the folder policies are updated successfully, expect *Operation completed successfully.*")
   public Response updateFolderPolicies(@PathParam("folderId") String folderId, String postedData) throws Exception
   {
      Type type = new TypeToken<List<ResourcePolicyDTO>>()
      {
      }.getType();

      List<ResourcePolicyDTO> resourcePolicies = (List<ResourcePolicyDTO>) GsonUtils.extractList(
            GsonUtils.readJsonArray(postedData), type);

      resourcePolicyService.savePolicy(folderId, resourcePolicies, true);

      return Response.ok(GsonUtils.toJsonHTMLSafeString(restCommonClientMessages.get("success.message"))).build();
   }

   /**
    * @param folderId
    * @param postedData
    * @return
    * @throws DocumentManagementServiceException
    * @throws UnsupportedEncodingException
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{folderId: .*}")
   @RequestDescription("Either url should contain complete path or message body must contain *parentFolderId* and *folderName* to be created")
   @ResponseDescription("On success, expect FolderDTO in response")
   public Response createFolder(@PathParam("folderId") String folderId, String postedData)
         throws DocumentManagementServiceException, UnsupportedEncodingException
   {
      // convert json to simple map
      Map<String, Object> folderDataMap = null;
      if (!StringUtils.isEmpty(postedData))
      {
         folderDataMap = JsonDTO.getAsMap(postedData);
      }

      FolderDTO folderDto = repositoryService.createFolder(folderId, folderDataMap);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(folderDto)).build();
   }

   /**
    * @param folderId
    * @param postedData
    * @return
    * @throws DocumentManagementServiceException
    * @throws UnsupportedEncodingException
    */
   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{folderId: .*}")
   @RequestDescription("Request must contain the attributes (key, value pair) of folder to be updated namely name")
   @ResponseDescription("if the folder is updated successfully, 'Operation completed successfully.' is sent back.")
   public Response updateFolder(@PathParam("folderId") String folderId, String postedData)
         throws DocumentManagementServiceException, UnsupportedEncodingException
   {
      // convert json to simple map
      Map<String, Object> folderDataMap = JsonDTO.getAsMap(postedData);
      repositoryService.updateFolder(folderId, folderDataMap);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(restCommonClientMessages.get("success.message"))).build();
   }

   /**
    * @param folderId
    * @return
    * @throws Exception
    */
   @DELETE
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{folderId: .*}")
   @ResponseDescription("if the folder deleted succussfully 'Operation completed successfully' is sent back.")
   public Response deleteFolder(@PathParam("folderId") String folderId) throws Exception
   {
      repositoryService.deleteFolder(folderId);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(restCommonClientMessages.get("success.message"))).build();
   }

   /**
    * @param folderId
    * @return
    * @throws DocumentManagementServiceException
    * @throws UnsupportedEncodingException
    * 
    * 
    */
   @GET
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   @Path("/export/{folderId: .*}")
   @ResponseDescription("return zip of the folder supplied as part of url")
   public Response exportFolder(@PathParam("folderId") String folderId) throws DocumentManagementServiceException,
         UnsupportedEncodingException
   {
      // convert json to simple map
      byte[] folderBytes = repositoryService.exportFolder(folderId);
      ByteArrayInputStream inputStream = new ByteArrayInputStream(folderBytes);
      return Response.ok(inputStream, MediaType.APPLICATION_OCTET_STREAM)
            .header("content-disposition", "attachment; filename = myfile.zip").build();
   }

   /**
    * @param attachments
    * @return
    * @throws Exception
    */
   @POST
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/import/{folderId: .*}")
   @RequestDescription("Deletes existing folder that is supplied as part of url, Parses the uploaded zip file")
   public Response importFolderAndCleanExisting(@PathParam("folderId") String folderId, List<Attachment> attachments)
         throws Exception
   {
      // parse attachments
      List<DocumentContentRequestDTO> uploadedFolder = FileUploadUtils.parseAttachments(attachments);
      repositoryService.importFolder(folderId, uploadedFolder, false);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(restCommonClientMessages.get("success.message"))).build();
   }

   /**
    * @param attachments
    * @return
    * @throws Exception
    */
   @PUT
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/import/{folderId: .*}")
   @RequestDescription("Existing folder is retained and new contents are merged, existing files will be revisioned")
   public Response importFolderAndMerge(@PathParam("folderId") String folderId, List<Attachment> attachments)
         throws Exception
   {
      // parse attachments
      List<DocumentContentRequestDTO> uploadedFolder = FileUploadUtils.parseAttachments(attachments);
      repositoryService.importFolder(folderId, uploadedFolder, true);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(restCommonClientMessages.get("success.message"))).build();
   }
}