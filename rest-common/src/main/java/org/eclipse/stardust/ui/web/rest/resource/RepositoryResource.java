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
package org.eclipse.stardust.ui.web.rest.resource;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.component.message.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.component.service.RepositoryService;
import org.eclipse.stardust.ui.web.rest.documentation.RequestDescription;
import org.eclipse.stardust.ui.web.rest.documentation.ResponseDescription;
import org.eclipse.stardust.ui.web.rest.dto.DocumentSearchFilterDTO;
import org.eclipse.stardust.ui.web.rest.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.dto.request.RepositorySearchRequestDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.RepositoryInstanceDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.RepositoryProviderDTO;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.reflect.TypeToken;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
@Path("/repository")
public class RepositoryResource
{
   @Autowired
   private RepositoryService repositoryService;

   @Resource
   private RestCommonClientMessages restCommonClientMessages;

   /**
    * @return
    */
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/providers")
   @ResponseDescription("Returns list of available Repository Providers in the form of *RepositoryProviderDTOs*")
   public Response getRepositoryProviders()
   {
      return Response.ok(RepositoryProviderDTO.toJson(repositoryService.getRepositoryProviders()),
            MediaType.APPLICATION_JSON).build();
   }

   /**
    * @return
    */
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @ResponseDescription("Returns List of available repositories in the form of *RepositoryInstanceDTOs*")
   public Response getRepositories()
   {
      return Response.ok(RepositoryInstanceDTO.toJson(repositoryService.getRepositories()),
            MediaType.APPLICATION_JSON).build();
   }

   /**
    * @param repositoryId
    * @return
    */
   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/default/{repositoryId: .*}")
   @RequestDescription("Expects repository id in the url")
   @ResponseDescription("Post updating default Repository, it returns *Operation completed successfully*.")
   public Response makeRepositoryDefault(@PathParam("repositoryId") String repositoryId)
   {
      repositoryService.setDefualtRepository(repositoryId);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(restCommonClientMessages.get("success.message"))).build();
   }

   /**
    * @param postedData
    * @return
    * @throws Exception
    */
   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/bind")
   @RequestDescription("Sample Request: \r\n"
         + "```javascript\r\n"
         + "{\r\n"
         + " \"providerId\": \"jcr-vfs\",\r\n"
         + " \"id\":\"Repo2\",\r\n"
         + " \"jndiName\": \"jcr/ContentRepository2\"\r\n"
         + "}\r\n"
         + "```\r\n"
         + "Whereas *providerId* and *id* are mandatory attributes and other attributes would be specific to repository.")
   @ResponseDescription("After binding Repository successfully, it returns - *Operation completed successfully*.")
   public Response bindRepository(String postedData) throws Exception
   {
      Map<String, Object> attributes = JsonDTO.getAsMap(postedData);
      repositoryService.bindRepository(attributes);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(restCommonClientMessages.get("success.message"))).build();
   }

   /**
    * @param repositoryId
    * @return
    */
   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/unbind/{repositoryId: .*}")
   @RequestDescription("unbinds the given repository")
   @ResponseDescription("In case of success, 'Operation completed successfully.' is sent back.")
   public Response unBindRepository(@PathParam("repositoryId") String repositoryId)
   {
      repositoryService.unbindRepository(repositoryId);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(restCommonClientMessages.get("success.message"))).build();
   }
   
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @RequestDescription("Search criteria can be provided as per RepositorySearchRequestDTO\r\n" + 
         "\r\n" + 
         "*Sample*\r\n" + 
         "\r\n" + 
         "```javascript\r\n" + 
         "{\r\n" + 
         "  name: \"Repo\",\r\n" + 
         "  searchType:\"Document\",\r\n" + 
         "  contentTypeIn:['text/plain'],\r\n" + 
         "  documentDataTableOption: {\r\n" + 
         "    filter: {\r\n" + 
         "      documentName: {\r\n" + 
         "        textSearch:\"REPO\"\r\n" + 
         "      }\r\n" + 
         "    }\r\n" + 
         "  }\r\n" + 
         "}\r\n" + 
         "```")
   @ResponseDescription("The response would be map of *documents* and *folders* with values in the form of *QueryResultDTO*")
   @Path("/search")
   public Response searchResources(String postData) throws Exception
   {
      // default orderBy is resource name
      Map<String, Type> customTokens = new HashMap<String, Type>();
      customTokens.put("filter", new TypeToken<DocumentSearchFilterDTO>()
      {
      }.getType());

      RepositorySearchRequestDTO repositorySearchRequestDTO = DTOBuilder.buildFromJSON(postData,
            RepositorySearchRequestDTO.class, customTokens);
      Map<String, QueryResultDTO> result = repositoryService.searchResources(repositorySearchRequestDTO);
      return Response.ok((GsonUtils.toJsonHTMLSafeString(result)), MediaType.APPLICATION_JSON).build();
   }
}