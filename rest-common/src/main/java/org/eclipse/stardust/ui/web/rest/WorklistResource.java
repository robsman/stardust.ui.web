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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.WorklistService;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.WorklistFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author Subodh.Godbole
 * @author Johnson.Quadras
 * @version $Revision: $
 */
@Component
@Path("/worklist")
public class WorklistResource
{
   private static final Logger trace = LogManager.getLogger(WorklistResource.class);

   @Autowired
   private WorklistService worklistService;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/participant/{participantQId}")
   public Response getWorklistForParticipant(@PathParam("participantQId") String participantQId,
         @QueryParam("skip") @DefaultValue("0") Integer skip, @QueryParam("pageSize") @DefaultValue("8") Integer pageSize, @QueryParam("filterBy") String filters,
         @QueryParam("orderBy") @DefaultValue("oid") String orderBy, @QueryParam("orderByDir") @DefaultValue("asc") String orderByDir)
   {
      try
      {
         Options options = new Options(pageSize, skip, orderBy, "asc".equalsIgnoreCase(orderByDir),getFilters(filters));
         QueryResultDTO resultDTO = getWorklistService().getWorklistForParticipant(participantQId, "default", options);

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

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/user/{userId}")
   public Response getWorklistForUser(@PathParam("userId") String userId,
         @QueryParam("skip") @DefaultValue("0") Integer skip, @QueryParam("pageSize") @DefaultValue("8") Integer pageSize, @QueryParam("filterBy") String filters,
         @QueryParam("orderBy") @DefaultValue("oid") String orderBy, @QueryParam("orderByDir") @DefaultValue("asc") String orderByDir)
   {
      try
      {
         Options options = new Options(pageSize, skip, orderBy, "asc".equalsIgnoreCase(orderByDir) , getFilters(filters));
         QueryResultDTO resultDTO = getWorklistService().getWorklistForUser(userId, "default", options);

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

   public WorklistService getWorklistService()
   {
      return worklistService;
   }

   public void setWorklistService(WorklistService worklistService)
   {
      this.worklistService = worklistService;
   }

   private WorklistFilterDTO getFilters(String jsonFilterString){
      WorklistFilterDTO worklistFilter = null;
      if(StringUtils.isNotEmpty(jsonFilterString)){
         try {
            worklistFilter =  DTOBuilder.buildFromJSON(jsonFilterString, WorklistFilterDTO.class,WorklistFilterDTO.getCustomTokens());
         } catch (Exception e) {
            trace.error("Error in Deserializing filter JSON", e);
         }
      }

      return worklistFilter;
   }
}
