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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.service.StartableProcessService;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.StartableProcessDTO;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonObject;

/**
 * @author Abhay.Thappan
 *
 */
@Path("/startable-process")
public class StartableProcessResource
{
   private static final Logger trace = LogManager.getLogger(StartableProcessResource.class);

   @Autowired
   private StartableProcessService startableProcessService;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Response getStartableProcess()
   {
      List<StartableProcessDTO> startableProcesses = startableProcessService.getStartableProcess();
      QueryResultDTO resut = new QueryResultDTO();
      resut.list = startableProcesses;
      return Response.ok(GsonUtils.toJson(resut), MediaType.APPLICATION_JSON).build();
   }
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/startProcessByDepartment/{departmentOid}/{processId}")
   public Response startProcessOnSelectDepartment(@PathParam("departmentOid") long departmentOid, @PathParam("processId") String processId)
   {
      ActivityInstance activityInstance = startableProcessService.startProcessOnSelectDepartment(departmentOid, processId);
      JsonObject result = new JsonObject();
      if (activityInstance != null)
      {
         result.addProperty("activityInstanceOid", activityInstance.getOID());
      }
      else
      {
         result.addProperty("processStarted", true);
      }

      return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
   }
   
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/startProcess/{processId}")
   public Response startProcess(@PathParam("processId") String processId){
      JsonObject result = startableProcessService.startProcess(processId);
      return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
   }
   
}