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
 * @author Johnson.Quadras
 */
package org.eclipse.stardust.ui.web.rest;

import java.util.Calendar;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.LocalizationService;
import org.eclipse.stardust.ui.web.rest.service.UserService;
import org.eclipse.stardust.ui.web.rest.service.dto.LocalizationInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/localization")
public class LocalizationResource
{

   @Autowired
   private LocalizationService localizationService;
   
   private static final Logger trace = LogManager.getLogger(UserService.class);

   /**
    * This method returns the logged In user
    * 
    * @return
    */
   @GET
   @Path("/info")
   public Response getLoggedInUser()
   {
      try
      {
         LocalizationInfoDTO infoDTO = localizationService.getLocalizationInfo();
         return Response.ok(infoDTO.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }

   }
   
   
   @GET
   @Path("/serverTimeZone")
   public Response getServerTimeZone()
   {
      try
      {
         Calendar c = java.util.GregorianCalendar.getInstance();
         int timeZoneOffset =   (c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET));
         return Response.ok(timeZoneOffset, MediaType.APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }

   }

}
