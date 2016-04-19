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

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.component.service.FavoriteReportsService;
import org.eclipse.stardust.ui.web.rest.documentation.DTODescription;
import org.eclipse.stardust.ui.web.rest.documentation.ResponseDescription;
import org.eclipse.stardust.ui.web.rest.dto.FavoriteReportDTO;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;

/**
 * @author Abhay.Thappan
 *
 */
@Path("/favorites-reports")
public class FavoriteReportsResource
{
   private static final Logger trace = LogManager.getLogger(FavoriteReportsResource.class);

   @Resource
   private FavoriteReportsService favoriteReportsService;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @ResponseDescription("#### Sample Response:\r\n" + "[{\r\n"
         + "   \"documentId\": \"{urn:repositoryId:System}{jcrUuid}12179aae-6212-4a63-a7f9-669fce20bcb1\",\r\n"
         + "   \"documentName\": \"Report Definition 1\"\r\n" + "}]")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.FavoriteReportDTO")
   public Response getFavoriteReports()
   {
      try
      {

         List<FavoriteReportDTO> listFavoriteReport = favoriteReportsService.getFavoriteReports();
         return Response.ok(GsonUtils.toJson(listFavoriteReport), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   @DELETE
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/removeFromFavorite/{documentId}")
   public Response removeFromFavorite(@PathParam("documentId") String documentId)
   {
      try
      {
         RepositoryUtility.removeFromFavorite(documentId);
         return Response.ok().build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }
}