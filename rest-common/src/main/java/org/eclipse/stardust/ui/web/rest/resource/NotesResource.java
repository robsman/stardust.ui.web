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
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.component.message.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.component.service.NotesService;
import org.eclipse.stardust.ui.web.rest.documentation.RequestDescription;
import org.eclipse.stardust.ui.web.rest.documentation.ResponseDescription;
import org.eclipse.stardust.ui.web.rest.dto.NotesResultDTO;
import org.eclipse.stardust.ui.web.rest.util.JsonMarshaller;

import com.google.gson.JsonObject;

/**
 * @author Abhay.Thappan
 * @author Yogesh.Manware
 *
 */
@Path("/notes")
public class NotesResource
{

   public static final Logger trace = LogManager.getLogger(NotesResource.class);

   @Resource
   private NotesService notesService;

   @Resource
   private RestCommonClientMessages restCommonClientMessages;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/{processInstanceOid}")
   @RequestDescription("*asc* specifies an order of notes returned from server, by default it is false")
   @ResponseDescription("json of QueryResultDTO containing NoteDTO - Process Instance level notes")
   public Response getNotes(@PathParam("processInstanceOid") long processInstanceOid,
         @QueryParam("asc") @DefaultValue("false") boolean asc)
   {
      NotesResultDTO resultDTO = notesService.getProcessNotes(processInstanceOid, asc);
      return Response.ok(resultDTO.toJson(), MediaType.APPLICATION_JSON).build();
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/save")
   @RequestDescription("Request should contains note text and either processInstanceOid or activityInstanceOid. Note that activity level and process level notes are saved by this end point")
   public Response saveNote(String postData) throws Exception
   {
      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject postJSON = jsonIo.readJsonObject(postData);

      long processInstanceOid = -1;
      if (postJSON.getAsJsonPrimitive("processInstanceOid") != null)
      {
         processInstanceOid = postJSON.getAsJsonPrimitive("processInstanceOid").getAsLong();
      }

      long activityInstanceOid = -1;

      if (postJSON.getAsJsonPrimitive("activityInstanceOid") != null)
      {
         activityInstanceOid = postJSON.getAsJsonPrimitive("activityInstanceOid").getAsLong();
      }

      String noteText = postJSON.getAsJsonPrimitive("noteText").getAsString();
      if (processInstanceOid > 0)
      {
         notesService.saveProcessNotes(processInstanceOid, noteText);
      }
      else
      {
         notesService.saveActivityNotes(activityInstanceOid, noteText);
      }

      return Response.ok(GsonUtils.toJsonHTMLSafeString(restCommonClientMessages.get("success.message"))).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/activity/{activityInstanceOid}")
   @ResponseDescription("Returns Activity Instance level notes")
   public Response getActivityNotes(@PathParam("activityInstanceOid") long activityInstanceOid,
         @QueryParam("asc") @DefaultValue("false") boolean asc)
   {
      NotesResultDTO resultDTO = notesService.getActivityNotes(activityInstanceOid, asc);
      return Response.ok(resultDTO.toJson(), MediaType.APPLICATION_JSON).build();
   }
}
