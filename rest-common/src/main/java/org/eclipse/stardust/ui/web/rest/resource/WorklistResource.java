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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.common.Options;
import org.eclipse.stardust.ui.web.rest.component.service.ProcessDefinitionService;
import org.eclipse.stardust.ui.web.rest.component.service.WorklistService;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityTableUtils;
import org.eclipse.stardust.ui.web.rest.dto.ActivityInstanceDTO;
import org.eclipse.stardust.ui.web.rest.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Subodh.Godbole
 * @author Johnson.Quadras
 * @version $Revision: $
 */
@Path("/worklist")
public class WorklistResource
{
   private static final String DEFAULT_ORDER_BY_FIELD = "oid";

   private static final String DEFAULT_PAGE_SIZE = "8";

   private static final String DEFAULT_SKIP_STEP = "0";

   private static final String DEFAULT_ORDER = "asc";

   private static final Logger trace = LogManager.getLogger(WorklistResource.class);

   @Autowired
   private WorklistService worklistService;

   @Autowired
   ProcessDefinitionService processDefService;

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/participant/{participantQId}")
   public Response getWorklistForParticipant(@PathParam("participantQId") String participantQId,
         @QueryParam("skip") @DefaultValue(DEFAULT_SKIP_STEP) Integer skip,
         @QueryParam("pageSize") @DefaultValue(DEFAULT_PAGE_SIZE) Integer pageSize,
         @QueryParam("orderBy") @DefaultValue(DEFAULT_ORDER_BY_FIELD) String orderBy,
         @QueryParam("orderByDir") @DefaultValue(DEFAULT_ORDER) String orderByDir, @QueryParam("userId") String userId,String postData)
   {
      try
      {
         Options options = new Options(pageSize, skip, orderBy, DEFAULT_ORDER.equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);

         QueryResultDTO resultDTO = getWorklistService().getWorklistForParticipant( participantQId, userId, options);

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

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/user/{userId}")
   public Response getWorklistForUser(@PathParam("userId") String userId,
         @QueryParam("fetchAllStates") @DefaultValue("false") String fetchAllStates,
         @QueryParam("skip") @DefaultValue(DEFAULT_SKIP_STEP) Integer skip,
         @QueryParam("pageSize") @DefaultValue(DEFAULT_PAGE_SIZE) Integer pageSize,
         @QueryParam("orderBy") @DefaultValue(DEFAULT_ORDER_BY_FIELD) String orderBy,
         @QueryParam("orderByDir") @DefaultValue(DEFAULT_ORDER) String orderByDir, String postData)
   {
      try
      {
         Options options = new Options(pageSize, skip, orderBy, DEFAULT_ORDER.equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);
         QueryResultDTO resultDTO = getWorklistService().getWorklistForUser(userId, options, Boolean.valueOf(fetchAllStates));
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
   
   
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/unified/user/{userId}")
   public Response getUnifiedWorklistForUser(@PathParam("userId") String userId,
         @QueryParam("skip") @DefaultValue(DEFAULT_SKIP_STEP) Integer skip,
         @QueryParam("pageSize") @DefaultValue(DEFAULT_PAGE_SIZE) Integer pageSize,
         @QueryParam("orderBy") @DefaultValue(DEFAULT_ORDER_BY_FIELD) String orderBy,
         @QueryParam("orderByDir") @DefaultValue(DEFAULT_ORDER) String orderByDir, String postData)
   {
      try
      {
         Options options = new Options(pageSize, skip, orderBy, DEFAULT_ORDER.equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);
         QueryResultDTO resultDTO = getWorklistService().getUnifiedWorklistForUser(userId, "default", options);
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

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/criticality/high")
   public Response getWorklistForHighCriticality(@QueryParam("skip") @DefaultValue(DEFAULT_SKIP_STEP) Integer skip,
         @QueryParam("pageSize") @DefaultValue(DEFAULT_PAGE_SIZE) Integer pageSize,
         @QueryParam("orderBy") @DefaultValue(DEFAULT_ORDER_BY_FIELD) String orderBy,
         @QueryParam("orderByDir") @DefaultValue(DEFAULT_ORDER) String orderByDir, String postData)
   {
      try
      {
         Options options = new Options(pageSize, skip, orderBy, DEFAULT_ORDER.equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);
         QueryResultDTO resultDTO = getWorklistService().getWorklistForHighCriticality(options);
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

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/allAssigned")
   public Response getAllAssignedWorkItems(@QueryParam("skip") @DefaultValue(DEFAULT_SKIP_STEP) Integer skip,
         @QueryParam("pageSize") @DefaultValue(DEFAULT_PAGE_SIZE) Integer pageSize,
         @QueryParam("orderBy") @DefaultValue(DEFAULT_ORDER_BY_FIELD) String orderBy,
         @QueryParam("orderByDir") @DefaultValue(DEFAULT_ORDER) String orderByDir, String postData)
   {
      try
      {
         Options options = new Options(pageSize, skip, orderBy, DEFAULT_ORDER.equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);
         QueryResultDTO resultDTO = getWorklistService().getAllAssignedWorkItems(options);
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

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/date/{dateId}")
   public Response getWorklistItemsFromDate(@PathParam("dateId") String dateId,
         @QueryParam("skip") @DefaultValue(DEFAULT_SKIP_STEP) Integer skip,
         @QueryParam("pageSize") @DefaultValue(DEFAULT_PAGE_SIZE) Integer pageSize,
         @QueryParam("orderBy") @DefaultValue(DEFAULT_ORDER_BY_FIELD) String orderBy,
         @QueryParam("orderByDir") @DefaultValue(DEFAULT_ORDER) String orderByDir, String postData)
   {
      try
      {
         Options options = new Options(pageSize, skip, orderBy, DEFAULT_ORDER.equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);
         QueryResultDTO resultDTO = getWorklistService().getWorklistItemsFromDate(dateId, options);
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

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/process/{processQId}")
   public Response getWorklistByProcess(@PathParam("processQId") String processQId,
         @QueryParam("skip") @DefaultValue(DEFAULT_SKIP_STEP) Integer skip,
         @QueryParam("pageSize") @DefaultValue(DEFAULT_PAGE_SIZE) Integer pageSize,
         @QueryParam("orderBy") @DefaultValue(DEFAULT_ORDER_BY_FIELD) String orderBy,
         @QueryParam("orderByDir") @DefaultValue(DEFAULT_ORDER) String orderByDir, String postData)
   {
      try
      {
         Options options = new Options(pageSize, skip, orderBy, DEFAULT_ORDER.equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);
         QueryResultDTO resultDTO = getWorklistService().getWorklistByProcess(processQId, options);
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

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/resubmissionActivities")
   public Response getWorklistForResubmissionActivities(
         @QueryParam("skip") @DefaultValue(DEFAULT_SKIP_STEP) Integer skip,
         @QueryParam("pageSize") @DefaultValue(DEFAULT_PAGE_SIZE) Integer pageSize,
         @QueryParam("orderBy") @DefaultValue(DEFAULT_ORDER_BY_FIELD) String orderBy,
         @QueryParam("orderByDir") @DefaultValue(DEFAULT_ORDER) String orderByDir, String postData)
   {
      try
      {
         Options options = new Options(pageSize, skip, orderBy, DEFAULT_ORDER.equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);
         QueryResultDTO resultDTO = getWorklistService().getWorklistForResubmissionActivities(options);
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

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/personalItems")
   public Response getWorklistForLoggedInUser(@QueryParam("skip") @DefaultValue(DEFAULT_SKIP_STEP) Integer skip,
         @QueryParam("pageSize") @DefaultValue(DEFAULT_PAGE_SIZE) Integer pageSize,
         @QueryParam("orderBy") @DefaultValue(DEFAULT_ORDER_BY_FIELD) String orderBy,
         @QueryParam("orderByDir") @DefaultValue(DEFAULT_ORDER) String orderByDir, String postData)
   {
      try
      {
         Options options = new Options(pageSize, skip, orderBy, DEFAULT_ORDER.equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);
         QueryResultDTO resultDTO = getWorklistService().getWorklistForLoggedInUser(options);
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

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/allActivable")
   public Response getAllActivable(@QueryParam("skip") @DefaultValue(DEFAULT_SKIP_STEP) Integer skip,
         @QueryParam("pageSize") @DefaultValue(DEFAULT_PAGE_SIZE) Integer pageSize,
         @QueryParam("orderBy") @DefaultValue(DEFAULT_ORDER_BY_FIELD) String orderBy,
         @QueryParam("orderByDir") @DefaultValue(DEFAULT_ORDER) String orderByDir, String postData)
   {
      try
      {
         Options options = new Options(pageSize, skip, orderBy, DEFAULT_ORDER.equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);
         QueryResultDTO resultDTO = getWorklistService().getAllActivable(options);
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
   
   
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/processInstance")
   public Response getWorklistForProcessInstances(@QueryParam("skip") @DefaultValue(DEFAULT_SKIP_STEP) Integer skip,
         @QueryParam("pageSize") @DefaultValue(DEFAULT_PAGE_SIZE) Integer pageSize,
         @QueryParam("orderBy") @DefaultValue(DEFAULT_ORDER_BY_FIELD) String orderBy,
         @QueryParam("orderByDir") @DefaultValue(DEFAULT_ORDER) String orderByDir,
         @QueryParam("oids") String pInstanceOids,
         String postData)
   {
      try
      {
         List<String> pInstanceOidList = new ArrayList<String>(Arrays.asList(pInstanceOids.split(",")));
         Options options = new Options(pageSize, skip, orderBy, DEFAULT_ORDER.equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);
         QueryResultDTO resultDTO = getWorklistService().getWorklistForProcessInstances(options, pInstanceOidList);
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
   @Path("/{showEmptyWorklist}")
   public Response getWorklistAssignemnt(@PathParam("showEmptyWorklist") boolean showEmptyWorklist) throws PortalException
   {
   QueryResultDTO result = worklistService.getWorklistAssignemnt(showEmptyWorklist);
   return Response.ok(result.toJson(), MediaType.APPLICATION_JSON).build();
   }
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/nextAssemblyLineActivity")
   public Response getNextAssemblyLineActivity() throws PortalException
   {
     ActivityInstanceDTO result = worklistService.getNextAssemblyLineActivity();
      
      return Response.ok(result.toJson(), MediaType.APPLICATION_JSON).build();
      
   }

   /**
    * 
    * @param options
    * @param postData
    */
   private void populatePostData(Options options, String postData)
   {
      List<DescriptorColumnDTO> availableDescriptors = processDefService.getDescriptorColumns(true);
      ActivityTableUtils.populatePostData(options, postData, availableDescriptors);
   }

   /**
    * 
    * @return
    */
   public WorklistService getWorklistService()
   {
      return worklistService;
   }

   /**
    * 
    * @param worklistService
    */
   public void setWorklistService(WorklistService worklistService)
   {
      this.worklistService = worklistService;
   }

}
