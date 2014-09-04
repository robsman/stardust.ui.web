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

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.ProcessInstanceService;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component
@Path("/process-instances")
public class ProcessInstanceResource
{
   private static final Logger trace = LogManager
         .getLogger(ActivityInstanceResource.class);

   @Autowired
   private ProcessInstanceService processInstanceService;

   private final JsonMarshaller jsonIo = new JsonMarshaller();

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{processInstanceOid: \\d+}/documents/{documentId}/split")
   public Response splitDocument(@PathParam("processInstanceOid")
   long processInstanceOid, @PathParam("documentId")
   String documentId, String postedData)
   {
      try
      {
         JsonObject json = jsonIo.readJsonObject(postedData);

         return Response.ok(
               getProcessInstanceService().splitDocument(processInstanceOid, documentId,
                     json).toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{processInstanceOid: \\d+}/documents/{dataPathId}")
   public Response addDocument(@PathParam("processInstanceOid")
   String processInstanceOid, @PathParam("dataPathId")
   String dataPathId, String postedData)
   {
      try
      {
         JsonObject json = jsonIo.readJsonObject(postedData);

         return Response.ok(
               getProcessInstanceService().addProcessInstanceDocument(
                     Long.parseLong(processInstanceOid), dataPathId, json).toString(),
               MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @DELETE
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{processInstanceOid: \\d+}/documents/{dataPathId}{documentId: (/documentId)?}")
   public Response removeDocument(@PathParam("processInstanceOid")
   String processInstanceOid, @PathParam("dataPathId")
   String dataPathId, @PathParam("documentId")
   String documentId)
   {
      try
      {
         return Response.ok(
               getProcessInstanceService().removeProcessInstanceDocument(
                     Long.parseLong(processInstanceOid), dataPathId, documentId)
                     .toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("documentRendezvous.json")
   public Response getPendingProcesses(String postedData)
   {
      try
      {
         JsonObject json = jsonIo.readJsonObject(postedData);

         return Response.ok(
               getProcessInstanceService().getPendingProcesses(json).toString(),
               MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("processes.json")
   public Response startProcess(String postedData)
   {
      try
      {
         JsonObject json = jsonIo.readJsonObject(postedData);

         return Response.ok(getProcessInstanceService().startProcess(json).toString(),
               MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   /**
    * @return
    */
   public ProcessInstanceService getProcessInstanceService()
   {
      return processInstanceService;
   }

   /**
    * @param ProcessInstanceService
    */
   public void setProcessInstanceService(ProcessInstanceService processInstanceService)
   {
      this.processInstanceService = processInstanceService;
   }
}
