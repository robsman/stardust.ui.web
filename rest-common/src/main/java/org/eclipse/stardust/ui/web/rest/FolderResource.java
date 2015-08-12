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

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.ui.web.rest.service.RepositoryService;
import org.eclipse.stardust.ui.web.rest.service.dto.response.FolderDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{folderId : .*}")
   public Response getFolder(@PathParam("folderId") String folderId,
         @QueryParam("levelOfDetail") @DefaultValue("1") int levelOfDetail)
   {
      if (folderId.indexOf("/") != 0)
      {
         folderId = "/" + folderId;
      }
      FolderDTO folderContents = repositoryService.getFolder(folderId, levelOfDetail);
      return Response.ok(folderContents.toJson(), MediaType.APPLICATION_JSON).build();
   }
}