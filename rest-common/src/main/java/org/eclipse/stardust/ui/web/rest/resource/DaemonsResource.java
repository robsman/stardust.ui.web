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
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.Daemon;
import org.eclipse.stardust.ui.web.rest.component.util.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.rest.dto.DaemonDTO;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.builder.DTOBuilder;

/**
 * @author Subodh.Godbole
 * @version $Revision: $
 */
@Path("/daemons")
public class DaemonsResource
{
   private static final Logger trace = LogManager.getLogger(DaemonsResource.class);

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/all")
   public Response getAll()
   {
      try
      {
         List<Daemon> daemons = serviceFactoryUtils.getAdministrationService().getAllDaemons(true);
         List<DaemonDTO> daemonsDto = DTOBuilder.buildList(daemons, DaemonDTO.class);
         QueryResultDTO result = new QueryResultDTO();
         result.list = daemonsDto;
         result.totalCount = daemonsDto.size();
         return Response.ok(result.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @PUT
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{daemonType}/start")
   public Response startDaemon(@PathParam("daemonType") String daemonType)
   {
      try
      {
         Daemon startedDaemon = serviceFactoryUtils.getAdministrationService().startDaemon(daemonType, false);
         DaemonDTO daemonDTO = DTOBuilder.build(startedDaemon, DaemonDTO.class);
         return Response.status(202).entity(daemonDTO.toJson()).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();

      }

   }

   @PUT
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{daemonType}/stop")
   public Response stopDaemon(@PathParam("daemonType") String daemonType)
   {
      try
      {
         Daemon stoppedDaemon = serviceFactoryUtils.getAdministrationService().stopDaemon(daemonType, false);
         DaemonDTO daemonDTO = DTOBuilder.build(stoppedDaemon, DaemonDTO.class);
         return Response.status(202).entity(daemonDTO.toJson()).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();

      }

   }
}
