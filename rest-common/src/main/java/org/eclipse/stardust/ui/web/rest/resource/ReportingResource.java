/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.ui.web.rest.component.service.ReportingService;
import org.eclipse.stardust.ui.web.rest.documentation.ResponseDescription;
import org.eclipse.stardust.ui.web.rest.dto.response.RepositoryProviderDTO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
@Path("/reports")
public class ReportingResource
{
   @Autowired
   private ReportingService reportingService;

   /**
    * @return
    */
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/personal")
   @ResponseDescription("Returns grant level reports of the logged in user in the form of List<FolderDTO> json")
   public Response getPersonalReports()
   {
      return Response.ok(RepositoryProviderDTO.toJson(reportingService.getPersonalReports()),
            MediaType.APPLICATION_JSON).build();
   }
}