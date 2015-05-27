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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.PortalConfigurationService;
import org.eclipse.stardust.ui.web.rest.service.dto.PortalConfigurationDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.WorklistViewConfigurationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Johnson.Quadras
 * @version $Revision: $
 */
@Component
@Path("/portalConfiguration")
public class PortalConfigurationResource
{
   private static final Logger trace = LogManager.getLogger(PortalConfigurationResource.class);

   @Autowired
   private PortalConfigurationService portalConfigurationService;

   /**
    * 
    */
   @GET
   @Path("/configuration/{scope}")
   @Produces(MediaType.APPLICATION_JSON)
   public Response getPortalConfiguration(@PathParam("scope") String scope)
   {
      try
      {
         PortalConfigurationDTO result = portalConfigurationService.getPortalConfiguration(scope);
         return Response.ok(result.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }
   
   
   /**
    * 
    */
   @GET
   @Path("/views/worklist/{scope}")
   @Produces(MediaType.APPLICATION_JSON)
   public Response getConfigurationForWorklistView(@PathParam("scope") String scope )
   {
      try
      {
         WorklistViewConfigurationDTO result = portalConfigurationService.getWorklistViewConfiguration( scope );
         return Response.ok(result.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }
}
