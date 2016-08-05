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
package org.eclipse.stardust.ui.web.rest.resource;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.component.service.TrafficLightService;
import org.eclipse.stardust.ui.web.rest.dto.AbstractDTO;


@Path("/trafficLight")
public class TrafficLightResource
{
   public static final Logger trace = LogManager.getLogger(TrafficLightResource.class);

   @Resource
   private TrafficLightService trafficLightService;

   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/processes")
   public Response getAllProcessesWithTrafficLight( )
   {
      return Response
            .ok(AbstractDTO.toJson( trafficLightService.getAllProcessesWithTrafficLight() ), MediaType.APPLICATION_JSON).build();
   }
   
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/categories/{processQId}")
   public Response getAllProcessesWithTrafficLight(@PathParam("processQId") String processQId )
   {
      return Response
            .ok(AbstractDTO.toJson( trafficLightService.getCateogries(processQId) ), MediaType.APPLICATION_JSON).build();
   }
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/activityColumns/{processQId}")
   public Response getActivityColumns(@PathParam("processQId") String processQId )
   {
      return Response
            .ok(AbstractDTO.toJson( trafficLightService.getActivitiyColumns(processQId) ), MediaType.APPLICATION_JSON).build();
   }
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/data/{processQId}")
   public Response getTrafficLightData(@PathParam("processQId") String processQId )
   {
      return Response
            .ok(AbstractDTO.toJson( trafficLightService.getTrafficLightData(processQId) ), MediaType.APPLICATION_JSON).build();
   }
}
