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
package org.eclipse.stardust.ui.web.rest.resource;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.component.service.ProcessResourceMgmtService;
import org.eclipse.stardust.ui.web.rest.dto.ProcessResourceMgmtDTO;

@Path("/processResourceManagement")
public class ProcessResourceMgmtResource
{
   private static final Logger trace = LogManager.getLogger(ProcessResourceMgmtResource.class);

   @Resource
   private ProcessResourceMgmtService processResourceMgmtService;

   /**
    * 
    * @return
    */
   @GET
   @Path("/availableRoles")
   public Response getProcessResourceRoles()
   {

      try
      {
         ProcessResourceMgmtDTO result = processResourceMgmtService.getProcessResourceRoles();

         return Response.ok(result.toJson(), MediaType.TEXT_PLAIN_TYPE).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   /**
    * 
    * @return
    */
   @GET
   @Path("/availableUsers")
   public Response getProcessResourceUsers()
   {

      try
      {
         ProcessResourceMgmtDTO result = processResourceMgmtService.getProcessResourceUsers();

         return Response.ok(result.toJson(), MediaType.TEXT_PLAIN_TYPE).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }
}
