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

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.ui.web.rest.component.service.AuditTrailService;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonObject;

/**
 * @author Abhay.Thappan
 *
 */
@Path("/audit-trail")
public class AuditTrailResource
{
   @Autowired
   private AuditTrailService auditTrailService;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/workflowEngineRecovery")
   public Response recoverWorkflowEngine()
   {
      auditTrailService.recoverWorkflowEngine();
      return Response.ok().build();
   }

   @DELETE
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/database")
   public Response cleanupATD(@QueryParam("retainUsersAndDepts") @DefaultValue("false") boolean retainUsersAndDepts)
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