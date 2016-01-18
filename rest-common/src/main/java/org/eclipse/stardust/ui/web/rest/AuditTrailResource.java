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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.AuditTrailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

/**
 * @author Abhay.Thappan
 *
 */
@Component
@Path("/audit-trail")
public class AuditTrailResource
{
   private static final Logger trace = LogManager.getLogger(AuditTrailResource.class);

   @Autowired
   private AuditTrailService auditTrailService;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/workflowEngineRecovery")
   public Response recoverWorkflowEngine()
   {
      auditTrailService.recoverWorkflowEngine();
      return Response.ok("SUCCESS", MediaType.APPLICATION_JSON).build();
   }

   @DELETE
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/database/{retainUsersAndDepts}")
   public Response cleanupATD(@PathParam("retainUsersAndDepts") boolean retainUsersAndDepts)
   {
      Boolean status = auditTrailService.cleanupATD(retainUsersAndDepts);
      JsonObject json = new JsonObject();
      json.addProperty("status", status);
      return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
   }

   @DELETE
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/databaseWithModel")
   public Response cleanupATMD()
   {
      Boolean status = auditTrailService.cleanupATMD();
      JsonObject json = new JsonObject();
      json.addProperty("status", status);
      return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
   }
}