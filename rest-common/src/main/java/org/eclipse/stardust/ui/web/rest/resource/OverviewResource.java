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

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.component.service.OverviewService;
import org.eclipse.stardust.ui.web.rest.documentation.DTODescription;
import org.eclipse.stardust.ui.web.rest.documentation.ResponseDescription;
import org.eclipse.stardust.ui.web.rest.dto.DataTableOptionsDTO;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;

/**
 * 
 * @author Abhay.Thappan
 *
 */
@Path("/overview")
public class OverviewResource
{

   public static final Logger trace = LogManager.getLogger(OverviewResource.class);

   @Resource
   private OverviewService overviewService;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/allLogEntries")
   @ResponseDescription("The response will contain list of LogEntryDTO")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.LogEntryDTO")
   public Response getAllLogEntries(@QueryParam("skip") @DefaultValue("0") Integer skip,
         @QueryParam("pageSize") @DefaultValue("8") Integer pageSize,
         @QueryParam("orderBy") @DefaultValue("timeStamp") String orderBy,
         @QueryParam("orderByDir") @DefaultValue("asc") String orderByDir)
   {
      try
      {
         DataTableOptionsDTO options = new DataTableOptionsDTO(pageSize, skip, orderBy, "asc".equalsIgnoreCase(orderByDir));
         QueryResultDTO resultDTO = overviewService.getAllLogEntries(options);

         return Response.ok(resultDTO.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (ObjectNotFoundException onfe)
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }
   }
}
