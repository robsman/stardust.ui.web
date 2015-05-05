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
/**
 * @author Abhay.Thappan
 */
package org.eclipse.stardust.ui.web.rest;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.DeputyManagementService;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.springframework.stereotype.Component;

@Component
@Path("/deputyManagement")
public class DeputyManagementResource
{
   private static final Logger trace = LogManager.getLogger(DeputyManagementResource.class);

   @Resource
   private DeputyManagementService deputyManagementService;

   /**
    * 
    * @return
    */
   @GET
   @Path("/users")
   public Response loadUsers()
   {
      try
      {
         QueryResultDTO result = deputyManagementService.loadUsers();
         return Response.ok(result.toJson(), MediaType.TEXT_PLAIN_TYPE).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   @GET
   @Path("/deputiesForUser/{userOID}")
   public Response loadDeputiesForUser(@PathParam("userOID") String userOID)
   {
      try
      {
         QueryResultDTO result = deputyManagementService.loadDeputiesForUser(Long.parseLong(userOID));
         return Response.ok(result.toJson(), MediaType.TEXT_PLAIN_TYPE).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }
}
