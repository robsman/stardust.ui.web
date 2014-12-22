/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.Daemon;
import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DaemonDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;

/**
 * @author Subodh.Godbole
 * @version $Revision: $
 */
@Component
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
         List<Daemon> daemons = serviceFactoryUtils.getAdministrationService().getAllDaemons(false);
         List<DaemonDTO> daemonsDto = DTOBuilder.buildList(daemons, DaemonDTO.class);
         return Response.ok(AbstractDTO.toJson(daemonsDto), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }
}
