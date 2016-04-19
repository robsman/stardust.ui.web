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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.component.service.ResourceLoginTimeService;
import org.eclipse.stardust.ui.web.rest.documentation.DTODescription;
import org.eclipse.stardust.ui.web.rest.documentation.ResponseDescription;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;

/**
 * 
 * @author Abhay.Thappan
 *
 */
@Path("/loginTimeInfo")
public class LoginTimeInfoResource
{

   public static final Logger trace = LogManager.getLogger(LoginTimeInfoResource.class);

   @Resource
   private ResourceLoginTimeService resourceLoginTimeService;
   /**
    * 
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @ResponseDescription("The response will contain list of ResourceLoginInfoDTO")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.ResourceLoginInfoDTO")
   public Response getResourceLoginTimeInfo()
   {
      try
      {
         QueryResultDTO resultDTO = resourceLoginTimeService.getResourceLoginTimeInfo();
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
